/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.consumer.metadata;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.ref.compliance.rules.complianceupdaters.ComplianceDepositCheck;
import org.dspace.utils.DSpace;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by jonas - jonas@atmire.com on 23/03/16.
 */
public class FirstComplianceDepositConsumer implements Consumer {

    /* Log4j logger*/
    private static final Logger log =  Logger.getLogger(FirstComplianceDepositConsumer.class);

    private Set<UUID> itemIDs = new HashSet<UUID>();

    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    
    private WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
    
    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void consume(Context context, Event event) throws Exception {
        int subjectType = event.getSubjectType();
        int eventType = event.getEventType();

        DSpaceObject dso = event.getSubject(context);

        switch (subjectType) {
            case Constants.ITEM:
                Item item = (Item) dso;
                //If we are updating the metadata and [ the item is archived or the submitter completed his submission ]
                if (eventType == Event.MODIFY_METADATA  && !item.isWithdrawn() && (item.isArchived() || workspaceItemService.findByItem(context, item)==null )) {
                    itemIDs.add(dso.getID());
                }
                break;
            default:
                log.debug("consume() got unrecognized event: " + event.toString());
        }
    }

    /**
     * Find the objects based on the IDS.
     * Process them here.
     */
    public void end(Context context) throws Exception {
        if (itemIDs.size() >= 0) {
            context.turnOffAuthorisationSystem();
            for (UUID itemID : itemIDs) {
                Item item = itemService.find(context, itemID);
                if (item != null) {
                    process(context, item);
                    // update objects
                    itemService.update(context, item);
                }
            }
            context.restoreAuthSystemState();
        }
        itemIDs.clear();
    }

    protected void process(Context context, Item item) throws SQLException {
        if(ConfigurationManager.getBooleanProperty("workflow", "compliant.ref.metadata.updates", true)){
            for(ComplianceDepositCheck complianceUpdate :new DSpace().getServiceManager().getServicesByType(ComplianceDepositCheck.class) ){
                complianceUpdate.checkAndUpdateCompliance(context, item);
            }
        }

    }

    @Override
    public void finish(Context context) throws Exception {

    }

}
