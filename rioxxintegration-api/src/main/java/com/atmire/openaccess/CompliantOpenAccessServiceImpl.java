/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.openaccess;

import com.atmire.openaccess.service.*;
import java.sql.*;
import java.util.Date;
import java.util.*;
import org.apache.commons.lang3.*;
import org.apache.log4j.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.core.LogManager;
import org.dspace.eperson.*;

/**
 * @author philip at atmire.com
 */
public class CompliantOpenAccessServiceImpl implements CompliantOpenAccessService {

    private static final Logger log = Logger.getLogger(CompliantOpenAccessServiceImpl.class);

    private final String fullIso = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private String groupname;
    private String bundle;
    private String schema;
    private String element;
    private String qualifier;


    public String updateItem(Context context, Item item) throws SQLException, AuthorizeException {
        if(item!=null && !hasDate(item)) {

            Group group = getGroup(context, groupname);

            if (group == null) {
                log.error("group with name " + groupname + " could not be resolved");
            } else {
                Bundle[] bundles = item.getBundles(bundle);

                int backupBitstreamID = -1;

                for (Bundle bundle : bundles) {
                    int primaryBitstreamID = bundle.getPrimaryBitstreamID();

                    if (primaryBitstreamID > 0) {
                        return updateDateOfCompliantOpenAccess(context, item, primaryBitstreamID, group);
                    } else if(bundle.getBitstreams() != null && bundle.getBitstreams().length > 0) {
                        backupBitstreamID = bundle.getBitstreams()[0].getID();
                    }
                }

                if(backupBitstreamID > 0) {
                    return updateDateOfCompliantOpenAccess(context, item, backupBitstreamID, group);
                }
            }
        }

        return null;
    }

    private String updateDateOfCompliantOpenAccess(Context context, Item item, int bitstreamId, Group group) throws SQLException, AuthorizeException {
        Bitstream bitstream = Bitstream.find(context, bitstreamId);

        List<ResourcePolicy> policiesActionFilter = AuthorizeManager.getPoliciesActionFilter(context, bitstream, Constants.READ);

        if (policiesActionFilter.size() > 0) {

            for (ResourcePolicy resourcePolicy : policiesActionFilter) {
                Group policyGroup = resourcePolicy.getGroup();
                if(policyGroup!=null) {
                    Date startDate = resourcePolicy.getStartDate();

                    if(group.getName().equals(policyGroup.getName())) {
                        if (startDate != null && startDate.before(new Date())) {
                            addDate(item, new DCDate(startDate));
                        } else if (startDate == null) {
                            addDate(item, DCDate.getCurrent());
                        }
                    }
                }
            }
        }

        return null;
    }

    private String addDate(Item item, DCDate startDate) throws SQLException, AuthorizeException {
        item.addMetadata(schema, element, qualifier, null, startDate.toString());
//        item.addMetadata(schema, element, qualifier, null, DateFormatUtils.format(startDate,fullIso));
        item.update();
        return "Compliant Open Access date " + startDate.toString() + " added to item " + item.getHandle();
    }

    private boolean hasDate(Item item){
        Metadatum[] metadata = item.getMetadata(schema, element, qualifier, Item.ANY);

        return metadata.length>0;
    }

    private Group getGroup(Context context, String name) throws SQLException {
        Group group = null;

        if (StringUtils.isNotBlank(name)) {
            group = Group.findByName(context, name);
            if (group == null) {
                log.warn(LogManager.getHeader(context, "group with name " + name + " could not be resolved", ""));
            }
        }

        return group;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }
}
