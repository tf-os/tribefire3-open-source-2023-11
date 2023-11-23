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

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.PersistentEntityReference;

/**
 * One dimensional set of directly state values;
 * 
 * This is used when the source entities are fully known up-front, e.g. when lazy-loading a property, the query might look like
 * <code>select e.prop from Entity e where e = ref</code>.
 * <p>
 * If we ever go more complex, this could be used for nested queries, e.g.: <code>... where x in (select...) ...</code>.
 */
public interface StaticSet extends ReferenceableTupleSet {

	EntityType<StaticSet> T = EntityTypes.T(StaticSet.class);

	/**
	 * Static values each representing a single position tuple.
	 * <p>
	 * These are not the final values, but they are resolved using query evaluation context. For example, these can be
	 * {@link PersistentEntityReference}s, which are resolved to the actual entities.
	 * <p>
	 * Note that values which resolve to <tt>null</tt> are ignored, as if they were not part of this set.
	 */
	Set<Object> getValues();
	void setValues(Set<Object> values);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.staticSet;
	}

}
