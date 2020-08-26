package org.dspace.ref.compliance.rules.factory;

import org.dspace.discovery.SearchService;
import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.DiscoverableRule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Builder that will instantiate a DiscoverableRule rule based on a rule definition.
 */
public class DiscoverableRuleBuilder extends ComplianceRuleBuilder {

    @Autowired
    private SearchService searchService;

    public ComplianceRule buildRule(final RuleDefinition ruleDefinition) {
        DiscoverableRule rule = new DiscoverableRule(searchService);
        applyDefinitionDescriptionAndResolutionHint(rule, ruleDefinition);
        return rule;
    }
}
