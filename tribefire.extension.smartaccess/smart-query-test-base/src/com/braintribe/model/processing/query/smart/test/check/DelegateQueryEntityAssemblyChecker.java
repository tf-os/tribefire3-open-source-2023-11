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
package com.braintribe.model.processing.query.smart.test.check;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.test.check.AbstractEntityAssemblyChecker;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.query.functions.value.AsString;

/**
 * 
 */
public class DelegateQueryEntityAssemblyChecker<P extends AbstractEntityAssemblyChecker<P>>
		extends AbstractEntityAssemblyChecker<DelegateQueryEntityAssemblyChecker<P>> {

	private final P parentChecker;

	public DelegateQueryEntityAssemblyChecker(P parentChecker, SelectQuery query) {
		super(query);

		this.parentChecker = parentChecker;
	}

	// ####################################
	// ## . . . . Root Components . . . .##
	// ####################################

	public DelegateQueryEntityAssemblyChecker<P> whereSelection(int size) {
		return clear().whereProperty("selections").isNotNull().isListWithSize(size);
	}

	public DelegateQueryEntityAssemblyChecker<P> whereFroms(int size) {
		return clear().whereProperty("froms").isNotNull().isListWithSize(size);
	}

	public DelegateQueryEntityAssemblyChecker<P> whereCondition() {
		return clear().whereProperty("restriction").isNotNull().whereProperty("condition");
	}

	public DelegateQueryEntityAssemblyChecker<P> withNoRestriction() {
		return clear().whereProperty("restriction").isNull().close();
	}

	public DelegateQueryEntityAssemblyChecker<P> withRestrictionWithoutPaging() {
		return clear().whereProperty("restriction").isNotNull().whereProperty("paging").isNull().close(2);
	}

	// ####################################
	// ## . . . . . Operands . . . . . . ##
	// ####################################

	public DelegateQueryEntityAssemblyChecker<P> isJoin(String propertyName, JoinType joinType) {
		return isJoin(propertyName).whereProperty("joinType").is_(joinType);
	}

	public DelegateQueryEntityAssemblyChecker<P> isJoin(String propertyName) {
		return hasType(Join.T).whereProperty("property").is_(propertyName);
	}

	public DelegateQueryEntityAssemblyChecker<P> isFrom(EntityType<?> et) {
		return isFrom(et.getTypeSignature());
	}

	public DelegateQueryEntityAssemblyChecker<P> isFrom(String signature) {
		return hasType(From.T).whereProperty("entityTypeSignature").is_(signature);
	}

	public DelegateQueryEntityAssemblyChecker<P> isPropertyOperand(String propertyValue) {
		return hasType(PropertyOperand.T).whereProperty("propertyName").is_(propertyValue);
	}

	public DelegateQueryEntityAssemblyChecker<P> isSourceOnlyPropertyOperand() {
		return hasType(PropertyOperand.T).whereProperty("propertyName").isNull().close();
	}

	public DelegateQueryEntityAssemblyChecker<P> isListIndexOnJoin(String joinProperty) {
		return hasType(ListIndex.T).whereProperty("join").isJoin(joinProperty);
	}

	public DelegateQueryEntityAssemblyChecker<P> isMapKeyOnJoin(String joinProperty) {
		return hasType(MapKey.T).whereProperty("join").isJoin(joinProperty);
	}

	public DelegateQueryEntityAssemblyChecker<P> castedToString() {
		return hasType(AsString.T).whereProperty("operand");
	}

	public DelegateQueryEntityAssemblyChecker<P> whereSource() {
		return whereProperty("source");
	}

	public DelegateQueryEntityAssemblyChecker<P> whereOrdering() {
		return clear().whereProperty("ordering").isNotNull();
	}

	public DelegateQueryEntityAssemblyChecker<P> withouthOrdering() {
		return clear().whereProperty("ordering").isNull();
	}

	public DelegateQueryEntityAssemblyChecker<P> withPagination(int max, int offset) {
		// @formatter:off
		return clear()
				.whereProperty("restriction").isNotNull()
					.whereProperty("paging").isNotNull()
						.whereProperty("pageSize").is_(max)
						.whereProperty("startIndex").is_(offset)
				;
		// @formatter:on
	}

	public DelegateQueryEntityAssemblyChecker<P> whereOperand() {
		return whereProperty("operand");
	}

	public DelegateQueryEntityAssemblyChecker<P> whereLeftOperand() {
		return whereProperty("leftOperand");
	}

	public DelegateQueryEntityAssemblyChecker<P> whereRightOperand() {
		return whereProperty("rightOperand");
	}

	// ####################################
	// ## . . . . . Conditions . . . . . ##
	// ####################################

	public DelegateQueryEntityAssemblyChecker<P> isSingleOperandConjunctionAndOperand() {
		return hasType(Conjunction.T).whereProperty("operands").isListWithSize(1).whereElementAt(0);
	}

	public DelegateQueryEntityAssemblyChecker<P> isSingleOperandDisjunctionAndOperand() {
		return hasType(Disjunction.T).whereProperty("operands").isListWithSize(1).whereElementAt(0);
	}

	public DelegateQueryEntityAssemblyChecker<P> isValueComparison(Operator operator) {
		return hasType(ValueComparison.T).whereProperty("operator").is_(operator);
	}

	public DelegateQueryEntityAssemblyChecker<P> isFulltextAndSource(String text) {
		return isFulltext(text).whereProperty("source");
	}

	public DelegateQueryEntityAssemblyChecker<P> isFulltext(String text) {
		return hasType(FulltextComparison.T).whereProperty("text").is_(text);
	}

	public DelegateQueryEntityAssemblyChecker<P> isConjunction(int expectedOperandsCount) {
		return hasType(Conjunction.T).whereProperty("operands").isListWithSize(expectedOperandsCount);
	}

	public DelegateQueryEntityAssemblyChecker<P> isDisjunction(int expectedOperandsCount) {
		return hasType(Disjunction.T).whereProperty("operands").isListWithSize(expectedOperandsCount);
	}

	// ####################################
	// ## . . . . . Ordering . . . . . . ##
	// ####################################

	public DelegateQueryEntityAssemblyChecker<P> isSimpleOrderingWhereValue(boolean descending) {
		return hasType(SimpleOrdering.T) //
				.whereProperty("direction").is_(descending ? OrderingDirection.descending : OrderingDirection.ascending) //
				.whereProperty("orderBy");
	}

	public DelegateQueryEntityAssemblyChecker<P> isCascadeOrdering() {
		return hasType(CascadedOrdering.T).whereProperty("orderings");
	}

	public P endQuery() {
		return parentChecker;
	}

}
