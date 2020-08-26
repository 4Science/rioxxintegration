package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.FieldIsNotBlankRule;

/**
 * Builder that will instantiate a NotBlank rule based on a rule definition.
 */
public class NotBlankRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        FieldIsNotBlankRule rule = new FieldIsNotBlankRule(ruleDefinition.getFieldDescription(), ruleDefinition.getField().get(0));
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }
}
