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
 * The AuthorizedRequest has to be used as super type if some request requires a session being given from the caller in order to verify its priviledges.
 * @author Dirk Scheffler
 *
 */
@Abstract
public interface AuthorizedRequest extends AuthorizableRequest {

	EntityType<AuthorizedRequest> T = EntityTypes.T(AuthorizedRequest.class);

	@Override
	default boolean requiresAuthentication() {
		return true;
	}

}
