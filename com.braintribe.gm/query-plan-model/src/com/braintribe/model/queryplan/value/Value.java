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

import com.braintribe.model.generic.annotation.Abstract;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.queryplan.filter.ValueComparison;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.SortCriterion;

/**
 * Basic class for any value. Values are used for comparisons (like {@link ValueComparison}), {@link Projection}s and also for sorting (
 * {@link SortCriterion}).
 * 
 * @see HashSetProjection
 * @see StaticValue
 * @see TupleComponent
 * @see ValueProperty
 */
@Abstract
public interface Value extends GenericEntity {

	EntityType<Value> T = EntityTypes.T(Value.class);

	ValueType valueType();

}
