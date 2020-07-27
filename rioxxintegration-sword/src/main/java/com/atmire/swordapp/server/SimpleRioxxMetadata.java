/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package com.atmire.swordapp.server;

import java.util.HashMap;
import java.util.Map;

public class SimpleRioxxMetadata {
    private Map<String, String> metadataMap = new HashMap<String, String>();
    private Map<String, String> atom = new HashMap<String, String>();

    public void addToMetadataMap(String element, String value) {
        this.metadataMap.put(element, value);
    }

    public void addAtom(String element, String value) {
        this.atom.put(element, value);
    }

    public Map<String, String> getMetadataMap() {
        return metadataMap;
    }

    public Map<String, String> getAtom() {
        return atom;
    }
}
