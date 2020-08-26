package org.dspace.ref.compliance.result;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dspace.util.subclasses.Metadata;

/**
 * Model class that represents the result of a compliance check
 */
public class ComplianceResult {

    private List<CategoryComplianceResult> categoryResults = new LinkedList<CategoryComplianceResult>();

    private List<RuleComplianceResult> exceptionResults = new LinkedList<RuleComplianceResult>();

    private Map<String, String> estimatedValues = new HashMap<String, String>();

    private boolean isApplicable;

    private List<RuleComplianceResult> preconditionResults = new LinkedList<RuleComplianceResult>();

    public List<CategoryComplianceResult> getOrderedCategoryResults() {
        Collections.sort(categoryResults);

        return categoryResults;
    }

    public List<RuleComplianceResult> getAppliedExceptions() {
        List<RuleComplianceResult> output = new LinkedList<RuleComplianceResult>();
        for (RuleComplianceResult ruleResult : exceptionResults) {

            if(ruleResult.isCompliant() && ruleResult.isApplicable()) {
                output.add(ruleResult);
            }
        }

        return output;
    }

    public Map<String, String> getEstimatedValues() {
        return estimatedValues;
    }

    public boolean isCompliant() {
        boolean isCompliant = true;

        if(!isCompliantByException()) {

            Iterator<CategoryComplianceResult> it = getOrderedCategoryResults().iterator();

            while (it.hasNext() && isCompliant) {
                CategoryComplianceResult next = it.next();
                isCompliant = next.isCompliant() || !next.isApplicable();
            }
        }

        return isCompliant;
    }

    public void addEstimatedValues(List<Metadata> fakeFields) {
        for (Metadata fakeField : fakeFields)
        {
            estimatedValues.put(fakeField.toString(), fakeField.getValue());
        }
    }

    public List<RuleComplianceResult> getViolatedPreconditions(){
        List<RuleComplianceResult> output = new LinkedList<RuleComplianceResult>();
        for (RuleComplianceResult preconditionResult : preconditionResults) {
            if(!preconditionResult.isCompliant()){
                output.add(preconditionResult);
            }
        }

        return output;
    }

    public boolean isCompliantByException() {
        return !getAppliedExceptions().isEmpty();
    }

    public void addExceptionResult(final RuleComplianceResult ruleResult) {
        exceptionResults.add(ruleResult);
    }

    public void addCategoryResult(final CategoryComplianceResult categoryResult) {
        categoryResults.add(categoryResult);
    }

    public void addPreconditionResult(final RuleComplianceResult ruleResult) {
        preconditionResults.add(ruleResult);
    }

    public boolean isApplicable() {
        return isApplicable;
    }

    public void setApplicable(boolean applicable) {
        isApplicable = applicable;
    }
}
