/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.CategorySet;
import org.dspace.util.XmlMarshaller;

/**
 * XML marshaller to unmarshall or marshall the validation rule definition file
 */
public class CategorySetMarshaller extends XmlMarshaller<CategorySet> {

    public CategorySetMarshaller() {
        super(CategorySet.class);
    }

}
