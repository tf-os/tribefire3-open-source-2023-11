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
package com.braintribe.model.processing.query.parser.impl;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.bvd.context.UserName;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EnumReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.generic.value.Variable;
import com.braintribe.model.query.CascadedOrdering;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.GroupBy;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.JoinType;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.Ordering;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SimpleOrdering;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.Conjunction;
import com.braintribe.model.query.conditions.Disjunction;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.model.query.functions.EntitySignature;
import com.braintribe.model.query.functions.ListIndex;
import com.braintribe.model.query.functions.Localize;
import com.braintribe.model.query.functions.MapKey;
import com.braintribe.model.query.functions.aggregate.Average;
import com.braintribe.model.query.functions.aggregate.Count;
import com.braintribe.model.query.functions.aggregate.Max;
import com.braintribe.model.query.functions.aggregate.Min;
import com.braintribe.model.query.functions.aggregate.Sum;
import com.braintribe.model.query.functions.value.AsString;
import com.braintribe.model.query.functions.value.Concatenation;
import com.braintribe.model.query.functions.value.Lower;
import com.braintribe.model.query.functions.value.Upper;
import com.braintribe.model.time.CalendarOffset;
import com.braintribe.model.time.DateOffset;
import com.braintribe.model.time.DateOffsetUnit;
import com.braintribe.model.time.TimeZoneOffset;

/**
 * A convenient class that contains all the instantiations any Query related
 * model
 * 
 */
public class FragmentsBuilder {

	public CalendarOffset dateOffset(Integer value, DateOffsetUnit unit) {
	    DateOffset dateOffset = DateOffset.T.create();
		dateOffset.setOffset(unit);
		dateOffset.setValue(value);
		return dateOffset;
	}

	public CalendarOffset timeZoneOffset(int minutes) {
	    TimeZoneOffset timeZoneOffset = TimeZoneOffset.T.create();
		timeZoneOffset.setMinutes(minutes);
		return timeZoneOffset;
	}

	public Disjunction disjunction(List<Object> operands) {
	    Disjunction disjunction = Disjunction.T.create();
		// This will be easier when QM3.0 is used
		List<Condition> conditionList = new ArrayList<Condition>();
		for (Object object : operands) {
			conditionList.add((Condition) object);
		}
		disjunction.setOperands(conditionList);
		return disjunction;
	}

	public Conjunction conjunction(List<Object> operands) {
	    Conjunction conjunction = Conjunction.T.create();
		// This will be easier when QM3.0 is used
		List<Condition> conditionList = new ArrayList<Condition>();
		for (Object object : operands) {
			conditionList.add((Condition) object);
		}
		conjunction.setOperands(conditionList);
		return conjunction;
	}

	public Negation negation(Object operand) {
	    Negation negation = Negation.T.create();
		negation.setOperand((Condition) operand);
		return negation;
	}

	public EntityReference entityReference(boolean persistent, String typeSignature, Object id, String partition) {
		EntityType<? extends EntityReference> refType = persistent ? PersistentEntityReference.T : PreliminaryEntityReference.T;
		return entityReference(refType, typeSignature, id, partition);
	}
	
	private static EntityReference entityReference(EntityType<? extends EntityReference> refType, String typeSignature, Object id, String partition) {
	    EntityReference reference = refType.create();
		reference.setTypeSignature(typeSignature);
		reference.setRefId(id);
		reference.setRefPartition(partition);
		return reference;
	}

	public ValueComparison comparison(Object leftOperand, Object rightOperand, String operator) {
	    ValueComparison comparison = ValueComparison.T.create();
		comparison.setLeftOperand(leftOperand);
		comparison.setRightOperand(rightOperand);
		comparison.setOperator(Operator.getOperatorToSign(operator.toLowerCase()));
		return comparison;
	}

	public EnumReference enumReference(String typeSignature, String constant) {
	    EnumReference reference = EnumReference.T.create();
		reference.setTypeSignature(typeSignature);
		reference.setConstant(constant);
		return reference;
	}

	public Paging pagination(int startIndex, int pageSize) {
	    Paging pagination = Paging.T.create();
		pagination.setStartIndex(startIndex);
		pagination.setPageSize(pageSize);
		return pagination;
	}

	public SimpleOrdering singleOrderBy(Object orderBy, OrderingDirection direction) {
	    SimpleOrdering order = SimpleOrdering.T.create();
		order.setOrderBy(orderBy);
		order.setDirection(direction);
		return order;
	}

	public Ordering orderBy(List<Object> orderByList) {
		if (orderByList.size() == 1) {
			return (Ordering) orderByList.get(0);
		} else {
		    CascadedOrdering ordering = CascadedOrdering.T.create();
			List<SimpleOrdering> orderingList = new ArrayList<SimpleOrdering>();
			for (Object orderBy : orderByList) {
				orderingList.add((SimpleOrdering) orderBy);
			}
			ordering.setOrderings(orderingList);
			return ordering;
		}
	}

	public PropertyOperand sourceProperty(Source source, String propertyName) {
	    PropertyOperand propertySource = PropertyOperand.T.create();
		propertySource.setSource(source);
		propertySource.setPropertyName(propertyName);
		return propertySource;

	}

	public Object aggregateAvg(Object operand) {
	    Average aggregate = Average.T.create();
		aggregate.setOperand(operand);
		return aggregate;
	}

	public Object aggregateMax(Object operand) {
	    Max aggregate = Max.T.create();
		aggregate.setOperand(operand);
		return aggregate;
	}

	public Object aggregateMin(Object operand) {
	    Min aggregate = Min.T.create();
		aggregate.setOperand(operand);
		return aggregate;
	}

	public Object aggregateSum(Object operand) {
	    Sum aggregate = Sum.T.create();
		aggregate.setOperand(operand);
		return aggregate;
	}

	public Object aggregateCount(Object operand, boolean distinct) {
	    Count aggregate = Count.T.create();
		aggregate.setOperand(operand);
		aggregate.setDistinct(distinct);
		return aggregate;
	}

	public Object lower(Object operand) {
	    Lower stringFunction = Lower.T.create();
		stringFunction.setOperand(operand);
		return stringFunction;
	}

	public Object upper(Object operand) {
	    Upper stringFunction = Upper.T.create();
		stringFunction.setOperand(operand);
		return stringFunction;
	}

	public Object toString(Object operand) {
	    AsString stringFunction = AsString.T.create();
		stringFunction.setOperand(operand);
		return stringFunction;
	}

	public Object concatenation(List<Object> operandList) {
	    Concatenation stringFunction = Concatenation.T.create();
		stringFunction.setOperands(operandList);
		return stringFunction;
	}

	public Condition fullTextComparison(Source source, String text) {
	    FulltextComparison stringFunction = FulltextComparison.T.create();
		stringFunction.setSource(source);
		stringFunction.setText(text);
		return stringFunction;
	}

	public Object localise(Object localiseString, String locale) {
	    Localize stringFunction = Localize.T.create();
		stringFunction.setLocalizedStringOperand(localiseString);
		stringFunction.setLocale(locale);
		return stringFunction;
	}
	
	public Object userName() {
		UserName userName = UserName.T.create();
		return userName;
	}

	public Object now() {
	    Now now = Now.T.create();
		return now;
	}

	public EntityQuery entityQuery() {
	    EntityQuery query = EntityQuery.T.create();
		return query;
	}

	public Restriction restriction(Condition condition, Paging pagination) {
	    Restriction restriction = Restriction.T.create();
		restriction.setCondition(condition);
		restriction.setPaging(pagination);
		return restriction;
	}

	public PropertyQuery propertyQuery() {
	    PropertyQuery query = PropertyQuery.T.create();
		return query;
	}

	public SelectQuery selectQuery() {
	    SelectQuery query = SelectQuery.T.create();
		return query;
	}

	public Object entitySignature(Object operand) {
	    EntitySignature entitySignature = EntitySignature.T.create();
		entitySignature.setOperand(operand);
		return entitySignature;
	}

	public From from(String signature) {
	    From from = From.T.create();
		from.setEntityTypeSignature(signature);
		return from;
	}

	public Join join(Source source, String propertyName, JoinType joinType) {
	    Join join = Join.T.create();
		join.setSource(source);
		join.setProperty(propertyName);
		join.setJoinType(joinType);
		return join;
	}

	public GroupBy groupBy(List<Object> operandList) {
	    GroupBy groupBy = GroupBy.T.create();
		groupBy.setOperands(operandList);
		return groupBy;
	}

	public ListIndex listIndex(Join join) {
	    ListIndex listIndex = ListIndex.T.create();
		listIndex.setJoin(join);
		return listIndex;
	}

	public MapKey mapKey(Join join) {
	    MapKey mapKey = MapKey.T.create();
		mapKey.setJoin(join);
		return mapKey;
	}

	public Variable variable(String name, String typeSignature, Object defaultValue) {
	    Variable variable = Variable.T.create();
		variable.setName(name);
		if (typeSignature != null) {
			variable.setTypeSignature(typeSignature);
		}
		if (defaultValue != null) {
			variable.setDefaultValue(defaultValue);
		}
		return variable;
	}

}
