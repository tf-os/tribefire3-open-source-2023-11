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
package com.braintribe.gm.model.marshaller.api.request;

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * NOTE: this request is currently not implemented
 * 
 * UnmarshallAccessData unmarshalls the data from {@link UnmarshallRequest#getResource()}, imports it into an access and returns it then.
 *  
 * @author Dirk Scheffler
 */
public interface UnmarshallAccessData extends UnmarshallRequest, AccessDataRequest {
	EntityType<UnmarshallAccessData> T = EntityTypes.T(UnmarshallAccessData.class);
}