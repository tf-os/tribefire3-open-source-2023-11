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
package com.braintribe.model.processing.smart.query.planner.structure.adapter;

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.braintribe.model.accessdeployment.smart.meta.KeyPropertyAssignment;

/**
 *  
 */
public class KeyPropertyAssignmentWrapper implements DqjDescriptor {

	private final List<String> joinedEntityDelegatePropertyNames = newList();
	private final Map<String, String> map = newMap();
	private final Map<String, ConversionWrapper> conversionMap = newMap();
	private final boolean forceExternalJoin;

	public KeyPropertyAssignmentWrapper(Collection<KeyPropertyAssignment> kpas) {
		boolean force = false;
		for (KeyPropertyAssignment kpa: kpas) {
			force |= kpa.getForceExternalJoin();

			String joinedProperty = kpa.getKeyProperty().getProperty().getName();
			String ownerProperty = kpa.getProperty().getProperty().getName();

			joinedEntityDelegatePropertyNames.add(joinedProperty);
			map.put(joinedProperty, ownerProperty);

			ConversionWrapper cw = ConversionWrapper.instanceFor(kpa.getProperty().getConversion());
			if (cw != null) {
				conversionMap.put(joinedProperty, cw);
			}
		}

		this.forceExternalJoin = force;
	}

	@Override
	public List<String> getJoinedEntityDelegatePropertyNames() {
		return joinedEntityDelegatePropertyNames;
	}

	@Override
	public String getRelationOwnerDelegatePropertyName(String joinedEntityDelegatePropertyName) {
		return map.get(joinedEntityDelegatePropertyName);
	}

	@Override
	public ConversionWrapper getRelationOwnerPropertyConversion(String joinedEntityDelegatePropertyName) {
		return conversionMap.get(joinedEntityDelegatePropertyName);
	}

	@Override
	public ConversionWrapper getJoinedEntityPropertyConversion(String joinedEntityDelegatePropertyName) {
		return ConversionWrapper.inverseOf(conversionMap.get(joinedEntityDelegatePropertyName));
	}

	@Override
	public boolean getForceExternalJoin() {
		return forceExternalJoin;
	}
	
}
