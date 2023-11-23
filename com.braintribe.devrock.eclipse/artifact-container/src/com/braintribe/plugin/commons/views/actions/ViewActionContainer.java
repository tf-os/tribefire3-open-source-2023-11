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
package com.braintribe.plugin.commons.views.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Display;

import com.braintribe.plugin.commons.views.tabbed.ActiveTabProvider;
import com.braintribe.plugin.commons.views.tabbed.TabProvider;
import com.braintribe.plugin.commons.views.tabbed.tabs.AbstractViewTab;

/**
 * a container for the actions that are linked to a certain tab of a tabbed view
 *  
 * @author pit
 *
 * @param <T> - something derived from the {@link AbstractViewTab}
 */
public interface ViewActionContainer<T extends AbstractViewTab> {
	/**
	 * the display the UI thread will need to run in
	 * @param display - the {@link Display}
	 */
	void setDisplay( Display display);
	/**
	 * representing the toolbar of the view 
	 * @param toolbarManager - {@link IToolBarManager}
	 */
	void setToolbarManager( IToolBarManager toolbarManager);
	/**
	 * representing the menu of the view 
	 * @param menuManager - the {@link IMenuManager}
	 */
	void setMenuManager( IMenuManager menuManager);
	/**
	 * provides the currently selected tab 
	 * @param provider - {@link ActiveTabProvider} (most commonly the view itself) 
	 */
	void setSelectionProvider( ActiveTabProvider<T> provider);
	/**
	 * provides ALL tabs of the view 
	 * @param provider - the {@link TabProvider} (most commonly the view itself)
	 */
	void setTabProvider( TabProvider<T> provider);
	/**
	 * creates the actions, incorporates them into the toolbar and menu, and returns
	 * the controller (which controls availability)
	 * @return - {@link ViewActionController}
	 */
	ViewActionController<T> create();
}
