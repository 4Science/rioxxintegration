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
 * @author philip at atmire.com
 */
public class AtLeastOneNotBlankRuleBuilder extends ComplianceRuleBuilder {

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        AtLeastOneNotBlankRule rule = new AtLeastOneNotBlankRule(ruleDefinition.getFieldDescription(), ruleDefinition.getField());
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }

}
