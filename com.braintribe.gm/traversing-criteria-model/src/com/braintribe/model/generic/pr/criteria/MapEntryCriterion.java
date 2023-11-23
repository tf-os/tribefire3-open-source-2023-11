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
package com.braintribe.model.generic.pr.criteria;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Criterion for map entry. The typeSignature is exactly the same as for {@link MapCriterion}. After this criterion is pushed on the stack,
 * the entry key/value are pushed as well, so this is used if someone wants to access the entire entry at once, but if the key/value may be
 * processed separately, one may ignore this and check {@link MapKeyCriterion} and/or {@link MapValueCriterion}.
 */

public interface MapEntryCriterion extends BasicCriterion {

	final EntityType<MapEntryCriterion> T = EntityTypes.T(MapEntryCriterion.class);

	@Override
	default CriterionType criterionType() {
		return CriterionType.MAP_ENTRY;
	}

}
