/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules;

import java.util.Collection;
import java.util.*;
import org.apache.commons.collections.*;
import static org.apache.commons.collections.CollectionUtils.*;
import org.apache.commons.lang.*;
import org.dspace.content.*;
import org.dspace.ref.compliance.definition.model.*;

/**
 * Rule that will check if an item field has a specified value
 */
public class FieldHasValueRule extends AbstractFieldCheckRule {

    private Map<String, Value> possibleValues = new LinkedHashMap<String, Value>();

    private Value checkedCompliantValue = null;

    public FieldHasValueRule(final String fieldDescription, final String metadataField, final Collection<Value> possibleValues) {
        super(fieldDescription, metadataField);

        if(CollectionUtils.isNotEmpty(possibleValues)) {
            for (Value possibleValue : possibleValues) {
                this.possibleValues.put(StringUtils.trimToEmpty(possibleValue.getValue()), possibleValue);
            }
        }
    }

    protected boolean checkFieldValues(final List<Metadatum> fieldValueList) {

        if (isEmpty(fieldValueList)) {
            addViolationDescription("The %s field has no value", fieldDescription);
            return false;
        } else if(possibleValues.containsKey(fieldValueList.get(0).value)) {
            checkedCompliantValue = possibleValues.get(fieldValueList.get(0).value);
            return true;
        } else {
            addViolationDescription("The %s field has value %s", fieldDescription, fieldValueList.get(0).value);
            return false;
        }
    }

    protected String getRuleDescriptionCompliant() {
        return String.format("the %s field (%s) has value %s", fieldDescription, metadataFieldToCheck,
                checkedCompliantValue == null ? buildValueString() : getValueDescription(checkedCompliantValue));
    }

    protected String getRuleDescriptionViolation() {
        return String.format("the %s field (%s) must have value %s", fieldDescription, metadataFieldToCheck,
                checkedCompliantValue == null ? buildValueString() : getValueDescription(checkedCompliantValue));
    }

    private String buildValueString() {
        if(possibleValues.size() == 1) {
            return getValueDescription(possibleValues.values().iterator().next());

        } else if(possibleValues.size() > 1) {
            StringBuilder output = new StringBuilder();
            int i = 1;

            for (Value value : possibleValues.values()) {
                output.append(getValueDescription(value));
                if(i == (possibleValues.size() - 1)) {
                    output.append(" or ");
                } else if(i < possibleValues.size()){
                    output.append(", ");
                }
                i++;
            }

            return output.toString();
        } else {
            return "";
        }
    }
}
