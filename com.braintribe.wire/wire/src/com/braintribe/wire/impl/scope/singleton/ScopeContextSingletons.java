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
package com.braintribe.wire.impl.scope.singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.scope.InstanceQualification;

public class ScopeContextSingletons implements ScopeContextHolders {
	private static final Logger logger = Logger.getLogger(ScopeContextSingletons.class.getName());
	protected Map<InstanceQualification, SingletonInstanceHolder> holders = new ConcurrentHashMap<InstanceQualification, SingletonInstanceHolder>();
	protected List<SingletonInstanceHolder> initializedHolders = new ArrayList<>();

	@Override
	public SingletonInstanceHolder acquireHolder(InstanceQualification keyHolder) {
		return holders.computeIfAbsent(keyHolder,
				k -> new SingletonInstanceHolder(keyHolder.space(), keyHolder.scope(), keyHolder.name(), this::append));
	}

	public void append(SingletonInstanceHolder holder) {
		synchronized (initializedHolders) {
			appendDirect(holder);
		}
	}

	private void appendDirect(SingletonInstanceHolder holder) {
		initializedHolders.add(holder);
	}

	@Override
	public void close() {
		synchronized (initializedHolders) {
			closeBeans();
		}
		holders.clear();
	}

	private void closeBeans() {
		for (ListIterator<SingletonInstanceHolder> it = initializedHolders.listIterator(initializedHolders.size()); it.hasPrevious();) {
			SingletonInstanceHolder holder = it.previous();
			try {
				logger.finest(() -> "Destroying bean " + holder.space().getClass().getName() + ":" + holder.name());
				holder.onDestroy();
			} catch (Exception e) {
				reportError(holder, e);
			} catch (IllegalAccessError e) {
				// This might occur during shutdown
				reportError(holder, e);
			} catch (Error e) {
				reportError(holder, e);
				throw e;
			}
		}
		logger.finest(() -> "Finished closing of beans: " + this.getClass().getName());
	}

	private void reportError(SingletonInstanceHolder holder, Throwable e) {
		logger.log(Level.SEVERE, "Exception while destroying bean " + holder.space().getClass().getName() + ":" + holder.name(), e);
	}
}