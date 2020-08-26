package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.CountGreaterThanRule;

/**
 * Builder that will instantiate a CountGreaterThan rule based on a rule definition.
 */
public class CountGreaterThanRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        CountGreaterThanRule rule = new CountGreaterThanRule(ruleDefinition.getFieldDescription(), ruleDefinition.getField().get(0), ruleDefinition.getFieldValue());
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }

}
