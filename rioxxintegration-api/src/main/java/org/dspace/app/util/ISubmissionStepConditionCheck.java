package org.dspace.app.util;

import org.dspace.content.Item;

public interface ISubmissionStepConditionCheck {

	public boolean allConditionsMet(Item item, String configName);
	
}
