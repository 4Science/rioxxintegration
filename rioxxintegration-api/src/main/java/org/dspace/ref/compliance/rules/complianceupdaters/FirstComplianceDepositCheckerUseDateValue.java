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
