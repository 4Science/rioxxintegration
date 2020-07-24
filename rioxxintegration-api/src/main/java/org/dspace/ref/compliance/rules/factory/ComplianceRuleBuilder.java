/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
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
