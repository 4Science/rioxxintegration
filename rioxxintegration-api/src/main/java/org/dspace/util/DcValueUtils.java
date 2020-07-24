/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import org.apache.commons.lang3.*;
import org.dspace.content.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 06 Mar 2014
 */
public class DcValueUtils {

    public static Metadatum copy(Metadatum source) {
        Metadatum copy = new Metadatum();
        copy.value = source.value;
        copy.authority = source.authority;
        copy.confidence = source.confidence;
        copy.element = source.element;
        copy.language = source.language;
        copy.qualifier = source.qualifier;
        copy.schema = source.schema;
        return copy;
    }

    public static boolean equalField(Metadatum dc1, Metadatum dc2) {
        if (dc1 == dc2) {
            return true;
        }
        if (dc1.element != null ? !dc1.element.equals(dc2.element) : dc2.element != null) {
            return false;
        }
        if (dc1.qualifier != null ? !dc1.qualifier.equals(dc2.qualifier) : dc2.qualifier != null) {
            return false;
        }
        if (dc1.schema != null ? !dc1.schema.equals(dc2.schema) : dc2.schema != null) {
            return false;
        }
        return true;
    }

    public static boolean equal(Metadatum dc1, Metadatum dc2) {
        if (dc1 == dc2) {
            return true;
        }

        if (dc1.confidence != dc2.confidence) {
            return false;
        }
        if (dc1.authority != null ? !dc1.authority.equals(dc2.authority) : dc2.authority != null) {
            return false;
        }
        if (dc1.element != null ? !dc1.element.equals(dc2.element) : dc2.element != null) {
            return false;
        }
        if (dc1.language != null ? !dc1.language.equals(dc2.language) : dc2.language != null) {
            return false;
        }
        if (dc1.qualifier != null ? !dc1.qualifier.equals(dc2.qualifier) : dc2.qualifier != null) {
            return false;
        }
        if (dc1.schema != null ? !dc1.schema.equals(dc2.schema) : dc2.schema != null) {
            return false;
        }
        if (dc1.value != null ? !dc1.value.equals(dc2.value) : dc2.value != null) {
            return false;
        }

        return true;
    }

    public static boolean isBlank(Metadatum dcValue) {
        return dcValue == null || StringUtils.isBlank(dcValue.value);
    }

    public static boolean hasValue(Metadatum dcValue, String value) {
        return dcValue != null && StringUtils.equals(dcValue.value, value);
    }

}
