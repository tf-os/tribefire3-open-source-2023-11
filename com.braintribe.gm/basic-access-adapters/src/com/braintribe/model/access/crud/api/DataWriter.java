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

import com.braintribe.model.access.crud.CrudExpertAccess;
import com.braintribe.model.access.crud.api.create.EntityCreator;
import com.braintribe.model.access.crud.api.delete.EntityDeleter;
import com.braintribe.model.access.crud.api.update.EntityUpdater;
import com.braintribe.model.generic.GenericEntity;

/**
 * A marker interface for all writing expert implementations used by the
 * {@link CrudExpertAccess}.
 * 
 * @see EntityCreator
 * @see EntityUpdater
 * @see EntityDeleter
 * 
 * @author gunther.schenk
 */
public interface DataWriter<T extends GenericEntity> extends CrudExpert<T> {
	// empty
}
