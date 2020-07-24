/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.utils;

import org.dspace.content.MetadataField;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 29/11/12
 * Time: 15:39
 */
public class Metadatum {


    private String schema;
    private String element;
    private String qualifier = null;


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(schema).append('.');
        sb.append(element);
        if(qualifier!=null) sb.append(".").append(qualifier);

        return sb.toString();
    }

    public Metadatum(org.dspace.content.Metadatum value){
        this(value.schema,value.element,value.qualifier);
    }

    public Metadatum(String schema, String element, String qualifier) {
        this.schema = schema;
        this.element = element;
        this.qualifier = qualifier;
    }

    public Metadatum(String schema, MetadataField field){
        this(schema,field.getElement(),field.getQualifier());

    }

    public Metadatum(String full) {
        String elements[]=full.split("\\.");
        if(elements.length==2){
            this.schema = elements[0];
            this.element =elements[1];
        } else if(elements.length==3){
            this.schema = elements[0];
            this.element =elements[1];
            this.qualifier = elements[2];
        }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metadatum that = (Metadatum) o;

        if (!element.equals(that.element)) return false;
        if (qualifier != null ? !qualifier.equals(that.qualifier) : that.qualifier != null) return false;
        if (!schema.equals(that.schema)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = schema.hashCode();
        result = 31 * result + element.hashCode();
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        return result;
    }
}
