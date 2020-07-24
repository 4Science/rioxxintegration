/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.definition.model;

public class RuleCategory {
	private RuleSet rules;
	private RuleSet exceptions;
	private int ordinal;
	private String name;
	private String description;
	private String resolutionHint;

	public RuleSet getRules() {
		return rules;
	}

	public void setRules(RuleSet rules) {
		this.rules = rules;
	}

	public RuleSet getExceptions() {
		return exceptions;
	}

	public void setExceptions(RuleSet exceptions) {
		this.exceptions = exceptions;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getResolutionHint() {
		return resolutionHint;
	}

	public void setResolutionHint(String resolutionHint) {
		this.resolutionHint = resolutionHint;
	}
}
