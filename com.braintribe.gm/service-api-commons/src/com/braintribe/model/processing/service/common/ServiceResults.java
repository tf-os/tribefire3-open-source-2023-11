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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.service.api.result.Unsatisfied;

/**
 * <p>
 * This class consists exclusively of static methods that operate on or return {@link ServiceResult ServiceResults}.
 * 
 */
public class ServiceResults {

	private ServiceResults() {
	}

	public static <T> T evaluate(ServiceResult serviceResult) {

		Objects.requireNonNull(serviceResult, "serviceResult must not be null");

		ResponseEnvelope envelope = serviceResult.asResponse();

		if (envelope != null) {
			return (T) envelope.getResult();
		}

		Failure failure = serviceResult.asFailure();

		if (failure != null) {
			Throwable throwable = decodeFailure(failure);
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			} else if (throwable instanceof Error) {
				throw (Error) throwable;
			} else {
				throw new UndeclaredThrowableException(throwable);
			}
		}

		throw new IllegalStateException("Unexpected " + ServiceResult.class.getName() + " type: " + serviceResult);

	}
	
	public static ServiceResult fromMaybe(Maybe<?> maybe) {
		return maybe.isSatisfied()? //
				envelope(maybe.get()): //
				Unsatisfied.from(maybe);
	}

	public static ResponseEnvelope envelope(Object result) {
		ResponseEnvelope envelope = ResponseEnvelope.T.create();
		envelope.setResult(result);
		return envelope;
	}

	public static Throwable decodeFailure(Failure failure) {
		Throwable throwable = FailureCodec.INSTANCE.decode(failure);
		return throwable;
	}

	public static Failure encodeFailure(Throwable throwable) {
		Failure failure = FailureCodec.INSTANCE.encode(throwable);
		return failure;
	}

}
