/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.swordapp.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.MetadataAuthorityService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.swordapp.server.SwordEntry;

import com.atmire.pure.consumer.RIOXXConsumer;
import com.atmire.swordapp.server.util.SimpleRioxxMetadataHelper;

public class RioxxSwordEntry extends SwordEntry {

    protected HashMap<String, String> dcMap = null;
    List<String> acceptedNamespaces = new LinkedList<>();
    
    private MetadataAuthorityService metadataAuthorityService = ContentAuthorityServiceFactory.getInstance().getMetadataAuthorityService();
    private ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
    		
    public RioxxSwordEntry(Entry entry) {
        super(entry);
        SimpleRioxxMetadataHelper simpleRioxxMetadataHelper = new SimpleRioxxMetadataHelper();
        dcMap = simpleRioxxMetadataHelper.getDcMap();
        String[] namespaces = configurationService.getArrayProperty("swordv2-server.swordv2.accepted.namespaces");
        for (String namespace : namespaces) {
            acceptedNamespaces.add(namespace.trim());
        }
    }

    // put the code in for getting a dublin core record out
    public Map<String, List<String>> getData() {
        Map<String, List<String>> data = new HashMap<String, List<String>>();
        List<Element> extensions = this.entry.getExtensions();
        for (Element element : extensions) {
            if (acceptedNamespaces.contains(element.getQName().getNamespaceURI())) {
                // we have a dublin core extension
                String field = element.getQName().getPrefix() + ":" + element.getQName().getLocalPart();
                String value = element.getText();

                if(StringUtils.isNotBlank(value)) {
                    value = constructValueCombination(element, field.replace(":", "."), value);

                    if (data.containsKey(field)) {
                        data.get(field).add(value);
                    } else {
                        ArrayList<String> values = new ArrayList<String>();
                        values.add(value);
                        data.put(field, values);
                    }
                }
            }
        }
        return data;
    }

    private String constructValueCombination(Element element, String field, String value) {
        String[] attributeNames = configurationService.getArrayProperty("swordv2-server.attributes."+ field);
        if (attributeNames!=null) {
            for (String attributeName : attributeNames) {

                value = element.getAttributeValue(attributeName.trim()) + "::" + value;
            }
        }

        String id = element.getAttributeValue("id");
        if (StringUtils.isNotBlank(id)) {
            if (metadataAuthorityService.isAuthorityControlled(dcMap.get(field).replace(".", "_"))) {
                value = id + "::" + value;

                String email = element.getAttributeValue("email");

                if(StringUtils.isNotBlank(email)){
                    value += "::" + email;
                }
            }
        }
        //only to check extra attribute to find corresponding author on pubr.author
        if(StringUtils.equals(field, "pubr.author")) {
        	String correspondingAuthor = element.getAttributeValue("corresp");
        	if(StringUtils.isNotBlank(correspondingAuthor)) {
        		Boolean correspondingAuthorBoolean = BooleanUtils.toBoolean(correspondingAuthor);
        		if(correspondingAuthorBoolean) {
        			value += RIOXXConsumer.CORRESPONDINGAUTHOR;
        		}
        	}
        }
        return value;
    }
}
