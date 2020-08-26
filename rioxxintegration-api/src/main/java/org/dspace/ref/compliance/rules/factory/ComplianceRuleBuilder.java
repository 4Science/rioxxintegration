package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.AbstractComplianceRule;
import org.dspace.ref.compliance.rules.ComplianceRule;

/**
 * Interface for a builder class that is able to instantiate compliance vaidation rules
 */
public abstract class ComplianceRuleBuilder {

    public abstract ComplianceRule buildRule(final RuleDefinition ruleDefinition);

    protected void applyDefinitionDescriptionAndResolutionHint(final AbstractComplianceRule rule, final RuleDefinition ruleDefinition) {
        rule.setDefinitionHint(ruleDefinition.getDescription());
        rule.setResolutionHint(ruleDefinition.getResolutionHint());
    }
}
