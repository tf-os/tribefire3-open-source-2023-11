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
package com.braintribe.model.uicommand;

import com.braintribe.model.command.Command;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Command that reloads the current view displayed in GME.
 * @author michel.docouto
 */
public interface ReloadView extends Command {
	
	final EntityType<ReloadView> T = EntityTypes.T(ReloadView.class);
	
	/**
	 * If true, then we will reload not only the current views, but all views (when they first are activated).
	 */
	void setReloadAll(boolean reloadAll);
	
	/**
	 * If true, then we will reload not only the current views, but all views (when they first are activated).
	 */
	boolean getReloadAll();

}
