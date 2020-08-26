/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.authorization.checks;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.GroupService;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.springframework.beans.factory.annotation.Autowired;

public class GroupAuthorizationInitializer implements FlywayCallback {

    /* Log4j logger*/
    private static final Logger log =  Logger.getLogger(GroupAuthorizationInitializer.class);
    
	private GroupAuthorizationCheck allowedGroupsForCompliance;
	
    @Autowired(required = true)
    protected GroupService groupService;
    
	@Override
	public void beforeClean(Connection connection) {
	}

	@Override
	public void afterClean(Connection connection) {
	}

	@Override
	public void beforeMigrate(Connection connection) {
	}

	@Override
	public void afterMigrate(Connection connection) {
        Context context = null;
        try {
            context = new Context();
            Group adminGroup = groupService.findByName(context,"Administrator");
            context.turnOffAuthorisationSystem();
            if(adminGroup==null) {
            	//try this to initialize default
            	groupService.initDefaultGroupNames(context);
            	adminGroup = groupService.findByName(context,"Administrator");
            }
            for(String group : getAllowedGroupsForCompliance().getAllowedGroups()){
                Group g = groupService.findByName(context,group);
                if(g == null){
                    Group createdGroup = groupService.create(context);
                    groupService.setName(createdGroup, group);
                    groupService.addMember(context, adminGroup, createdGroup);
                    groupService.update(context, createdGroup);
                }
            }
            context.restoreAuthSystemState();
        } catch (SQLException e) {
           log.error("Error while checking for non-existing groups during the authorization check.",e);
        } catch (AuthorizeException e) {
            log.error(e);
        } finally {
            if(context!=null){
                context.abort();
            }
        }
	}

	@Override
	public void beforeEachMigrate(Connection connection, MigrationInfo info) {
	}

	@Override
	public void afterEachMigrate(Connection connection, MigrationInfo info) {
	}

	@Override
	public void beforeValidate(Connection connection) {
	}

	@Override
	public void afterValidate(Connection connection) {
	}

	@Override
	public void beforeBaseline(Connection connection) {
	}

	@Override
	public void afterBaseline(Connection connection) {
	}

	@Override
	public void beforeRepair(Connection connection) {
	}

	@Override
	public void afterRepair(Connection connection) {
	}

	@Override
	public void beforeInfo(Connection connection) {
	}

	@Override
	public void afterInfo(Connection connection) {
	}

	public GroupAuthorizationCheck getAllowedGroupsForCompliance() {
		return allowedGroupsForCompliance;
	}

	public void setAllowedGroupsForCompliance(GroupAuthorizationCheck allowedGroupsForCompliance) {
		this.allowedGroupsForCompliance = allowedGroupsForCompliance;
	}

}
