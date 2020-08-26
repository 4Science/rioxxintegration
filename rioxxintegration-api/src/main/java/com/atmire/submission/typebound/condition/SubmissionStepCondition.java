package com.atmire.submission.typebound.condition;

import org.dspace.content.Item;

/**
 * Created by jonas - jonas@atmire.com on 11/04/16.
 */
public interface SubmissionStepCondition {

    public boolean conditionMet(Item item);
}
