/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.swordapp.server;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.sword2.DSpaceSwordException;
import org.dspace.sword2.DepositResult;
import org.dspace.sword2.SwordEntryIngester;
import org.dspace.sword2.VerboseDescription;
import org.dspace.util.MetadataFieldString;
import org.dspace.util.subclasses.Metadata;
import org.swordapp.server.Deposit;
import org.swordapp.server.SwordAuthException;
import org.swordapp.server.SwordError;
import org.swordapp.server.SwordServerException;

import com.atmire.swordapp.server.util.SimpleRioxxMetadataHelper;


public class SimpleRioxxDCEntryIngester extends AbstractSimpleRioxx implements SwordEntryIngester {

    private static final Logger log = Logger
            .getLogger(SimpleRioxxDCEntryIngester.class);
    
    protected HashMap<String, String> dcMap = null;
    
    private WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();

    protected ConfigurationService configurationService = DSpaceServicesFactory
            .getInstance().getConfigurationService();
    
    public SimpleRioxxDCEntryIngester() {
        SimpleRioxxMetadataHelper simpleRioxxMetadataHelper = new SimpleRioxxMetadataHelper();
        dcMap = simpleRioxxMetadataHelper.getDcMap();
    }

    public DepositResult ingest(Context context, Deposit deposit, DSpaceObject dso,
                                VerboseDescription verboseDescription)
        throws DSpaceSwordException, SwordError, SwordAuthException, SwordServerException {
        return this.ingest(context, deposit, dso, verboseDescription, null, false);
    }

    public DepositResult ingest(Context context, Deposit deposit, DSpaceObject dso,
                                VerboseDescription verboseDescription, DepositResult result, boolean replace)
        throws DSpaceSwordException, SwordError, SwordAuthException, SwordServerException {
        if (dso instanceof Collection) {
            return this.ingestToCollection(context, deposit, (Collection) dso, verboseDescription, result);
        } else if (dso instanceof Item) {
            return this.ingestToItem(context, deposit, (Item) dso, verboseDescription, result, replace);
        }
        return null;
    }

    public DepositResult ingestToItem(Context context, Deposit deposit, Item item,
                                      VerboseDescription verboseDescription, DepositResult result, boolean replace)
        throws DSpaceSwordException, SwordError, SwordAuthException, SwordServerException {
        try {
            if (result == null) {
                result = new DepositResult();
            }
            result.setItem(item);

            // clean out any existing item metadata which is allowed to be replaced
            if (replace) {
                this.removeMetadata(context, item);
            }

            // add the metadata to the item
            this.addMetadataToItem(context, deposit, item);

            // update the item metadata to inclue the current time as
            // the updated date
            this.setUpdatedDate(context, item, verboseDescription);

            // in order to write these changes, we need to bypass the
            // authorisation briefly, because although the user may be
            // able to add stuff to the repository, they may not have
            // WRITE permissions on the archive.
            context.turnOffAuthorisationSystem();
            getItemService().update(context, item);
            context.restoreAuthSystemState();

            verboseDescription.append("Update successful");

            result.setItem(item);
            result.setTreatment(this.getTreatment());

            return result;
        } catch (SQLException e) {
            throw new DSpaceSwordException(e);
        } catch (AuthorizeException e) {
            throw new DSpaceSwordException(e);
        }
    }

    private void removeMetadata(Context context, Item item)
            throws DSpaceSwordException
    {
        String[] replaceableMetadata = configurationService
                .getArrayProperty("swordv2-server.metadata.replaceable");
        for (String part : replaceableMetadata)
        {
            Metadata info = this
                    .makeDCValue(part.trim(), null);
            try
            {
                itemService
                        .clearMetadata(context, item, info.schema, info.element,
                                info.qualifier, Item.ANY);
            }
            catch (SQLException e)
            {
                log.error("Caught exception trying to remove metadata", e);
                throw new DSpaceSwordException(e);
            }
        }
    }

    private void addUniqueMetadata(Context context, Metadata dcv, Item item) throws SQLException {
        String qual = dcv.qualifier;
        if (dcv.qualifier == null) {
            qual = Item.ANY;
        }

        String lang = dcv.language;
        if (dcv.language == null) {
            lang = Item.ANY;
        }
        List<MetadataValue> existing = getItemService().getMetadata(item, dcv.schema, dcv.element, qual, lang);
        for (MetadataValue dcValue : existing) {
            // FIXME: probably we want to be slightly more careful about qualifiers and languages
            //
            // if the submitted value is already attached to the item, just skip it
            if (dcValue.getValue().equals(dcv.value)) {
                return;
            }
        }

        // if we get to here, go on and add the metadata
        getItemService().addMetadata(context, item, dcv.schema, dcv.element, dcv.qualifier, dcv.language, dcv.value);
    }

    private void addMetadataToItem(Context context, Deposit deposit, Item item)
        throws DSpaceSwordException, SQLException {
        // now, go through and get the metadata from the EntryPart and put it in DSpace
        RioxxSwordEntry se = (RioxxSwordEntry) deposit.getSwordEntry();

        // first do the standard atom terms (which may get overridden later)
        String title = se.getTitle();
        String summary = se.getSummary();
        if (title != null) {
            String titleField = this.dcMap.get("title");
            if (titleField != null) {
                Metadata dcv = this.makeDCValue(titleField, title);
                this.addUniqueMetadata(context, dcv, item);
            }
        }
        if (summary != null) {
            String abstractField = this.dcMap.get("abstract");
            if (abstractField != null) {
            	Metadata dcv = this.makeDCValue(abstractField, summary);
                this.addUniqueMetadata(context, dcv, item);
            }
        }

        Metadata workflowSwordSubmission = this.makeDCValue("dc.sword.submission", "true");
        this.addUniqueMetadata(context, workflowSwordSubmission, item);


        Map<String, List<String>> dc = se.getData();
        for (String term : dc.keySet()) {
            String dsTerm = this.dcMap.get(term.replace(":", "."));
            if (dsTerm == null) {
                // ignore anything we don't understand
                continue;
            }

            String[] fields = StringUtils.split(dsTerm, ',');

            if(fields.length == 1) {
                // now add all the metadata terms
                Metadata dcv = this.makeDCValue(fields[0], null);
                for (String value : dc.get(term)) {
                    dcv.value = value;
                    this.addUniqueMetadata(context, dcv, item);
                }
            }
            else {
                for (String field : fields) {
                    Metadata dcv = this.makeDCValue(field, null);
                    for (String value : dc.get(term)) {
                        dcv.value = value;
                        this.addUniqueMetadata(context, dcv, item);
                    }
                }
            }
        }
    }

    public DepositResult ingestToCollection(Context context, Deposit deposit, Collection collection,
                                            VerboseDescription verboseDescription, DepositResult result)
        throws DSpaceSwordException, SwordError, SwordAuthException, SwordServerException {
        try {
            // decide whether we have a new item or an existing one
            Item item = null;
            WorkspaceItem wsi = null;
            if (result != null) {
                item = result.getItem();
            } else {
                result = new DepositResult();
            }
            if (item == null) {
                // simple zip ingester uses the item template, since there is no native metadata
                wsi = workspaceItemService.create(context, collection, true);
                item = wsi.getItem();
            }

            // add the metadata to the item
            this.addMetadataToItem(context, deposit, item);

            // update the item metadata to inclue the current time as
            // the updated date
            this.setUpdatedDate(context, item, verboseDescription);

            // DSpace ignores the slug value as suggested identifier, but
            // it does store it in the metadata
            this.setSlug(context, item, deposit.getSlug(), verboseDescription);

            // in order to write these changes, we need to bypass the
            // authorisation briefly, because although the user may be
            // able to add stuff to the repository, they may not have
            // WRITE permissions on the archive.
            context.turnOffAuthorisationSystem();
            getItemService().update(context, item);
            context.restoreAuthSystemState();

            verboseDescription.append("Ingest successful");
            verboseDescription.append("Item created with internal identifier: " + item.getID());

            result.setItem(item);
            result.setTreatment(this.getTreatment());

            return result;
        } catch (AuthorizeException e) {
            throw new SwordAuthException(e);
        } catch (SQLException e) {
            throw new DSpaceSwordException(e);
        }
    }

    public Metadata makeDCValue(String field, String value)
        throws DSpaceSwordException {
    	return MetadataFieldString.encapsulate(field, value);
    }

    /**
     * Add the current date to the item metadata.  This looks up
     * the field in which to store this metadata in the configuration
     * sword.updated.field
     *
     * @param item
     * @throws DSpaceSwordException
     * @throws SQLException 
     */
    protected void setUpdatedDate(Context context, Item item, VerboseDescription verboseDescription)
        throws DSpaceSwordException, SQLException {
        String field = configurationService
                .getProperty("swordv2-server.updated.field");
        if (StringUtils.isBlank(field))
        {
            throw new DSpaceSwordException(
                    "No configuration, or configuration is invalid for: swordv2-server.updated.field");
        }


        Metadata dc = this.makeDCValue(field, null);
        getItemService().clearMetadata(context, item, dc.schema, dc.element, dc.qualifier, Item.ANY);
        DCDate date = new DCDate(new Date());
        getItemService().addMetadata(context, item, dc.schema, dc.element, dc.qualifier, null, date.toString());

        verboseDescription.append("Updated date added to response from item metadata where available");
    }

    /**
     * Store the given slug value (which is used for suggested identifiers,
     * and which DSpace ignores) in the item metadata.  This looks up the
     * field in which to store this metadata in the configuration
     * sword.slug.field
     *
     * @param item
     * @param slugVal
     * @throws DSpaceSwordException
     * @throws SQLException 
     */
    protected void setSlug(Context context, Item item, String slugVal, VerboseDescription verboseDescription)
        throws DSpaceSwordException, SQLException {
        // if there isn't a slug value, don't set it
        if (slugVal == null)
        {
            return;
        }

        String field = configurationService
                .getProperty("swordv2-server.slug.field");
        if (StringUtils.isBlank(field))
        {
            throw new DSpaceSwordException(
                    "No configuration, or configuration is invalid for: swordv2-server.slug.field");
        }

        Metadata dc = this.makeDCValue(field, null);
        getItemService().clearMetadata(context, item, dc.schema, dc.element, dc.qualifier, Item.ANY);
        getItemService().addMetadata(context, item, dc.schema, dc.element, dc.qualifier, null, slugVal);

        verboseDescription.append("Slug value set in response where available");
    }

    /**
     * The human readable description of the treatment this ingester has
     * put the deposit through
     *
     * @return
     * @throws DSpaceSwordException
     */
    private String getTreatment() throws DSpaceSwordException {
        return "A metadata only item has been created";
    }
}
