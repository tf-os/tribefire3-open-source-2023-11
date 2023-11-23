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

import java.util.IdentityHashMap;
import java.util.Map;

import com.braintribe.codec.Codec;
import com.braintribe.codec.CodecException;
import com.braintribe.common.ExceptionBuilder;
import com.braintribe.common.lcd.StackTraceCodec;
import com.braintribe.model.processing.service.api.ServiceProcessorNotificationException;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;

/**
 * <p>
 * Codec for converting {@link Throwable} to {@link Failure} and vice-versa.
 */
public class FailureCodec implements Codec<Throwable, Failure> {

	public final static FailureCodec INSTANCE = new FailureCodec();

	private FailureCodec() {
	}

	@Override
	public Failure encode(Throwable throwable) {
		Failure failure = FailureConverter.INSTANCE.apply(throwable);
		return failure;
	}

	@Override
	public Throwable decode(Failure failure) throws CodecException {
		Throwable throwable = createThrowable(failure, new IdentityHashMap<Failure, Throwable>());
		return throwable;
	}

	@Override
	public Class<Throwable> getValueClass() {
		return Throwable.class;
	}

	private Throwable createThrowable(Failure failure, Map<Failure, Throwable> created) {

		if (failure == null) {
			return null;
		}

		Throwable exception = created.get(failure);

		if (exception != null) {
			return exception;
		}

		exception = ExceptionBuilder.createException(failure.getType(), failure.getMessage(), createThrowable(failure.getCause(), created));

		if (exception instanceof ServiceProcessorNotificationException) {
			ServiceRequest notification = failure.getNotification();
			if (notification!= null) {
				((ServiceProcessorNotificationException)exception).setNotification(notification);
			}
		}

		StackTraceElement[] stackTrace = StackTraceCodec.INSTANCE.decode(failure.getDetails());
		if (stackTrace != null) {
			exception.setStackTrace(stackTrace);
		}

		created.put(failure, exception);

		for (Failure suppressed : failure.getSuppressed()) {
			Throwable suppressedThrowable = createThrowable(suppressed, created);
			if (suppressedThrowable != null) {
				exception.addSuppressed(suppressedThrowable);
			}
		}

		return exception;
	}

}
