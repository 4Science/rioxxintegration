/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.definition.model;

import java.util.List;

public class RuleSet {
	
	private List<RuleDefinition> rule;

	public List<RuleDefinition> getRule() {
		return rule;
	}

	public void setRule(List<RuleDefinition> rule) {
		this.rule = rule;
	}
	
	
}
