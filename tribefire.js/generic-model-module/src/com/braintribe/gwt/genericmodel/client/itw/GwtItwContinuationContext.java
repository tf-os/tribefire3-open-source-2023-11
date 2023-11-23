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
package com.braintribe.gwt.genericmodel.client.itw;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;


public class GwtItwContinuationContext {
	private final GmMetaModel model;
	private final long workerSliceThresholdInMs = 100;
	private final GwtItwContinuationFragment anchor = new GwtItwContinuationFragment();
	private GwtItwContinuationFragment last = anchor;
	private final Map<GmType, GenericModelType> types = newMap();
	private final Map<GmType, GenericModelType> newTypes = newMap();
	
	public GwtItwContinuationContext(GmMetaModel model) {
		this.model = model;
	}

	public void pushFragment(GwtItwContinuationFragment fragment) {
		last = last.next = fragment;
	}
	
	public Future<Void> execute(GwtItwContinuationFragment masterFragment) {
		final Future<Void> future = new Future<Void>();
		
		pushFragment(masterFragment);
		
		// start worker "thread"
		Scheduler.get().scheduleIncremental(new RepeatingCommand() {
			private  GwtItwContinuationFragment fragment = anchor.next;
			
			@Override
			public boolean execute() {
				try {
					GwtItwContinuationContext context = GwtItwContinuationContext.this;
					long s = System.currentTimeMillis();
					GwtItwContinuationFragment fragment = this.fragment; 
					while (fragment != null) {
						fragment.execute(context);
						fragment = fragment.next;

						long delta = System.currentTimeMillis() - s;
						
						if (delta > workerSliceThresholdInMs) {
							this.fragment = fragment; 
							return true;
						}
					}					
					
					future.onSuccess(null);
					return false;

				} catch (Exception e) {
					future.onFailure(new GmSessionException("Error while executing ITW fragment when weaving model: " + model.getName(), e));
					return false;
				}
			}
		});

		return future;
	}

	public void executeSync(GwtItwContinuationFragment masterFragment) {
		pushFragment(masterFragment);

		GwtItwContinuationFragment fragment = this.anchor.next;
		
		try {
			while (fragment != null) {
				fragment.execute(this);
				fragment = fragment.next;
			}
		} catch (Exception e) {
			throw new RuntimeException("Error while doing ITW: " + e.getMessage(), e);
		}
	}
	
	public GenericModelType findType(GmType gmType) {
		return types.get(gmType);
	}

	public void registerType(GmType gmType, GenericModelType type, boolean newType) {
		types.put(gmType, type);
		if (newType)
			newTypes.put(gmType, type);
	}
}
