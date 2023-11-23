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
package com.braintribe.model.access.crud.api.update;

import java.util.Set;

import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport;
import com.braintribe.model.access.crud.api.DataWritingContext;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;

/**
 * A {@link DataWritingContext} implementation provided to {@link EntityUpdater} experts containing 
 * necessary informations on updated entities.
 *  
 * @author gunther.schenk
 */
public interface EntityUpdateContext<T extends GenericEntity> extends DataWritingContext<T> {

	/**
	 * @return the locally updated instance.  
	 */
	T getUpdated();
	
	/**
	 * @return the collection of properties which have been updated.
	 */
	Set<Property> getUpdatedProperties();

	/**
	 * Static helper method to build a new {@link EntityUpdateContext} instance.
	 *  
	 * @param updated the locally updated instance.
	 * @param report the {@link AdapterManipulationReport} provided by the {@link BasicAccessAdapter}
	 */	
	static <T extends GenericEntity> EntityUpdateContext<T> create(T updated, AdapterManipulationReport report) {
		return new EntityUpdateContext<T>() {
			@Override
			public T getUpdated() {
				return updated;
			}
			@Override
			public Set<Property> getUpdatedProperties() {
				return report.getTouchedPropertiesOfEntities().get(updated);
			}
			@Override
			public AdapterManipulationReport getManipulationReport() {
				return report;
			}
		};
	}
	

}
