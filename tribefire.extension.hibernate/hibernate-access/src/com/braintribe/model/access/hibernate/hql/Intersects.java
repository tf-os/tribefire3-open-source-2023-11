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
package com.braintribe.model.access.hibernate.hql;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.conditions.Comparison;

/**
 * @see DisjunctedInOptimizer
 * 
 * @author peter.gazdik
 */
public interface Intersects extends Comparison {

	EntityType<Intersects> T = EntityTypes.T(Intersects.class);

	PropertyOperand getPropertyOperand();
	void setPropertyOperand(PropertyOperand operand);

	Set<Object> getValues();
	void setValues(Set<Object> values);

	static Intersects create(PropertyOperand operand, Set<Object> values) {
		Intersects result = Intersects.T.create();
		result.setPropertyOperand(operand);
		result.setValues(values);

		return result;
	}

}
