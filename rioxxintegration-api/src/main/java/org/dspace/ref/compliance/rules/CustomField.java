package org.dspace.ref.compliance.rules;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactoryImpl;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.Group;

import com.atmire.utils.EmbargoUtils;

/**
 * TODO TOM UNIT TEST
 */
public enum CustomField {

	BITSTREAM_COUNT("bitstream.count") {
		public List<String> createValueList(final Context context, final Item item) throws SQLException {
			List<String> output = new LinkedList<String>();
			for (Bitstream bitstream : itemService.getNonInternalBitstreams(context, item)) {
				java.util.List<ResourcePolicy> policiesByDSOAndType = authorizeService.getPoliciesActionFilter(context,
						bitstream, Constants.READ);

				for (ResourcePolicy pol : policiesByDSOAndType) {
					// We are only interested in bitstreams that have a READ policy for Anonymous
					if (pol.getGroup().getName() == Group.ANONYMOUS) {
						String value = new String();
						value = bitstream.getName();
						output.add(value);
					}
				}
			}

			return output;
		}
	},
	BITSTREAM_EMBARGO_ENABLED("bitstream.embargo.enabled") {
		public List<String> createValueList(final Context context, final Item item) throws SQLException {
			List<String> output = new LinkedList<String>();
			String value = new String();
			if (EmbargoUtils.getLastEmbargo(item, context) == null) {
				value = "false";
			} else {
				value = "true";
			}
			output.add(value);
			return output;
		}
	},
	BITSTREAM_EMBARGO_ENDDATE("bitstream.embargo.enddate") {
		public List<String> createValueList(final Context context, final Item item) throws SQLException {
			List<String> output = new LinkedList<String>();
			Date embargo = EmbargoUtils.getLastEmbargo(item, context);
			if (embargo != null) {
				String value = AbstractComplianceRule.getDateTimePrinter().print(embargo.getTime());
				output.add(value);
			}
			return output;
		}
	},
	ITEM_LIFECYCLE_STATUS("item.status") {
		public List<String> createValueList(final Context context, final Item item) throws SQLException {
			List<String> output = new LinkedList<String>();

			String value = "";
			if (item.isArchived()) {
				value = "archived";
			} else if (item.isWithdrawn()) {
				value = "withdrawn";
			} else if (ContentServiceFactoryImpl.getInstance().getWorkspaceItemService().findByItem(context,
					item) != null) {
				value = "workspace";
			} else {
				value = "workflow";
			}

			output.add(value);
			return output;
		}
	};

	private final String fieldName;

	private final static ItemService itemService = ContentServiceFactoryImpl.getInstance().getItemService();

	private final static AuthorizeService authorizeService = AuthorizeServiceFactory.getInstance()
			.getAuthorizeService();

	CustomField(final String field) {
		this.fieldName = field;
	}

	public String getFieldName() {
		return fieldName;
	}

	public abstract List<String> createValueList(final Context context, final Item item) throws SQLException;

	public static CustomField findByField(final String field) {
		CustomField result = null;

		for (CustomField customField : CustomField.values()) {
			if (StringUtils.equals(customField.getFieldName(), field)) {
				result = customField;
				break;
			}
		}

		return result;
	}

}
