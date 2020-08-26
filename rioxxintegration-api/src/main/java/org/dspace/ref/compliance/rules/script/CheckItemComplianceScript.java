package org.dspace.ref.compliance.rules.script;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.collections.CollectionUtils;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.handle.service.HandleService;
import org.dspace.ref.compliance.result.CategoryComplianceResult;
import org.dspace.ref.compliance.result.ComplianceResult;
import org.dspace.ref.compliance.result.RuleComplianceResult;
import org.dspace.ref.compliance.service.ComplianceCheckService;
import org.dspace.scripts.ContextScript;
import org.dspace.scripts.Script;
import org.dspace.utils.DSpace;

/**
 * Utility script to check for a given handle if the corresponding item is compliant with the validation rules
 * defined in the Validation Rule definition file (config/item-validation-rules.xml)
 * <p>
 * E.g.: bin/dspace dsrun org.dspace.ref.compliance.rules.script.CheckItemComplianceScript -i 123456789/3
 */
public class CheckItemComplianceScript extends ContextScript {

    public static final String ITEM_HANDLE_FLAG = "i";
    public static final String SHOW_ALL_FLAG = "a";

    private String itemHandle;
    private boolean showAll = false;

    public static void main(String[] args) {
        Script script = new CheckItemComplianceScript();
        script.mainImpl(args);
    }

    @Override
    protected int processLine(CommandLine line) {
        int status = super.processLine(line);

        if (status == 0) {
            // other arguments
            if (line.hasOption(ITEM_HANDLE_FLAG)) {
                itemHandle = line.getOptionValue(ITEM_HANDLE_FLAG);
            }
            if (line.hasOption(SHOW_ALL_FLAG)) {
                showAll = true;
            }
        }
        return status;
    }

    @Override
    protected Options createCommandLineOptions() {
        Options options = super.createCommandLineOptions();
        options.addOption(ITEM_HANDLE_FLAG, "item-handle", true, "The handle of the item to check");
        options.addOption(SHOW_ALL_FLAG, "show-all-rules", false, "Use this flag to show all rules, including the not applicable ones");
        return options;
    }

    @Override
    public void run() throws Exception {
        print("Checking REF compliance of item with handle " + itemHandle);
        HandleService handleService = HandleServiceFactory.getInstance().getHandleService();
        DSpaceObject object = handleService.resolveToObject(context, itemHandle);

        if (object != null && object instanceof Item) {
            Item item = (Item) object;

            ComplianceCheckService complianceCheckService = new DSpace().getServiceManager()
                    .getServiceByName("refComplianceCheckService", ComplianceCheckService.class);

            ComplianceResult result = complianceCheckService.checkCompliance(context, item);

            print("");

            if(result.isApplicable()) {
                print("The item is " + (result.isCompliant() ? "" : " NOT ") + "compliant");

                if (result.isCompliantByException()) {
                    List<RuleComplianceResult> appliedExceptions = result.getAppliedExceptions();

                    print("");
                    print("***** Compliant by Exception *****");
                    for (RuleComplianceResult appliedException : appliedExceptions) {
                        print("Exception: " + appliedException.getResultDescription()
                                + " (" + appliedException.getDefinitionHint() + ")");
                    }

                }

                for (CategoryComplianceResult categoryResult : result.getOrderedCategoryResults()) {
                    if (categoryResult.isApplicable() || showAll) {
                        print("");
                        print("***** " + categoryResult.getCategoryName() + ": " +
                                (categoryResult.isApplicable() ?
                                        (categoryResult.isCompliant() ? "Compliant" : "NOT compliant") : "NOT APPLICABLE")
                                + " *****");


                        if (categoryResult.isCompliantByException()) {
                            List<RuleComplianceResult> appliedExceptions = categoryResult.getAppliedExceptions();

                            for (RuleComplianceResult appliedException : appliedExceptions) {
                                print("Compliant by exception: " + appliedException.getResultDescription()
                                        + " (" + appliedException.getDefinitionHint() + ")");
                            }
                        } else {

                            List<RuleComplianceResult> compliantRules = filterRules(categoryResult.getCompliantRules());
                            if (CollectionUtils.isNotEmpty(compliantRules)) {
                                print("* Rules that are OK:");
                                for (RuleComplianceResult ruleResult : compliantRules) {
                                    print("- " + ruleResult.getResultDescription()
                                            + " (" + ruleResult.getDefinitionHint() + ")");

                                    if (ruleResult.exceptionApplied()) {
                                        print("--- Exception: " + ruleResult.getExceptionDescription());
                                    }
                                }
                            }

                            List<RuleComplianceResult> violatedRules = filterRules(categoryResult.getViolatedRules());
                            if (CollectionUtils.isNotEmpty(violatedRules)) {
                                print("* Rules that are NOT OK:");
                                for (RuleComplianceResult ruleResult : violatedRules) {
                                    print("- " + ruleResult.getResultDescription()
                                            + " (" + ruleResult.getDefinitionHint() + ")");
                                    for (String violation : ruleResult.getViolationDescriptions()) {
                                        print("--- " + violation);
                                    }
                                    print("--- RESOLUTION: " + ruleResult.getResolutionHint());
                                }
                            }
                        }
                    }
                }
            }
            else {
                print("The item is not applicable");

                List<RuleComplianceResult> violatedpreconditionRules = filterRules(result.getViolatedPreconditions());
                if (CollectionUtils.isNotEmpty(violatedpreconditionRules)) {
                    print("* Rules that are not ok:");
                    for (RuleComplianceResult ruleResult : violatedpreconditionRules) {
                        print("- " + ruleResult.getResultDescription()
                                + " (" + ruleResult.getDefinitionHint() + ")");
                        for (String violation : ruleResult.getViolationDescriptions()) {
                            print("--- " + violation);
                        }
                        print("--- RESOLUTION: " + ruleResult.getResolutionHint());
                    }
                }
            }

        } else {
            print("The item with handle " + itemHandle + " was not found.");
        }
    }

    private List<RuleComplianceResult> filterRules(final List<RuleComplianceResult> categoryResults) {
        List<RuleComplianceResult> output = new ArrayList<RuleComplianceResult>(categoryResults.size());
        for (RuleComplianceResult result : categoryResults) {
            if (result.isApplicable() || showAll) {
                output.add(result);
            }
        }
        return output;
    }

}
