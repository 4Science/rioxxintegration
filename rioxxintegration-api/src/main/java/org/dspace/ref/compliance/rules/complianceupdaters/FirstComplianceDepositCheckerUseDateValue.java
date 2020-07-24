/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.complianceupdaters;

import org.dspace.content.DCDate;
import org.dspace.content.Item;

import java.util.Date;

/**
 * Created by jonas - jonas@atmire.com on 24/03/16.
 * This class uses the current date to fill in the metadata
 */

public class FirstComplianceDepositCheckerUseDateValue extends FirstComplianceDepositCheckOnMetadata implements ComplianceDepositCheck {

    @Override
    protected String complianceMetadataValue(Item item) {
        return new DCDate(new Date()).toString();
    }
}
