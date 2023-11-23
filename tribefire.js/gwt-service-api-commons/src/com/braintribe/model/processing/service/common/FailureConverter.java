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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.common.lcd.StackTraceCodec;
import com.braintribe.model.processing.service.api.ServiceProcessorNotificationException;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;

/**
 * <p>
 * Converts {@link Throwable} to {@link Failure}.
 */
public class FailureConverter implements Function<Throwable, Failure> {

	public final static FailureConverter INSTANCE = new FailureConverter();

	private FailureConverter() {
	}

	@Override
	public Failure apply(Throwable throwable) {

		if (throwable == null) {
			return null;
		}

		Failure failure = createFailure(throwable, new IdentityHashMap<Throwable, Failure>());
		return failure;

	}

	private Failure createFailure(Throwable throwable, Map<Throwable, Failure> created) {

		Failure failure = created.get(throwable);

		if (failure != null) {
			return failure;
		}

		failure = Failure.T.create();
		failure.setType(throwable.getClass().getName());
		failure.setMessage(throwable.getMessage());
		failure.setDetails(StackTraceCodec.INSTANCE.encode(throwable.getStackTrace()));

		created.put(throwable, failure);

		Throwable cause = throwable.getCause();

		if (cause != null) {
			Failure failureCause = createFailure(cause, created);
			failure.setCause(failureCause);
		}

		if (throwable instanceof ServiceProcessorNotificationException) {
			ServiceRequest notification = ((ServiceProcessorNotificationException) throwable).getNotification();
			if (notification != null) {
				failure.setNotification(notification);
			}
		}

		Throwable[] suppressedExceptions = throwable.getSuppressed();
		if (suppressedExceptions != null && suppressedExceptions.length > 0) {
			List<Failure> suppressed = failure.getSuppressed();
			for (Throwable suppressedException : suppressedExceptions) {
				if (suppressedException != null) {
					Failure suppressedFailure = createFailure(suppressedException, created);
					suppressed.add(suppressedFailure);
				}
			}
		}

		return failure;

	}

}
