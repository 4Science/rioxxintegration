/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.complianceupdaters;

import org.dspace.content.Item;
import org.dspace.core.Context;

/**
 * Created by jonas - jonas@atmire.com on 23/03/16.
 */
public interface ComplianceDepositCheck {

    void checkAndUpdateCompliance(Context context, Item item);

}
