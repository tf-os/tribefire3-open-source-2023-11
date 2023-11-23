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

import java.util.concurrent.Callable;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.gm.model.reason.essential.InternalError;
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

public interface SecurityExceptionsTrait {
	default <T> Maybe<T> executeUnreasoned(Callable<T> callable) {
		try {
			return Maybe.complete(callable.call());
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
		catch (UnsatisfiedMaybeTunneling e) {
			return e.getMaybe();
		}
		catch (UnsupportedOperationException e) {
			return Reasons.build(UnsupportedOperation.T).text(e.getMessage()).toMaybe();
		}
		catch (Exception e) {
			return InternalError.from(e).asMaybe();
		}
	}
}
