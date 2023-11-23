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

import com.braintribe.model.access.crud.api.DataReader;
import com.braintribe.model.generic.GenericEntity;

/**
 * A {@link DataReader} expert that is responsible for providing a single instance of the
 * type he is registered for identified by the id provided through the passed context.
 * 
 * @author gunther.schenk
 */
public interface EntityReader<T extends GenericEntity> extends DataReader<T> {
	
	/**
	 * @return the instance of the requested type identified by the id provided
	 *         through the context. <br/>
	 *         This method can return <code>null</code> in case no instance could be
	 *         found for passed id.
	 */
	T getEntity (EntityReadingContext<T> context);

}
