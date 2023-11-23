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
package com.braintribe.model.securityservice.messages;

import java.util.Set;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.credentials.HasUserIdentification;
import com.braintribe.model.securityservice.credentials.UserPasswordCredentials;
import com.braintribe.model.securityservice.credentials.identification.EmailIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserIdentification;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;

/** @deprecated re-adding as used in GWT */
@Deprecated
public interface UnsupportedCredentialsMessage extends AuthenticationStatusMessage {

	EntityType<UnsupportedCredentialsMessage> T = EntityTypes.T(UnsupportedCredentialsMessage.class);

	/**
	 * <p>Gets the type signatures of the {@link Credentials} types supported by the component which responded with this message.
	 *
	 * @return
	 *   The type signatures of the {@link Credentials} types supported by the component which responded with this message.
	 */
	Set<String> getSupportedCredentials();
	
	/**
	 * <p>Sets the type signatures of the {@link Credentials} types supported by the component which responded with this message.
	 *
	 * @param supportedCredentials
	 *   The type signatures of the {@link Credentials} types supported by the component which responded with this message.
	 */
	void setSupportedCredentials(Set<String> supportedCredentials);

	/**
	 * <p>Gets the type signatures of the {@link UserIdentification} types supported by component the which responded with this message.
	 * 
	 * <p>If a supported {@link Credentials} type implements {@link HasUserIdentification}, components may still respond with a 
	 * {@code UnsupportedCredentialsMessage} in case a particular {@link UserIdentification} type is not supported alongside the supported credentials.
	 * 
	 * <p>e.g.: A component may choose to support {@link UserPasswordCredentials} only when associated with {@link UserNameIdentification}, 
	 *    responding with a {@code UnsupportedCredentialsMessage} if the supported credentials contained a different {@link UserIdentification}
	 *    type, like {@link EmailIdentification}.
	 * 
	 * @return
	 *   The type signatures of the {@link UserIdentification} types supported by component the which responded with this message.
	 */
	Set<String> getSupportedUserIdentifications();
	
	/**
	 * <p>Sets the type signatures of the {@link UserIdentification} types supported by component the which responded with this message.
	 * 
	 * @see #getSupportedUserIdentifications()
	 * 
	 * @param supportedUserIdentifications
	 *   The type signatures of the {@link UserIdentification} types supported by component the which responded with this message.
	 */
	void setSupportedUserIdentifications(Set<String> supportedUserIdentifications);

}
