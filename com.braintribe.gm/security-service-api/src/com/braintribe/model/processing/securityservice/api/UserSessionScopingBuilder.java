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

import java.util.concurrent.Callable;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;

public interface UserSessionScopingBuilder {
	
	UserSessionScope push() throws SecurityServiceException;
	
	void runInScope(Runnable runnable) throws SecurityServiceException;
	
	default Runnable scoped(Runnable runnable) {
		return () -> runInScope(runnable);
	}
	
	default <T> Callable<T> scoped(Callable<T> callable) {
		return () -> {
			
			class PromiseRunnable implements Runnable {
				Throwable throwable;
				T result;
				
				@Override
				public void run() {
					try {
						result = callable.call();
					} catch(Throwable e) {
						throwable = e;
					}
				}
			}
			PromiseRunnable runnable = new PromiseRunnable();
			
			runInScope(runnable);
			
			if (runnable.throwable != null) {
				Exception exception = Exceptions.normalizer(runnable.throwable).asExceptionOrThrowUnchecked();
				throw exception;
			} else {
				return runnable.result;
			}

		};
	}
}
