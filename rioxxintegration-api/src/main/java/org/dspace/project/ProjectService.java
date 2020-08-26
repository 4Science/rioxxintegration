/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.project;

import org.dspace.authority.AuthoritySolrServiceImpl;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 03/09/15
 * Time: 10:24
 */
public class ProjectService {

    protected AuthorityValueService authorityValueService;
    
    public ProjectAuthorityValue createProject(Context context, String projectId, String funderAuthorityId) {

        AuthorityValue project = getAuthorityValueService().findByProjectIDAndFunderId(context, projectId, funderAuthorityId);

        if(project!=null){
            throw new IllegalArgumentException("project with id " + projectId + " already exists");
        }

        AuthorityValue funder = getAuthorityValueService().findByUID(context, funderAuthorityId);

        if(funder==null){
            throw new IllegalArgumentException("funder with authority id " + funderAuthorityId + " could not be found");
        }

        ProjectAuthorityValue newProject = ProjectAuthorityValue.create();
        newProject.setValue(projectId);
        newProject.setFunderAuthorityValue((FunderAuthorityValue) funder);

        AuthoritySolrServiceImpl solrService = (AuthoritySolrServiceImpl) new DSpace().getServiceManager().getServiceByName(AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
        solrService.indexContent(newProject);
        solrService.commit();
        return newProject;
    }

    public ProjectAuthorityValue getProjectByAuthorityId(Context context, String authorityId){
        AuthorityValue authorityValue = getAuthorityValueService().findByUID(context, authorityId);

        if(authorityValue==null || !authorityValue.getAuthorityType().equals("project")) {
            throw new IllegalArgumentException("project with authority id " + authorityId + " could not be found");
        }

        ProjectAuthorityValue projectAuthorityValue = (ProjectAuthorityValue) authorityValue;

        if(projectAuthorityValue.getFunderAuthorityValue()==null){
            throw new IllegalArgumentException("project authority with id " + authorityId + " does not have a valid funder");
        }

        return projectAuthorityValue;
    }
    
	public AuthorityValueService getAuthorityValueService() {
		if(authorityValueService == null) {
			authorityValueService = AuthorityServiceFactory.getInstance().getAuthorityValueService();
		}
		return authorityValueService;
	}

	public void setAuthorityValueService(AuthorityValueService authorityValueService) {
		this.authorityValueService = authorityValueService;
	}
}
