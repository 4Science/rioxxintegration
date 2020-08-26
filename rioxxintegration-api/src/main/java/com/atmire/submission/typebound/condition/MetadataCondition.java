/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.submission.typebound.condition;

import java.util.List;

import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by jonas - jonas@atmire.com on 11/04/16.
 */
public class MetadataCondition implements SubmissionStepCondition {

    private String metadatum;
    private List<String> allowedValues;
    
    @Autowired
    private ItemService itemService;
    
    @Required
    public void setMetadatum(String metadata) {
        this.metadatum = metadata;
    }

    @Required
    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    @Override
    public boolean conditionMet(Item item) {
        List<MetadataValue> dcValues = itemService.getMetadataByMetadataString(item, metadatum);
        for (MetadataValue dcValue : dcValues) {
            if(allowedValues.contains(dcValue.getValue())){
                return true;
            }
        }
        return false;
    }

	public ItemService getItemService() {
		return itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
}
