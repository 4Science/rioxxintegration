package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.DateRangeSmallerThanRule;

/**
 * Builder that will instantiate a DateRangeSmallerThan rule based on a rule definition.
 */
public class DateRangeSmallerThanRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        DateRangeSmallerThanRule rule = new DateRangeSmallerThanRule(ruleDefinition.getFrom(), ruleDefinition.getTo(),
                ruleDefinition.getFieldDescription(), ruleDefinition.getFieldValue());
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }

}
