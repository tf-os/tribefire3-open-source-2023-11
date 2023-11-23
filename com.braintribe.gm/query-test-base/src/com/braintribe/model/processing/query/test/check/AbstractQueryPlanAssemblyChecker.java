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
package com.braintribe.model.processing.query.test.check;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.queryplan.index.RepositoryMetricIndex;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.JoinKind;
import com.braintribe.model.queryplan.value.AggregateFunction;
import com.braintribe.model.queryplan.value.AggregationFunctionType;
import com.braintribe.model.queryplan.value.QueryFunctionValue;
import com.braintribe.model.queryplan.value.StaticValue;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.ValueProperty;

/**
 * 
 */
public class AbstractQueryPlanAssemblyChecker<T extends AbstractQueryPlanAssemblyChecker<T>> extends AbstractEntityAssemblyChecker<T> {

	public AbstractQueryPlanAssemblyChecker(GenericEntity root) {
		super(root);
	}

	public T isTupleComponent_(int index) {
		return hasType(TupleComponent.T).whereProperty("tupleComponentIndex").is_(index).close();
	}

	public T isValueProperty_(String propertyPath) {
		return isValueProperty(propertyPath).close();
	}

	public T isValueProperty(String propertyPath) {
		return hasType(ValueProperty.T).whereProperty("propertyPath").is_(propertyPath);
	}

	public T isStaticSet_(Object... elements) {
		return isStaticValue_(asSet(elements));
	}

	public T isStaticValue_(Object value) {
		return hasType(StaticValue.T).whereValue().is_(value).close();
	}

	public T isStaticValue_(EntityType<? extends GenericEntity> et, Object id, String partition) {
		return hasType(StaticValue.T) //
				.whereValue().isReference_(et.getTypeSignature(), id, partition) //
				.close();
	}

	public T whereValue() {
		return whereProperty("value");
	}

	public T hasValues(int size) {
		return whereProperty("values").isListWithSize(size);
	}

	public T whereOperand() {
		return whereProperty("operand");
	}

	public T isQueryFunctionValueAndQf() {
		return hasType(QueryFunctionValue.class).whereProperty("queryFunction");
	}

	public T isSourceSet_(EntityType<?> soureType) {
		return hasType(SourceSet.T).whereProperty("typeSignature").is_(soureType.getTypeSignature()).close();
	}

	public T isIndexSubSet_(EntityType<?> soureType, String property) {
		return isIndexSubSet(soureType, property).close();
	}

	public T isIndexSubSet(EntityType<?> soureType, String property) {
		return hasType(IndexSubSet.T) //
				.whereProperty("typeSignature").is_(soureType.getTypeSignature()) //
				.whereProperty("propertyName").is_(property);
	}

	public T isIndexOrderedSet_(EntityType<?> soureType, String property, boolean descending) {
		return hasType(IndexOrderedSet.T) //
				.whereProperty("typeSignature").is_(soureType.getTypeSignature()) //
				.whereProperty("propertyName").is_(property) //
				.whereProperty("descending").is_(descending) //
				.whereProperty("metricIndex").hasType_(RepositoryMetricIndex.T) //
				.close();
	}

	public T isPaginatedSetWithOperand(Integer max, Integer offset) {
		return hasType(PaginatedSet.T) //
				.whereProperty("limit").is_(max) //
				.whereProperty("offset").is_(offset) //
				.whereOperand();
	}

	public T isEntityJoin(JoinKind joinKind) {
		return hasType(EntityJoin.T).whereProperty("joinKind").is_(joinKind);
	}
	public T isPropertyOperandAndSource(String propertyName) {
		return isPropertyOperand(propertyName).whereProperty("source");
	}

	public T isPropertyOperand(String propertyName) {
		return hasType(PropertyOperand.T) //
				.whereProperty("propertyName").is_(propertyName);
	}

	public T isAggregateFunction(AggregationFunctionType type) {
		return hasType(AggregateFunction.T) //
				.whereProperty("aggregationFunctionType").is_(type);
	}

}
