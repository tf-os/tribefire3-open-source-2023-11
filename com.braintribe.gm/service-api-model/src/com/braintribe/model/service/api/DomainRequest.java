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
 * A DomainRequest is used to make the service domain who's model is used to associate processors and interceptors configurable per request.
 */
@Abstract
public interface DomainRequest extends ServiceRequest {

	EntityType<DomainRequest> T = EntityTypes.T(DomainRequest.class);

	String domainId = "domainId";

	/**
	 * The id of the service domain which has to be used to evaluate the request.
	 */
	String getDomainId();
	void setDomainId(String domainId);

	@Override
	default String domainId() {
		String d = getDomainId();
		if (d == null) {
			// Enables subtypes to infer the default domainId() value from their initializers
			return (String) this.entityType().getProperty(domainId).getInitializer();
		} else {
			return d;
		}
	}

}
