/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.authorization.checks;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by jonas - jonas@atmire.com on 13/04/16.
 */
public class GroupAuthorizationCheck implements AuthorizationCheck{

    /* Log4j logger*/
    private static final Logger log =  Logger.getLogger(GroupAuthorizationCheck.class);

    private List<String> allowedGroups;
    
    @Autowired
    private GroupService groupService;
    
    @Required
    public void setAllowedGroups(List<String> allowedGroups){
        this.allowedGroups=allowedGroups;
    }

    public List<String> getGroups(){
        return allowedGroups;
    }
    @Override
    public boolean checkAuthorization(Context context,DSpaceObject dso) {
        EPerson ePerson = context.getCurrentUser();

        for(String group:allowedGroups){
            try {
                Group g = groupService.findByName(context,group);

               if(g!=null && groupService.allMembers(context, g).contains(ePerson)){
                   return true;
                }
            } catch (Exception e) {
                log.error(e);
            }

        }
        return false;
    }
	public GroupService getGroupService() {
		return groupService;
	}
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public List<String> getAllowedGroups() {
		return allowedGroups;
	}
}
