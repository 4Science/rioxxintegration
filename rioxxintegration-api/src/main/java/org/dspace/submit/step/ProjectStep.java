/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.submit.step;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.app.util.Util;
import org.dspace.authority.DefaultAuthorityCreator;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.Choices;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.project.ProjectService;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 03/09/15
 * Time: 10:48
 */
public class ProjectStep extends AbstractProcessingStep {
    private static Logger log = Logger.getLogger(ProjectStep.class);

    public static final int ADD_PROJECT_SUCCESS = 1;
    public static final int CREATE_PROJECT_ERROR = 2;
    public static final int LOOKUP_PROJECT_ERROR = 3;
    public static final int REMOVE_PROJECT_SUCCESS = 4;
    public static final int No_PROJECTS_ADDED = 5;

    private ProjectService projectService;
    private DefaultAuthorityCreator defaultAuthorityCreator;

    protected final ChoiceAuthorityService choiceAuthorityService;
    protected final MetadataAuthorityService metadataAuthorityService;
    
    /** Constructor */
    public ProjectStep() throws ServletException
    {
        metadataAuthorityService = ContentAuthorityServiceFactory.getInstance().getMetadataAuthorityService();
        choiceAuthorityService = ContentAuthorityServiceFactory.getInstance().getChoiceAuthorityService();
    }

    
    @Override
    public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, SQLException, AuthorizeException {
        Item item = subInfo.getSubmissionItem().getItem();
        clearErrorFields(request);

        String buttonPressed = Util.getSubmitButton(request, "");
        String removeButton = "submit_remove";
        String nextButton = "submit_next";
        String addButton = "submit_add";

        addFundersWithoutAuthority(context, request, item);

        if (buttonPressed.startsWith(removeButton)) {
            List<MetadataValue> dcValues = itemService.getMetadata(item, "rioxxterms", "identifier", "project", Item.ANY);

            int index = Integer.parseInt(buttonPressed.substring(buttonPressed.lastIndexOf("_") + 1, buttonPressed.length()));

            itemService.clearMetadata(context, item, "rioxxterms", "identifier", "project", Item.ANY);
            itemService.clearMetadata(context, item, "rioxxterms", "funder", null, Item.ANY);

            int counter = 0;
            for (MetadataValue dcv : dcValues) {
                if (counter != index) {
                    try {
                        ProjectAuthorityValue project = getProjectService().getProjectByAuthorityId(context, dcv.getAuthority());
                        itemService.addMetadata(context, item, dcv.getMetadataField(), dcv.getLanguage(), dcv.getValue(), dcv.getAuthority(), dcv.getConfidence());
                        itemService.addMetadata(context, item, "rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                        project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    } catch (IllegalArgumentException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                counter++;
            }

            itemService.update(context, item);

            return REMOVE_PROJECT_SUCCESS;
        }

        if (buttonPressed.startsWith(addButton)) {
            return processProjectField(context, request, item, "rioxxterms", "identifier", "project", ADD_PROJECT_SUCCESS);
        }

        if (buttonPressed.startsWith(nextButton)) {
            int success = processProjectField(context, request, item, "rioxxterms", "identifier", "project", STATUS_COMPLETE);

            if (success == STATUS_COMPLETE) {
                //check that at least one project is added
                List<MetadataValue> dcValues = itemService.getMetadata(item, "rioxxterms", "identifier", "project", Item.ANY);

                if (dcValues.size() == 0) {
                    if(ConfigurationManager.getBooleanProperty("rioxx", "submission.funder.required")){
                    	String metadataField = metadataFieldService.findByElement(context, "rioxxterms", "identifier", "project").toString();
                        addErrorField(request, metadataField);
                        success = No_PROJECTS_ADDED;
                    }
                }
            }
            return success;
        }

        return STATUS_COMPLETE;
    }

    private void addFundersWithoutAuthority(final Context context, final HttpServletRequest request, final Item item) throws SQLException, AuthorizeException {
        itemService.clearMetadata(context, item, "dc", "description", "sponsorship",  Item.ANY);
        readText(context, request, item, "dc", "description", "sponsorship", true, getDefaultLanguageQualifier());
    }

    private int processProjectField(Context context, HttpServletRequest request, Item item, String schema,
                                    String element, String qualifier, int success) throws SQLException, AuthorizeException {
        String metadataField = metadataFieldService.findByElement(context, schema, element, qualifier).toString();
        String value = request.getParameter(metadataField);
        String av = request.getParameter(metadataField + "_authority");
        String cv = request.getParameter(metadataField + "_confidence");
        if (StringUtils.isBlank(value) && noProjectAndFunderAttached(item)) {
            ProjectAuthorityValue project = getDefaultAuthorityCreator().retrieveDefaultProject(context);

            if(project!=null) {
                value = project.getValue();
                av = project.getId();
            }
        }
        if (StringUtils.isNotBlank(value)) {
            if (StringUtils.isNotBlank(av)) {
                try {
                    ProjectAuthorityValue project = getProjectService().getProjectByAuthorityId(context, av);
                    itemService.addMetadata(context, item, schema, element, qualifier, getDefaultLanguageQualifier(), value,
                            av, (cv != null && cv.length() > 0) ?
                                    Choices.getConfidenceValue(cv) : Choices.CF_ACCEPTED);
                    itemService.addMetadata(context, item, "rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                            project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    itemService.update(context, item);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                    addErrorField(request, metadataField);
                    return LOOKUP_PROJECT_ERROR;
                }
            } else {
                String metadataFieldFunder = metadataFieldService.findByElement(context, "rioxxterms", "funder", null).toString();
                String funderAuthority = request.getParameter(metadataFieldFunder + "_authority");
                try {
                    if (StringUtils.isBlank(funderAuthority)) {
                        FunderAuthorityValue defaultAuthority = getDefaultAuthorityCreator().retrieveDefaultFunder(context);

                        if(defaultAuthority!=null) {
                            funderAuthority = defaultAuthority.getId();
                        }
                    }
                    ProjectAuthorityValue project = getProjectService().createProject(context, value, funderAuthority);
                    itemService.addMetadata(context, item, "rioxxterms", "identifier", "project", getDefaultLanguageQualifier(), value, project.getId(), Choices.CF_ACCEPTED);
                    itemService.addMetadata(context, item, "rioxxterms", "funder", null, getDefaultLanguageQualifier(), project.getFunderAuthorityValue().getValue(),
                            project.getFunderAuthorityValue().getId(), (Choices.CF_ACCEPTED));
                    itemService.update(context, item);
                    request.getSession().setAttribute("newProject", project);
                } catch (IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                    addErrorField(request, metadataField);
                    return CREATE_PROJECT_ERROR;
                }
            }
        } else {
            addErrorField(request, metadataField);
        }

        return success;
    }

    private boolean noProjectAndFunderAttached(Item item) {
    	List<MetadataValue> funders = itemService.getMetadata(item, "rioxxterms", "funder", null, Item.ANY);
        List<MetadataValue> projects = itemService.getMetadata(item, "rioxxterms", "identifier", "project", Item.ANY);
        return funders.size() == 0 && projects.size() == 0;
    }


    @Override
    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }

    public static String getDefaultLanguageQualifier() {
        String language = "";
        language = ConfigurationManager.getProperty("default.language");
        if (StringUtils.isEmpty(language)) {
            language = "en";
        }
        return language;
    }

    protected void readText(Context context, HttpServletRequest request, Item item, String schema,
                            String element, String qualifier, boolean repeated, String lang) throws SQLException {
        // some other way
        String metadataField = metadataFieldService.findByElement(context, schema, element, qualifier).toString();

        String fieldKey = metadataAuthorityService.makeFieldKey(schema, element, qualifier);
        boolean isAuthorityControlled = metadataAuthorityService.isAuthorityControlled(fieldKey);
        
        // Values to add
        List<String> vals = null;
        List<String> auths = null;
        List<String> confs = null;

        if (repeated) {
            vals = getRepeatedParameter(request, metadataField, metadataField);
            if (isAuthorityControlled) {
                auths = getRepeatedParameter(request, metadataField, metadataField + "_authority");
                confs = getRepeatedParameter(request, metadataField, metadataField + "_confidence");
            }

        } else {
            // Just a single name
            vals = new LinkedList<String>();
            String value = request.getParameter(metadataField);
            if (value != null) {
                vals.add(value.trim());
            }
            if (isAuthorityControlled) {
                auths = new LinkedList<String>();
                confs = new LinkedList<String>();
                String av = request.getParameter(metadataField + "_authority");
                String cv = request.getParameter(metadataField + "_confidence");
                auths.add(av == null ? "" : av.trim());
                confs.add(cv == null ? "" : cv.trim());
            }
        }

        // Put the names in the correct form
        for (int i = 0; i < vals.size(); i++) {
            // Add to the database if non-empty
            String s = vals.get(i);
            if ((s != null) && !s.equals("")) {
                if (isAuthorityControlled) {
                    String authKey = auths.size() > i ? auths.get(i) : null;
                    String sconf = (authKey != null && confs.size() > i) ? confs.get(i) : null;
                    if (metadataAuthorityService.isAuthorityRequired(fieldKey) &&
                            (authKey == null || authKey.length() == 0)) {
                        log.warn("Skipping value of " + metadataField + " because the required Authority key is missing or empty.");
                        addErrorField(request, metadataField);
                    } else {
                        itemService.addMetadata(context, item, schema, element, qualifier, lang, s,
                                authKey, (sconf != null && sconf.length() > 0) ?
                                        Choices.getConfidenceValue(sconf) : Choices.CF_ACCEPTED);
                    }
                } else {
                	itemService.addMetadata(context, item, schema, element, qualifier, lang, s);
                }
            }
        }
    }

    protected List<String> getRepeatedParameter(HttpServletRequest request,
                                                String metadataField, String param) {
        List<String> vals = new LinkedList<String>();

        int i = 1;    //start index at the first of the previously entered values
        boolean foundLast = false;

        // Iterate through the values in the form.
        while (!foundLast) {
            String s = null;

            //First, add the previously entered values.
            // This ensures we preserve the order that these values were entered
            s = request.getParameter(param + "_" + i);

            // If there are no more previously entered values,
            // see if there's a new value entered in textbox
            if (s == null) {
                s = request.getParameter(param);
                //this will be the last value added
                foundLast = true;
            }

            // We're only going to add non-null values
            if (s != null) {
                boolean addValue = true;

                // Check to make sure that this value was not selected to be
                // removed.
                // (This is for the "remove multiple" option available in
                // Manakin)
                String[] selected = request.getParameterValues(metadataField
                        + "_selected");

                if (selected != null) {
                    for (int j = 0; j < selected.length; j++) {
                        if (selected[j].equals(metadataField + "_" + i)) {
                            addValue = false;
                        }
                    }
                }

                if (addValue) {
                    vals.add(s.trim());
                }
            }

            i++;
        }

        log.debug("getRepeatedParameter: metadataField=" + metadataField
                + " param=" + metadataField + ", return count = " + vals.size());

        return vals;
    }

	public DefaultAuthorityCreator getDefaultAuthorityCreator() {
	    if(defaultAuthorityCreator == null) {
	    	defaultAuthorityCreator = new DSpace().getServiceManager().getServiceByName("defaultAuthorityCreator", DefaultAuthorityCreator.class);
	    }
		return defaultAuthorityCreator;
	}

	public void setDefaultAuthorityCreator(DefaultAuthorityCreator defaultAuthorityCreator) {
		this.defaultAuthorityCreator = defaultAuthorityCreator;
	}

	public ProjectService getProjectService() {
		if(projectService == null) {
			projectService = new DSpace().getServiceManager().getServiceByName("ProjectService", ProjectService.class);
		}
		return projectService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}
}
