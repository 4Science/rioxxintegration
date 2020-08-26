/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.compliance;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.cocoon.ProcessingException;
import org.dspace.app.xmlui.cocoon.AbstractDSpaceTransformer;
import org.dspace.app.xmlui.utils.HandleUtil;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Body;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.PageMeta;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

/**
 * Created by Philip Vissenaekens (philip at atmire dot com)
 * Date: 21/03/16
 * Time: 17:37
 */
public class RIOXXItemComplianceMain extends AbstractDSpaceTransformer {

    private static final Message T_dspace_home =
            message("xmlui.general.dspace_home");

    protected static final Message T_head =
            message("xmlui.administrative.item.ItemComplianceMain.head");

    protected static final Message T_trail =
            message("xmlui.administrative.item.ItemComplianceMain.trail");

    protected static final Message T_no_item =
            message("xmlui.administrative.item.ItemComplianceMain.no_item");

    protected static final Message T_return =
            message("xmlui.administrative.item.ItemComplianceMain.return");

    protected static final Message T_item =
            message("xmlui.administrative.item.ItemComplianceMain.item");

    private ComplianceUI complianceUI = new DSpace().getServiceManager().getServiceByName("rioxxComplianceUI", ComplianceUI.class);

    @Override
    public void addPageMeta(PageMeta pageMeta) throws SAXException, WingException, SQLException, IOException, AuthorizeException {
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        if(dso!=null && dso.getType() == org.dspace.core.Constants.ITEM) {
            HandleUtil.buildHandleTrail(context, dso, pageMeta, contextPath);
            pageMeta.addTrailLink(contextPath + "/handle/" + dso.getHandle(), T_item);
        }

        pageMeta.addTrail().addContent(T_trail);

        pageMeta.addMetadata("title").addContent(T_head.parameterize(complianceUI.getShortname()));
    }

    @Override
    public void addBody(Body body) throws SAXException, WingException, SQLException, IOException, AuthorizeException, ProcessingException {
        Division div = body.addDivision("item-compliance");
        div.setHead(T_head.parameterize(complianceUI.getShortname()));

        DSpaceObject dso = HandleUtil.obtainHandle(objectModel);

        if(dso==null || dso.getType() != org.dspace.core.Constants.ITEM) {
            div.addPara("compliance-error","compliance-error").addContent(T_no_item);
            return;
        }

        Item item = (Item) dso;

        complianceUI.addComplianceSections(div, item, context);

        div.addPara().addXref(contextPath + "/handle/" + item.getHandle(), T_return, "compliance-return-link");
    }


}
