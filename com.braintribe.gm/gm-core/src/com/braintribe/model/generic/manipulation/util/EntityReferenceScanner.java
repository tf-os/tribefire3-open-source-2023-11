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
package com.braintribe.model.generic.manipulation.util;

import java.util.Set;
import java.util.function.Function;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.tools.AssemblyTools;
import com.braintribe.model.generic.value.EntityReference;


public class EntityReferenceScanner implements Function<Manipulation, Set<EntityReference>> {

	/**
	 * @deprecated use {@link #findEntityReferences(Manipulation)}
	 */
	@Override
	@Deprecated
	public Set<EntityReference> apply(Manipulation manipulation) throws RuntimeException {
		return EntityReferenceScanner.findEntityReferences(manipulation);
	}

	public static Set<EntityReference> findEntityReferences(Manipulation manipulation) {
		Set<EntityReference> references = AssemblyTools.findAll(manipulation, EntityReference.T, t -> true);
		
		Set<EntityReference> result = CodingSet.create(EntRefHashingComparator.INSTANCE);
		result.addAll(references);
		return result;
	}

}
