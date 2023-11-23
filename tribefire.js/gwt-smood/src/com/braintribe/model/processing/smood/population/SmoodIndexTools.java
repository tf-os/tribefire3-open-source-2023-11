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
package com.braintribe.model.processing.smood.population;

import java.util.Comparator;

import com.braintribe.common.lcd.UnsupportedEnumException;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.processing.query.eval.tools.EntityComparator;
import com.braintribe.model.processing.query.planner.QueryPlanner;
import com.braintribe.model.processing.query.tools.ScalarComparator;
import com.braintribe.utils.collection.impl.ComparableComparator;

/**
 * 
 */
public class SmoodIndexTools {

	/**
	 * @see #indexId(String, String)
	 */
	public static String indexId(EntityType<?> et, String propertyName) {
		return indexId(et.getTypeSignature(), propertyName);
	}

	/**
	 * Returns a unique id for given entity-signature and property. Note that this means there can only be one index for
	 * given property. This id is then used by the {@link QueryPlanner} and the query-evaluator component as a unique
	 * identifier of an index.
	 */
	public static String indexId(String typeSignature, String propertyName) {
		// Way faster than the typeSignature+"#"+ propertyName variant
		if (typeSignature == null) {
			if (propertyName == null)
				return "null#null";

			return "null#".concat(propertyName);
		}

		if (propertyName == null)
			return typeSignature.concat("#null");

		return typeSignature.concat("#").concat(propertyName); 
	}

	/** Returns the right {@link Comparator} implementation given property type. */
	public static <T> Comparator<T> getComparator(GenericModelType type) {
		return (Comparator<T>) getComparatorHelper(type);
	}

	private static Comparator<?> getComparatorHelper(GenericModelType type) {
		switch (type.getTypeCode()) {
			case dateType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
			case booleanType:
			case enumType:
				return ComparableComparator.unboundedInstance();
			case entityType:
				return EntityComparator.INSTANCE;

			case listType:
			case mapType:
			case setType:
				throw new IllegalArgumentException("Comparator is not supported for collection type!");

			case objectType:
				// For now we only assume id property can be indexed, thus we only have to worry about scalar types
				return ScalarComparator.INSTANCE;

			default:
				throw new UnsupportedEnumException("Unknown type: " + type + " with code: " + type.getTypeCode());
		}
	}

	/** Returns <tt>true</tt> iff it is possible to create an index for given property type. */
	public static boolean supportsIndex(GenericModelType propertyType) {
		switch (propertyType.getTypeCode()) {
			case dateType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
			case booleanType:
			case entityType:
			case enumType:
				return true;

				/* I know the default is enough, but I want to show what the other types are (if new type is introduced,
				 * it will also be obvious whether or not we already considered it here) */
			case listType:
			case mapType:
			case objectType:
			case setType:
			default:
				return false;
		}
	}

	/** Returns <tt>true</tt> iff it is possible to create a metric index for given property type. */
	public static boolean supportsMetric(GenericModelType propertyType) {
		switch (propertyType.getTypeCode()) {
			case dateType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
				return true;

			case booleanType:
			case entityType:
			case enumType:
				return false;

				// These do not support indices at all right now.

				/* I know the default is enough, but I want to show what the others are (if new type is introduced, it
				 * will also be obvious whether or not we already considered it here) */
			case listType:
			case mapType:
			case objectType:
			case setType:
			default:
				return false;
		}
	}

}
