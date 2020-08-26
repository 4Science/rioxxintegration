package com.atmire.pure.consumer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.GroupService;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.springframework.beans.factory.annotation.Autowired;

public class RIOXXEmbargoConsumer implements Consumer {

    private Set<UUID> itemIds = new HashSet<>();

    private static Logger log = Logger.getLogger(RIOXXEmbargoConsumer.class);

    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    
    private AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();

    private GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
    
    public void initialize() throws Exception {

    }

    public void consume(Context ctx, Event event) throws Exception {
        int subjectType = event.getSubjectType();
        UUID subjectID = event.getSubjectID();

        switch (subjectType) {
            case Constants.ITEM:
                itemIds.add(subjectID);
                break;

            default:
                log.warn("consume() got unrecognized event: " + event.toString());
        }

    }

    public void end(Context ctx) throws Exception {
        for (UUID itemId : itemIds) {
            Item item = itemService.find(ctx, itemId);
            log.debug("updating rioxxEmbargo of item " + item.getID());

            List<MetadataValue> metadatum = itemService.getMetadata(item, "dc", "sword", "submission", Item.ANY);
            if (metadatum.size() > 0 && StringUtils.equals(metadatum.get(0).getValue(), "true")) {
            	List<MetadataValue> dateMetadatum = itemService.getMetadata(item, "dc", "rights", "embargodate", Item.ANY);
                if (dateMetadatum.size() > 0) {
                    DCDate policyStartDate = new DCDate(dateMetadatum.get(0).getValue());
                    if (policyStartDate.toDate().after(DCDate.getCurrent().toDate())) {
                        processOriginalBundleWithPolicyStartDate(ctx, item, policyStartDate);
                    }
                }
            }
        }
        itemIds.clear();

    }

    private void processOriginalBundleWithPolicyStartDate(Context ctx, Item item, DCDate policyStartDate)
        throws SQLException, AuthorizeException {
        List<Bundle> originalBundles = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        if(originalBundles.size() > 0) {
            Bundle orginalBundle = originalBundles.get(0);
            for (Bitstream bitstream : orginalBundle.getBitstreams()) {
                handleBitstreamPolicy(ctx, policyStartDate, bitstream);
            }
            itemService.update(ctx, item);
        }
    }

    private void handleBitstreamPolicy(Context ctx, DCDate policyStartDate, Bitstream bitstream)
        throws SQLException, AuthorizeException {

        //Remove all policies and prepare for the assigning of the embargo policy
        authorizeService.removeAllPolicies(ctx, bitstream);

        ResourcePolicy resourcePolicy = authorizeService.createResourcePolicy(ctx, bitstream, groupService.findByName(ctx, Group.ANONYMOUS), null, Constants.READ, "RIOXXEmbargo");
        resourcePolicy.setStartDate(policyStartDate.toDate());

        List<ResourcePolicy> resourcePolicyList = new LinkedList<>();
        resourcePolicyList.add(resourcePolicy);

        authorizeService.addPolicies(ctx, resourcePolicyList, bitstream);
    }

    public void finish(Context ctx) throws Exception {

    }

}
