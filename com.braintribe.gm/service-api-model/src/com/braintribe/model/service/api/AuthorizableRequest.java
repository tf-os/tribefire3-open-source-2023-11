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
package com.braintribe.model.service.api;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * The AuthorizableRequest can be used as super type if some request optionally supports a session being given from the caller in order to verify its priviledges.
 * @author Dirk Scheffler
 *
 */
@Abstract
public interface AuthorizableRequest extends ServiceRequest {

	EntityType<AuthorizableRequest> T = EntityTypes.T(AuthorizableRequest.class);

	String sessionId = "sessionId";

	/**
	 * The id of a session that should be used to check priviledges/roles in the processing
	 */
	String getSessionId();
	void setSessionId(String sessionId);

	@Override
	default boolean supportsAuthentication() {
		return true;
	}
	
	@Override
	default boolean requiresAuthentication() {
		return getSessionId() != null;
	}
}
