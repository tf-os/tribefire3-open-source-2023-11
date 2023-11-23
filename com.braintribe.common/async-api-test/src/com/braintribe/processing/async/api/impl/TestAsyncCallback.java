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
package com.braintribe.processing.async.api.impl;

import java.util.concurrent.LinkedBlockingQueue;

import com.braintribe.processing.async.api.AsyncCallback;

public class TestAsyncCallback implements AsyncCallback<String> {

	private String value;
	private Throwable error;
	private LinkedBlockingQueue<String> resultQueue;

	public TestAsyncCallback(LinkedBlockingQueue<String> resultQueue) {
		this.resultQueue = resultQueue;
	}
	
	@Override
	public void onSuccess(String value) {
		this.value = value;
		resultQueue.add(value);
	}

	@Override
	public void onFailure(Throwable error) {
		this.error = error;
	}


	public String getValue() {
		return value;
	}

	public Throwable getError() {
		return error;
	}
}
