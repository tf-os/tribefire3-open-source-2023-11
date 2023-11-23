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
package com.braintribe.model.securityservice;

import com.braintribe.model.generic.annotation.Abstract;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.securityservice.messages.AuthenticationResponseMessage;
import com.braintribe.model.securityservice.messages.AuthenticationStatusMessage;

/** @deprecated re-added because it's used in GWT */
@Deprecated
@Abstract
public interface AbstractAuthenticationResponse extends SecurityResponse {

	EntityType<AbstractAuthenticationResponse> T = EntityTypes.T(AbstractAuthenticationResponse.class);

	boolean getSuccessful();
	void setSuccessful(boolean successful);

	AuthenticationStatusMessage getStatusMessage();
	void setStatusMessage(AuthenticationStatusMessage statusMessage);

	Set<AuthenticationResponseMessage> getMessages();
	void setMessages(Set<AuthenticationResponseMessage> messages);

}
