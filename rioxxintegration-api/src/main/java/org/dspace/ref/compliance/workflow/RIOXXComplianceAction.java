/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.workflow;

import java.io.*;
import java.sql.*;
import javax.servlet.http.*;
import org.dspace.authorize.*;
import org.dspace.core.*;
import org.dspace.ref.compliance.result.*;
import org.dspace.ref.compliance.service.*;
import org.dspace.utils.*;
import org.dspace.xmlworkflow.*;
import org.dspace.xmlworkflow.state.*;
import org.dspace.xmlworkflow.state.actions.*;
import org.dspace.xmlworkflow.state.actions.processingaction.*;
import org.dspace.xmlworkflow.storedcomponents.*;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 22/03/16
 * Time: 16:28
 */
public class RIOXXComplianceAction extends ProcessingAction {

    private static ComplianceCheckService complianceCheckService = new DSpace().getServiceManager()
            .getServiceByName("rioxxComplianceCheckService", ComplianceCheckService.class);

    @Override
    public void activate(Context c, XmlWorkflowItem wf) throws SQLException, IOException, AuthorizeException, WorkflowException {

    }

    @Override
    public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request) throws SQLException, AuthorizeException, IOException, WorkflowException {
        if(request.getParameter("submit_continue") != null && allowContinue(c, wfi)){
            return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, ActionResult.OUTCOME_COMPLETE);
        } else if (request.getParameter("submit_return") != null){
            return new ActionResult(ActionResult.TYPE.TYPE_OUTCOME, 1);
        } else {
            return new ActionResult(ActionResult.TYPE.TYPE_SUBMISSION_PAGE);
        }
    }

    private boolean allowContinue(Context c, XmlWorkflowItem wfi){
        boolean blockWorkflow = complianceCheckService.blockOnWorkflow(wfi.getCollection().getHandle());

        if (blockWorkflow) {
            ComplianceResult result = complianceCheckService.checkCompliance(c, wfi.getItem());

            if (!result.isCompliant()) {
                return false;
            }
        }

        return true;
    }
}
