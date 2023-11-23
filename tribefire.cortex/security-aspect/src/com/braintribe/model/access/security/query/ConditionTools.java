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
package com.braintribe.model.access.security.query;

import java.util.Arrays;
import java.util.List;

import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.Negation;

/**
 * 
 */
public class ConditionTools {

	public static Negation not(Condition condition) {
		Negation negation = Negation.T.create();
		negation.setOperand(condition);
		return negation;
	}

	public static Conjunction and(Condition... conditions) {
		return and(Arrays.asList(conditions));
	}

	public static Conjunction and(List<Condition> conditions) {
		Conjunction and = Conjunction.T.create();
		and.setOperands(conditions);
		return and;
	}

	public static Disjunction or(Condition... conditions) {
		return or(Arrays.asList(conditions));
	}

	public static Disjunction or(List<Condition> conditions) {
		Disjunction result = Disjunction.T.create();
		result.setOperands(conditions);
		return result;
	}

}
