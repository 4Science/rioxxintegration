package com.atmire.openaccess;

import com.atmire.openaccess.service.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
import org.dspace.content.*;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.*;
import org.dspace.event.*;
import org.dspace.utils.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author philip at atmire.com
 */
public class DateOfCompliantOpenAccessConsumer implements Consumer {

    private static final Logger log = Logger.getLogger(DateOfCompliantOpenAccessConsumer.class);

    private Set<UUID> itemsToUpdate = new HashSet<UUID>();

    private CompliantOpenAccessService compliantOpenAccessService = new DSpace().getServiceManager().getServiceByName("CompliantOpenAccessService",CompliantOpenAccessService.class);

    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    
    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void consume(Context ctx, Event event) throws Exception {
        DSpaceObject dso = event.getSubject(ctx);
        int et = event.getEventType();

        Item item = null;

        if(dso instanceof Bitstream && et == Event.MODIFY){
            Bitstream bitstream = (Bitstream) dso;
             item = getParentItem(bitstream);
        }
        else if(dso instanceof Item && et == Event.INSTALL){
            item = (Item) dso;
        }

        if(item!=null && item.isArchived()){
            itemsToUpdate.add(item.getID());
        }
    }

    @Override
    public void end(Context ctx) throws Exception {
        ctx.turnOffAuthorisationSystem();
        for (UUID id : itemsToUpdate) {
            Item item = itemService.find(ctx, id);
            try {
                compliantOpenAccessService.updateItem(ctx, item);
            }
            catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }

        ctx.restoreAuthSystemState();

        itemsToUpdate.clear();
    }

    @Override
    public void finish(Context ctx) throws Exception {

    }

    private Item getParentItem(Bitstream bitstream) throws SQLException {
        
    	Item parentObject = null; 
    	
    	List<Bundle> bundles = bitstream.getBundles();

    	if(bundles!=null && !bundles.isEmpty()){
        	List<Item> items = bundles.get(0).getItems();
        	if(items!=null && !items.isEmpty()){
        		parentObject = items.get(0);
        	}
        }

        return parentObject;
    }

}
