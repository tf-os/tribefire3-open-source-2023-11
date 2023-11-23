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
package com.braintribe.model.processing.query.eval.set.aggregate.arithmetic;

import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.set.aggregate.ArithmeticExpert;
import com.braintribe.model.queryplan.value.AggregationFunctionType;

/**
 * 
 */
public class ArithmeticAggregateExpertRegistry {

	public static ArithmeticExpert<Object> getExpertIfPossible(AggregationFunctionType type) {
		return (ArithmeticExpert<Object>) getExpertHelper(type);
	}

	private static ArithmeticExpert<?> getExpertHelper(AggregationFunctionType type) {
		switch (type) {
			case count:
				return IntegerAggregateExpert.INSTANCE;

			case countDistinct:
				return new CountDistinctAggregateExpert();

			default:
				return null;
		}
	}

	public static ArithmeticExpert<Object> getExpertFor(GenericModelType gmType) {
		return (ArithmeticExpert<Object>) getExpertHelper(gmType);
	}

	private static ArithmeticExpert<?> getExpertHelper(GenericModelType gmType) {
		switch (gmType.getTypeCode()) {
			case dateType:
				return DateAggregateExpert.INSTANCE;
			case decimalType:
				return DecimalAggregateExpert.INSTANCE;
			case doubleType:
				return DoubleAggregateExpert.INSTANCE;
			case floatType:
				return FloatAggregateExpert.INSTANCE;
			case integerType:
				return IntegerAggregateExpert.INSTANCE;
			case longType:
				return LongAggregateExpert.INSTANCE;
			case stringType:
				return StringAggregateExpert.INSTANCE;

			case booleanType:
			case entityType:
			case enumType:
			case listType:
			case mapType:
			case objectType:
			case setType:
				throw new RuntimeQueryEvaluationException("Aggregation is not supported for type: " + gmType);
		}

		throw new RuntimeQueryEvaluationException("Unknown GenericModelType: " + gmType + " typeCode: " + gmType.getTypeCode());

	}

}
