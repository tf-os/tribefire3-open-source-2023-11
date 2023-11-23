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
package com.braintribe.model.processing.accessory.impl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.processing.session.api.notifying.interceptors.InterceptorIdentification;

// Currently only used internally, we might bring it to the gm-core later  
/* package */ class ReadOnlyPropertyAccessInterceptor extends PropertyAccessInterceptor {

	/** {@link InterceptorIdentification} for the {@link ReadOnlyPropertyAccessInterceptor}. */
	public static interface ReadOnlyPai extends InterceptorIdentification {
		// empty
	}

	// This needs to be set to true once we want to make the session read-only
	public boolean isReadOnly;

	private String errorDescription;

	private final Set<Object> unmodifiableCollections = Collections.newSetFromMap(new IdentityHashMap<>());

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	@Override
	public Object getProperty(Property property, GenericEntity entity, boolean isVd) {
		if (!isReadOnly || !isBaseOrCollection(property.getType()))
			return super.getProperty(property, entity, isVd);

		/* if we have already used an Unmodifiable wrapper we cannot access this using super.getProperty, as the CollectionEnhancingPai would think it
		 * is not an enhanced collection */
		Object result = property.getDirectUnsafe(entity);
		if (unmodifiableCollections.contains(result))
			return result;

		result = super.getProperty(property, entity, isVd);

		if (result != null) {
			Object unmodifiableResult = makeUnmodifiable(result);
			if (result != unmodifiableResult) {
				result = unmodifiableResult;
				property.setDirectUnsafe(entity, result);
				unmodifiableCollections.add(result);
			}
		}

		return result;
	}

	private boolean isBaseOrCollection(GenericModelType propType) {
		return propType.isCollection() || propType.isBase();
	}

	private Object makeUnmodifiable(Object result) {
		if (result instanceof List<?>)
			return Collections.unmodifiableList((List<?>) result);

		if (result instanceof Set<?>)
			return Collections.unmodifiableSet((Set<?>) result);

		if (result instanceof Map<?, ?>)
			return Collections.unmodifiableMap((Map<?, ?>) result);

		return result;
	}

	@Override
	public Object setProperty(Property property, GenericEntity entity, Object value, boolean isVd) {
		if (isReadOnly)
			throw new RuntimeException("Cannnot modify entities attached to a read-only session! Entity: " + entity + errorSuffix());
		else
			return super.setProperty(property, entity, value, isVd);
	}

	private String errorSuffix() {
		return errorDescription == null ? "" : errorDescription;
	}

}
