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
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.*;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.*;
import org.dspace.core.LogManager;
import org.dspace.eperson.*;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private AuthorizeService authorizeService;
    
    @Autowired
    private GroupService groupService;
    
    public String updateItem(Context context, Item item) throws SQLException, AuthorizeException {
        if(item!=null && !hasDate(item)) {

            Group group = getGroup(context, groupname);

            if (group == null) {
                log.error("group with name " + groupname + " could not be resolved");
            } else {
                List<Bundle> bundles = itemService.getBundles(item, bundle);

                Bitstream backupBitstream = null;

                for (Bundle bundle : bundles) {
                	
                	Bitstream bitstream = bundle.getPrimaryBitstream();
                    if (bitstream!=null) {
                        return updateDateOfCompliantOpenAccess(context, item, bitstream, group);
                    } else if(bundle.getBitstreams() != null && !bundle.getBitstreams().isEmpty()) {
                    	backupBitstream = bundle.getBitstreams().get(0);
                    }
                }

                if(backupBitstream!=null) {
                    return updateDateOfCompliantOpenAccess(context, item, backupBitstream, group);
                }
            }
        }

        return null;
    }

    private String updateDateOfCompliantOpenAccess(Context context, Item item, Bitstream bitstream, Group group) throws SQLException, AuthorizeException {
        
        List<ResourcePolicy> policiesActionFilter = authorizeService.getPoliciesActionFilter(context, bitstream, Constants.READ);

        if (policiesActionFilter.size() > 0) {

            for (ResourcePolicy resourcePolicy : policiesActionFilter) {
                Group policyGroup = resourcePolicy.getGroup();
                if(policyGroup!=null) {
                    Date startDate = resourcePolicy.getStartDate();

                    if(group.getName().equals(policyGroup.getName())) {
                        if (startDate != null && startDate.before(new Date())) {
                            addDate(context, item, new DCDate(startDate));
                        } else if (startDate == null) {
                            addDate(context, item, DCDate.getCurrent());
                        }
                    }
                }
            }
        }

        return null;
    }

    private String addDate(Context context, Item item, DCDate startDate) throws SQLException, AuthorizeException {
    	itemService.addMetadata(context, item, schema, element, qualifier, null, startDate.toString());
//        item.addMetadata(schema, element, qualifier, null, DateFormatUtils.format(startDate,fullIso));
        itemService.update(context, item);
        return "Compliant Open Access date " + startDate.toString() + " added to item " + item.getHandle();
    }

    private boolean hasDate(Item item){
        List<MetadataValue> metadata = itemService.getMetadata(item, schema, element, qualifier, Item.ANY);

        return !metadata.isEmpty();
    }

    private Group getGroup(Context context, String name) throws SQLException {
        Group group = null;

        if (StringUtils.isNotBlank(name)) {
            group = groupService.findByName(context, name);
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

	public ItemService getItemService() {
		return itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public AuthorizeService getAuthorizeService() {
		return authorizeService;
	}

	public void setAuthorizeService(AuthorizeService authorizeService) {
		this.authorizeService = authorizeService;
	}

	public GroupService getGroupService() {
		return groupService;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}
}
