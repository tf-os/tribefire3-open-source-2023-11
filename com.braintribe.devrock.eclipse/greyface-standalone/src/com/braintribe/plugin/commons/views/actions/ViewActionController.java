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

import com.braintribe.devrock.greyface.view.tab.GenericViewTab;

/**
 * controls the availability of the actions depending of the currently selected tab
 * @author pit
 *
 * @param <T> - a sub type of {@link GenericViewTab}
 */
public interface ViewActionController<T extends GenericViewTab> {
	/**
	 * enables or disables the action in question according the selected tab  
	 * @param tab - a instance deriving from {@link GenericViewTab}
	 */
	void controlAvailablity(T tab);
}
