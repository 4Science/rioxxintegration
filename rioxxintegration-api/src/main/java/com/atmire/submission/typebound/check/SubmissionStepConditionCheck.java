package com.atmire.submission.typebound.check;

import java.util.ArrayList;
import java.util.List;

import org.dspace.app.util.ISubmissionStepConditionCheck;
import org.dspace.content.Item;
import org.dspace.utils.DSpace;

import com.atmire.submission.typebound.condition.ConditionWrapper;
import com.atmire.submission.typebound.condition.SubmissionStepCondition;

/**
 * Created by jonas - jonas@atmire.com on 11/04/16.
 */
public class SubmissionStepConditionCheck implements ISubmissionStepConditionCheck {

    public boolean allConditionsMet(Item item, String configName) {
        ConditionWrapper wrapper = new DSpace().getServiceManager().getServiceByName(configName, ConditionWrapper.class);
        List<SubmissionStepCondition> conditions = wrapper.getSubmissionStepConditions();

        List<Boolean> conditionsMet = new ArrayList<Boolean>();
        for (SubmissionStepCondition condition : conditions) {
            conditionsMet.add(condition.conditionMet(item));
        }
        return !conditionsMet.contains(false);
    }

}
