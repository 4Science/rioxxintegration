package org.dspace.app.xmlui.objectmanager;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;
import org.dspace.project.ProjectService;
import org.dspace.util.subclasses.Metadata;

/**
 * Metadata enricher that will add the Rioxx Project - Funder relation to the metadata
 */
public class RioxxProjectFunderEnricher implements MetaDatumEnricher {

    private static Logger log = Logger.getLogger(RioxxProjectFunderEnricher.class);

    private static final String RIOXX_SHEMA = "rioxxterms";
    private static final String IDENTIFIER_ELEMENT = "identifier";
    private static final String PROJECT_QUALIFIER = "project";
    private static final String FUNDER_ELEMENT = "funder";
    private static final String ID_QUALIFIER = "id";

    private ProjectService projectService;

    public List<Metadata> enrichMetadata(final Context context, final List<MetadataValue> metadataList) {
    	List<Metadata> newMetaData = new LinkedList<>();
    	if(CollectionUtils.isNotEmpty(metadataList)) {
            //For all RIOXX project identifiers
            for (MetadataValue metadatum : metadataList) {
                if(StringUtils.equals(RIOXX_SHEMA, metadatum.getMetadataField().getMetadataSchema().getName())
                        && StringUtils.equals(IDENTIFIER_ELEMENT, metadatum.getMetadataField().getElement())
                        && StringUtils.equals(PROJECT_QUALIFIER, metadatum.getMetadataField().getQualifier())) {

                    //Check if we can find the corresponding Funder Authority
                    ProjectAuthorityValue authorityValue = null;

                    try {
                        authorityValue = projectService.getProjectByAuthorityId(context, metadatum.getAuthority());
                    }
                    catch (IllegalArgumentException e) {
                        log.error(e.getMessage(), e);
                    }

                    if(authorityValue != null && authorityValue.getFunderAuthorityValue() != null) {
                        String language = metadatum.getLanguage();
                        int confidence = metadatum.getConfidence();
                        //If we do, create a new "fake" metadata value that contains the link between the project and the funder
                        newMetaData.add(createProjectFunderRelationMetadatumRecord(authorityValue, language, confidence));

                        //also expose the funder ID
                        newMetaData.add(createFunderIdMetadatumRecord(authorityValue, language, confidence));
                    }
                }
            }
        }
    	return newMetaData;
    }

    public static Metadata createProjectFunderRelationMetadatumRecord(final ProjectAuthorityValue authorityValue, final String language, final int confidence) {
        //Store the funder authority in the authority field and Store the project authority in the value field
    	Metadata newMetadatum = new Metadata(RIOXX_SHEMA, FUNDER_ELEMENT, PROJECT_QUALIFIER, language, authorityValue.getId(), authorityValue.getFunderAuthorityValue().getId(), confidence);
        return newMetadatum;
    }

    public static Metadata createFunderIdMetadatumRecord(final ProjectAuthorityValue authorityValue, final String language, final int confidence) {
        //Store the Funder ID in the value field
    	Metadata newMetadatum = new Metadata(RIOXX_SHEMA, FUNDER_ELEMENT, ID_QUALIFIER, language, authorityValue.getFunderAuthorityValue().getFunderID(), null, -1);
        return newMetadatum;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
}