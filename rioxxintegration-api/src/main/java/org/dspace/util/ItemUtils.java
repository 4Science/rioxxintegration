/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.storage.rdbms.*;
import org.dspace.util.subclasses.*;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 06 Mar 2014
 */
public class ItemUtils {

    public static void addAllMetadata(Item item, List<Metadatum> values) {
        for (Metadatum value : values) {
            addMetadata(item, value);
        }
    }

    public static void addMetadata(Item item, Metadatum dcValue) {
        item.addMetadata(dcValue.schema, dcValue.element, dcValue.qualifier, dcValue.language, dcValue.value, dcValue.authority, dcValue.confidence);
    }

    public static void addMetadata(Item item, String mdField, String value) {
        Metadata metadata = MetadataFieldString.encapsulate(mdField);
        item.addMetadata(metadata.getSchema(), metadata.getElement(), metadata.getQualifier(), metadata.getLanguage(), value);
    }

    public static void clearAllMetadata(Item item) {
        item.clearMetadata(Item.ANY, Item.ANY, Item.ANY, Item.ANY);
    }

    public static void clearMetadata(Item item, Metadatum dcValue) {
        item.clearMetadata(dcValue.schema, dcValue.element, dcValue.qualifier, dcValue.language);
    }

    public static void clearMetadata(Item item, String mdField) {
        Metadata metadata = MetadataFieldString.encapsulate(mdField).withWildcards();
        item.clearMetadata(metadata.getSchema(), metadata.getElement(), metadata.getQualifier(), metadata.getLanguage());
    }

    public static void clearMetadata(Item item, String schema, String element, String qualifier, String lang, String authority) {
        // We will build a list of values NOT matching the values to clear
        List<Metadatum> values = new ArrayList<Metadatum>();
        for (Metadatum dcv : getMetadata(item)) {
            if (!match(schema, element, qualifier, lang, authority, dcv)) {
                values.add(dcv);
            }
        }

        // Now swap the old list of values for the new, unremoved values
        clearAllMetadata(item);
        addAllMetadata(item, values);
    }

    public static void copyMetadata(Item item, Item item2) {
        List<Metadatum> metadata = getMetadata(item2);
        clearAllMetadata(item);
        addAllMetadata(item, metadata);
    }

    public static ItemIterator findByMetadataFieldAuthority(Context context, String mdString, String authority)
            throws SQLException, AuthorizeException, IOException {
        Metadata encapsulate = MetadataFieldString.encapsulate(mdString);
        return findByMetadataFieldAuthority(context, encapsulate.getSchema(), encapsulate.getElement(), encapsulate.getQualifier(), authority);
    }

    public static ItemIterator findByMetadataFieldAuthority(Context context, String schema, String element, String qualifier, String authority)
            throws SQLException, AuthorizeException, IOException {
        MetadataSchema mds = MetadataSchema.find(context, schema);
        if (mds == null) {
            throw new IllegalArgumentException("No such metadata schema: " + schema);
        }
        MetadataField mdf = MetadataField.findByElement(context, mds.getSchemaID(), element, qualifier);
        if (mdf == null) {
            throw new IllegalArgumentException(
                    "No such metadata field: schema=" + schema + ", element=" + element + ", qualifier=" + qualifier);
        }

        String query = "SELECT item.* FROM metadatavalue,item WHERE item.in_archive='1' " +
                "AND item.item_id = metadatavalue.item_id AND metadata_field_id = ?";
        TableRowIterator rows = null;
        if (Item.ANY.equals(authority)) {
            rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID());
        } else {
            query += " AND metadatavalue.authority = ?";
            rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID(), authority);
        }
        return new ItemIterator(context, rows);
    }

    public static Set<Bitstream> getBitstreams(Item item) throws SQLException {
        Set<Bitstream> bitstreams = new HashSet<Bitstream>();
        for (Bundle bundle : item.getBundles()) {
            for (Bitstream bitstream : bundle.getBitstreams()) {
                bitstreams.add(bitstream);
            }
        }
        return bitstreams;
    }

    public static List<Metadatum> getMetadata(Item item) {
        return getMetadata(item, Item.ANY + "." + Item.ANY + "." + Item.ANY);
    }

    public static List<Metadatum> getMetadata(Item item, String mdString) {
        Metadata elements = MetadataFieldString.encapsulate(mdString).withWildcards();
        Metadatum[] metadata = item.getMetadata(elements.getSchema(), elements.getElement(), elements.getQualifier(), elements.getLanguage());
        if(metadata == null || metadata.length == 0) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(metadata);
        }
    }

    public static List<Metadatum> getMetadata(Item item, String mdString, String authority) {
        Metadata elements = MetadataFieldString.encapsulate(mdString).withWildcards();
        return getMetadata(item, elements.getSchema(), elements.getElement(), elements.getQualifier(), elements.getLanguage(), authority);
    }

    public static List<Metadatum> getMetadata(Item item, String schema, String element, String qualifier, String lang, String authority) {
        Metadatum[] metadata = item.getMetadata(schema, element, qualifier, lang);
        List<Metadatum> dcValues = Arrays.asList(metadata);
        if (!authority.equals(Item.ANY)) {
            for (Metadatum dcValue : metadata) {
                if (!authority.equals(dcValue.authority)) {
                    dcValues.remove(dcValue);
                }
            }
        }
        return dcValues;
    }

    public static String getMetadataFirstValue(Item item, String fieldName) {
        Metadata elements = MetadataFieldString.encapsulate(fieldName); // this is better not with wildcards
        return getMetadataFirstValue(item, elements.getSchema(), elements.getElement(), elements.getQualifier(), elements.getLanguage());
    }

    public static String getMetadataFirstValue(Item item, String schema, String element, String qualifier, String language) {
        Metadatum[] metadata = item.getMetadata(schema, element, qualifier, language);
        String value = null;
        if (metadata.length > 0) {
            value = metadata[0].value;
        }
        return value;
    }

    public static void replaceMetadataValue(Item item, Metadatum oldValue, Metadatum newValue) {
        // check both dcvalues are for the same field
        if (DcValueUtils.equalField(oldValue, newValue)) {

            String schema = oldValue.schema;
            String element = oldValue.element;
            String qualifier = oldValue.qualifier;

            // Save all metadata for this field
            Metadatum[] dcvalues = item.getMetadata(schema, element, qualifier, Item.ANY);
            item.clearMetadata(schema, element, qualifier, Item.ANY);

            for (Metadatum dcvalue : dcvalues) {
                if (DcValueUtils.equal(dcvalue, oldValue)) {
                    item.addMetadata(schema, element, qualifier, newValue.language, newValue.value, newValue.authority, newValue.confidence);
                } else {
                    item.addMetadata(schema, element, qualifier, dcvalue.language, dcvalue.value, dcvalue.authority, dcvalue.confidence);
                }
            }
        }
    }

    private static boolean match(String schema, String element, String qualifier, String language, String authority, Metadatum dcv) {
        boolean match = match(schema, element, qualifier, language, dcv);
        if (match) {
            if (!authority.equals(Item.ANY)) {
                if (authority != null) {
                    match = authority.equals(dcv.authority);
                } else {
                    match = dcv.authority == null;
                }
            }
        }
        return match;
    }

    /**
     * (yes this is a copy of the original class)
     * <p/>
     * Utility method for pattern-matching metadata elements.  This
     * method will return <code>true</code> if the given schema,
     * element, qualifier and language match the schema, element,
     * qualifier and language of the <code>DCValue</code> object passed
     * in.  Any or all of the element, qualifier and language passed
     * in can be the <code>Item.ANY</code> wildcard.
     *
     * @param schema    the schema for the metadata field. <em>Must</em> match
     *                  the <code>name</code> of an existing metadata schema.
     * @param element   the element to match, or <code>Item.ANY</code>
     * @param qualifier the qualifier to match, or <code>Item.ANY</code>
     * @param language  the language to match, or <code>Item.ANY</code>
     * @param dcv       the Dublin Core value
     * @return <code>true</code> if there is a match
     */
    private static boolean match(String schema, String element, String qualifier,
                                 String language, Metadatum dcv) {
        // We will attempt to disprove a match - if we can't we have a match
        if (!element.equals(Item.ANY) && !element.equals(dcv.element)) {
            // Elements do not match, no wildcard
            return false;
        }

        if (qualifier == null) {
            // Value must be unqualified
            if (dcv.qualifier != null) {
                // Value is qualified, so no match
                return false;
            }
        } else if (!qualifier.equals(Item.ANY)) {
            // Not a wildcard, so qualifier must match exactly
            if (!qualifier.equals(dcv.qualifier)) {
                return false;
            }
        }

        if (language == null) {
            // Value must be null language to match
            if (dcv.language != null) {
                // Value is qualified, so no match
                return false;
            }
        } else if (!language.equals(Item.ANY)) {
            // Not a wildcard, so language must match exactly
            if (!language.equals(dcv.language)) {
                return false;
            }
        }

        if (!schema.equals(Item.ANY)) {
            if (dcv.schema != null && !dcv.schema.equals(schema)) {
                // The namespace doesn't match
                return false;
            }
        }

        // If we get this far, we have a match
        return true;
    }

}
