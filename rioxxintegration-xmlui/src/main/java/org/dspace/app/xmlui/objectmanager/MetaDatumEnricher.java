/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.objectmanager;

import java.util.List;

import org.dspace.content.MetadataValue;
import org.dspace.core.Context;
import org.dspace.util.subclasses.Metadata;

/**
 * Class that will enrich the metadata of an item
 */
public interface MetaDatumEnricher {

    List<Metadata> enrichMetadata(Context context, List<MetadataValue> metadataList);

}