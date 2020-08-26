package com.atmire.ref.compliance.submission;

import org.springframework.beans.factory.annotation.Required;

/**
 * Created by jonas - jonas@atmire.com on 03/05/16.
 */
public class ExceptionInformation {

    private String exceptionSubField;
    private String helpText;

    public String getExceptionSubField() {
        return exceptionSubField;
    }
    @Required
    public void setExceptionSubField(String exceptionSubField) {
        this.exceptionSubField = exceptionSubField;
    }

    public String getHelpText() {
        return helpText;
    }

    @Required
    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }


}
