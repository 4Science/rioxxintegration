/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.pure.consumer;

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
import org.dspace.authority.AuthorityValueFinder;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.IndexingUtils;
import org.dspace.authority.PersonAuthorityValue;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.orcid.Orcidv2AuthorityValue;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.MetadataAuthorityManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.event.Consumer;
import org.dspace.event.Event;
import org.dspace.project.ProjectService;
import org.dspace.utils.DSpace;

public class RIOXXConsumer implements Consumer {

    private Set<Integer> itemIds = new HashSet<>();
    private ProjectService projectService = new DSpace().getServiceManager()
            .getServiceByName("ProjectService", ProjectService.class);
    private static final String ORCID_ID_SYNTAX = "\\d{4}-\\d{4}-\\d{4}-(\\d{3}X|\\d{4})";
    AuthorityValueFinder authorityValueFinder = new AuthorityValueFinder();

    private static Logger log = Logger.getLogger(RIOXXConsumer.class);

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
        for (Integer itemId : itemIds) {
            Item item = Item.find(ctx, itemId);
            log.debug("updating funders of item " + item.getID());

            Metadatum[] metadatum = item.getMetadata("dc", "sword", "submission", Item.ANY);
            if (!(metadatum.length == 0 || StringUtils.equals(metadatum[0].value, "false"))) {
                Metadatum[] metadata = item.getMetadata("rioxxterms", "newfunderprojectpair", null, Item.ANY);
                item.clearMetadata("rioxxterms", "newfunderprojectpair", null, Item.ANY);

                for (Metadatum m : metadata) {

                    String funderName = "";
                    String funderID = "";
                    String project = "";


                    //funder and project are in same metadatafield separated by ::
                    String[] split = m.value.split("::");

                    if (split.length == 3) {
                        funderID =  StringUtils.substringAfter(split[0], "dx.doi.org/");
                        funderName = split[1];
                        project = split[2];
                    }

                    if ((StringUtils.isBlank(funderID) && StringUtils.isBlank(funderName)) || StringUtils.isBlank(project)) {
                        log.warn("No funder or no project found");
                    } else {
                        AuthorityValueFinder authorityValueFinder = new AuthorityValueFinder();

                        List<AuthorityValue> projectAuthorityList = authorityValueFinder
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
                            addValue(item, "rioxxterms", "identifier", "project", null, projectAuthority);
                            addValue(item, "rioxxterms", "funder", null, null, funderAuthority);
                            log.info("project - funder pair (" + projectAuthority.getValue() + " - " + funderAuthority
                                    .getValue() + ") is added to the item " + item.getID());
                        } else {
                            item.addMetadata("rioxxterms", "newfunderprojectpair", null, m.language, m.value);
                        }
                    }
                }
                for (Metadatum m : item.getMetadata("*", "*", "*", "*")) {
                    if (MetadataAuthorityManager.getManager().isAuthorityControlled(m.getField().replace(".", "_"))) {
                        handleAuthorityControlledMetadatum(ctx, item, m);
                    }
                }
                item.update();
            }
        }
        itemIds.clear();
        // commit context
        ctx.getDBConnection().commit();


    }

    private void handleAuthorityControlledMetadatum(Context ctx, Item item, Metadatum m) {
        String[] split = m.value.split("::");
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
            } else {
                PersonAuthorityValue personAuthorityValue = handlePersonAuthority(ctx, m, name, email);
                replaceMetadatumWithAuthority(item, m, name, personAuthorityValue.getId());
            }
        }
    }

    private void handleOrcidMetadatum(Context ctx, Item item, Metadatum m, String orcidID, String name, String email) {
        Orcidv2AuthorityValue authorityValue = (Orcidv2AuthorityValue) authorityValueFinder.findByOrcidID(ctx, orcidID);
        if (authorityValue != null) {
            if (StringUtils.isNotBlank(email) && !authorityValue.getEmails().contains(email)) {
                authorityValue.addEmail(email);
                indexAuthority(authorityValue);
            }
            replaceMetadatumWithAuthority(item, m, name, authorityValue.getId());
        } else {
            Orcidv2AuthorityValue orcidAuthorityValue = handleOrcidAuthority(m, orcidID, name, email);
            replaceMetadatumWithAuthority(item, m, name, orcidAuthorityValue.getId());
        }
    }

    private Orcidv2AuthorityValue handleOrcidAuthority(Metadatum m, String orcidID, String name, String email) {
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
        orcidAuthorityValue.setField(m.getField().replace(".", "_"));
        indexAuthority(orcidAuthorityValue);
        return orcidAuthorityValue;
    }

    private PersonAuthorityValue handlePersonAuthority(Context context, Metadatum m,  String name, String email) {
        List<AuthorityValue> authorities = authorityValueFinder.findByExactValue(context, StringUtils.replace(m.getField(), ".", "_" ), name);

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
        personAuthorityValue.setField(m.getField().replace(".", "_"));
        indexAuthority(personAuthorityValue);
        return personAuthorityValue;
    }

    private void indexAuthority(AuthorityValue authorityValue) {
        AuthorityIndexingService indexingService = IndexingUtils.getServiceManager().getServiceByName(
                AuthorityIndexingService.class.getName(), AuthorityIndexingService.class);
        indexingService.indexContent(authorityValue, true);
        indexingService.commit();
    }

    private void replaceMetadatumWithAuthority(Item item, Metadatum m, String name, String id) {
        Metadatum authorityMetadatum = m.copy();
        authorityMetadatum.value = name;
        authorityMetadatum.authority = id;
        authorityMetadatum.confidence = Choices.CF_ACCEPTED;

        Metadatum[] list = item.getMetadata(m.schema, m.element, m.qualifier, m.language);
        item.clearMetadata(m.schema, m.element, m.qualifier, m.language);

        for (Metadatum metadatum : list) {
            if (!metadatum.equals(m)) {
                item.addMetadata(metadatum.schema, metadatum.element, metadatum.qualifier, metadatum.language, metadatum.value, metadatum.authority, metadatum.confidence);
            }
        }
        item.addMetadata(authorityMetadatum.schema, authorityMetadatum.element, authorityMetadatum.qualifier,
                authorityMetadatum.language, authorityMetadatum.value, authorityMetadatum.authority,
                authorityMetadatum.confidence);
    }

    public void finish(Context ctx) throws Exception {

    }

    private void addValue(Item item, String schema, String element, String qualifier, String lang,
                          AuthorityValue authorityValue) {
        if (!itemFieldHasValue(item, schema, element, qualifier, lang, authorityValue)) {
            item.addMetadata(schema, element, qualifier, null, authorityValue.getValue(), authorityValue.getId(),
                    Choices.CF_ACCEPTED);
        }
    }

    private boolean itemFieldHasValue(Item item, String schema, String element, String qualifier, String lang,
                                      AuthorityValue value) {
        String language = lang;
        if (StringUtils.isBlank(lang)) {
            language = Item.ANY;
        }

        List<Metadatum> metadata = item.getMetadata(schema, element, qualifier, language, Item.ANY);

        for (Metadatum metadatum : metadata) {
            if (StringUtils.equals(value.getValue(), metadatum.value) && StringUtils
                    .equals(value.getId(), metadatum.authority)) {
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
            funderAuthorityList = authorityValueFinder
                    .findByFieldAndValue(ctx, "label_funderID", funderID);
        }

        if(CollectionUtils.isEmpty(funderAuthorityList) && StringUtils.isNotBlank(funderName)) {
            funderAuthorityList = authorityValueFinder
                    .findByValue(ctx, "rioxxterms_funder", funderName);
        }

        //Save them if they are already in core
        if (CollectionUtils.isNotEmpty(funderAuthorityList)) {
            funderAuthority = (FunderAuthorityValue) funderAuthorityList.get(0);
        }

        return funderAuthority;
    }
}
