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

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.InvalidSession;
import com.braintribe.gm.model.security.reason.SessionExpired;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.securityservice.api.exceptions.ExpiredSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidCredentialsException;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.SessionNotFoundException;
import com.braintribe.model.processing.securityservice.api.exceptions.UserNotFoundException;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.UnsupportedRequestTypeException;
import com.braintribe.model.securityservice.SecurityRequest;

/**
 * <p>
 * ServiceProcessor that wraps the actualy Security ServiceProcessor and turns exceptions into reasons
 * @author Dirk Scheffler
 */
public class ReasonedSecurityServiceProcessor implements ReasonedServiceProcessor<SecurityRequest, Object> {

	private ServiceProcessor<SecurityRequest, ?> delegate;

	@Required
	@Configurable
	public void setDelegate(ServiceProcessor<SecurityRequest, ?> delegate) {
		this.delegate = delegate;
	}

	@Override
	public Maybe<? extends Object> processReasoned(ServiceRequestContext requestContext, SecurityRequest request) {
		try {
			Object response = delegate.process(requestContext, request);
			return Maybe.complete(response);
		} //
		catch (SessionNotFoundException e) {
			return Reasons.build(SessionNotFound.T).text(e.getMessage()).toMaybe();
		} //
		catch (ExpiredSessionException e) {
			return Reasons.build(SessionExpired.T).text(e.getMessage()).toMaybe();
		} //
		catch (InvalidSessionException e) {
			return Reasons.build(InvalidSession.T).text(e.getMessage()).toMaybe();
		} //
		catch (InvalidCredentialsException | UserNotFoundException e) {
			return Reasons.build(InvalidCredentials.T).text(e.getMessage()).toMaybe();
		} //
		catch (AuthenticationException e) {
			return Reasons.build(AuthenticationFailure.T).text(e.getMessage()).toMaybe();
		} // 
		catch (UnsupportedRequestTypeException e) {
			return Reasons.build(UnsupportedOperation.T).text(e.getMessage()).toMaybe();
		}
	}
}