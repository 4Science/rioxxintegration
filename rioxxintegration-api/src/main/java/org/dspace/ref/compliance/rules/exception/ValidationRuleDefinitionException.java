/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.exception;

/**
 * Exception that is thrown when something is wrong the with Validation Rule definition file (config/item-validation-rules.xml)
 */
public class ValidationRuleDefinitionException extends Exception {

    public ValidationRuleDefinitionException(final String message, final Throwable ex) {
        super(message, ex);
    }

    public ValidationRuleDefinitionException(final String message) {
        super(message);
    }
}
