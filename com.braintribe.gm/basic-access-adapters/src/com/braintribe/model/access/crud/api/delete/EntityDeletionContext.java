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
package com.braintribe.model.access.crud.api.delete;

import com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport;
import com.braintribe.model.access.crud.api.DataWritingContext;
import com.braintribe.model.generic.GenericEntity;

/**
* {@link DataWritingContext} implementation provided to {@link EntityDeleter} experts containing 
* necessary informations on deleted instances.
*  
* @author gunther.schenk
*/
public interface EntityDeletionContext<T extends GenericEntity> extends DataWritingContext<T> {

	/**
	 * @return the local instance that should be deleted.  
	 */
	T getDeleted();

	/**
	 * Static helper method to build a new {@link EntityDeletionContext} instance.
	 */
	static <T extends GenericEntity> EntityDeletionContext<T> create (T deleted, AdapterManipulationReport report) {
		return new EntityDeletionContext<T>() {
			@Override
			public T getDeleted() {
				return deleted;
			}
			@Override
			public AdapterManipulationReport getManipulationReport() {
				return report;
			}
		};
	}
	
	

}
