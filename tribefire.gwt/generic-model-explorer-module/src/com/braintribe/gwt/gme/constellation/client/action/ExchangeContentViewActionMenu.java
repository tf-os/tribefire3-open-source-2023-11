// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.gwt.gme.constellation.client.action;

import java.util.Arrays;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.ActionMenu;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;

public class ExchangeContentViewActionMenu extends ActionMenu{
	
	private int currentActionIndex = 0;
	
	public ExchangeContentViewActionMenu() {
		setHidden(false);
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		super.perform(triggerInfo);
		Action action = getActions().get(currentActionIndex);
		if (action != null)
			action.perform(triggerInfo);
	}
	
	public void raiseCurrentActionIndex(){
		currentActionIndex = ((currentActionIndex + 1) < getActions().size() ? currentActionIndex + 1 : 0);		
	}
	
	public void setCurrentAction(Action action) {
		if (action != null) {
			setIcon(action.getIcon());
			setHoverIcon(action.getHoverIcon());
			setName(action.getName());
		}
	}

}
