package org.dspace.ref.compliance.rules;

import java.util.*;
import org.apache.commons.collections.*;
import org.apache.commons.lang.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.ref.compliance.definition.model.*;
import org.joda.time.*;

/**
 * Rule to check if a date range defined by two metadata fields is smaller than a specified threshold
 */
public class DateRangeSmallerThanRule extends AbstractComplianceRule {

    private String fromField;
    private String toField;
    private String rangeDescription;

    private Value thresholdValue;
    private Integer thresholdNumber;

    public DateRangeSmallerThanRule(final String fromField, final String toField, final String rangeDescription,
                                    final List<Value> thresholdValues) {
        this.fromField = StringUtils.trimToNull(fromField);
        this.toField = StringUtils.trimToNull(toField);
        this.rangeDescription = StringUtils.trimToNull(rangeDescription);

        thresholdValue = CollectionUtils.isEmpty(thresholdValues) ? null : thresholdValues.get(0);

        try {
            this.thresholdNumber = thresholdValue == null ? null : Integer.valueOf(StringUtils.trimToEmpty(thresholdValue.getValue()));
        } catch(NumberFormatException ex) {
            addViolationDescription("the provided threshold value %s is not a number", thresholdValue);
            this.thresholdNumber = null;
        }
    }

    protected String getRuleDescriptionCompliant() {
        return String.format("the %s (from %s to %s) is smaller than %s month(s)", rangeDescription, fromField, toField,
                thresholdValue == null ? "ERROR" : getValueDescription(thresholdValue));
    }

    protected String getRuleDescriptionViolation() {
        return String.format("the %s (from %s to %s) must be smaller than %s month(s)", rangeDescription, fromField, toField,
                thresholdValue == null ? "ERROR" : getValueDescription(thresholdValue));
    }

    protected boolean doValidationAndBuildDescription(final Context context, final Item item) {
        boolean valid = false;

        if (fromField == null || toField == null) {
            addViolationDescription("the from and to date fields of a date range validation rule cannot be blank.");
        } else {
            if(thresholdNumber != null) {
                DateTime from = getFirstDateValue(context, item, fromField);
                DateTime to = getFirstDateValue(context, item, toField);

                if(from == null) {
                    addViolationDescription("there is no valid value for the field " + fromField);
                }
                if(to == null) {
                    addViolationDescription("there is no valid value for the field " + toField);
                }

                if (to != null && from != null) {
                    if(to.isAfter(from)){
                        to = to.minusDays(1);
                    }
                    int months = Months.monthsBetween(from.toLocalDate(), to.toLocalDate()).getMonths();

                    if (months < thresholdNumber) {
                        valid = true;
                    } else {
                        addViolationDescription("the %s is %d month(s)", rangeDescription, months);
                    }
                }
            }
        }

        return valid;
    }

}
