/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.definition.model;

import java.util.List;

public class RuleDefinition {

	private RuleSet preconditions;
	private List<String> field;
	private String from;
	private String to;
	private String fieldDescription;
	private List<Value> fieldValue;
	private RuleSet exceptions;
	private String type;
	private String description;
	private String resolutionHint;

	public RuleSet getPreconditions() {
		return preconditions;
	}

	public void setPreconditions(RuleSet preconditions) {
		this.preconditions = preconditions;
	}

	public List<String> getField() {
		return field;
	}

	public void setField(List<String> field) {
		this.field = field;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFieldDescription() {
		return fieldDescription;
	}

	public void setFieldDescription(String fieldDescription) {
		this.fieldDescription = fieldDescription;
	}

	public List<Value> getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(List<Value> fieldValue) {
		this.fieldValue = fieldValue;
	}

	public RuleSet getExceptions() {
		return exceptions;
	}

	public void setExceptions(RuleSet exceptions) {
		this.exceptions = exceptions;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
