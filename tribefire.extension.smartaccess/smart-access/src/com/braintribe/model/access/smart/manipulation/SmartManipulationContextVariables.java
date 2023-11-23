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
package com.braintribe.model.access.smart.manipulation;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityMapping;
import com.braintribe.model.processing.smart.query.planner.structure.adapter.EntityPropertyMapping;

/**
 * 
 * @author peter.gazdik
 */
public class SmartManipulationContextVariables {

	// context variables
	public EntityProperty currentSmartOwner;
	public EntityProperty currentDelegateOwner; // only for induced manipulations

	public EntityReference currentSmartReference;
	public EntityReference currentDelegateReference;
	public GmEntityType currentSmartType;
	public EntityMapping currentEntityMapping;
	public IncrementalAccess currentAccess;

	// this whole block only for direct (not-induced) manipulations
	public GmProperty currentSmartGmProperty; // only if property manipulation
	public GmEntityType currentSmartReferencedEntityType; // only if property is of EntityType or a collection referencing entities
	public boolean currentSmartPropertyReferencesUnmappedType;

	public EntityPropertyMapping currentEpm; // only for induced manipulations

	public final Map<IncrementalAccess, List<Manipulation>> delegateManipulations = newMap();
	public final Map<IncrementalAccess, ManipulationResponse> delegateResponses = newMap();

	public final List<Manipulation> smartInducedManipulations = newList();

}
