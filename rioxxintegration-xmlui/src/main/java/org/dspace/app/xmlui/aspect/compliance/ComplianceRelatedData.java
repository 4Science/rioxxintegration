package org.dspace.app.xmlui.aspect.compliance;

import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.core.*;
import org.dspace.ref.compliance.result.*;

/**
 * @author philip at atmire.com
 */
public interface ComplianceRelatedData {

    public void renderRelatedData(Context context, org.dspace.content.Item item, ComplianceResult result, Division div) throws WingException;
}
