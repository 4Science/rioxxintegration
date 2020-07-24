/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.complianceupdaters;

import org.dspace.content.Item;

/**
 * Created by jonas - jonas@atmire.com on 24/03/16.
 * Uses the value that is checked against to put into a new metadata field.
 */
public class FirstComplianceDepositCheckerUseCopiedValue extends FirstComplianceDepositCheckOnMetadata implements ComplianceDepositCheck {

    @Override
    protected String complianceMetadataValue(Item item) {
        return item.getMetadataByMetadataString(getCompliantField())[0].value;
    }
}
