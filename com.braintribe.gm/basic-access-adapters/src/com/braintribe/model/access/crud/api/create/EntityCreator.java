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

import com.braintribe.model.access.crud.api.DataWriter;
import com.braintribe.model.generic.GenericEntity;

/**
 * A {@link DataWriter} expert that is responsible for persisting new created
 * instances of the type he is registered for.
 * 
 * @author gunther.schenk
 */
public interface EntityCreator<T extends GenericEntity> extends DataWriter<T> {

	/**
	 * Create/Persist a new instance of registered type using the informations
	 * provided by the passed context. <br/>
	 * Note, that the locally created entity provided by the context
	 * {@link EntityCreationContext#getCreated()} is bound to a local session and
	 * any modification on it is tracked and finally reported back as an induced
	 * manipulation to the caller. <br/>
	 * Usually that mechanism is used to report back created id's by calling
	 * {@link GenericEntity#setId(Object)} with the generated id value on the
	 * created object after writing to the persistence layer.
	 */
	void createEntity(EntityCreationContext<T> context);

}
