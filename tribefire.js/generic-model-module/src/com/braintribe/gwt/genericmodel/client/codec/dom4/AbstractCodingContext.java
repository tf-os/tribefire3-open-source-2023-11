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
package com.braintribe.gwt.genericmodel.client.codec.dom4;

import com.braintribe.codec.CodecException;
import com.braintribe.gwt.async.client.Future;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public class AbstractCodingContext {
	private DeferredProcessor anchorProcessor = new DeferredProcessor();
	protected DeferredProcessor lastProcessor = anchorProcessor;

	public void appendDeferredProcessor(DeferredProcessor processor) {
		lastProcessor = lastProcessor.next = processor;
	}

	
	protected void runDeferredProcessors() throws CodecException {
		DeferredProcessor deferredProcessor = anchorProcessor.next;
		
		while (deferredProcessor != null) {
			while (deferredProcessor.continueProcessing()) { /* Nothing to do here */ }
			deferredProcessor = deferredProcessor.next;
		}
	}

	protected Future<Void> runDeferredProcessorsAsync() {
		final Future<Void> future = new Future<Void>();
		
		Scheduler.get().scheduleIncremental(new RepeatingCommand() {
			
			private DeferredProcessor deferredProcessor = anchorProcessor.next;
			
			@Override
			public boolean execute() {
				DeferredProcessor deferredProcessor = this.deferredProcessor;
				
				try {
					long s = System.currentTimeMillis();
					while (deferredProcessor != null) {
						while (deferredProcessor.continueProcessing()) { /* Nothing to do here */ }
						
						long e = System.currentTimeMillis();
						long d = e - s;
						deferredProcessor = deferredProcessor.next;
						
						if (d > 15 && deferredProcessor != null) {
							this.deferredProcessor = deferredProcessor;
							return true;
						}
					}
					
					future.onSuccess(null);
					
				} catch (Exception e) {
					future.onFailure(e);
				}
				
				return false;
			}
		});
		
		return future;
	}

}
