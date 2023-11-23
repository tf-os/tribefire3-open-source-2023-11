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
package com.braintribe.gwt.gmview.client;

import com.braintribe.gwt.gmview.actionbar.client.GmViewActionBar;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.prompt.CondensationMode;


public interface GmCondensationView extends GmSelectionSupport, GmSessionHandler, UseCaseHandler {
	
	/**
	 * Checks if local condensation and uncondensation is enabled
	 */
	boolean isLocalCondensationEnabled();
	
	/**
	 * Checks if the last selected entry may be uncondensed.
	 */
	boolean checkUncondenseLocalEnablement();
	
	/**
	 * Gets the condensed property of the last selected entry.
	 */
	String getCondensendProperty();
	
	String getCurrentCondensedProperty(EntityType<?> entityType);
	
	void uncondenseLocal();
	
	void condenseLocal();
	
	void condense(String propertyName, CondensationMode condensationMode, EntityType<?> entityType);
	
	GmViewActionBar getGmViewActionBar();
	
	boolean isUseCondensationActions();
	
	EntityType<GenericEntity> getEntityTypeForProperties();

}
