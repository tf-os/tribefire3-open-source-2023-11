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
package com.braintribe.model.processing.service.common;

import static java.util.Collections.emptySet;

import java.util.Set;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.gm.model.security.reason.MissingSession;
import com.braintribe.model.processing.service.api.ReasonedServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;

public class RoleBasedAuthorizingInterceptor implements ReasonedServicePreProcessor<ServiceRequest> {

	private Set<String> allowedRoles = emptySet();

	@Required
	public void setAllowedRoles(Set<String> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}

	@Override
	public Maybe<? extends ServiceRequest> processReasoned(ServiceRequestContext requestContext, ServiceRequest request) {
		UserSession userSession = requestContext.findOrNull(UserSessionAspect.class);
		if (userSession == null)
			return Reasons.build(MissingSession.T).text("Missing session").toMaybe();

		Set<String> roles = userSession.getEffectiveRoles();

		boolean missesRequiredRole = !allowedRoles.stream() //
				.filter(roles::contains) //
				.findFirst() //
				.isPresent();

		if (missesRequiredRole)
			return Reasons.build(Forbidden.T).text("Not authorized").toMaybe();

		return Maybe.complete(request);
	}
}
