/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package com.atmire.swordapp.server;

import java.util.HashMap;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;

import com.atmire.swordapp.server.util.SimpleRioxxMetadataHelper;

public class AbstractSimpleRioxx {
    protected HashMap<String, String> dcMap = null;
    protected HashMap<String, String> atomMap = null;
    
    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    
    protected SimpleRioxxMetadata getMetadata(Item item) {
        SimpleRioxxMetadataHelper simpleRioxxMetadataHelper = new SimpleRioxxMetadataHelper();
        dcMap = simpleRioxxMetadataHelper.getDcMap();

        SimpleRioxxMetadata md = new SimpleRioxxMetadata();
        List<MetadataValue> all = itemService.getMetadata(item, Item.ANY, Item.ANY, Item.ANY, Item.ANY);

        for (MetadataValue dcv : all) {
            String valueMatch = dcv.getMetadataField().getMetadataSchema().getName() + "." + dcv.getMetadataField().getElement();
            if (dcv.getMetadataField().getQualifier()!= null) {
                valueMatch += "." + dcv.getMetadataField().getQualifier();
            }

            // look for the metadata in the dublin core map
            for (String key : this.dcMap.keySet()) {
                String value = this.dcMap.get(key);
                if (valueMatch.equals(value)) {
                    md.addToMetadataMap(key, dcv.getValue());
                }
            }

            // look for the metadata in the atom map
            for (String key : this.atomMap.keySet()) {
                String value = this.atomMap.get(key);
                if (valueMatch.equals(value)) {
                    md.addAtom(key, dcv.getValue());
                }
            }
        }

        return md;
    }

	public ItemService getItemService() {
		return itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
}
