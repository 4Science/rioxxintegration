/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.indexer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Authority indexer that will (re)index all authorities used in items that are still
 * in the workspace.
 */
public class WorkspaceAuthorityIndexer implements AuthorityIndexerInterface, InitializingBean {
	
    private static final Logger log = Logger.getLogger(WorkspaceAuthorityIndexer.class);

    @Autowired(required = true)
    protected AuthorityValueService authorityValueService;
    
    @Autowired(required = true)
    protected ItemService itemService;
    
    private Set<String> metadataFields;

    @Autowired(required = true)
    protected ConfigurationService configurationService;

    @Autowired(required = true)
    protected WorkspaceItemService workspaceItemService; 
    
    @Override
    public boolean isConfiguredProperly() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int counter = 1;
        String field;
        metadataFields = new HashSet<String>();
        while ((field = configurationService.getProperty("authority.author.indexer.field." + counter)) != null) {
            metadataFields.add(field);
            counter++;
        }
    }

    private List<WorkspaceItem> loadWorkspaceItems(Context context) throws SQLException {
       return workspaceItemService.findAll(context);
    }
    
    @Override
    public List<AuthorityValue> getAuthorityValues(Context context, Item item)
            throws SQLException, AuthorizeException
    {
        return getAuthorityValues(context, item, null);
    }

    @Override
    public List<AuthorityValue> getAuthorityValues(Context context, Item item, Map<String, AuthorityValue> cache)
            throws SQLException, AuthorizeException
    {
        List<AuthorityValue> values = new ArrayList<>();

        for (String metadataField : metadataFields) {
            List<MetadataValue> metadataValues = itemService.getMetadataByMetadataString(item, metadataField);
            for (MetadataValue metadataValue : metadataValues) {
                String content = metadataValue.getValue();
                String authorityKey = metadataValue.getAuthority();

                // We only want to update our item IF our UUID is not present
                // or if we need to generate one.
                boolean requiresItemUpdate = StringUtils.isBlank(authorityKey) ||
                        StringUtils.startsWith(authorityKey, AuthorityValueService.GENERATE);

                AuthorityValue value = null;
                if (StringUtils.isBlank(authorityKey) && cache != null) {
                    // This is a value currently without an authority. So query
                    // the cache, if an authority is found for the exact value.
                    value = cache.get(content);
                }

                if (value == null) {
                    value = getAuthorityValue(context, metadataField, content,authorityKey);
                }

                if (value != null) {
                    if (requiresItemUpdate) {
                        value.updateItem(context, item, metadataValue);

                        try {
                            itemService.update(context, item);
                        }
                        catch (Exception e) {
                            log.error("Error creating a metadatavalue's authority", e);
                        }
                    }

                    if (cache != null) {
                        cache.put(content, value);
                    }

                    values.add(value);
                }
                else {
                    log.error("Error getting an authority value for " +
                            "the metadata value \"" + content + "\" " +
                            "in the field \"" + metadataField + "\" " +
                            "of the item " + item.getHandle());
                }
            }
        }

        return values;
    }
    
    private AuthorityValue getAuthorityValue(Context context, String metadataField,
            String metadataContent, String metadataAuthorityKey)
    {
        if (StringUtils.isNotBlank(metadataAuthorityKey) &&
                !metadataAuthorityKey.startsWith(AuthorityValueService.GENERATE)) {
            // !uid.startsWith(AuthorityValueGenerator.GENERATE) is not strictly
            // necessary here but it prevents exceptions in solr

            AuthorityValue value = authorityValueService.findByUID(context, metadataAuthorityKey);
            if (value != null) {
                return value;
            }
        }

        return authorityValueService.generate(context, metadataAuthorityKey,
                metadataContent, metadataField.replaceAll("\\.", "_"));
    }
    
	@Override
	public List<AuthorityValue> getAuthorityValues(Context context) throws SQLException, AuthorizeException {
		List<AuthorityValue> result = new ArrayList<AuthorityValue>();
		List<WorkspaceItem> list = loadWorkspaceItems(context);
		for(WorkspaceItem ll : list) {
			Item item = ll.getItem();
			result.addAll(getAuthorityValues(context, item));
		}
		return result;
	}
	
	@Override
	public boolean isOnlyForIndexerUse() {
		return true;
	}
}
