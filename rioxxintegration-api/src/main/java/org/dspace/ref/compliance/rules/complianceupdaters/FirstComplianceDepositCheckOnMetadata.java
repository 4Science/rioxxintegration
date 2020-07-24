/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules.complianceupdaters;

import com.atmire.utils.Metadatum;
import org.apache.commons.lang3.StringUtils;
import org.dspace.content.DCDate;
import org.dspace.content.Item;
import org.dspace.content.MetadataSchema;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Required;

import java.util.Set;

/**
 * Created by jonas - jonas@atmire.com on 23/03/16.
 */
public abstract class FirstComplianceDepositCheckOnMetadata implements ComplianceDepositCheck {

    private Metadatum metadatumHelper;
    private String compliantField;
    private Set<String> compliantValues;

    @Required
    public void setMetadata(String metadata){
        this.metadatumHelper=new Metadatum(metadata);

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


    public void checkAndUpdateCompliance(Context context, Item item){

        if(processingRequired(item) && compliantValuesApply(item)){

            if(item.getMetadataByMetadataString(metadatumHelper.toString()).length==0){
                addProvenanceInformation(context, item);
                item.addMetadata(metadatumHelper.getSchema(), metadatumHelper.getElement(), metadatumHelper.getQualifier(), null, complianceMetadataValue(item));
            }

        }
    }

    private boolean processingRequired(Item item) {
        org.dspace.content.Metadatum[] metadata = item.getMetadataByMetadataString(metadatumHelper.toString());
        return metadata == null || metadata.length==0 || (metadata != null && metadata.length>0 && StringUtils.isBlank(metadata[0].value));
    }

    private void addProvenanceInformation(Context context, Item item) {
        String now = DCDate.getCurrent().toString();
        EPerson currentUser = context.getCurrentUser();
        String submitter = currentUser.getFullName();

        submitter = submitter + "(" + currentUser.getEmail() + ")";
        String provDescription = "Item updated for first compliance. Info stored in metadatafield: \""+ metadatumHelper.toString()+"\" by "
                + submitter + " on " + now + " (GMT) ";
        item.addMetadata(MetadataSchema.DC_SCHEMA, "description", "provenance", "en", provDescription);
    }

    protected boolean compliantValuesApply(Item item) {
        org.dspace.content.Metadatum[] metadata = item.getMetadataByMetadataString(getCompliantField());
        for(org.dspace.content.Metadatum dcValue :metadata){
            if(getCompliantValues().contains(dcValue.value)){
                return true;
            }
        }
        return false;
    }

    protected abstract String complianceMetadataValue(Item item);

}
