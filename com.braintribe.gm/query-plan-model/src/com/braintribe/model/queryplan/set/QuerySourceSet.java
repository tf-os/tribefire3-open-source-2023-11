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
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.conditions.Condition;

/**
 * Source of entities, which are retrieved using an entity query.
 * <p>
 * This entity is only used by the planner if such retrieving is supported by the repository, which is the case for accesses which might delegate to
 * other systems, like e.g. CspAccess (that was the primary motivation to introduce this entity). This is merely an optimization for what would be a
 * composition of a {@link FilteredSet} and a regular {@link SourceSet}.
 */

public interface QuerySourceSet extends ReferenceableTupleSet {

	EntityType<QuerySourceSet> T = EntityTypes.T(QuerySourceSet.class);

	String getEntityTypeSignature();
	void setEntityTypeSignature(String entityTypeSignature);

	Condition getCondition();
	void setCondition(Condition condition);

	Ordering getOrdering();
	void setOrdering(Ordering ordering);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.querySourceSet;
	}

}
