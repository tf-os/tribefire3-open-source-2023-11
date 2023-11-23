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
package com.braintribe.devrock.greyface.views.dependency.tabs.capability;

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;
import com.braintribe.plugin.commons.views.actions.AbstractViewActionContainer;
import com.braintribe.plugin.commons.views.actions.ViewActionContainer;
import com.braintribe.plugin.commons.views.tabbed.ActiveTabProvider;
import com.braintribe.plugin.commons.views.tabbed.TabProvider;

/**
 * an implementation of the basic {@link ViewActionContainer} for the dependency view's tabs
 * @author pit
 *
 */
public abstract class AbstractDependencyViewActionContainer extends AbstractViewActionContainer<GenericViewTab> implements ViewActionContainer<GenericViewTab> {
	
	protected ActiveTabProvider<GenericViewTab> activeTabProvider;
	protected TabProvider<GenericViewTab> tabProvider;
	
	@Override
	public void setSelectionProvider(ActiveTabProvider<GenericViewTab> provider) {
		this.activeTabProvider = provider;
	}

	@Override
	public void setTabProvider(TabProvider<GenericViewTab> provider) {
		this.tabProvider = provider;
	}

	
	


}
