/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util.subclasses;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Item;

import com.atmire.utils.Metadatum;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 19 Sep 2014
 */
public class Metadata extends Metadatum {

	public String language;
	public String authority;
	public String value;
	public int confidence;
	
    public Metadata(String schema, String element, String qualifier, String language, String value, String authority, int confidence) {
    	super(schema, element, qualifier);
        this.language = language;
        this.value = value;
        this.authority = authority;
        this.confidence = confidence;
    }

    public String getSchema() {
        return schema;
    }


    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Metadata metadata = (Metadata) o;

        if (confidence != metadata.confidence) {
            return false;
        }
        if (authority != null ? !authority.equals(metadata.authority) : metadata.authority != null) {
            return false;
        }
        if (element != null ? !element.equals(metadata.element) : metadata.element != null) {
            return false;
        }
        if (language != null ? !language.equals(metadata.language) : metadata.language != null) {
            return false;
        }
        if (qualifier != null ? !qualifier.equals(metadata.qualifier) : metadata.qualifier != null) {
            return false;
        }
        if (schema != null ? !schema.equals(metadata.schema) : metadata.schema != null) {
            return false;
        }
        if (value != null ? !value.equals(metadata.value) : metadata.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = schema != null ? schema.hashCode() : 0;
        result = 31 * result + (element != null ? element.hashCode() : 0);
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        result = 31 * result + (language != null ? language.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (authority != null ? authority.hashCode() : 0);
        result = 31 * result + confidence;
        return result;
    }

    public Metadata withWildcards() {
        String wilcard = Item.ANY;
        String schema1 = StringUtils.isBlank(schema) ? wilcard : schema;
        String element1 = StringUtils.isBlank(element) ? wilcard : element;
        String qualifier1 = StringUtils.isBlank(qualifier) ? wilcard : qualifier;
        String language1 = StringUtils.isBlank(language) ? wilcard : language;
        return new Metadata(schema1, element1, qualifier1, language1, value, authority, confidence);
    }
}