package org.dspace.ref.compliance.rules.factory;

import org.dspace.ref.compliance.rules.CompliancePolicy;
import org.dspace.ref.compliance.rules.exception.ValidationRuleDefinitionException;

/**
 * Interface for a factory that is able to instantiate all required compliance validation categories and their rules based on the
 * Validation Rule definition file (config/item-validation-rules.xml)
 */
public interface ComplianceCategoryRulesFactory {

    CompliancePolicy createComplianceRulePolicy() throws ValidationRuleDefinitionException;

}