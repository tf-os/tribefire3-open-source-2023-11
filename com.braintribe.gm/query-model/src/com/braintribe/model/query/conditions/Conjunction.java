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
package com.braintribe.model.query.conditions;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An {@link AbstractJunction} where all the contained <code>operands</code> are to be treated as coupled with a logical
 * AND statement.
 */
public interface Conjunction extends AbstractJunction {

	EntityType<Conjunction> T = EntityTypes.T(Conjunction.class);

	@Override
	default ConditionType conditionType() {
		return ConditionType.conjunction;
	}
	
	static Conjunction of(Condition... conditions) {
		Conjunction conjunction = Conjunction.T.create();
		List<Condition> operands = conjunction.getOperands();
		for (Condition condition: conditions) {
			operands.add(condition);
		}
		return conjunction;
	}
	
	@Override
	default Conjunction add(Condition operand) {
		getOperands().add(operand);
		return this;
	}
}
