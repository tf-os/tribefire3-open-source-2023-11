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
package com.braintribe.exception;

public class ThrowableNormalizer {

	private final Throwable throwable;
	
	private boolean checked = false;
	private boolean isException = false;
	private boolean isError = false;

	public ThrowableNormalizer(Throwable throwable) {
		this.throwable = throwable;
		if (throwable instanceof Error) {
			checked = false;
			isException = false;
			isError = true;
		} else if (throwable instanceof RuntimeException) {
			checked = false;
			isException = true;
			isError = false;
		} else if (throwable instanceof Exception) {
			checked = true;
			isException = true;
			isError = false;
		} else {
			checked = true;
			isException = false;
			isError = false;
		}
	}
	
	public RuntimeException asRuntimeException() {
		if (isException && !checked) {
			return (RuntimeException) throwable;
		} else {
			return new RuntimeException(throwable);
		}
	}
	
	public Exception asException() {
		if (isException) {
			return (Exception) throwable;
		} else {
			return new Exception(throwable);
		}
	}
	
	public Exception asExceptionOrThrowUnchecked() throws RuntimeException, Error {
		if (isException && !checked) {
			throw (RuntimeException) throwable;
		} else if (isError) {
			throw (Error) throwable;
		} else {
			return asException();
		}
	}
	
	public Throwable asThrowableOrThrowUnchecked() throws RuntimeException, Error {
		if (isException && !checked) {
			throw (RuntimeException) throwable;
		} else if (isError) {
			throw (Error) throwable;
		} else {
			return throwable;
		}
	}
	
	public boolean isError() {
		return isError;
	}
	public boolean isChecked() {
		return checked;
	}
	public boolean isException() {
		return isException;
	}

}
