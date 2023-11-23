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
package com.braintribe.model.access.crud.api;

import com.braintribe.model.access.BasicAccessAdapter;
import com.braintribe.model.access.BasicAccessAdapter.AdapterManipulationReport;
import com.braintribe.model.access.crud.api.create.EntityCreationContext;
import com.braintribe.model.access.crud.api.delete.EntityDeletionContext;
import com.braintribe.model.access.crud.api.update.EntityUpdateContext;
import com.braintribe.model.generic.GenericEntity;

/**
 * Base type for all context objects passed to individual {@link DataWriter}
 * implementations.
 * 
 * @see EntityCreationContext
 * @see EntityUpdateContext
 * @see EntityDeletionContext
 * 
 * @author gunther.schenk
 */
public interface DataWritingContext<T extends GenericEntity> extends CrudExpertContext<T> {

	/**
	 * @return the full {@link AdapterManipulationReport} provided by the
	 *         {@link BasicAccessAdapter}
	 */
	AdapterManipulationReport getManipulationReport();
}
