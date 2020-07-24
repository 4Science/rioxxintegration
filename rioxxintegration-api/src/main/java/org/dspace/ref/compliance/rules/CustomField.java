/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.ref.compliance.rules;

import com.atmire.utils.EmbargoUtils;
import org.apache.commons.lang3.StringUtils;
import org.dspace.authorize.AuthorizeManager;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.content.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO TOM UNIT TEST
 */
public enum CustomField {

    BITSTREAM_COUNT("bitstream.count") {
        public List<Metadatum> createValueList(final Context context, final Item item) throws SQLException {
            List<Metadatum> output = new LinkedList<Metadatum>();
            for (Bitstream bitstream : item.getNonInternalBitstreams()) {
                java.util.List<ResourcePolicy> policiesByDSOAndType = AuthorizeManager.getPoliciesActionFilter(context, bitstream, Constants.READ);

                for (ResourcePolicy pol : policiesByDSOAndType) {
                    //We are only interested in bitstreams that have a READ policy for Anonymous
                    if (pol.getGroupID() == EmbargoUtils.ANONYMOUS_GROUP_ID) {
                Metadatum value = new Metadatum();
                value.value = bitstream.getName();
                output.add(value);
            }
                }
            }

            return output;
        }
    },
    BITSTREAM_EMBARGO_ENABLED("bitstream.embargo.enabled") {
        public List<Metadatum> createValueList(final Context context, final Item item) throws SQLException {
            List<Metadatum> output = new LinkedList<Metadatum>();
            Metadatum value = new Metadatum();
            if (EmbargoUtils.getLastEmbargo(item, context) == null) {
                value.value = "false";
            } else {
                value.value = "true";
            }
            output.add(value);
            return output;
        }
    },
    BITSTREAM_EMBARGO_ENDDATE("bitstream.embargo.enddate") {
        public List<Metadatum> createValueList(final Context context, final Item item) throws SQLException {
            List<Metadatum> output = new LinkedList<Metadatum>();
            Date embargo = EmbargoUtils.getLastEmbargo(item, context);
            if(embargo != null) {
                Metadatum value = new Metadatum();
                value.value = AbstractComplianceRule.getDateTimePrinter().print(embargo.getTime());
                output.add(value);
            }
            return output;
        }
    },
    ITEM_LIFECYCLE_STATUS("item.status") {
        public List<Metadatum> createValueList(final Context context, final Item item) throws SQLException {
            List<Metadatum> output = new LinkedList<Metadatum>();

            Metadatum value = new Metadatum();
            if(item.isArchived()) {
                value.value = "archived";
            } else if(item.isWithdrawn()) {
                value.value = "withdrawn";
            } else if(WorkspaceItem.findByItem(context, item) != null) {
                value.value = "workspace";
            } else {
                value.value = "workflow";
            }

            output.add(value);
            return output;
        }
    };

    private final String fieldName;

    CustomField(final String field) {
        this.fieldName = field;
    }

    public String getFieldName() {
        return fieldName;
    }

    public abstract List<Metadatum> createValueList(final Context context, final Item item) throws SQLException;

    public static CustomField findByField(final String field) {
        CustomField result = null;

        for (CustomField customField : CustomField.values()) {
            if(StringUtils.equals(customField.getFieldName(), field)) {
                result = customField;
                break;
            }
        }

        return result;
    }

}
