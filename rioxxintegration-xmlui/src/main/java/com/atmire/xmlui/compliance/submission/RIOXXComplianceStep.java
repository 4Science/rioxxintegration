package com.atmire.xmlui.compliance.submission;

import java.io.*;
import java.sql.*;
import org.apache.cocoon.*;
import org.dspace.app.xmlui.aspect.compliance.*;
import org.dspace.app.xmlui.aspect.submission.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.utils.*;
import org.xml.sax.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 23/03/16
 * Time: 10:57
 */
public class RIOXXComplianceStep extends AbstractSubmissionStep {

    protected static final Message T_error_blocked =
            message("xmlui.compliance.submission.error_blocked");

    protected static final Message T_head =
            message("xmlui.compliance.submission.head");

    private ComplianceUI complianceUI =  new DSpace().getServiceManager().getServiceByName("rioxxComplianceUI", ComplianceUI.class);

    @Override
    public List addReviewSection(List reviewList) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        return null;
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException, ProcessingException {
        org.dspace.content.Item item = submission.getItem();
        Collection collection = submission.getCollection();
        String actionURL = contextPath + "/handle/"+collection.getHandle() + "/submit/" + knot.getId() + ".continue";

        Division div = body.addInteractiveDivision("submit-describe",actionURL,Division.METHOD_POST,"primary submission");
        div.setHead(T_submission_head);
        addSubmissionProgressList(div);

        div.addList("submit-compliance-1",List.TYPE_FORM).setHead(T_head.parameterize(complianceUI.getShortname()));

        complianceUI.addComplianceSections(div, item, context);

        if (this.errorFlag== org.dspace.ref.compliance.submission.REFComplianceStep.STATUS_BLOCKED)
        {
            div.addPara("compliance-error", "compliance-error").addContent(T_error_blocked);
        }

        List form = div.addList("submit-compliance-2",List.TYPE_FORM);

        addControlButtons(form);
    }
}
