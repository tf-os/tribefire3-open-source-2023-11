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
package com.braintribe.model.processing.query.building;

import java.util.Set;

import com.braintribe.model.query.Operator;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.aggregate.Count;

public abstract class Queries {
	public static Conjunction and(Condition... conditions) {
		return Conjunction.of(conditions);
	}

	public static Disjunction or(Condition... conditions) {
		return Disjunction.of(conditions);
	}

	public static Negation not(Condition condition) {
		return Negation.of(condition);
	}

	public static ValueComparison compare(Object op1, Operator operator, Object op2) {
		return ValueComparison.compare(op1, operator, op2);
	}

	public static ValueComparison eq(Object op1, Object op2) {
		return ValueComparison.eq(op1, op2);
	}

	public static ValueComparison ne(Object op1, Object op2) {
		return ValueComparison.ne(op1, op2);
	}

	public static ValueComparison gt(Object op1, Object op2) {
		return ValueComparison.gt(op1, op2);
	}

	public static ValueComparison ge(Object op1, Object op2) {
		return ValueComparison.ge(op1, op2);
	}

	public static ValueComparison lt(Object op1, Object op2) {
		return ValueComparison.lt(op1, op2);
	}

	public static ValueComparison le(Object op1, Object op2) {
		return ValueComparison.le(op1, op2);
	}

	public static ValueComparison in(Object element, Set<?> set) {
		return ValueComparison.in(element, set);
	}

	public static ValueComparison in(Object element, Object collection) {
		return compare(element, Operator.in, collection);
	}

	public static ValueComparison like(Object value, String pattern) {
		return ValueComparison.like(value, pattern);
	}

	public static ValueComparison ilike(Object value, String pattern) {
		return ValueComparison.ilike(value, pattern);
	}

	public static EntitySignature entitySignature(Object operand) {
		return EntitySignature.of(operand);
	}

	public static Count count(Object operand) {
		return Count.of(operand);
	}

	public static Count count(Object operand, boolean distinct) {
		Count count = Count.of(operand);
		count.setDistinct(distinct);
		return count;
	}

}