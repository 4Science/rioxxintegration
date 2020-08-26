package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.DateSmallerThanRule;

/**
 * Builder that will instantiate a DateSmallerThan rule based on a rule definition.
 */
public class DateSmallerThanRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        DateSmallerThanRule rule = new DateSmallerThanRule(ruleDefinition.getFieldDescription(), ruleDefinition.getField().get(0),
                ruleDefinition.getFieldValue());
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }
}
