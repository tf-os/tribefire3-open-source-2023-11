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
package com.braintribe.model.access.crud.api.create;

import com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport;
import com.braintribe.model.access.crud.api.DataWritingContext;
import com.braintribe.model.generic.GenericEntity;

/**
 * A {@link DataWritingContext} implementation provided to {@link EntityCreator}
 * experts containing necessary informations on newly created instances.
 * 
 * @author gunther.schenk
 */
public interface EntityCreationContext<T extends GenericEntity> extends DataWritingContext<T> {

	/**
	 * @return the locally newly created instance.
	 */
	T getCreated();

	/**
	 * Static helper method to build a new {@link EntityCreationContext} instance.
	 */
	static <T extends GenericEntity> EntityCreationContext<T> create(T created, AdapterManipulationReport report) {
		return new EntityCreationContext<T>() {
			@Override
			public T getCreated() {
				return created;
			}

			@Override
			public AdapterManipulationReport getManipulationReport() {
				return report;
			}
		};
	}

}
