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
package com.braintribe.model.queryplan.value;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


import com.braintribe.model.queryplan.TupleComponentPosition;

/**
 * A {@linkplain TupleComponent} represents a single component inside a tuple set.
 * <p>
 * It is similar to a column in SQL, but the difference between this model and hibernate (HQL).When doing a select query in hibernate, you
 * get one value for each property of an entity. In that sense you could have many tuple values for one entity. In our case, on the other
 * hand, the entire entity would be just one tuple value. In general, a tuple component may be anything modeled by a sub-type of
 * {@link TupleComponentPosition}.
 * 
 * @author pit & dirk
 */

public interface TupleComponent extends Value {

	EntityType<TupleComponent> T = EntityTypes.T(TupleComponent.class);

	int getTupleComponentIndex();

	void setTupleComponentIndex(int tupleComponentIndex);

	@Override
	default ValueType valueType() {
		return ValueType.tupleComponent;
	}

}
