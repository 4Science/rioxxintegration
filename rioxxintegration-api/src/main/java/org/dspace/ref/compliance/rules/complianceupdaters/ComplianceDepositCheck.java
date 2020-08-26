package org.dspace.ref.compliance.rules.complianceupdaters;

import java.sql.SQLException;

import org.dspace.content.Item;
import org.dspace.core.Context;

/**
 * Created by jonas - jonas@atmire.com on 23/03/16.
 */
public interface ComplianceDepositCheck {

    void checkAndUpdateCompliance(Context context, Item item) throws SQLException;

}
