/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.submission.typebound.check;

import com.atmire.submission.typebound.condition.ConditionWrapper;
import com.atmire.submission.typebound.condition.SubmissionStepCondition;
import org.dspace.content.Item;
import org.dspace.utils.DSpace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jonas - jonas@atmire.com on 11/04/16.
 */
public class SubmissionStepConditionCheck {

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
