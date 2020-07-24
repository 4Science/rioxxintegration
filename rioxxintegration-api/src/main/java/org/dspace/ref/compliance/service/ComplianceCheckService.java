/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.service;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.ref.compliance.result.ComplianceResult;

/**
 * Service to check if an item is compliant with the defined validation rules
 */
public interface ComplianceCheckService {

    ComplianceResult checkCompliance(final Context context, final Item item);

    boolean blockOnWorkflow(String collectionHandle);
}
