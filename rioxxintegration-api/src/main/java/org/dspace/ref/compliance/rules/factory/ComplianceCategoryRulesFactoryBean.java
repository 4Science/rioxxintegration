package org.dspace.ref.compliance.rules.factory;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.core.ConfigurationManager;
import org.dspace.ref.compliance.definition.model.CategorySet;
import org.dspace.ref.compliance.definition.model.RuleCategory;
import org.dspace.ref.compliance.definition.model.RuleDefinition;
import org.dspace.ref.compliance.definition.model.RuleSet;
import org.dspace.ref.compliance.rules.ComplianceCategory;
import org.dspace.ref.compliance.rules.CompliancePolicy;
import org.dspace.ref.compliance.rules.ComplianceRule;
import org.dspace.ref.compliance.rules.exception.ValidationRuleDefinitionException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory that is able to instantiate all required compliance validation categories with their rules based on the
 * Validation Rule definition file (config/item-validation-rules.xml)
 */
public class ComplianceCategoryRulesFactoryBean implements ComplianceCategoryRulesFactory {

    private String ruleDefinitionFile;

    private CategorySetMarshaller marshaller = new CategorySetMarshaller();

    private Map<String, ComplianceRuleBuilder> builderMap = new HashMap<String, ComplianceRuleBuilder>();

    public void setBuilderMap(final Map<String, ComplianceRuleBuilder> builderMap) {
        this.builderMap = builderMap;
    }

    private CategorySet categorySet;

    private long lastCategorySetUpdate;

    private long categorySetDateModified;

    private int minutesBetweentCategorySetupdate;

    public CompliancePolicy createComplianceRulePolicy() throws ValidationRuleDefinitionException {
        CompliancePolicy output = new CompliancePolicy();

        CategorySet categorySet = loadRuleDefinitionSet();

        if (categorySet != null) {

            if (CollectionUtils.isNotEmpty(categorySet.getCategory())) {
                List<RuleCategory> categories = categorySet.getCategory();
                for (RuleCategory categoryDefinition : categories) {
                    ComplianceCategory category = convertToComplianceCategory(categoryDefinition);

                    if (category != null) {
                        output.addComplianceCategory(category);
                    }
                }
            }

            List<ComplianceRule> exceptions = convertRuleSet(categorySet.getExceptions());
            if(CollectionUtils.isNotEmpty(exceptions)) {
                output.addExceptionRules(exceptions);
            }

            List<ComplianceRule> preconditionRules = convertRuleSet(categorySet.getPreconditions());
            if(CollectionUtils.isNotEmpty(preconditionRules)) {
                output.addPreconditionRules(preconditionRules);
            }
        }

        return output;
    }

    private ComplianceCategory convertToComplianceCategory(final RuleCategory categoryDefinition) throws ValidationRuleDefinitionException {
        if (categoryDefinition == null) {
            return null;
        } else {
            ComplianceCategory category = new ComplianceCategory();
            category.setOrdinal(categoryDefinition.getOrdinal());
            category.setName(categoryDefinition.getName());
            category.setDescription(categoryDefinition.getDescription());
            category.setResolutionHint(categoryDefinition.getResolutionHint());

            List<ComplianceRule> rules = convertRuleSet(categoryDefinition.getRules());
            if (CollectionUtils.isNotEmpty(rules)) {
                category.addComplianceRules(rules);
            }

            List<ComplianceRule> exceptions = convertRuleSet(categoryDefinition.getExceptions());
            if (CollectionUtils.isNotEmpty(exceptions)) {
                category.addExceptionRules(exceptions);
            }

            return category;
        }
    }

    private List<ComplianceRule> convertRuleSet(final RuleSet rules) throws ValidationRuleDefinitionException {
        List<ComplianceRule> output = new LinkedList<ComplianceRule>();

        if(rules != null && CollectionUtils.isNotEmpty(rules.getRule())) {
            List<RuleDefinition> definitions = rules.getRule();
            for (RuleDefinition ruleDefinition : definitions) {
                ComplianceRule rule = convertToComplianceRule(ruleDefinition);

                if (rule != null) {
                    output.add(rule);
                }
            }
        }

        return output;
    }

    private ComplianceRule convertToComplianceRule(final RuleDefinition ruleDefinition) throws ValidationRuleDefinitionException {
        ComplianceRule rule = null;
        ComplianceRuleBuilder builder = builderMap.get(ruleDefinition.getType());

        if (builder == null) {
            throw new ValidationRuleDefinitionException("Unable to find a rule builder for rule type " + ruleDefinition.getType());
        } else {
            rule = builder.buildRule(ruleDefinition);

            if (ruleDefinition.getExceptions() != null && CollectionUtils.isNotEmpty(ruleDefinition.getExceptions().getRule())) {
                for (RuleDefinition ruleDefinition1 : ruleDefinition.getExceptions().getRule()) {
                    ComplianceRule complianceRule = convertToComplianceRule(ruleDefinition1);

                    if (complianceRule != null) {
                        rule.addExceptionRule(complianceRule);
                    }
                }
            }

            if (ruleDefinition.getPreconditions() != null && CollectionUtils.isNotEmpty(ruleDefinition.getPreconditions().getRule())) {
                for (RuleDefinition ruleDefinition1 : ruleDefinition.getPreconditions().getRule()) {
                    ComplianceRule complianceRule = convertToComplianceRule(ruleDefinition1);

                    if (complianceRule != null) {
                        rule.addPreconditionRule(complianceRule);
                    }
                }
            }
        }
        return rule;
    }

    private CategorySet loadRuleDefinitionSet() throws ValidationRuleDefinitionException {
        // minutes to milliseconds
        long millisecondsBetweentCategorySetupdate = minutesBetweentCategorySetupdate * 60 * 1000;

        // update if null or if the last update was longer ago then the configured time between updates
        if(categorySet == null || lastCategorySetUpdate < (System.currentTimeMillis() - millisecondsBetweentCategorySetupdate)) {
        File file = new File(ConfigurationManager.getProperty("dspace.dir") + File.separator + "config" + File.separator + ruleDefinitionFile);

            if(file.lastModified() != categorySetDateModified) {

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
                    categorySet = marshaller.unmarshal(inputStream);
            inputStream.close();

                    lastCategorySetUpdate = System.currentTimeMillis();
                    categorySetDateModified = file.lastModified();

        } catch (FileNotFoundException e) {
            throw new ValidationRuleDefinitionException("The validation rule definition file " + ruleDefinitionFile + " was not found.", e);
        } catch (IOException e) {
            throw new ValidationRuleDefinitionException("There was a problem reading the validation rule definition file " + ruleDefinitionFile + ".", e);
        } catch (JAXBException e) {
            throw new ValidationRuleDefinitionException("There was a problem unmarshalling the validation rule definitions from file " + ruleDefinitionFile + ".", e);
        }
            }
        }

        return categorySet;
    }

    public void setMinutesBetweentCategorySetupdate(int minutesBetweentCategorySetupdate) {
        this.minutesBetweentCategorySetupdate = minutesBetweentCategorySetupdate;
    }

    public void setRuleDefinitionFile(String ruleDefinitionFile) {
        this.ruleDefinitionFile = ruleDefinitionFile;
    }
}
