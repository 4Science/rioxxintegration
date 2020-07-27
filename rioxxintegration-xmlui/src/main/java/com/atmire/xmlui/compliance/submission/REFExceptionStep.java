package com.atmire.xmlui.compliance.submission;

import com.atmire.ref.compliance.submission.ExceptionInformation;
import org.apache.cocoon.ProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.dspace.app.xmlui.aspect.compliance.ComplianceUI;
import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Metadatum;
import org.dspace.ref.compliance.result.CategoryComplianceResult;
import org.dspace.ref.compliance.result.ComplianceResult;
import org.dspace.ref.compliance.service.ComplianceCheckService;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by jonas - jonas@atmire.com on 08/04/16.
 */
public class REFExceptionStep extends AbstractSubmissionStep {

    protected static final Message T_head = message("xmlui.compliance.submission.ExceptionStep.head");
    protected static final Message T_info = message("xmlui.compliance.submission.ExceptionStep.info");
    protected static final Message T_none = message("xmlui.compliance.submission.ExceptionStep.exception.none");
    private static final String TITLE_1_BASE = "xmlui.compliance.submission.ExceptionStep.specificException.title1.";
    private static final String PARA_1_BASE = "xmlui.compliance.submission.ExceptionStep.specificException.para1.";
    private static final String TITLE_2_BASE = "xmlui.compliance.submission.ExceptionStep.specificException.title2.";
    private static final String PARA_2_BASE = "xmlui.compliance.submission.ExceptionStep.specificException.para2.";
    private static final String EXCEPTION_RADIOBUTTON_BASE = "xmlui.compliance.submission.ExceptionStep.exception.";

    private static final Message T_compliant = message("xmlui.compliance.submission.ExceptionStep.compliant");
    private static final Message T_not_compliant = message("xmlui.compliance.submission.ExceptionStep.not_compliant");

    private static ComplianceCheckService complianceCheckService = new DSpace().getServiceManager()
            .getServiceByName("refComplianceCheckService", ComplianceCheckService.class);

    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, SQLException, IOException, AuthorizeException {
        super.addPageMeta(pageMeta);
        pageMeta.addMetadata("javascript", null, "person-lookup", true).addContent("../../static/js/ref-exceptions.js");

    }

    @Override
    public List addReviewSection(List list) throws SAXException, WingException, SQLException, IOException, AuthorizeException {
        return null;
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, SQLException, IOException, AuthorizeException, ProcessingException {
        Collection collection = submission.getCollection();
        String actionURL = contextPath + "/handle/" + collection.getHandle() + "/submit/" + knot.getId() + ".continue";

        Division div = body.addInteractiveDivision("submit-describe", actionURL, Division.METHOD_POST, "primary submission");
        addSubmissionProgressList(div);

        div.setHead(T_head);

        org.dspace.content.Item item = submission.getItem();
        ComplianceResult complianceResult = complianceCheckService.checkCompliance(context, item);

        //Only show exception select options when the item is applicable for REF.
        if(complianceResult.isApplicable()) {
            div.addPara(T_info);

            String[] specifiedExceptions = new DSpace().getServiceManager().getServiceByName("configuredExceptions", String[].class);

            List form = div.addList("ref-compliance-exceptions", List.TYPE_ORDERED);

            Item item1 = form.addItem("exceptions-list", null);
            Radio exceptions = item1.addRadio("exception-options");
            exceptions.addOption(true, "none", T_none);

            addBaseExceptionSelection(specifiedExceptions, exceptions, item);

            for (int i = 0; i < specifiedExceptions.length; i++) {
                String specifiedException = specifiedExceptions[i];
                addSpecifiedExceptionPart(div, item, specifiedException);
            }

            Division complianceDiv = div.addDivision("ref-compliance", "ref-compliance");

            if (complianceResult.isCompliant()) {
                complianceDiv.addPara().addContent(T_compliant);
            } else {
                complianceDiv.addPara().addContent(T_not_compliant);
                List list = complianceDiv.addList("requirements-list");

                for (CategoryComplianceResult categoryComplianceResult : complianceResult.getOrderedCategoryResults()) {
                    if (!categoryComplianceResult.isCompliant()) {
                        list.addItem().addContent(categoryComplianceResult.getCategoryName());
                    }
                }
            }

        } else {
            //Inform the user that his item is not applicable and that he doesn't need to select an exception.
            ComplianceUI complianceUI = new ComplianceUI();
            complianceUI.setIdentifier("ref");
            complianceUI.addItemNotApplicableSection(div, complianceResult);
        }

        List submit = div.addList("submit-exception", List.TYPE_FORM);
        addControlButtons(submit);
    }

    /**
     *  Add configured exceptions as options to radiobutton
     * @param specifiedExceptions The exceptions configured in refterms-exceptions.xml
     * @param exceptions A Radio object to add the exceptions to
     * @param item The item to check metadata against
     * @throws WingException
     */
    private void addBaseExceptionSelection(String[] specifiedExceptions, Radio exceptions, org.dspace.content.Item item) throws WingException {
        // Loop over configured exceptions to create the select box
        for(int i = 0; i<specifiedExceptions.length; i++){
            String specifiedException = specifiedExceptions[i];
            boolean preselected = checkPreselection(item, specifiedException,null);
            exceptions.addOption(preselected, specifiedException, message(EXCEPTION_RADIOBUTTON_BASE + specifiedExceptions[i]));
        }
    }

    /**
     * Adds an (optional) dropdown with configured values and a (required) textarea to fill in
     * @param div Base division
     * @param item Item to check metadata against
     * @param specifiedException The actual exception to create a subdivision for
     * @throws WingException
     */
    private void addSpecifiedExceptionPart(Division div, org.dspace.content.Item item, String specifiedException) throws WingException {
        boolean preselected = checkPreselection(item, specifiedException,null)||checkPreselection(item, specifiedException+"Explanation",null);
        String hidden = (preselected)?"":"hidden ";
        Division specificException = div.addDivision(specifiedException,hidden+"specificExceptions");
        HashMap<String,ExceptionInformation> reftermsExplanation = new DSpace().getServiceManager().getServiceByName("refterms-"+specifiedException, HashMap.class);

        addDropDownSelection(specifiedException, specificException,reftermsExplanation);
        addTextAreaField(specifiedException, specificException,reftermsExplanation,preselected);
    }

    /**
     * Check if a given metadata is filled in. (with an optional value to check against.
     * @param item The item to check against
     * @param metadata The metadata to check against. (will be prepended with "refterms."
     * @param valueToCheck (Optional) value to check against, if left null, if ANYTHING is found in the metadata, return true), otherwise, check equals
     * @return
     */
    private boolean checkPreselection(org.dspace.content.Item item, String metadata, String valueToCheck) {
        Metadatum[] specifiedExceptionMetadata = item.getMetadataByMetadataString("refterms." + metadata);
        boolean preselected =false;
        if(specifiedExceptionMetadata !=null && specifiedExceptionMetadata.length>0 && StringUtils.isNotBlank(specifiedExceptionMetadata[0].value)){
            if(StringUtils.isBlank(valueToCheck)||StringUtils.equals(specifiedExceptionMetadata[0].value,valueToCheck)){
                preselected=true;
            }
        }
        return preselected;
    }

    /**
     * Create textAreaField for a specifiedException, prefilled if the metadata is found
     * @param specifiedException
     * @param specificException
     * @param reftermsExplanation
     * @param preselected
     * @throws WingException
     */
    private void addTextAreaField(String specifiedException, Division specificException,HashMap<String,ExceptionInformation> reftermsExplanation,boolean preselected) throws WingException {
        Division textAreaDivision = specificException.addDivision("textAreaDivision");
        textAreaDivision.setHead(message(TITLE_2_BASE + specifiedException));
        textAreaDivision.addPara(message(PARA_2_BASE + specifiedException));

        List exceptionexplanation = textAreaDivision.addList(specifiedException+"-explanation", List.TYPE_ORDERED);
        Item explanation = exceptionexplanation.addItem();

        TextArea textArea = explanation.addTextArea(specifiedException +"Explanation");
        if(preselected){
            // Check for both the refterms."exception" and refterms."exception"Explanation (this is only applicable for both if no config is present )
            String metadata = retrieveMetadata("refterms." + specifiedException + "Explanation");
            if(StringUtils.isBlank(metadata) && reftermsExplanation==null){
                metadata=retrieveMetadata("refterms." + specifiedException);
            }
            if(StringUtils.isNotBlank(metadata)){
                textArea.setValue(metadata);
            }
        }
        if(reftermsExplanation!=null && !reftermsExplanation.isEmpty()){
            Iterator it = reftermsExplanation.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String,ExceptionInformation> pair = (Map.Entry)it.next();
                boolean preselectedhelp = checkPreselection(submission.getItem(), specifiedException,pair.getKey());

                String helpText = pair.getValue().getHelpText();
                if(StringUtils.isNotBlank(helpText)){
                    // Add (Optional) helptext underneath the textarea
                    Para para =specificException.addPara(pair.getKey(),(preselectedhelp)?"helpText":"helpText hidden");
                    para.addContent(helpText);
                }
            }
        }
    }

    private String retrieveMetadata(String specifiedException) {
        String metadata = "";
        Metadatum[] prefill = submission.getItem().getMetadataByMetadataString(specifiedException);
        if(prefill!=null && prefill.length>0 && StringUtils.isNotBlank(prefill[0].value)){
            metadata=prefill[0].value;
        }
        return metadata;
    }

    /**
     * Add a dropdown selection to a division.
     * Preselects values if previously saved metadata apply
     * @param specifiedException The current exception to create for
     * @param specificException
     * @param reftermsExplanation
     * @throws WingException
     */
    private void addDropDownSelection(String specifiedException, Division specificException,HashMap<String,ExceptionInformation> reftermsExplanation ) throws WingException {

        if(reftermsExplanation!=null && !reftermsExplanation.isEmpty()){
            Division dropdownDivision = specificException.addDivision("dropdownDivision");
            dropdownDivision.setHead(message(TITLE_1_BASE + specifiedException));
            dropdownDivision.addPara(message(PARA_1_BASE + specifiedException));
            List exceptionSpecific = dropdownDivision.addList(specifiedException+"-dropdown", List.TYPE_ORDERED);
            Item exceptionSubdivisions = exceptionSpecific.addItem(specifiedException + "dropdown", null);
            Select select =exceptionSubdivisions.addSelect(specifiedException+"-dropdown","subdivision");
            Iterator it = reftermsExplanation.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String,ExceptionInformation> pair = (Map.Entry)it.next();
                boolean preselected = checkPreselection(submission.getItem(), specifiedException,pair.getKey());

                select.addOption(preselected, pair.getKey(), pair.getValue().getExceptionSubField());
            }

        }
    }
}
