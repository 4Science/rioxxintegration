/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.submission.typebound.condition;

import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * Created by jonas - jonas@atmire.com on 11/04/16.
 */
public class ConditionWrapper {

    private List<SubmissionStepCondition> submissionStepConditions;

    @Required
    public void setSubmissionStepConditions(List<SubmissionStepCondition> submissionStepConditions){
        this.submissionStepConditions=submissionStepConditions;
    }

    public List<SubmissionStepCondition> getSubmissionStepConditions(){
        return submissionStepConditions;
    }
}
