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

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.smart.meta.ConvertibleQualifiedProperty;
import com.braintribe.model.accessdeployment.smart.meta.InverseKeyPropertyAssignment;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.QualifiedProperty;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;

/**
 * 
 */

public class InverseKeyPropertyAssignmentWrapper implements DqjDescriptor {

	private final List<String> joinedEntityDelegatePropertyNames = newList();
	private final Map<String, String> map = newMap();
	private final Map<String, ConversionWrapper> conversionMap = newMap();
	private final boolean forceExternalJoin;

	public InverseKeyPropertyAssignmentWrapper(GmEntityType smartOwnerType, IncrementalAccess ownerAccess,
			Collection<InverseKeyPropertyAssignment> ikpas, ModelExpert modelExpert) {

		boolean force = false;
		for (InverseKeyPropertyAssignment ikpa : ikpas) {
			force |= ikpa.getForceExternalJoin();

			QualifiedProperty qualifiedKeyProperty = ikpa.getKeyProperty();
			ConvertibleQualifiedProperty property = ikpa.getProperty();

			String ownerProperty = qualifiedKeyProperty.getProperty().getName();
			String joinedProperty = property.getProperty().getName();

			// FYI: We only do this "ensuring we have delegate property" if IKPA (no KPA). Makes sense, think about it.
			ownerProperty = modelExpert.findDelegatePropertyForKeyProperty(qualifiedKeyProperty.propertyOwner(), ownerAccess, ownerProperty,
					smartOwnerType);

			joinedEntityDelegatePropertyNames.add(joinedProperty);
			map.put(joinedProperty, ownerProperty);

			ConversionWrapper cw = ConversionWrapper.instanceFor(property.getConversion());
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
		return ConversionWrapper.inverseOf(conversionMap.get(joinedEntityDelegatePropertyName));
	}

	@Override
	public ConversionWrapper getJoinedEntityPropertyConversion(String joinedEntityDelegatePropertyName) {
		return conversionMap.get(joinedEntityDelegatePropertyName);
	}

	@Override
	public boolean getForceExternalJoin() {
		return forceExternalJoin;
	}

}
