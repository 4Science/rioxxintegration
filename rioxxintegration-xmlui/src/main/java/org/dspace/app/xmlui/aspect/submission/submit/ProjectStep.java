package org.dspace.app.xmlui.aspect.submission.submit;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.aspect.project.ProjectFieldRenderer;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Instance;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.authority.DefaultAuthorityCreator;
import org.dspace.authority.ProjectAuthorityValue;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.project.ProjectService;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 03/09/15
 * Time: 10:51
 */
public class ProjectStep extends AbstractSubmissionStep {
    private static Logger log = Logger.getLogger(ProjectStep.class);

    protected static final Message T_head = message("xmlui.Submission.submit.ProjectStep.head");
    protected static final Message T_required_field = message("xmlui.Submission.submit.DescribeStep.required_field");

    protected static final Message T_project_label = message("xmlui.Submission.submit.ProjectStep.project.label");
    protected static final Message T_funder_label = message("xmlui.Submission.submit.ProjectStep.funder.label");
    protected static final Message T_project_funder_hint = message("xmlui.Submission.submit.ProjectStep.project_funder.hint");
    protected static final Message T_remove = message("xmlui.Submission.submit.ProjectStep.remove");
    protected static final Message T_add = message("xmlui.Submission.submit.ProjectStep.add");
    protected static final Message T_create_project_error = message("xmlui.Submission.submit.ProjectStep.create_project_error");
    protected static final Message T_lookup_project_error = message("xmlui.Submission.submit.ProjectStep.lookup_project_error");
    protected static final Message T_default_project_warning = message("xmlui.Submission.submit.ProjectStep.default_project_warning");
    protected static final Message T_no_default_project_warning = message("xmlui.Submission.submit.ProjectStep.no_default_project_warning");
    protected static final Message T_no_default_project_compliance_warning = message("xmlui.Submission.submit.ProjectStep.no_default_project_compliance_warning");
    protected static final Message T_funders_without_authority_head = message("xmlui.Submission.submit.ProjectStep.funders_without_authority_head");
    protected static final Message T_funders_without_authority_hint = message("xmlui.Submission.submit.ProjectStep.funders_without_authority_hint");

    private ProjectAuthorityValue newProject;
    private String defaultProject;
    private String defaultFunder;

    private DefaultAuthorityCreator defaultAuthorityCreator;
    private ProjectService projectService;

    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    protected ChoiceAuthorityService choiceAuthorityService = ContentAuthorityServiceFactory.getInstance().getChoiceAuthorityService();

    protected MetadataAuthorityService metadataAuthorityService = ContentAuthorityServiceFactory.getInstance().getMetadataAuthorityService();
    
    @Override
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);
        Request request = ObjectModelHelper.getRequest(objectModel);
        newProject = (ProjectAuthorityValue) request.getSession().getAttribute("newProject");

        ProjectAuthorityValue project = getDefaultAuthorityCreator().retrieveDefaultProject(context);

        if(project!=null) {
            defaultFunder = project.getFunderAuthorityValue().getValue();
            defaultProject = project.getValue();
        }
        else {
            defaultFunder = null;
            defaultProject = null;
        }
    }

    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        super.addPageMeta(pageMeta);
        UUID collectionID = submission.getCollection().getID();
        pageMeta.addMetadata("choice", "collection").addContent(String.valueOf(collectionID));
        pageMeta.addMetadata("stylesheet", "screen", "datatables", true).addContent("../../static/Datatables/DataTables-1.8.0/media/css/datatables.css");
        pageMeta.addMetadata("javascript", "static", "datatables", true).addContent("static/Datatables/DataTables-1.8.0/media/js/jquery.dataTables.min.js");
        pageMeta.addMetadata("stylesheet", "screen", "person-lookup", true).addContent("../../static/css/authority/person-lookup.css");
        pageMeta.addMetadata("javascript", null, "person-lookup", true).addContent("../../static/js/person-lookup.js");


        String jumpTo = submissionInfo.getJumpToField();
        if (jumpTo != null)
        {
            pageMeta.addMetadata("page", "jumpTo").addContent(jumpTo);
        }
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
        Item item = submission.getItem();
        Collection collection = submission.getCollection();

        String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";
        Division div = body.addInteractiveDivision("submit-describe", actionURL, Division.METHOD_POST, "primary submission");
        div.setHead(T_submission_head);
        addSubmissionProgressList(div);

        List form = div.addList("submit-project", List.TYPE_FORM);
        form.setHead(T_head);

        String fieldName = "rioxxterms_identifier_project";
        renderOneboxField(form, fieldName, false, false, T_project_label, null);

        fieldName = "rioxxterms_funder";
        renderOneboxField(form, fieldName, true, false, T_funder_label, null);

        form.addItem("project_funder_help","").addContent(T_project_funder_hint.parameterize(ConfigurationManager.getProperty("mail.admin")));

        java.util.List<MetadataValue> dcValues = itemService.getMetadata(item, "rioxxterms", "identifier", "project", Item.ANY);

        form.addItem().addButton("submit_add").setValue(T_add);

        renderResults(div, dcValues);

        boolean enableDcSponsorship = ConfigurationManager.getBooleanProperty("rioxx", "submission.funder.enableDcSponsorship");
        if(enableDcSponsorship) {
            addFundersWithoutAuthorityField(div, item);
        }

        addControlButtons(div.addList("submit-project-part2", List.TYPE_FORM));
    }

    private void addFundersWithoutAuthorityField(final Division div, final Item item) throws WingException {
        java.util.List<MetadataValue> dcValues = itemService.getMetadata(item, "dc", "description", "sponsorship", Item.ANY);
        String fieldName = metadataAuthorityService.makeFieldKey("dc", "description", "sponsorship");

        List fundersWithoutAuthorityList = div.addList("funders-without-authority", List.TYPE_FORM);

        fundersWithoutAuthorityList.setHead(T_funders_without_authority_head);

        Text text = fundersWithoutAuthorityList.addItem().addText(fieldName, "submit-text");

        // Setup the select field
        text.setHelp(T_funders_without_authority_hint);
        text.enableAddOperation();

        if (isFieldInError(fieldName)) {
            text.addError(T_required_field);
        }
        if (dcValues.size() > 1) {
        }

        // Setup the field's values
        if (dcValues.size() >= 1) {
            text.enableDeleteOperation();
            for (MetadataValue dcValue : dcValues) {
                Instance ti = text.addInstance();
                ti.setValue(dcValue.getValue());
            }
        }
    }

    private void renderResults(Division div, java.util.List<MetadataValue> dcValues) throws WingException {
        if (dcValues != null && dcValues.size() > 0) {
            Division projectDiv = div.addDivision("projects");

            Table table = projectDiv.addTable("project-table", 3, dcValues.size() + 1);
            Row header = table.addRow(Row.ROLE_HEADER);
            header.addCell().addContent(T_project_label);
            header.addCell().addContent(T_funder_label);
            header.addCell().addContent("");

            int count = 0;

            for (MetadataValue dcValue : dcValues) {
                ProjectAuthorityValue project;
                if (newProject != null && newProject.getId().equals(dcValue.getAuthority())) {
                    project = newProject;
                } else {
                    project = getProjectService().getProjectByAuthorityId(context, dcValue.getAuthority());
                }

                if (project != null) {
                    Row row = table.addRow();
                    row.addCell().addContent(project.getValue());
                    row.addCell().addContent(project.getFunderAuthorityValue().getValue());
                    row.addCell().addButton("submit_remove_project_" + count).setValue(T_remove);
                }

                count++;
            }
        } else {
            if(ConfigurationManager.getBooleanProperty("rioxx","submission.funder.required")){
                Division requiredWarningDiv = div.addDivision("default-project-warning", "alert alert-warning bold");
                if(defaultProject!=null && defaultFunder!=null) {
                    requiredWarningDiv.addPara().addContent(T_default_project_warning.parameterize(defaultProject, defaultFunder));
                }else{
                    requiredWarningDiv.addPara().addContent(T_no_default_project_warning);
                }
            } else{
                Division notRequiredWarningDiv = div.addDivision("default-project-warning", "alert alert-warning bold");
                if(defaultProject==null || defaultFunder==null) {
                    notRequiredWarningDiv.addPara().addContent(T_no_default_project_compliance_warning);
                }
            }

        }
    }

    private void renderOneboxField(List form, String fieldName, boolean readOnly, boolean required, Message label, Message hint) throws WingException {
        Text text = new ProjectFieldRenderer().generalisedOneBoxFieldRender(form, fieldName, readOnly, required, label, hint);

        if (isFieldInError(fieldName)) {
            if (this.errorFlag == org.dspace.submit.step.ProjectStep.CREATE_PROJECT_ERROR) {
                text.addError(T_create_project_error);

            } else if (this.errorFlag == org.dspace.submit.step.ProjectStep.LOOKUP_PROJECT_ERROR) {
                text.addError(T_lookup_project_error);
            } else {
                text.addError(T_required_field);
            }
        }

    }




    /**
     * Check if the given fieldname is listed as being in error.
     *
     * @param fieldName
     * @return
     */
    private boolean isFieldInError(String fieldName) {
        return (this.errorFields.contains(fieldName));
    }

    @Override
    public List addReviewSection(List reviewList) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        List projectSection = reviewList.addList("submit-review-" + this.stepAndPage, List.TYPE_FORM);
        projectSection.setHead(T_head);

        java.util.List<MetadataValue> dcValues = itemService.getMetadata(submission.getItem(), "rioxxterms", "identifier", "project", Item.ANY);

        for (MetadataValue dcValue : dcValues) {
            org.dspace.app.xmlui.wing.element.Item project = projectSection.addItem();

            try {
                ProjectAuthorityValue projectAuthorityValue = getProjectService().getProjectByAuthorityId(context, dcValue.getAuthority());

                project.addContent(projectAuthorityValue.getValue() + " - " + projectAuthorityValue.getFunderAuthorityValue().getValue());
            }
            catch (IllegalArgumentException e){
                log.error(e.getMessage(),e);
            }
        }

        boolean enableDcSponsorship = ConfigurationManager.getBooleanProperty("rioxx", "submission.funder.enableDcSponsorship");
        if(enableDcSponsorship) {
            dcValues = itemService.getMetadata(submission.getItem(), "dc", "description", "sponsorship", Item.ANY);
            for (MetadataValue dcValue : dcValues) {
                org.dspace.app.xmlui.wing.element.Item funder = projectSection.addItem();
                funder.addContent(dcValue.getValue());
            }
        }

        return projectSection;
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
