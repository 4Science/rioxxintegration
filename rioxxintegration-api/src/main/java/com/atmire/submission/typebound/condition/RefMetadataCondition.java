/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.submission.typebound.condition;

import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;

/**
 * Implementation to disabled/enabled REF Conditions
 */
public class RefMetadataCondition extends MetadataCondition {

    @Override
    public boolean conditionMet(Item item) {
    	boolean refEnabled = ConfigurationManager.getBooleanProperty("rioxx", "ref.enabled", true);
    	if(refEnabled) {
    		return super.conditionMet(item);
        }
        return false;
    }
}
