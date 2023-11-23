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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class UmbrellaFuture extends Future<Void>{
	
	private Future<Void> thisFuture = this;
	private List<Future<?>> futures = new ArrayList<Future<?>>();
	private UmbrellaException exceptions = new UmbrellaException(new HashSet<Throwable>());
	private boolean allFuturesGiven = false;
	
	public boolean addFuture(final Future<?> future){		
		future.get(new AsyncCallback<Object>() {			
			@Override
			public void onFailure(Throwable caught) {
				exceptions.getCauses().add(caught);
				if(processingHasFinished()){
					if(exceptions.getCauses().size() == 1)
						thisFuture.onFailure((Throwable) exceptions.getCauses().toArray()[0]);
					else if(exceptions.getCauses().size() > 1)	
						thisFuture.onFailure(exceptions);
				}
			}

			@Override
			public void onSuccess(Object result) {
				futures.remove(future);
				if(processingHasFinished())
					thisFuture.onSuccess(null);
			}
		});
		return futures.add(future);
	}
	
	public boolean removeFuture(Future<?> future){
		return futures.remove(future);
	}
	
	private boolean processingHasFinished(){
		return futures.size() == 0 && allFuturesGiven;
	}
	
	@Override
	public void get(AsyncCallback<? super Void> asyncCallback) {
		super.get(asyncCallback);
		allFuturesGiven = true;
	}
}
