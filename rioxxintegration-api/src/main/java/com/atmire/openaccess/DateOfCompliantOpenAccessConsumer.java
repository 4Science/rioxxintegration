/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.openaccess;

import com.atmire.openaccess.service.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.event.*;
import org.dspace.utils.*;

/**
 * @author philip at atmire.com
 */
public class DateOfCompliantOpenAccessConsumer implements Consumer {

    private static final Logger log = Logger.getLogger(DateOfCompliantOpenAccessConsumer.class);

    private Set<Integer> itemsToUpdate = new HashSet<Integer>();

    private CompliantOpenAccessService compliantOpenAccessService = new DSpace().getServiceManager().getServiceByName("CompliantOpenAccessService",CompliantOpenAccessService.class);

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void consume(Context ctx, Event event) throws Exception {
    	boolean refEnabled = ConfigurationManager.getBooleanProperty("rioxx", "ref.enabled", true);
    	if(refEnabled) {
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
    }

    @Override
    public void end(Context ctx) throws Exception {
        ctx.turnOffAuthorisationSystem();
        for (Integer id : itemsToUpdate) {
            Item item = Item.find(ctx, id);
            try {
                compliantOpenAccessService.updateItem(ctx, item);
            }
            catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }

        ctx.getDBConnection().commit();

        ctx.restoreAuthSystemState();

        itemsToUpdate.clear();
    }

    @Override
    public void finish(Context ctx) throws Exception {

    }

    private Item getParentItem(Bitstream bitstream) throws SQLException {
        DSpaceObject parentObject = bitstream.getParentObject();

        while(parentObject!=null && parentObject.getType()!=Constants.ITEM){
            parentObject = parentObject.getParentObject();
        }

        return (Item) parentObject;
    }
}
