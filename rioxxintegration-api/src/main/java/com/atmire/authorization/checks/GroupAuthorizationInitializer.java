/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.authorization.checks;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupAuthorizationInitializer {

    private static final Logger log =  Logger.getLogger(GroupAuthorizationInitializer.class);
    
    @Autowired(required = true)
    protected GroupService groupService;

    protected GroupAuthorizationCheck allowedGroupsForCompliance;
    
	/**
	 * Added administrator group to all allowedGroupsForCompliance (e.g REF Compliance Viewers group)
	 * 
	 * @param context
	 * @throws SQLException
	 * @throws AuthorizeException
	 */
	public void afterMigrate(Context context) throws SQLException, AuthorizeException {
		Group adminGroup = groupService.findByName(context,Group.ADMIN);
		if(adminGroup != null) {
			for(String group : getAllowedGroupsForCompliance().getAllowedGroups()){
			    Group g = groupService.findByName(context,group);
			    if(g == null){
			        Group createdGroup = groupService.create(context);
			        groupService.setName(createdGroup, group);
			        groupService.addMember(context, createdGroup, adminGroup);
			        groupService.update(context, createdGroup);
			        log.info("Add Administrator to the group:" + group);
			    }
			}
		}
		else {
			log.warn("Administrator group not found: not possible added to the allowedGroupsForCompliance");
		}
	}

	public GroupAuthorizationCheck getAllowedGroupsForCompliance() {
		return allowedGroupsForCompliance;
	}

	public void setAllowedGroupsForCompliance(GroupAuthorizationCheck allowedGroupsForCompliance) {
		this.allowedGroupsForCompliance = allowedGroupsForCompliance;
	}
}
