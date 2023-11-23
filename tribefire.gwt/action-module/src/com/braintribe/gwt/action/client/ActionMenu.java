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
package com.braintribe.gwt.action.client;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.gwt.ioc.client.Configurable;

/**
 * The ActionMenu is a action that has inner actions.
 * Normally, this actions normally don't perform anything, and are used as Button with a menu,
 * and this menu has the inner actions.
 * @author dirk.scheffler
 *
 */
public class ActionMenu extends Action {

	private boolean syncEnabledAndHiddenWithActions = true;
	private List<Action> actions = new ArrayList<Action>();

	@Configurable
	public void addAction(Action action) {
		actions.add(action);
		if(syncEnabledAndHiddenWithActions)
		{
			action.addPropertyListener(new PropertyListener() {

				@Override
				public void propertyChanged(ActionOrGroup source, String property) {
					if(Action.PROPERTY_ENABLED.equals(property))
							syncEnablement();		
					if(Action.PROPERTY_HIDDEN.equals(property))
							syncHiddenStatus();
				}
			});
			
			syncEnablement();
			syncHiddenStatus();
		}
	}
	
	/**
	 * Iterates through all actions and disables the ActionMenu when no action is enabled
	 */
	protected void syncEnablement()
	{
		boolean groupEnabled = false;

		for(Action action : getActions())
		{
			if(action.getEnabled())
				groupEnabled=true;

			if(groupEnabled)
				break;
		}
		setEnabled(groupEnabled);	
	}

	/**
	 * Iterates through all actions and hides the ActionMenu when all actions are hidden
	 */
	protected void syncHiddenStatus()
	{
		boolean groupHidden = true;
		for(Action action : getActions())
		{
			if(!action.getHidden())
				groupHidden=false;

			if(!groupHidden)
				break;
		}
		setHidden(groupHidden);
	}

	public List<Action> getActions() {
		return actions;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		//NOP
	}
	
	/**
	 * When this is true, the ActionMenu monitors enabled- and hidden-property-changes of its actions and sets its own status accordingly
	 * Defaults to true
	 */
	public void setSyncEnabledAndHiddenWithActions(boolean syncEnabledAndHiddenWithActions) {
		this.syncEnabledAndHiddenWithActions = syncEnabledAndHiddenWithActions;
	}
}
