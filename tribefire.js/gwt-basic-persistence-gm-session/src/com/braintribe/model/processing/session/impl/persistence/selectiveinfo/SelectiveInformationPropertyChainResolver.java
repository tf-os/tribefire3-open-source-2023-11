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
package com.braintribe.model.processing.session.impl.persistence.selectiveinfo;

import static com.braintribe.model.processing.core.commons.SelectiveInformationSupport.buildTcFor;
import static com.braintribe.model.processing.session.impl.persistence.selectiveinfo.SelectiveInformationLoadingTools.convertToPropertyChains;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Helper class which resolved a list of {@link Property} chains needed for the selective information (SI) of given type, together with the
 * {@link TraversingCriterion} (TC) for those entities where at least one property is needed for the SI. This TC is of course used when
 * querying the entity.
 * 
 * @author peter.gazdik
 */
class SelectiveInformationPropertyChainResolver {

	private final PersistenceGmSession session;
	private final Map<EntityType<?>, List<Property[]>> propertyPathsForEntityType = newMap();
	private final Map<EntityType<?>, TraversingCriterion> tcForEntityType = newMap();

	public SelectiveInformationPropertyChainResolver(PersistenceGmSession session) {
		this.session = session;
	}

	/**
	 * Returns property path needed for
	 */
	public List<Property[]> getPropertyChain(EntityType<?> et) {
		List<Property[]> result = propertyPathsForEntityType.get(et);
		if (result == null) {
			List<String[]> propertyNameChains = SelectiveInformationLoadingTools. resolveSiPropertyNameChainsFor(et, session);

			result = convertToPropertyChains(et, propertyNameChains);
			propertyPathsForEntityType.put(et, result);

			if (!result.isEmpty()) {
				TraversingCriterion resultTc = buildTcFor(propertyNameChains);
				tcForEntityType.put(et, resultTc);
			}
		}
		return result;
	}

	public TraversingCriterion getTc(EntityType<?> et) {
		return tcForEntityType.get(et);
	}

}
