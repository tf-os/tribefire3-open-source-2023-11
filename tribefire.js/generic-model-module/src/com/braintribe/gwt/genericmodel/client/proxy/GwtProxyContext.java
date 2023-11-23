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
package com.braintribe.gwt.genericmodel.client.proxy;

import java.util.Iterator;

import com.braintribe.codec.CodecException;
import com.braintribe.model.generic.proxy.DeferredApplier;
import com.braintribe.model.generic.proxy.ProxyContext;
import com.braintribe.processing.async.api.AsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

public class GwtProxyContext extends ProxyContext {
	@Override
	public void resolveProxiesAndApply(AsyncCallback<Void> callback) {
		AsyncProxyApplication application = new AsyncProxyApplication(appliers.iterator(), callback);
		application.start();
	}
	
	private class AsyncProxyApplication implements RepeatingCommand {
		private Iterator<DeferredApplier> it;
		private AsyncCallback<Void> callback;
		
		public AsyncProxyApplication(Iterator<DeferredApplier> iterator, AsyncCallback<Void> callback) {
			super();
			this.it = iterator;
			this.callback = callback;
		}

		public void start() {
			Scheduler.get().scheduleIncremental(this);
		}
		
		@Override
		public boolean execute() {
			long s = System.currentTimeMillis();
			try {
				while (it.hasNext()) {
					
						DeferredApplier applier = it.next();
						applier.apply();
					
					long d = System.currentTimeMillis() - s;
					
					if (d > 100 && it.hasNext())
						return true;
				}
			} catch (Exception e) {
				callback.onFailure(new CodecException("error while decoding js fragment", e));
			}

			return false;
		}
	}

}
