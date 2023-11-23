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
package com.braintribe.common.lcd;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.utils.lcd.CommonTools;

/**
 * A {@link Comparator} that compares objects based on one of their properties (e.g. their name, their id, their size, etc.).
 *
 * @author michael.lafite
 *
 * @param <O>
 *            The type of the objects to compare.
 * @param <P>
 *            The type of the properties used to compare the objects.
 */
public class WeightedPropertyComparator<O, P> implements Comparator<O> {

	private final Function<O, P> propertyProvider;
	private final Function<P, Integer> propertyWeightProvider;

	/**
	 * Creates a new instance.
	 *
	 * @param propertyProvider
	 *            the provider that gets the property of an object (used for comparison).
	 * @param propertyValueProvider
	 *            the provider that gets the value of a property (used for comparison).
	 */
	public WeightedPropertyComparator(final Function<O, P> propertyProvider, final Function<P, Integer> propertyValueProvider) {
		this.propertyProvider = propertyProvider;
		this.propertyWeightProvider = propertyValueProvider;
	}

	@Override
	public int compare(final O object1, final O object2) {
		final Integer value1 = getPropertyValue(object1);
		final Integer value2 = getPropertyValue(object2);

		return value1.compareTo(value2);
	}

	private int getPropertyValue(O object) {
		P property = propertyProvider.apply(object);
		return propertyWeightProvider.apply(property);
	}

	/**
	 * Provides the weight for an object of type.
	 *
	 * @author michael.lafite
	 *
	 * @param <T>
	 *            the type of the objects this provider provides a weight for.
	 */
	public static class WeightProvider<T> implements Function<T, Integer> {

		private Integer defaultWeight;
		private Map<T, Integer> weights;

		public WeightProvider(final Map<T, Integer> weights) {
			this(weights, Integer.MAX_VALUE);
		}

		public WeightProvider(final Map<T, Integer> weights, final Integer defaultWeight) {
			this.defaultWeight = defaultWeight;
			this.weights = weights;
		}

		public WeightProvider(final List<T> propertiesOrderedByValue) {
			this(CommonTools.getElementToIndexMap(propertiesOrderedByValue), Integer.MAX_VALUE);
		}

		@Override
		public Integer apply(final T object) {
			Integer weight = getWeights().get(object);
			if (weight == null) {
				weight = getDefaultWeight();
			}
			return weight;
		}

		private Integer getDefaultWeight() {
			return this.defaultWeight;
		}

		private Map<T, Integer> getWeights() {
			return this.weights;
		}
	}
}
