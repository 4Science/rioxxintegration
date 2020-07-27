package org.dspace.app.xmlui.aspect.compliance.workflow;

import java.io.*;
import java.sql.*;
import org.apache.cocoon.environment.*;
import org.dspace.app.xmlui.aspect.compliance.ComplianceUI;
import org.dspace.app.xmlui.aspect.xmlworkflow.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.utils.*;
import org.xml.sax.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 22/03/16
 * Time: 16:21
 */
public class REFComplianceAction extends AbstractXMLUIAction {

    private static final Message T_HEAD = message("xmlui.compliance.workflow.REFComplianceAction.head");

    @Override
    public void addBody(Body body) throws SAXException, WingException, SQLException, IOException, AuthorizeException {
        org.dspace.content.Item item = workflowItem.getItem();
        Collection collection = workflowItem.getCollection();
        Request request = ObjectModelHelper.getRequest(objectModel);

        String actionURL = contextPath + "/handle/"+collection.getHandle() + "/xmlworkflow";

        Division div = body.addInteractiveDivision("perform-task", actionURL, Division.METHOD_POST, "primary workflow");

        addWorkflowItemInformation(div, item, request);

        ComplianceUI complianceUI = new DSpace().getServiceManager().getServiceByName("refComplianceUI", ComplianceUI.class);

        complianceUI.addWorkflowButtons(div, context, item, knot, collection.getHandle());

        Division complianceDiv = div.addDivision("compliance-wrapper").addDivision("compliance-div");
        complianceDiv.setHead(T_HEAD);
        complianceUI.addComplianceSections(complianceDiv, item, context);
    }
}
