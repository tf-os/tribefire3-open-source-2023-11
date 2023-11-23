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
package com.braintribe.gwt.action.adapter.gxt.client;

import com.braintribe.gwt.action.client.Action;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

/**
 * This class creates a Menu that accepts actions as items
 * @author michel.docouto
 *
 */
public class ActionContextMenu extends Menu {
	
	public void addActions(Iterable<Action> actions) {
		for (Action action: actions) {
			addActionToMenu(action);
		}
	}
	
	public void addActionToMenu(Action action) {
		MenuItem item = new MenuItem();
		MenuItemActionAdapter.linkActionToMenuItem(action, item);
		this.add(item);
	}

}
