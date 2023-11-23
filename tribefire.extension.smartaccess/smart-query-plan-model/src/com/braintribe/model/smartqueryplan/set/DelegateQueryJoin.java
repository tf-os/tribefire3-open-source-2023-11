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
package com.braintribe.model.smartqueryplan.set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import java.util.List;


import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;

/**
 * This operation combines already retrieved data represented by <tt>materializedSet</tt> with data that is to be retrieved from some
 * access, joining the two tuples (materialized one and queried one) based on correlation properties (which have to be present in both
 * tuples).
 * 
 * Lets say our smart entity <tt>Person</tt> is split into <tt>PersonA</tt> and <tt>PersonB</tt>, and we are evaluating this query:
 * 
 * <code>
 * 		select p.nameA, p.nameB from Person p where p.ageB = 25
 * </code>.
 * 
 * Let's say <tt>Person</tt> also has a property <tt>correlation</tt> mapped to both <tt>PersonA.correlationA</tt> and
 * <tt>PersonB.correlationB</tt>.
 * 
 * The plan should look like this:
 * 
 * <code>
 * DelegateQueryJoin [
 * 	materializedSet: DelegateQuerySet
 * 		delegateAccess: accessA [exertnalId]
 * 		delegateQuery:  select p.correlationA, p.nameA from PersonA p 
 * 		ScalarMappings:
 * 		- ScalarMapping
 * 			0* <- TupleComponent[index: 0]
 * 		- ScalarMapping
 * 			1 <- TupleComponent[index: 1]
 * 	querySet: DelegateQuerySet
 * 		delegateAccess: accessB [exertnalId]
 * 		delegateQuery:  select p.nameB, p.correlationB from PersonB p  where (p.ageB = 25 and (p.correlationB = null))
 * 		ScalarMappings:
 * 		- ScalarMapping
 * 			2 <- TupleComponent[index: 0]
 * 		- ScalarMapping
 * 			0* <- TupleComponent[index: 1]
 * 	JoinRestrictions: 
 * 	- OperandRestriction
 * 		operand: PropertyOperand [
 * 			source: Source (PersonB)
 * 			propertyName: correlationB
 * 		]
 * 		materializedCorrelationPosition: 0*
 * ]
 * </code>
 * 
 * Note * indicates the slot for the (in this case single) correlation property value. Note the same position is used for both sides of the
 * DQJ.
 * 
 * Note we are using HQL (i.e. String) representation of the queries, but they are of course represented by {@link SelectQuery} instances.
 * The select query for the querySet contains a condition which in general is a conjunction of operands, where all operands are taken from
 * the original query, and one new operand is created - the correlation property condition. These correlation conditions are represented by
 * a disjunction, which coming from the planner consists of only a single operand. This one operand may actually be ignored, it is there
 * just so printing that query as HQL produces reasonable results. The information contained in this condition is duplicated in the
 * <tt>joinRestrictions</tt> property, which serves as a template for operands of this disjunction.
 * 
 * In our case, the join restriction only has one element, an operand that actually means "p.correlationB" and a
 * materializedCorrelationPosition, which says how we should get the right correlation value from the materialized tuples. For example, if
 * our materialized tuples look like <code>[1, "name1", -, -], [2, "name2", -, -], [5, "name5", -, -]</code>, then our disjunction should
 * look like: <code>(p.correlationB = 1 OR p.correlationB = 2 OR p.correlationB = 5)</code> (we were taking values on position 0 from the
 * materialized tuples as right-side of the comparison).
 * 
 * We might be using more than one correlation properties (if our materializedSet and delegateSet share multiple entities which are being
 * joined using correlation property), in which case we would join the corresponding value-comparison with a conjunction. It might look
 * something like this:<code>
 * ((p.correlationB = 1 AND c.correlationB = 11) OR (p.correlationB = 2 AND c.correlationB = 22) OR (p.correlationB = 5 AND c.correlationB = 55))
 * </code>
 */
public interface DelegateQueryJoin extends SmartTupleSet {

	EntityType<DelegateQueryJoin> T = EntityTypes.T(DelegateQueryJoin.class);

	TupleSet getMaterializedSet();
	void setMaterializedSet(TupleSet materializedSet);

	DelegateQuerySet getQuerySet();
	void setQuerySet(DelegateQuerySet querySet);

	Disjunction getJoinDisjunction();
	void setJoinDisjunction(Disjunction joinDisjunction);

	List<OperandRestriction> getJoinRestrictions();
	void setJoinRestrictions(List<OperandRestriction> joinRestrictions);

	boolean getIsLeftJoin();
	void setIsLeftJoin(boolean isLeftJoin);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.extension;
	}

	@Override
	default SmartTupleSetType smartType() {
		return SmartTupleSetType.delegateQueryJoin;
	}

}
