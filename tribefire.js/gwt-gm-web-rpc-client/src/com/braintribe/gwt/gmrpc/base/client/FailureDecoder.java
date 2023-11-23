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
package com.braintribe.gwt.gmrpc.base.client;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.braintribe.common.lcd.StackTraceCodec;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.exception.CommunicationException;
import com.braintribe.exception.GenericServiceException;
import com.braintribe.gwt.gmrpc.api.client.exception.GmRpcException;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceProcessorNotificationException;
import com.braintribe.model.service.api.result.Failure;

/**
 * <p>
 * A GWT compatible {@link Failure} to {@link Throwable} conversion function.
 */
public class FailureDecoder implements Function<Failure, Throwable> {

	private ExceptionFactory exceptionFactory;

	/**
	 * Initializes the {@link FailureDecoder}.
	 */
	public FailureDecoder() {
	}

	/**
	 * Initializes the {@link FailureDecoder} with an {@link ExceptionFactory}.
	 */
	public FailureDecoder(ExceptionFactory exceptionFactory) {
		this.exceptionFactory = exceptionFactory;
	}

	@Override
	public Throwable apply(Failure failure) {
		Throwable throwable = decode(failure, new IdentityHashMap<Failure, Throwable>());
		return throwable;
	}

	private Throwable decode(Failure failure, Map<Failure, Throwable> created) {

		if (failure == null) {
			return null;
		}

		Throwable exception = created.get(failure);

		if (exception != null) {
			return exception;
		}

		Throwable cause = null;

		Failure failureCause = failure.getCause();
		if (failureCause != null) {
			cause = decode(failureCause, created);
		}

		if (exceptionFactory != null) {
			try {
				exception = exceptionFactory.createException(failure.getType(), failure.getMessage());
				if (exception != null && cause != null) {
					exception.initCause(cause);
				}
			} catch (Exception e) {
				// Generic is created if emulation fails or returns null.
			}
		}

		if (exception == null) {
			exception = createException(failure, cause);
		}

		created.put(failure, exception);

		StackTraceElement[] stackTrace = StackTraceCodec.INSTANCE.decode(failure.getDetails());
		if (stackTrace != null && stackTrace.length != 0) {
			exception.setStackTrace(stackTrace);
		}

		List<Failure> suppressedList = failure.getSuppressed();
		if (suppressedList != null) {
			for (Failure suppressed : suppressedList) {
				Throwable suppressedThrowable = decode(suppressed, created);
				if (suppressedThrowable != null) {
					exception.addSuppressed(suppressedThrowable);
				}
			}
		}

		return exception;

	}
	
	private static Map<String, Function<String, ? extends Throwable>> standardExceptionFactories = new HashMap<>();
	
	private static <T extends Throwable> void registerStandardExceptionFactory(Class<T> exceptionClass, Function<String, T> factory) {
		standardExceptionFactories.put(exceptionClass.getName(), factory);
	}
	
	static {
		registerStandardExceptionFactory(AuthorizationException.class, AuthorizationException::new);
		registerStandardExceptionFactory(CommunicationException.class, CommunicationException::new);
		registerStandardExceptionFactory(GmRpcException.class, GmRpcException::new);
		registerStandardExceptionFactory(ServiceProcessorException.class, ServiceProcessorException::new);
		registerStandardExceptionFactory(ServiceProcessorNotificationException.class, ServiceProcessorNotificationException::new);
		registerStandardExceptionFactory(NoSuchElementException.class, NoSuchElementException::new);
		registerStandardExceptionFactory(UnsupportedOperationException.class, UnsupportedOperationException::new);
		registerStandardExceptionFactory(IllegalStateException.class, IllegalStateException::new);
		registerStandardExceptionFactory(IllegalArgumentException.class, IllegalArgumentException::new);
		registerStandardExceptionFactory(NullPointerException.class, NullPointerException::new);
	}

	public Throwable createException(Failure failure, Throwable cause) {

		String typeSignature = failure.getType();
		String message = failure.getMessage();
		
		Throwable throwable = standardExceptionFactories
				.getOrDefault(typeSignature, GenericServiceException::new)
				.apply(message);
		
		throwable.initCause(cause);
		
		if (throwable instanceof ServiceProcessorNotificationException) {
			ServiceProcessorNotificationException serviceProcessorNotificationException = (ServiceProcessorNotificationException)throwable;
			serviceProcessorNotificationException.setNotification(failure.getNotification());
		}

		return throwable;

	}

}
