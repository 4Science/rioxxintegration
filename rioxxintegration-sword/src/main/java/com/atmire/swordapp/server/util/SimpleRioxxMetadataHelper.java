package com.atmire.swordapp.server.util;

import java.util.HashMap;
import java.util.Properties;

import org.dspace.core.ConfigurationManager;

public class SimpleRioxxMetadataHelper {
    protected HashMap<String, String> dcMap = null;
    protected HashMap<String, String> atomMap = null;

    private void loadMetadataMaps() {
        if (this.dcMap == null) {
            // we should load our DC map from configuration
            this.dcMap = new HashMap<String, String>();
            Properties props = ConfigurationManager.getProperties("swordv2-server");
            for (Object key : props.keySet()) {
                String keyString = (String) key;
                if (keyString.startsWith("simplerioxx.")) {
                    String k = keyString.substring("simplerioxx.".length());
                    String v = (String) props.get(key);
                    this.dcMap.put(k, v);
                }
            }
        }

        if (this.atomMap == null) {
            this.atomMap = new HashMap<String, String>();
            Properties props = ConfigurationManager.getProperties("swordv2-server");
            for (Object key : props.keySet()) {
                String keyString = (String) key;
                if (keyString.startsWith("atom.")) {
                    String k = keyString.substring("atom.".length());
                    String v = (String) props.get(key);
                    this.atomMap.put(k, v);
                }
            }
        }
    }

    public HashMap<String, String> getDcMap() {
        if (this.dcMap == null) {
            loadMetadataMaps();
        }
        return dcMap;
    }

    public HashMap<String, String> getAtomMap() {
        if (this.atomMap == null) {
            loadMetadataMaps();
        }
        return atomMap;
    }
}
