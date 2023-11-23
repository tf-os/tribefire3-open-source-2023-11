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
package com.braintribe.model.access.crud.api.read;

import com.braintribe.model.access.crud.api.DataReadingContext;
import com.braintribe.model.generic.GenericEntity;

/**
 * A {@link DataReadingContext} implementation provided to
 * {@link PropertyReader} experts containing necessary informations on the
 * expected property value.
 * 
 * @author gunther.schenk
 */
public interface PropertyReadingContext<T extends GenericEntity> extends DataReadingContext<T> {

	T getHolder();

	String getPropertyName();

	/**
	 * Static helper method to build a new {@link PropertyReadingContext} instance.
	 */
	static <T extends GenericEntity> PropertyReadingContext<T> create(T holder, String propertyName) {
		return create(holder, propertyName, null);
	}

	/**
	 * Static helper method to build a new {@link PropertyReadingContext} instance with query context.
	 */
	static <T extends GenericEntity> PropertyReadingContext<T> create(T holder, String propertyName, QueryContext context) {
		return new PropertyReadingContext<T>() {
			@Override
			public T getHolder() {
				return holder;
			}

			@Override
			public String getPropertyName() {
				return propertyName;
			}
			@Override
			public QueryContext getQueryContext() {
				return context;
			}
		};
	}

}
