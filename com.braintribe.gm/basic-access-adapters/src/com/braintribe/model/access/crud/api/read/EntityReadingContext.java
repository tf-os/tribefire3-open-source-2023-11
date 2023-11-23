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
import com.braintribe.model.generic.reflection.EntityType;

/**
 * A {@link DataReadingContext} implementation provided to {@link EntityReader} experts containing 
 * necessary informations on the expected instance.
 *  
 * @author gunther.schenk
 */
public interface EntityReadingContext<T extends GenericEntity> extends DataReadingContext<T> {
	
	/**
	 * @return the id value of the expected instance.
	 */
	<I> I getId();

	/**
	 * @return the actual requested type of the expected instance.
	 */
	EntityType<?> getRequestedType();

	/**
	 * Static helper method to build a new {@link EntityReadingContext} instance.
	 */
	static <T extends GenericEntity, I> EntityReadingContext<T> create(EntityType<T> requestedType, I id) {
		return create(requestedType, id, null);
	}
	
	/**
	 * Static helper method to build a new {@link EntityReadingContext} instance with query context.
	 */
	static <T extends GenericEntity, I> EntityReadingContext<T> create(EntityType<T> requestedType, I id, QueryContext context) {
		return new EntityReadingContext<T>() {
			@Override
			public I getId() {
				return id;
			}
			@Override
			public EntityType<?> getRequestedType() {
				return requestedType;
			}
			@Override
			public QueryContext getQueryContext() {
				return context;
			}
		};
	}
	
	
	
	
}
