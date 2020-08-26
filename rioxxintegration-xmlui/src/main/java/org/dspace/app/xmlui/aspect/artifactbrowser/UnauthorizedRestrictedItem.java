/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.artifactbrowser;

import org.apache.cocoon.environment.http.HttpEnvironment;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jonas - jonas@atmire.com on 14/04/16.
 */
public class UnauthorizedRestrictedItem extends RestrictedItem {

    protected void setResponse() {
        //Finally, set proper response. Return "403 Not Found" for all restricted/withdrawn items
        HttpServletResponse response = (HttpServletResponse)objectModel
                .get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
