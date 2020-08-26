/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.utils;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactoryImpl;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 16/04/13
 * Time: 14:22
 */
public class EmbargoUtils {

    private static Logger log = Logger.getLogger(EmbargoUtils.class);

    private static ItemService itemService = ContentServiceFactoryImpl.getInstance().getItemService();
    
    private static AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance().getAuthorizeService();
    
    public static Date getEmbargo(Bitstream bitstream, Context context) {
        java.util.List<ResourcePolicy> policiesByDSOAndType = null;
        try {
            policiesByDSOAndType = authorizeService.getPoliciesActionFilter(context, bitstream, Constants.READ);
        } catch (SQLException e) {
            return null;
        }

        for(ResourcePolicy pol:policiesByDSOAndType){
            //This will be the start date of the Anonymous policy: Anonymous will get read access on
            Date date=pol.getStartDate();
            if(date!=null)
                return date;

        }
        return null;
    }

    public static  Date getLastEmbargo(Item item, Context context){
        Date lastEmbargo=null;
        try{
            List<Bitstream> bitstreams = itemService.getNonInternalBitstreams(context, item);

            for (Bitstream bitstream : bitstreams) {
                    java.util.List<ResourcePolicy> policiesByDSOAndType = authorizeService.getPoliciesActionFilter(context, bitstream, Constants.READ);

                    for(ResourcePolicy pol:policiesByDSOAndType){
                    if (pol.getGroup().getName() == Group.ANONYMOUS) {
                        //This will be the start date of the Anonymous policy: Anonymous will get read access on
                        Date date=pol.getStartDate();
                        if(date!=null)
                            if(lastEmbargo==null){
                                lastEmbargo=date;
                            } else if(date.after(lastEmbargo)) {
                                lastEmbargo=date;
                            }
                    }
                }
            }

        }catch(Exception e){
            log.error("error in in getting embargo action", e);

        }
        return lastEmbargo;
    }

}
