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
package com.braintribe.model.generic.eval;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.logging.Logger;
import com.braintribe.processing.async.api.AsyncCallback;

/**
 * Callback which logs an error, which is either the {@link Throwable} passed to {@link #onFailure(Throwable)}, or the
 * {@link Maybe#whyUnsatisfied() reason} why the result is {@link Maybe#isUnsatisfied() unsatisfied}.
 * 
 * @author peter.gazdik
 */
public class ErrorLoggingCallback<R> implements AsyncCallback<Maybe<R>> {

	private static final Logger log = Logger.getLogger(ErrorLoggingCallback.class);

	private static final ErrorLoggingCallback<?> INSTANCE = new ErrorLoggingCallback<>();

	public static <R> ErrorLoggingCallback<R> instance() {
		return (ErrorLoggingCallback<R>) INSTANCE;
	}

	private ErrorLoggingCallback() {
	}

	@Override
	public void onSuccess(Maybe<R> result) {
		if (result.isUnsatisfied())
			log.error(result.whyUnsatisfied().stringify());
	}

	@Override
	public void onFailure(Throwable t) {
		log.error(t);
	}

}
