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
package com.braintribe.model.wopi.service.integration;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.service.api.result.Failure;

/**
 * Base for all WOPI service results; in case of a known error a {@link Failure} will be returned. In case of an error
 * this type will be returned
 * 
 *
 */
public interface WopiResult extends StandardIdentifiable, HasNotifications {

	EntityType<WopiResult> T = EntityTypes.T(WopiResult.class);

	String failure = "failure";

	@Name("Failure")
	@Description("Failure appearing in service execution")
	Failure getFailure();
	void setFailure(Failure failure);

}
