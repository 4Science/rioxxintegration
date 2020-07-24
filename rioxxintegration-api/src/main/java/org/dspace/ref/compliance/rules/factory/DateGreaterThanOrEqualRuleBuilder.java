/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.definition.model.*;
import org.dspace.ref.compliance.rules.*;

/**
 * Builder that will instantiate a DateGreaterThanOrEqual rule based on a rule definition.
 */
public class DateGreaterThanOrEqualRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        DateGreaterThanOrEqualRule rule = new DateGreaterThanOrEqualRule(ruleDefinition.getFieldDescription(), ruleDefinition.getField().get(0),
                ruleDefinition.getFieldValue());
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }
}
