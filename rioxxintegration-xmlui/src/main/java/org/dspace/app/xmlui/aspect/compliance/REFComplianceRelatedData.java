/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.compliance;

import static org.dspace.app.xmlui.wing.AbstractWingTransformer.message;

import java.util.Date;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.Cell;
import org.dspace.app.xmlui.wing.element.Division;
import org.dspace.app.xmlui.wing.element.Para;
import org.dspace.app.xmlui.wing.element.Row;
import org.dspace.app.xmlui.wing.element.Table;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.ref.compliance.result.ComplianceResult;

import com.atmire.utils.EmbargoUtils;

/**
 * @author philip at atmire.com
 */
public class REFComplianceRelatedData implements ComplianceRelatedData {

    protected static final Message T_related_data_title =
            message("xmlui.compliance.ComplianceUI.ref_related_data_title");

    protected static final Message T_related_field_base =
            message("xmlui.compliance.ComplianceUI.ref_related_field_");

    protected static final Message T_related_field_embargo_enddate =
            message("xmlui.compliance.ComplianceUI.ref_related_field_embargo_enddate");

    protected static final Message T_estimated_hint =
            message("xmlui.compliance.ComplianceUI.estimated_hint");

    protected static final Message T_estimated_help_info =
            message("xmlui.compliance.ComplianceUI.estimated_help_info");

    private final String fullIso = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    protected ItemService itemService;
    
    @Override
    public void renderRelatedData(Context context, org.dspace.content.Item item, ComplianceResult result, Division div) throws WingException {
            Division relatedDataDiv = div.addDivision("related-data-section", "related-data-section");
            Para para = relatedDataDiv.addPara("related-data-title", "related-data-title");
            para.addContent(T_related_data_title);
            Table table = relatedDataDiv.addTable("related-data-table", 5, 2, "related-data-table");
            Row header = table.addRow(Row.ROLE_HEADER);
            header.addCellContent("Field");
            header.addCellContent("Value");

            addRelatedDataFields(table, item, result);
            addEmbargoEndData(table, item, context);
            addEstimatedHelpInfo(relatedDataDiv, result);
    }

    private void addRelatedDataFields(Table table, org.dspace.content.Item item, ComplianceResult result) throws WingException {
        int counter = 1;
        String field;
        while ((field = ConfigurationManager.getProperty("item-compliance","related.data.field." + counter)) != null) {
            counter++;
            Row row = table.addRow();
            row.addCellContent(message(T_related_field_base.getKey() + field));
            Cell cell = row.addCell();

            String metadata = getItemService().getMetadata(item, field);

            if(StringUtils.isNotBlank(metadata)){
                cell.addContent(metadata);
            } else if(result.getEstimatedValues().get(field) != null && StringUtils.isNotBlank(result.getEstimatedValues().get(field))) {
                cell.addContent(result.getEstimatedValues().get(field) + " ");
                cell.addContent(T_estimated_hint);
            }
        }
    }

    private void addEmbargoEndData(Table table, org.dspace.content.Item item, Context context) throws WingException {
        Row row = table.addRow();
        row.addCellContent(T_related_field_embargo_enddate);
        Cell cell = row.addCell();

        Date lastEmbargoDate = EmbargoUtils.getLastEmbargo(item, context);

        if(lastEmbargoDate!=null){
            cell.addContent(DateFormatUtils.format(lastEmbargoDate,fullIso));
        }
    }

    private void addEstimatedHelpInfo(Division div, ComplianceResult result) throws WingException {
        if(MapUtils.isNotEmpty(result.getEstimatedValues())) {
            Para estimatedHelpPara = div.addPara("", "estimated-help-paragraph");
            estimatedHelpPara.addContent(T_estimated_help_info);
        }
    }

	public ItemService getItemService() {
		if(itemService == null) {
			itemService = ContentServiceFactory.getInstance().getItemService();
		}
		return itemService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
}
