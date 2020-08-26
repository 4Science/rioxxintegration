/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.complianceupdaters;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.util.MetadataFieldString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

/**
 * Created by jonas - jonas@atmire.com on 23/03/16.
 */
public abstract class FirstComplianceDepositCheckOnMetadata implements ComplianceDepositCheck {

    private String metadatumHelper;
    private String compliantField;
    private Set<String> compliantValues;

    @Autowired
    private ItemService itemService;
    
    @Required
    public void setMetadata(String metadata){
        this.metadatumHelper= metadata;

    }
    public String getMetadata(){
        return metadatumHelper.toString();
    }

    @Required
    public void setCompliantField(String compliantField){
        this.compliantField=compliantField;
    }

    public String getCompliantField(){
        return compliantField;
    }

    @Required
    public void setCompliantValues(Set<String> compliantValues){
        this.compliantValues=compliantValues;
    }

    public Set<String> getCompliantValues(){
        return compliantValues;
    }


    public void checkAndUpdateCompliance(Context context, Item item) throws SQLException{

        if(processingRequired(item) && compliantValuesApply(item)){

            if(itemService.getMetadataByMetadataString(item, metadatumHelper).size()==0){
                addProvenanceInformation(context, item);
                itemService.addMetadata(context, item, MetadataFieldString.getSchema(metadatumHelper), MetadataFieldString.getElement(metadatumHelper), MetadataFieldString.getQualifier(metadatumHelper), null, complianceMetadataValue(item));
            }

        }
    }

    private boolean processingRequired(Item item) {
        String metadata = itemService.getMetadata(item, metadatumHelper);
        return StringUtils.isBlank(metadata);
    }

    private void addProvenanceInformation(Context context, Item item) throws SQLException {
        String now = DCDate.getCurrent().toString();
        EPerson currentUser = context.getCurrentUser();
        String submitter = currentUser.getFullName();

        submitter = submitter + "(" + currentUser.getEmail() + ")";
        String provDescription = "Item updated for first compliance. Info stored in metadatafield: \""+ metadatumHelper.toString()+"\" by "
                + submitter + " on " + now + " (GMT) ";
        itemService.addMetadata(context, item, MetadataSchema.DC_SCHEMA, "description", "provenance", "en", provDescription);
    }

    protected boolean compliantValuesApply(Item item) {
        List<MetadataValue> metadata = itemService.getMetadataByMetadataString(item, getCompliantField());
        for(MetadataValue dcValue :metadata){
            if(getCompliantValues().contains(dcValue.getValue())){
                return true;
            }
        }
        return false;
    }

    protected abstract String complianceMetadataValue(Item item);
	public ItemService getItemService() {
		return itemService;
	}
	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

}
