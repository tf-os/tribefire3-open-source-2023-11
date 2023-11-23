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

import java.util.Comparator;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

/**
 * 
 */
public class PropertyBasedComparator implements Comparator<Object> {

	private final String[] propertyChain;

	public PropertyBasedComparator(String propertyPath) {
		this.propertyChain = propertyPath.split("\\.");
	}

	@Override
	public int compare(Object o1, Object o2) {
		for (String propertyName: propertyChain) {
			o1 = evaluate(o1, propertyName);
			o2 = evaluate(o2, propertyName);
		}

		return compareObjects(o1, o2);
	}

	private int compareObjects(Object o1, Object o2) {
		if (o1 == null)
			return -1;

		if (o2 == null)
			return 1;

		return ((Comparable<Object>) o1).compareTo(o2);
	}

	private Object evaluate(Object o, String property) {
		if (o == null)
			return null;

		if (!(o instanceof GenericEntity))
			throw new RuntimeException("Cannot resolve property '" + property + "' for: " + o + ". The object is not a GenericEntity.");

		GenericEntity entity = (GenericEntity) o;
		Property p = entity.entityType().findProperty(property);

		return p.get(entity);
	}

}
