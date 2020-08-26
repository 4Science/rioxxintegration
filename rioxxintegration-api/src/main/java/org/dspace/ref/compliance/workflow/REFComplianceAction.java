/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.workflow;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.ref.compliance.result.ComplianceResult;
import org.dspace.ref.compliance.service.ComplianceCheckService;
import org.dspace.utils.DSpace;
import org.dspace.xmlworkflow.state.Step;
import org.dspace.xmlworkflow.state.actions.ActionResult;
import org.dspace.xmlworkflow.state.actions.processingaction.ProcessingAction;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 22/03/16
 * Time: 16:28
 */
public class REFComplianceAction extends ProcessingAction {

    private static ComplianceCheckService complianceCheckService = new DSpace().getServiceManager()
            .getServiceByName("refComplianceCheckService", ComplianceCheckService.class);

    @Override
    public void activate(Context c, XmlWorkflowItem wf) throws SQLException, IOException, AuthorizeException {

    }

    @Override
    public ActionResult execute(Context c, XmlWorkflowItem wfi, Step step, HttpServletRequest request) throws SQLException, AuthorizeException, IOException {
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
