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
package com.braintribe.gwt.async.client;

import java.util.Iterator;

import com.google.gwt.user.client.Timer;

public class ForEach<T> {
	private long synchedTimeSpan = 1000;
	private int asyncDelay = 100;
	private Iterator<T> iterator;
	private Processor<T> processor;
	
	public ForEach(Iterable<T> iterable) {
		this.iterator = iterable.iterator();
	}
	
	public ForEach<T> setSynchedTimeSpan(long synchedTimeSpan) {
		this.synchedTimeSpan = synchedTimeSpan;
		return this;
	}
	
	public ForEach<T> setAsyncDelay(int asyncDelay) {
		this.asyncDelay = asyncDelay;
		return this;
	}
	
	public Future<Void> process(Processor<T> processor) {
		this.processor = processor;
		Future<Void> future = new Future<Void>();
		processSynched(future);
		return future;
	}
	
	protected void processSynched(final Future<Void> future) {
		long start = System.currentTimeMillis();
		
		while (iterator.hasNext()) {
			T element = iterator.next();
			try {
				processor.process(element);
			}
			catch (CanceledException e) {
				future.onSuccess(null);
			}
			catch (Throwable e) {
				future.onFailure(e);
			}
			long end = System.currentTimeMillis();
			if (end - start > synchedTimeSpan) {
				new Timer() {
					@Override
					public void run() {
						processSynched(future);
					}
				}.schedule(asyncDelay);
				break;
			}
		}
	}
}
