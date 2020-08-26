/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.indexer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dspace.authority.AuthorityValue;
import org.dspace.authority.FunderAuthorityValue;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author philip at atmire.com
 */
public class FunderAuthorityIndexer implements AuthorityIndexerInterface {

    private static final int PAGE_SIZE = 1000;

    @Autowired(required = true)
    protected AuthorityValueService authorityValueService;
    
    @Override
    public boolean isConfiguredProperly() {
        return true;
    }

    private List<AuthorityValue> loadFunders(Context context){
        List<AuthorityValue> funders = new ArrayList<>();
        int page = 0;

        do {
            List<AuthorityValue> authorityValues = authorityValueService.findByAuthorityType(context, new FunderAuthorityValue().getAuthorityType(), page, PAGE_SIZE);
            funders.addAll(authorityValues);
            page++;
        }
        while (funders.size() == (page*PAGE_SIZE));
        return funders;
    }
    
	@Override
	public List<AuthorityValue> getAuthorityValues(Context context, Item item) throws SQLException, AuthorizeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuthorityValue> getAuthorityValues(Context context, Item item, Map<String, AuthorityValue> cache)
			throws SQLException, AuthorizeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuthorityValue> getAuthorityValues(Context context) throws SQLException, AuthorizeException {
		return loadFunders(context);
	}
	
	@Override
	public boolean isOnlyForIndexerUse() {
		return true;
	}

}
