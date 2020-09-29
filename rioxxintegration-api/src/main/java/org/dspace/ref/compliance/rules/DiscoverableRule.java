/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;

/**
 * Validation rule that checks if an item is discoverable by an anonymous user through the DSpace search functionality.
 */
public class DiscoverableRule extends AbstractComplianceRule {

    private SearchService searchService;

    public DiscoverableRule(final SearchService searchService) {
        this.searchService = searchService;
    }

    protected String getRuleDescriptionCompliant() {
        return "the item is discoverable using the search functionality";
    }

    protected String getRuleDescriptionViolation() {
        return "the item must be discoverable using the search functionality";
    }

    protected boolean doValidationAndBuildDescription(final Context context, final Item item) {
        boolean valid = false;

        DiscoverQuery query = new DiscoverQuery();
        query.setQuery("handle:\"" + item.getHandle() + "\"");
        query.setMaxResults(0);
        query.setStart(0);

        try {
        	Context anonymousContext = new Context();
            DiscoverResult result = searchService.search(anonymousContext, query);
            if(result != null && result.getTotalSearchResults() > 0) {
                valid = true;
            } else {
                addViolationDescription(item);
            }            
        } catch (SearchServiceException e) {
            addViolationDescription("unable to query discovery for item %s: %s", item.getHandle(), e.getMessage());
        } 

        return valid;
    }

    private void addViolationDescription(final Item item) {
        String description = "item with %s %s is not discoverable using the search functionality";
        if(item.getHandle() == null) {
            addViolationDescription(description, "title", "\"" + getItemService().getMetadataFirstValue(item, "dc", "title", null, Item.ANY) + "\"");
        } else {
            addViolationDescription(description, "handle", item.getHandle());
        }
    }
}
