/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package com.atmire.swordapp.server;

import java.util.HashMap;
import java.util.Properties;

import com.atmire.swordapp.server.util.SimpleRioxxMetadataHelper;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.ConfigurationManager;
import org.dspace.sword2.SimpleDCMetadata;

public class AbstractSimpleRioxx {
    protected HashMap<String, String> dcMap = null;
    protected HashMap<String, String> atomMap = null;

    protected SimpleRioxxMetadata getMetadata(Item item) {
        SimpleRioxxMetadataHelper simpleRioxxMetadataHelper = new SimpleRioxxMetadataHelper();
        dcMap = simpleRioxxMetadataHelper.getDcMap();

        SimpleRioxxMetadata md = new SimpleRioxxMetadata();
        Metadatum[] all = item.getMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);

        for (Metadatum dcv : all) {
            String valueMatch = dcv.schema + "." + dcv.element;
            if (dcv.qualifier != null) {
                valueMatch += "." + dcv.qualifier;
            }

            // look for the metadata in the dublin core map
            for (String key : this.dcMap.keySet()) {
                String value = this.dcMap.get(key);
                if (valueMatch.equals(value)) {
                    md.addToMetadataMap(key, dcv.value);
                }
            }

            // look for the metadata in the atom map
            for (String key : this.atomMap.keySet()) {
                String value = this.atomMap.get(key);
                if (valueMatch.equals(value)) {
                    md.addAtom(key, dcv.value);
                }
            }
        }

        return md;
    }
}
