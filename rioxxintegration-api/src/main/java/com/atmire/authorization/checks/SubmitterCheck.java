/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.authorization.checks;

import org.apache.log4j.Logger;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Context;

/**
 * Created by jonas - jonas@atmire.com on 13/04/16.
 */
public class SubmitterCheck implements AuthorizationCheck {
	/* Log4j logger */
	private static final Logger log = Logger.getLogger(SubmitterCheck.class);

	@Override
	public boolean checkAuthorization(Context context, DSpaceObject dso) {
		if (dso instanceof Item) {
			if (context.getCurrentUser() == ((Item) dso).getSubmitter()) {
				return true;
			}
		}
		return false;
	}
}
