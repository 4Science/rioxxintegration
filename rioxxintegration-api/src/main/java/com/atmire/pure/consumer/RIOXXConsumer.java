/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.pure.consumer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.AuthorityValueServiceImpl;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.IndexingUtils;
import org.dspace.authority.PersonAuthorityValue;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.project.ProjectService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.utils.DSpace;

public class RIOXXConsumer implements Consumer {

    private Set<UUID> itemIds = new HashSet<>();
    private ProjectService projectService = new DSpace().getServiceManager()
            .getServiceByName("ProjectService", ProjectService.class);
    private static final String ORCID_ID_SYNTAX = "\\d{4}-\\d{4}-\\d{4}-(\\d{3}X|\\d{4})";
    
    protected AuthorityValueServiceImpl authorityValueService = DSpaceServicesFactory.getInstance().getServiceManager().getServicesByType(AuthorityValueServiceImpl.class).get(0);

    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    
    protected MetadataAuthorityService metadataAuthorityService = ContentAuthorityServiceFactory.getInstance().getMetadataAuthorityService();
    
    private static Logger log = Logger.getLogger(RIOXXConsumer.class);

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
	ctx.turnOffAuthorisationSystem();
        for (UUID itemId : itemIds) {
            Item item = itemService.find(ctx, itemId);
	    if (log.isDebugEnabled()) {
		// leave out of the next if/else to highlight state of the consumer (throw NPE)
		log.debug("updating funders of item " + item.getID());
	    }
            
            if(item!=null) {
	
	            List<MetadataValue> metadatum = itemService.getMetadata(item, "dc", "sword", "submission", Item.ANY);
	            if (!(metadatum.size() == 0 || StringUtils.equals(metadatum.get(0).getValue(), "false"))) {
	                List<MetadataValue> metadata = itemService.getMetadata(item, "rioxxterms", "newfunderprojectpair", null, Item.ANY);
	                itemService.clearMetadata(ctx, item, "rioxxterms", "newfunderprojectpair", null, Item.ANY);
	
	                for (MetadataValue m : metadata) {
	
	                    String funderName = "";
	                    String funderID = "";
	                    String project = "";
	
	
	                    //funder and project are in same metadatafield separated by ::
	                    String[] split = m.getValue().split("::");
	
	                    if (split.length == 3) {
	                        funderID =  StringUtils.substringAfter(split[0], "dx.doi.org/");
	                        funderName = split[1];
	                        project = split[2];
	                    }
	
	                    if ((StringUtils.isBlank(funderID) && StringUtils.isBlank(funderName)) || StringUtils.isBlank(project)) {
	                        log.warn("No funder or no project found");
	                    } else {
	
	                        List<AuthorityValue> projectAuthorityList = authorityValueService
	                                .findByValue(ctx, "rioxxterms_identifier_project", project);
	
	                        FunderAuthorityValue funderAuthority = findFunder(ctx, funderName, funderID);
	                        ProjectAuthorityValue projectAuthority = null;
	
	                        if (CollectionUtils.isNotEmpty(projectAuthorityList)) {
	                            projectAuthority = (ProjectAuthorityValue) projectAuthorityList.get(0);
	                        }
	
	                        if (funderAuthority != null) {
	
	                            //Create new project if there isn't one just yet
	                            if (projectAuthority != null) {
	                            } else {
	                                projectAuthority = projectService.createProject(ctx, project, funderAuthority.getId());
	
	                            }
	
	                        } else {
	                            log.warn("no exact match for funder " + funderName + " in item " + item.getID());
	                        }
	
	                        //Add the metadata + delete the newfunderprojectpair
	                        if (projectAuthority != null && funderAuthority != null) {
	                            addValue(ctx, item, "rioxxterms", "identifier", "project", null, projectAuthority);
	                            addValue(ctx, item, "rioxxterms", "funder", null, null, funderAuthority);
	                            log.info("project - funder pair (" + projectAuthority.getValue() + " - " + funderAuthority
	                                    .getValue() + ") is added to the item " + item.getID());
	                        } else {
	                            itemService.addMetadata(ctx, item, "rioxxterms", "newfunderprojectpair", null, m.getLanguage(), m.getValue());
	                        }
	                    }
	                }
	                Set<MetadataField> metadataToRefresh = new HashSet<MetadataField>();
	                for (MetadataValue m : itemService.getMetadata(item, Item.ANY, Item.ANY, Item.ANY, Item.ANY)) {
	                    if (metadataAuthorityService.isAuthorityControlled(m.getMetadataField().toString('_'))) {
	                        metadataToRefresh.add(m.getMetadataField());
	                    }
	                }
	                for(MetadataField metadataToRefreshItem : metadataToRefresh) {
	                	String schema = metadataToRefreshItem.getMetadataSchema().getName();
						String element = metadataToRefreshItem.getElement();
						String qualifier = metadataToRefreshItem.getQualifier();
						List<MetadataValue> list = itemService.getMetadata(item, schema, element, qualifier, Item.ANY);
						itemService.clearMetadata(ctx, item, schema, element, qualifier, Item.ANY);
	                	for (MetadataValue m : list) {
	                		boolean handled = handleAuthorityControlledMetadatum(ctx, item, m);
	                		if(!handled) {
	                			itemService.addMetadata(ctx, item, schema, element, qualifier, m.getLanguage(), m.getValue(), m.getAuthority(), m.getConfidence());
	                		}
	                	}
	                }
	                itemService.update(ctx, item);
	            }
            }
        }
        itemIds.clear();
        ctx.restoreAuthSystemState();
    }

    private boolean handleAuthorityControlledMetadatum(Context ctx, Item item, MetadataValue m) throws SQLException, AuthorizeException {
        String[] split = m.getValue().split("::");
        String email = "";
        String name = "";

        String orcidValue = split[0];

        if (split.length >= 2) {
            name = split[1];
        }
        if (split.length >= 3) {
            email = split[2];
        }

        int beginIndex = orcidValue.lastIndexOf("/");

        //Handling the string alterations in case it's appended with an orcid url. These rules are in place to guard
        // us from String alteration related errors
        if (beginIndex == -1) {
            beginIndex = 0;
        } else {
            beginIndex += 1;
        }

        String orcidID = orcidValue.substring(beginIndex);
        if(StringUtils.isNotBlank(orcidID) && StringUtils.isNotBlank(name)) {
            if (orcidID.matches(ORCID_ID_SYNTAX)) {
                handleOrcidMetadatum(ctx, item, m, orcidID, name, email);
                return true;
            } else {
                PersonAuthorityValue personAuthorityValue = handlePersonAuthority(ctx, m, name, email);
                replaceMetadatumWithAuthority(ctx, item, m, name, personAuthorityValue.getId());
                return true;
            }
        }
        return false;
    }

    private void handleOrcidMetadatum(Context ctx, Item item, MetadataValue m, String orcidID, String name, String email) throws SQLException, AuthorizeException {
        Orcidv2AuthorityValue authorityValue = (Orcidv2AuthorityValue) authorityValueService.findByOrcidID(ctx, orcidID);
        if (authorityValue != null) {
            if (StringUtils.isNotBlank(email) && !authorityValue.getEmails().contains(email)) {
                authorityValue.addEmail(email);
                indexAuthority(authorityValue);
            }
            replaceMetadatumWithAuthority(ctx, item, m, name, authorityValue.getId());
        } else {
            Orcidv2AuthorityValue orcidAuthorityValue = handleOrcidAuthority(m, orcidID, name, email);
            replaceMetadatumWithAuthority(ctx, item, m, name, orcidAuthorityValue.getId());
        }
    }

    private Orcidv2AuthorityValue handleOrcidAuthority(MetadataValue m, String orcidID, String name, String email) {
        Orcidv2AuthorityValue orcidAuthorityValue = Orcidv2AuthorityValue.create();
        orcidAuthorityValue.setOrcid_id(orcidID);
        orcidAuthorityValue.setValue(name);
        orcidAuthorityValue.addEmail(email);

        String lastName = "";
        String firstName = "";

        if(StringUtils.isNotBlank(name)) {
            String[] nameParts = name.split(",");

            if(nameParts.length > 0) {
                lastName = nameParts[0];

                if (nameParts.length > 1) {
                    firstName = nameParts[1];
                }
            }
        }

        orcidAuthorityValue.setFirstName(firstName);
        orcidAuthorityValue.setLastName(lastName);
        orcidAuthorityValue.setField(m.getMetadataField().toString('_'));
        indexAuthority(orcidAuthorityValue);
        return orcidAuthorityValue;
    }

    private PersonAuthorityValue handlePersonAuthority(Context context, MetadataValue m,  String name, String email) {
        List<AuthorityValue> authorities = authorityValueService.findByExactValue(context, m.getMetadataField().toString('_'), name);

        for (AuthorityValue authorityValue : authorities) {
            if(authorityValue.getAuthorityType().equals("person")){
                return (PersonAuthorityValue) authorityValue;
            }
        }

        // create a new authority
        PersonAuthorityValue personAuthorityValue = new PersonAuthorityValue();
        personAuthorityValue.setValue(name);
        personAuthorityValue.addEmail(email);
        personAuthorityValue.setId(UUID.randomUUID().toString());
        personAuthorityValue.setLastModified(new Date());
        personAuthorityValue.setCreationDate(new Date());

        String lastName = "";
        String firstName = "";

        if(StringUtils.isNotBlank(name)) {
            String[] nameParts = name.split(",");

            if(nameParts.length > 0) {
                lastName = nameParts[0];

                if (nameParts.length > 1) {
                    firstName = nameParts[1];
                }
            }
        }

        personAuthorityValue.setFirstName(firstName);
        personAuthorityValue.setLastName(lastName);
        personAuthorityValue.setField(m.getMetadataField().toString('_'));
        indexAuthority(personAuthorityValue);
        return personAuthorityValue;
    }

    private void indexAuthority(AuthorityValue authorityValue) {
        AuthorityIndexingService indexingService = IndexingUtils.getServiceManager().getServiceByName(
                AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
        indexingService.indexContent(authorityValue);
        indexingService.commit();
    }

    private void replaceMetadatumWithAuthority(Context context, Item item, MetadataValue m, String name, String id) throws SQLException, AuthorizeException {
    	
    	String schema = m.getMetadataField().getMetadataSchema().getName();
    	String element = m.getMetadataField().getElement();
    	String qualifier = m.getMetadataField().getQualifier();
    	String language = m.getLanguage();
    	
        itemService.addMetadata(context, item, schema, element, qualifier, language, name, id, Choices.CF_ACCEPTED);
    }

    public void finish(Context ctx) throws Exception {

    }

    private void addValue(Context context, Item item, String schema, String element, String qualifier, String lang,
                          AuthorityValue authorityValue) throws SQLException {
        if (!itemFieldHasValue(item, schema, element, qualifier, lang, authorityValue)) {
            itemService.addMetadata(context, item, schema, element, qualifier, null, authorityValue.getValue(), authorityValue.getId(),
                    Choices.CF_ACCEPTED);
        }
    }

    private boolean itemFieldHasValue(Item item, String schema, String element, String qualifier, String lang,
                                      AuthorityValue value) {
        String language = lang;
        if (StringUtils.isBlank(lang)) {
            language = Item.ANY;
        }

        List<MetadataValue> metadata = itemService.getMetadata(item, schema, element, qualifier, language, Item.ANY);

        for (MetadataValue metadatum : metadata) {
            if (StringUtils.equals(value.getValue(), metadatum.getValue()) && StringUtils
                    .equals(value.getId(), metadatum.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    private FunderAuthorityValue findFunder(Context ctx, String funderName, String funderID) {
        FunderAuthorityValue funderAuthority = null;
        List<AuthorityValue> funderAuthorityList = new ArrayList<>();

        if(StringUtils.isNotBlank(funderID)) {
            //Look for funder and project in authority core
            funderAuthorityList = authorityValueService
                    .findByFieldAndValue(ctx, "label_funderID", funderID);
        }

        if(CollectionUtils.isEmpty(funderAuthorityList) && StringUtils.isNotBlank(funderName)) {
            funderAuthorityList = authorityValueService
                    .findByValue(ctx, "rioxxterms_funder", funderName);
        }

        //Save them if they are already in core
        if (CollectionUtils.isNotEmpty(funderAuthorityList)) {
            funderAuthority = (FunderAuthorityValue) funderAuthorityList.get(0);
        }

        return funderAuthority;
    }
    
}
