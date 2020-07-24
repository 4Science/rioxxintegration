/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.definition.model;

import java.util.List;

public class CategorySet {
    private RuleSet preconditions;
    private List<RuleCategory> category;
    private RuleSet exceptions;
	public RuleSet getPreconditions() {
		return preconditions;
	}
	public void setPreconditions(RuleSet preconditions) {
		this.preconditions = preconditions;
	}
	public List<RuleCategory> getCategory() {
		return category;
	}
	public void setCategory(List<RuleCategory> category) {
		this.category = category;
	}
	public RuleSet getExceptions() {
		return exceptions;
	}
	public void setExceptions(RuleSet exceptions) {
		this.exceptions = exceptions;
	}
}
