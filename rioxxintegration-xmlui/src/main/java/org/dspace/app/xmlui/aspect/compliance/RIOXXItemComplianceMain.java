package org.dspace.app.xmlui.aspect.compliance;

import java.io.*;
import java.sql.*;
import org.apache.cocoon.*;
import org.dspace.app.xmlui.cocoon.*;
import org.dspace.app.xmlui.utils.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.Item;
import org.dspace.utils.*;
import org.xml.sax.*;

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
            HandleUtil.buildHandleTrail(dso, pageMeta, contextPath);
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
