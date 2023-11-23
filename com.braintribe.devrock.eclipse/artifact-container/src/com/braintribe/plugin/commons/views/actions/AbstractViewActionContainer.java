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

import com.braintribe.plugin.commons.views.tabbed.tabs.AbstractViewTab;

/**
 * an abstract implementation of the basic features of a {@link ViewActionContainer}
 * 
 * @author pit
 *
 * @param <T> - a sub type of {@link AbstractViewTab}
 */
public abstract class AbstractViewActionContainer<T extends AbstractViewTab> implements ViewActionContainer<T> {
	
	protected Display display;
	protected IToolBarManager toolbarManager;
	protected IMenuManager menuManager;
	
	
	@Override
	public void setDisplay(Display display) {
		this.display = display;
		
	}

	@Override
	public void setToolbarManager(IToolBarManager toolbarManager) {
		this.toolbarManager = toolbarManager;
		
	}

	@Override
	public void setMenuManager(IMenuManager menuManager) {
		this.menuManager = menuManager;
		
	}


}
