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
package com.braintribe.gwt.gme.constellation.client;

import java.util.List;

import com.braintribe.gwt.action.adapter.gxt.client.MenuItemActionAdapter;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.gme.constellation.client.action.HasSubMenu;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class SettingsMenu extends Menu implements InitializableBean {
	
	private List<Action> menuActions;
	
	/**
	 * Configures the required list of actions for this menu.
	 */
	@Required
	public void setMenuActions(List<Action> menuActions) {
		this.menuActions = menuActions;
	}
	
	public List<Action> getMenuActions() {
		return menuActions;
	}
	
	@Override
	public void intializeBean() throws Exception {
		initializeActions();
	}
	
	public void initializeActions() {
		for (final Action menuAction : menuActions) {
			MenuItem menuItem;
			if (!(menuAction instanceof HasSubMenu))
				menuItem = new MenuItem();
			else {
				if (menuAction.getIcon() != null)
					menuItem = new MenuItem(menuAction.getName(), menuAction.getIcon());
				else
					menuItem = new MenuItem(menuAction.getName());
				
				menuItem.addSelectionHandler(event -> menuAction.perform(null));
				menuItem.setSubMenu(((HasSubMenu) menuAction).getSubMenu());
			}
			
			MenuItemActionAdapter.linkActionToMenuItem(menuAction, menuItem);
			add(menuItem);
		}
	}
}
