package com.atmire.swordapp.server;

import com.atmire.swordapp.server.util.*;
import java.util.*;
import org.apache.abdera.model.*;
import org.apache.commons.lang.*;
import org.dspace.content.authority.*;
import org.dspace.core.*;
import org.swordapp.server.*;

public class RioxxSwordEntry extends SwordEntry {

    protected HashMap<String, String> dcMap = null;
    List<String> acceptedNamespaces = new LinkedList<>();

    public RioxxSwordEntry(Entry entry) {
        super(entry);
        SimpleRioxxMetadataHelper simpleRioxxMetadataHelper = new SimpleRioxxMetadataHelper();
        dcMap = simpleRioxxMetadataHelper.getDcMap();
        String namespaces = ConfigurationManager.getProperty("swordv2-server","swordv2.accepted.namespaces");
        for (String namespace : namespaces.split(",")) {
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
        String attributeNames = ConfigurationManager.getProperty("swordv2-server", "attributes."+ field);
        if (StringUtils.isNotBlank(attributeNames)) {
            for (String attributeName : attributeNames.split(",")) {

                value = element.getAttributeValue(attributeName.trim()) + "::" + value;
            }
        }

        String id = element.getAttributeValue("id");
        if (StringUtils.isNotBlank(id)) {
            if (MetadataAuthorityManager.getManager().isAuthorityControlled(dcMap.get(field).replace(".", "_"))) {
                value = id + "::" + value;

                String email = element.getAttributeValue("email");

                if(StringUtils.isNotBlank(email)){
                    value += "::" + email;
                }
            }
        }
        return value;
    }
}
