package org.dspace.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dspace.content.authority.Choices;
import org.dspace.util.subclasses.Metadata;

/**
 * metadataFieldString = schema.element.qualifier[language]::authority::confidence
 * qualifier is optional
 * [language] is optional
 * authority is optional
 * confidence: not if no authority, otherwise optional
 * <p/>
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 19 Sep 2014
 */
public class MetadataFieldString {

    private static enum Parts {
        SCHEMA, ELEMENT, QUALIFIER, LANGUAGE, AUTHORITY, CONFIDENCE
    }

    private static final String SIGNATURE_SEPARATOR = ".";
    private static final String AUTHORITY_SEPARATOR = "::";

    public static String getSchema(String metadataFieldString) {
        return parse(metadataFieldString).get(Parts.SCHEMA.ordinal());
    }

    public static String getElement(String metadataFieldString) {
        return parse(metadataFieldString).get(Parts.ELEMENT.ordinal());
    }

    public static String getQualifier(String metadataFieldString) {
        return parse(metadataFieldString).get(Parts.QUALIFIER.ordinal());
    }

    public static String getLanguage(String metadataFieldString) {
        return parse(metadataFieldString).get(Parts.LANGUAGE.ordinal());
    }

    public static String getAuthority(String metadataFieldString) {
        return parse(metadataFieldString).get(Parts.AUTHORITY.ordinal());
    }

    public static int getConfidence(String metadataFieldString) {
        int confidence;
        try {
            confidence = Integer.parseInt(parse(metadataFieldString).get(Parts.CONFIDENCE.ordinal()));
        } catch (NumberFormatException n) {
            confidence = Choices.CF_UNSET;
        }
        return confidence;
    }

    private static List<String> parse(String metadataFieldString) {
        String schema, element, qualifier = null, language = null, authority = null, confidence = null;

        String[] tokens;
        if (StringUtils.isBlank(metadataFieldString)) {
            tokens = new String[]{};
        } else {
            tokens = StringUtils.split(metadataFieldString, SIGNATURE_SEPARATOR);
        }

        if (tokens.length < 2) {
            throw new IllegalArgumentException("invalid metadataFieldString: too few tokens");
        } else if (tokens.length >= 4) {
            throw new IllegalArgumentException("invalid metadataFieldString: too many tokens");
        } else {
            schema = tokens[0];
            element = tokens[1];

            if (tokens.length == 3) {
                String rest = tokens[2];

                String[] authoritySplit = StringUtils.split(rest, AUTHORITY_SEPARATOR);
                if (authoritySplit.length > 1) {
                    authority = authoritySplit[1];
                }
                if (authoritySplit.length > 2) {
                    confidence = authoritySplit[2];
                }

                rest = authoritySplit[0];
                if (rest.contains("[")) {
                    int open = rest.indexOf('[');
                    int close = rest.indexOf(']');
                    qualifier = rest.substring(0, open);
                    language = rest.substring(open + 1, close);
                } else {
                    qualifier = rest;
                }
            }
        }

        ArrayList<String> parsed = new ArrayList<String>();
        Collections.addAll(parsed, new String[Parts.values().length]);
        parsed.set(Parts.SCHEMA.ordinal(), schema);
        parsed.set(Parts.ELEMENT.ordinal(), element);
        parsed.set(Parts.QUALIFIER.ordinal(), qualifier);
        parsed.set(Parts.LANGUAGE.ordinal(), language);
        parsed.set(Parts.AUTHORITY.ordinal(), authority);
        parsed.set(Parts.CONFIDENCE.ordinal(), confidence);

        return parsed;
    }


    public static Metadata encapsulate(String metadataFieldString) {
        String schema = getSchema(metadataFieldString);
        String element = getElement(metadataFieldString);
        String qualifier = getQualifier(metadataFieldString);
        String language = getLanguage(metadataFieldString);
        String authority = getAuthority(metadataFieldString);
        int confidence = getConfidence(metadataFieldString);
        return new Metadata(schema, element, qualifier, language, null, authority, confidence);
    }
    
    public static Metadata encapsulate(String metadataFieldString, String value) {
        String schema = getSchema(metadataFieldString);
        String element = getElement(metadataFieldString);
        String qualifier = getQualifier(metadataFieldString);
        String language = getLanguage(metadataFieldString);
        String authority = getAuthority(metadataFieldString);
        int confidence = getConfidence(metadataFieldString);
        return new Metadata(schema, element, qualifier, language, value, authority, confidence);
    }
}