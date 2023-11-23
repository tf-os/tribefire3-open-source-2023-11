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
package com.braintribe.model.queryplan.set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A source set is what you get directly from the source, i.e. it's a one-dimensional tuple set and therefore has a type signature as it represents a
 * population. In other words, source set represents an entire population for given entity type.
 */
public interface SourceSet extends ReferenceableTupleSet {

	EntityType<SourceSet> T = EntityTypes.T(SourceSet.class);

	String getTypeSignature();
	void setTypeSignature(String typeSignature);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.sourceSet;
	}

}
