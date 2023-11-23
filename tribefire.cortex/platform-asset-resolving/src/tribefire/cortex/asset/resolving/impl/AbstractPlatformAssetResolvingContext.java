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
package tribefire.cortex.asset.resolving.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.impl.managed.BasicManagedGmSession;

import tribefire.cortex.asset.resolving.api.PlatformAssetResolvingContext;

public abstract class AbstractPlatformAssetResolvingContext implements PlatformAssetResolvingContext {
	
	private ManagedGmSession session = new BasicManagedGmSession();
	private Map<Class<?>, Object> sharedInfos = new HashMap<>();

	@Override
	public ManagedGmSession session() {
		return session;
	}
	
	@Override
	public <C> C findSharedInfo(Class<C> key) {
		return (C) sharedInfos.get(key);
	}
	
	@Override
	public <C> C getSharedInfo(Class<C> key, Supplier<C> supplier) {
		return (C) sharedInfos.computeIfAbsent(key, k -> supplier.get());
	}
}
