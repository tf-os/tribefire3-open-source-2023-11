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
package com.braintribe.model.processing.securityservice.api;

import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.securityservice.UserAuthenticationResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.model.securityservice.messages.UnsupportedCredentialsMessage;

public interface AuthenticationService {
	
	/**
	 * <p>Performs authentication with the given {@code credentials}.</p>
	 * 
	 * <p>If the given {@link Credentials} type <b>is</b> supported by the implementation, this method must:</p>
	 * 
	 * <ul>
	 *   <li>Upon successful authentication
	 *     <ul>
	 *       <li>Return a {@link UserAuthenticationResponse} with:
	 *         <ul>
	 *           <li>{@code true} as the successful flag ({@link UserAuthenticationResponse#getSuccessful()})</li>
	 *           <li>A {@link com.braintribe.model.user.User} entity reference ({@link UserAuthenticationResponse#getUser()})</li>
	 *         </ul>
	 *       </li>
	 *     </ul>
	 *   </li>
	 *   <li>Upon authentication failure
	 *     <ul>
	 *       <li>Throw the appropriate {@link AuthenticationException}</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 * 
	 * <p>If the given {@link Credentials} type <b>is not</b> supported by the implementation, this method must either:</p>
	 * 
	 * <ul>
	 *   <li>Return {@code null}; or</li>
	 *   <li>Return a {@link UserAuthenticationResponse} with:
	 *     <ul>
	 *       <li>{@code false} as the successful flag ({@link UserAuthenticationResponse#getSuccessful()})</li>
	 *       <li>{@link UnsupportedCredentialsMessage} as the status message ({@link UserAuthenticationResponse#getStatusMessage()})</li>
	 *       <li>No {@link com.braintribe.model.user.User} reference ({@link UserAuthenticationResponse#getUser()})</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 * 
	 * @param credentials
	 * 
	 *   The credentials to be authenticated.
	 * 
	 * @return
	 * 
	 *   <p>{@link UserAuthenticationResponse}, which may be:
	 *   <ul>
	 *   	<li>{@code null}, indicating the given {@link Credentials} type <b>is not</b> supported by the implementation</li>
	 *   	<li>Successful ({@code true} returned by {@link UserAuthenticationResponse#getSuccessful()}), 
	 *          if the method authenticated the credentials and provided a User in the response.
	 *   
	 *   	<li>Unsuccessful ({@code false} returned by {@link UserAuthenticationResponse#getSuccessful()}), 
	 *          if the method decided not to handle the credentials for some reason, which may be specified via {@link UserAuthenticationResponse#getStatusMessage()}.
	 *          As opposed to an {@link AuthenticationException} being thrown, an unsuccessful response signals the caller to try a fallback authentication mechanism.
	 *   </ul>
	 *   
	 * @throws AuthenticationException
	 * 
	 *   If the given {@link Credentials} type is supported by the implementation, but the authentication failed.
	 *   Throwing a {@link AuthenticationException} signals the caller to not attempt any further authentication with the given credentials.
	 *   
	 */
	UserAuthenticationResponse authenticate(Credentials credentials) throws AuthenticationException;
	
}
