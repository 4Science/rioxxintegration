/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.objectmanager;

import org.dspace.content.Metadatum;
import org.dspace.core.Context;

import java.util.List;

/**
 * Class that will enrich the metadata of an item
 */
public interface MetaDatumEnricher {

    void enrichMetadata(Context context, List<Metadatum> metadataList);

}