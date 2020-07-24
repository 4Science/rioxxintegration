/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.indexer;

import org.dspace.authority.*;
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
    public void init(Context context, Item item) {
        //This indexer should do nothing when called from the AuthorityConsumer
    }

    @Override
    public void init(Context context, boolean useCache) {
        loadDefaultProject(context);
    }

    @Override
    public void init(Context context) {
        loadDefaultProject(context);
    }

    @Override
    public AuthorityValue nextValue() {
        indexed = true;
        return defaultProject;
    }

    @Override
    public boolean hasMore() {
        return !indexed && defaultProject!=null;
    }

    @Override
    public void close() {

    }

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
}
