package org.dspace.app.xmlui.aspect.compliance;

import com.atmire.utils.*;
import java.util.*;
import org.apache.commons.collections.*;
import org.apache.commons.lang.*;
import org.apache.commons.lang3.time.*;
import static org.dspace.app.xmlui.wing.AbstractWingTransformer.*;
import org.dspace.app.xmlui.wing.*;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.content.Metadatum;
import org.dspace.core.*;
import org.dspace.ref.compliance.result.*;

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

            Metadatum[] metadata = item.getMetadataByMetadataString(field);

            if(metadata.length>0 && StringUtils.isNotBlank(metadata[0].value)){
                cell.addContent(metadata[0].value);
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
}
