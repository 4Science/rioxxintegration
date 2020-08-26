/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.compliance.authorization;

import com.atmire.authorization.AuthorizationChecker;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.selection.Selector;
import org.apache.log4j.Logger;
import org.dspace.app.xmlui.aspect.general.AuthenticatedSelector;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.ref.compliance.service.ComplianceCheckService;
import org.dspace.utils.DSpace;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by jonas - jonas@atmire.com on 13/04/16.
 */
public class ComplianceAuthorizedSelector extends AuthenticatedSelector implements Selector {

    /* Log4j logger*/
    private static final Logger log =  Logger.getLogger(ComplianceAuthorizedSelector.class);

    private AuthorizationChecker complianceAuthorizationChecker;

    public boolean select(String expression, Map objectModel,
                          Parameters parameters){
       boolean authenticatedSelect = super.select(expression,objectModel,parameters);
        if(!authenticatedSelect){
            return authenticatedSelect;
        }
        Context context;
        boolean authorized=false;
        try {
            context = ContextUtil.obtainContext(objectModel);

            DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

            authorized= authorizedForCompliance(context, dso);

        } catch (SQLException e) {
            log.error(e);
        }

        return authenticatedSelect && authorized ;
    }

    public boolean authorizedForCompliance(Context context, DSpaceObject dso){
        return getComplianceAuthorizationChecker().checkAuthorization(context, dso);
    }

	public AuthorizationChecker getComplianceAuthorizationChecker() {
		if(complianceAuthorizationChecker == null) {
			complianceAuthorizationChecker = new DSpace().getServiceManager().getServiceByName("ComplianceAuthorizationChecker", AuthorizationChecker.class);
		}
		return complianceAuthorizationChecker;
	}

	public void setComplianceAuthorizationChecker(AuthorizationChecker complianceCheckService) {
		this.complianceAuthorizationChecker = complianceCheckService;
	}
}
