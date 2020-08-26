package org.dspace.app.xmlui.aspect.project;

import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.List;
import org.dspace.app.xmlui.wing.element.Text;
import org.dspace.content.authority.factory.ContentAuthorityServiceFactory;
import org.dspace.content.authority.service.ChoiceAuthorityService;
import org.dspace.content.authority.service.MetadataAuthorityService;

/**
 * Created by jonas - jonas@atmire.com on 03/10/16.
 */
public class ProjectFieldRenderer {

    protected ChoiceAuthorityService choiceAuthorityService = ContentAuthorityServiceFactory.getInstance().getChoiceAuthorityService();
    protected MetadataAuthorityService metadataAuthorityService = ContentAuthorityServiceFactory.getInstance().getMetadataAuthorityService();

    public Text generalisedOneBoxFieldRender(List form, String fieldName, boolean readOnly, boolean required, Message label, Message hint) throws WingException {
        org.dspace.app.xmlui.wing.element.Item formItem = form.addItem();
        Text text = formItem.addText(fieldName, "submit-text");

        // Setup the select field
        text.setLabel(label);
        if(hint!=null) {
            text.setHelp(hint);
        }
        String fieldKey = metadataAuthorityService.makeFieldKey("rioxxterms", "identifier", "project");
        text.setAuthorityControlled();
        text.setAuthorityRequired(metadataAuthorityService.isAuthorityRequired(fieldKey));

        if (choiceAuthorityService.isChoicesConfigured(fieldKey)) {
            text.setChoices(fieldKey);
            text.setChoicesPresentation(choiceAuthorityService.getPresentation(fieldKey));
            text.setChoicesClosed(choiceAuthorityService.isClosed(fieldKey));
        }

        if (readOnly) {
            text.setDisabled();
        }

        if (required) {
            text.setRequired();
        }
        return text;
    }
}
