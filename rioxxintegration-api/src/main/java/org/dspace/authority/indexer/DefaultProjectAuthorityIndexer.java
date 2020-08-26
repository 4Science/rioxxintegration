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

import org.dspace.authority.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.*;
import org.dspace.core.*;

/**
 * @author philip at atmire.com
 */
public class DefaultProjectAuthorityIndexer implements AuthorityIndexerInterface {

    private ProjectAuthorityValue defaultProject;
    private boolean indexed;
    private DefaultAuthorityCreator defaultAuthorityCreator;

    @Override
    public boolean isConfiguredProperly() {
        return true;
    }

    private void loadDefaultProject(Context context){
        indexed = false;

        if(defaultAuthorityCreator!=null) {
            defaultProject = defaultAuthorityCreator.retrieveDefaultProject(context);
        }
    }

    public void setDefaultAuthorityCreator(DefaultAuthorityCreator defaultAuthorityCreator) {
        this.defaultAuthorityCreator = defaultAuthorityCreator;
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
		List<AuthorityValue> result = new ArrayList<AuthorityValue>();
		loadDefaultProject(context);
		result.add(defaultProject);
		return result;
	}
	
	@Override
	public boolean isOnlyForIndexerUse() {
		return true;
	}
}
