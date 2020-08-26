/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Validation rule that will check if a field has a non-blank value.
 */
public class FieldIsNotBlankRule extends AbstractFieldCheckRule {

    public FieldIsNotBlankRule(final String fieldDescription, final String metadataField) {
        super(fieldDescription, metadataField);
    }

    @Override
    protected boolean checkFieldValues(final List<String> fieldValueList) {
        if (isEmpty(fieldValueList)) {
            addViolationDescription("the %s field has no value", fieldDescription);
            return false;
        } if(StringUtils.isBlank(fieldValueList.get(0))) {
            addViolationDescription("the %s field has a blank value", fieldDescription);
            return false;
        } else {
            return true;
        }
    }

    protected String getRuleDescriptionCompliant() {
        return String.format("the %s field (%s) is filled in", fieldDescription, metadataFieldToCheck);
    }

    protected String getRuleDescriptionViolation() {
        return String.format("the %s field (%s) must be filled in", fieldDescription, metadataFieldToCheck);
    }
}
