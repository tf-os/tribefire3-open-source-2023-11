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
package com.braintribe.wire.api.context;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;

/**
 * This interface is used internally by {@link InstanceHolder} implementations
 * @author dirk.scheffler
 *
 */
public interface InternalWireContext {
	/**
	 * Resolves a {@link WireSpace} by its class. If it is already loaded it will be returned 
	 * otherwise it will be loaded and then returned.
	 */
	<T extends WireSpace> T resolveSpace(Class<T> class1);
	
	/**
	 * Resolves a {@link WireScope} by its class. If it is already instantiated it will be returned 
	 * otherwise it will be instantiated and then returned.
	 */
	WireScope getScope(Class<? extends WireScope> scopeClass);
	
	/**
	 * {@link InstanceHolder} implementations should forward their according notifications to this method
	 * @param instanceHolder the holder that is used to manage the instance
	 * @param instance The instance being destroyed
	 */
	void onPostConstruct(InstanceHolder instanceHolder, Object instance);
	
	/**
	 * {@link InstanceHolder} implementations should forward their according notifications to this method
	 * @param instanceHolder the holder that is used to manage the instance
	 * @param instance The instance being destroyed
	 */
	void onPreDestroy(InstanceHolder instanceHolder, Object instance);

	boolean lockCreation(InstanceHolder instanceHolder);

	void unlockCreation(InstanceHolder instanceHolder);
	
	InstancePath currentInstancePath();
	
	ScopeContextHolders getScopeForContext(ScopeContext context);
	
	void close(ScopeContext scopeContext);
}
