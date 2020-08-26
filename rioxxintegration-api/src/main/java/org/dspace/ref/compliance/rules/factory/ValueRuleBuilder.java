package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.FieldHasValueRule;

/**
 * Builder that will instantiate a ValueRule rule based on a rule definition.
 */
public class ValueRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        FieldHasValueRule rule = new FieldHasValueRule(ruleDefinition.getFieldDescription(), ruleDefinition.getField().get(0),
                ruleDefinition.getFieldValue());
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }

}
