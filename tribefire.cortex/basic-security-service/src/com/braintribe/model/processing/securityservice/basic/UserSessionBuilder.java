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
package com.braintribe.model.processing.securityservice.basic;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.user.User;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.model.usersession.UserSessionType;

public interface UserSessionBuilder {

	UserSessionBuilder type(UserSessionType type);
	UserSessionBuilder request(OpenUserSession request);
	UserSessionBuilder requestContext(ServiceRequestContext requestContext);
	UserSessionBuilder internetAddress(String internetAddress);
	UserSessionBuilder expiryDate(Date expiryDate);
	UserSessionBuilder locale(String locale);
	UserSessionBuilder acquirationKey(String acquirationKey);
	UserSessionBuilder blocksAuthenticationAfterLogout(boolean blocksAuthenticationAfterLogout);
	UserSessionBuilder addProperty(String key, String value);
	UserSessionBuilder addProperties(Map<String, String> properties);

	Maybe<UserSession> buildFor(User user);
	Maybe<UserSession> buildFor(String userId, Set<String> roles);

}
