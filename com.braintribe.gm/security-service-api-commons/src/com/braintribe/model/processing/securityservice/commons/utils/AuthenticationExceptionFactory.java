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
package com.braintribe.model.processing.securityservice.commons.utils;

import java.util.function.Function;

import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.gm.model.security.reason.SessionExpired;
import com.braintribe.gm.model.security.reason.SessionNotFound;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.securityservice.api.exceptions.ExpiredSessionException;
import com.braintribe.model.processing.securityservice.api.exceptions.InvalidCredentialsException;
import com.braintribe.model.processing.securityservice.api.exceptions.SessionNotFoundException;

public class AuthenticationExceptionFactory implements Function<Reason, RuntimeException> {
	@Override
	public RuntimeException apply(Reason reason) {
		if (reason instanceof AuthenticationFailure) {
			return transpose((AuthenticationFailure)reason);
		}
		else {
			return new ReasonException(reason);
		}
	}

	private AuthenticationException transpose(AuthenticationFailure reason) {
		
		ReasonException reasonException = new ReasonException(reason);
		
		if (reason instanceof InvalidCredentials) {
			return new InvalidCredentialsException(reason.getText(), reasonException);
		}
		else if (reason instanceof SessionNotFound) {
			return new SessionNotFoundException(reason.getText(), reasonException);
		}
		else if (reason instanceof SessionExpired) {
			return new ExpiredSessionException(reason.getText(), reasonException);
		}
		else 
			return new AuthenticationException(reason.getText(), reasonException);
	}
}
