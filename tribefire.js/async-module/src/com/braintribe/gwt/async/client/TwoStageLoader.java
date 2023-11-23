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

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TwoStageLoader<T> {
	private static class QueueEntry<T> {
		private Loader<T> loader;
		private AsyncCallback<T> asyncCallback;
		public QueueEntry(Loader<T> loader, AsyncCallback<T> asyncCallback) {
			super();
			this.loader = loader;
			this.asyncCallback = asyncCallback;
		}
		
		public AsyncCallback<T> getAsyncCallback() {
			return asyncCallback;
		}
	}
	
	private QueueEntry<T> loadingEntry;
	private QueueEntry<T> waitingEntry;

	
	public void loadFrom(Loader<T> loader, AsyncCallback<T> asyncCallback) {
		load(new QueueEntry<T>(loader, asyncCallback));
	}
	
	private void load(QueueEntry<T> entry) {
		if (loadingEntry == null) {
			loadingEntry = entry;
			loadingEntry.loader.load(asyncCallback);
		}
		else {
			if (waitingEntry instanceof CancelListener) {
				CancelListener cancelListener = (CancelListener)waitingEntry;
				cancelListener.onCanceled();
			}
			waitingEntry = entry;
		}
	}
	
	private AsyncCallback<T> asyncCallback = new AsyncCallback<T>() {
		@Override
		public void onSuccess(T result) {
			loadingEntry.getAsyncCallback().onSuccess(result);
			next();
		}
		
		@Override
		public void onFailure(Throwable caught) {
			loadingEntry.getAsyncCallback().onFailure(caught);
			next();
		}
	};
	
	private void next() {
		if (waitingEntry != null) {
			loadingEntry = waitingEntry;
			waitingEntry = null;
			loadingEntry.loader.load(asyncCallback);
		} else
			loadingEntry = null;
	}
}
