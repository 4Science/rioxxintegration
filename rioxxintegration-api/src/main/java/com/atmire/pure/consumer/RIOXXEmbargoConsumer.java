/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.pure.consumer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.event.Consumer;
import org.dspace.event.Event;

public class RIOXXEmbargoConsumer implements Consumer {

    private Set<Integer> itemIds = new HashSet<>();

    private static Logger log = Logger.getLogger(RIOXXEmbargoConsumer.class);

    public void initialize() throws Exception {

    }

    public void consume(Context ctx, Event event) throws Exception {
        int subjectType = event.getSubjectType();
        int subjectID = event.getSubjectID();

        switch (subjectType) {
            case Constants.ITEM:
                itemIds.add(subjectID);
                break;

            default:
                log.warn("consume() got unrecognized event: " + event.toString());
        }

    }

    public void end(Context ctx) throws Exception {
	ctx.turnOffAuthorisationSystem();
        for (Integer itemId : itemIds) {
            Item item = Item.find(ctx, itemId);
	    if (log.isDebugEnabled()) {
		log.debug("updating rioxxEmbargo of item " + item.getID());
	    }
	    if(item!=null) {
            Metadatum[] metadatum = item.getMetadata("dc", "sword", "submission", Item.ANY);
            if (metadatum.length > 0 && StringUtils.equals(metadatum[0].value, "true")) {
                Metadatum[] dateMetadatum = item.getMetadata("dc", "rights", "embargodate", Item.ANY);
                if (dateMetadatum.length > 0) {
                    DCDate policyStartDate = new DCDate(dateMetadatum[0].value);
                    if (policyStartDate.toDate().after(DCDate.getCurrent().toDate())) {
                        processOriginalBundleWithPolicyStartDate(ctx, item, policyStartDate);
                    }
                }
            }
	    }
        }
        itemIds.clear();
        // commit context
        ctx.getDBConnection().commit();
        ctx.restoreAuthSystemState();
    }

    private void processOriginalBundleWithPolicyStartDate(Context ctx, Item item, DCDate policyStartDate)
        throws SQLException, AuthorizeException {
        Bundle[] originalBundles = item.getBundles(Constants.CONTENT_BUNDLE_NAME);
        if(originalBundles.length > 0) {
            Bundle orginalBundle = originalBundles[0];
            for (Bitstream bitstream : orginalBundle.getBitstreams()) {
                handleBitstreamPolicy(ctx, policyStartDate, bitstream);
            }
            item.update();
        }
    }

    private void handleBitstreamPolicy(Context ctx, DCDate policyStartDate, Bitstream bitstream)
        throws SQLException, AuthorizeException {

        //Remove all policies and prepare for the assigning of the embargo policy
        AuthorizeManager.removeAllPolicies(ctx, bitstream);

        ResourcePolicy resourcePolicy = ResourcePolicy.create(ctx);
        resourcePolicy.setGroup(Group.find(ctx, Group.ANONYMOUS_ID));
        resourcePolicy.setAction(Constants.READ);
        resourcePolicy.setResource(bitstream);
        resourcePolicy.setRpName("RIOXXEmbargo");
        resourcePolicy.setStartDate(policyStartDate.toDate());

        List<ResourcePolicy> resourcePolicyList = new LinkedList<>();
        resourcePolicyList.add(resourcePolicy);

        AuthorizeManager.addPolicies(ctx, resourcePolicyList, bitstream);
    }

    public void finish(Context ctx) throws Exception {

    }

}
