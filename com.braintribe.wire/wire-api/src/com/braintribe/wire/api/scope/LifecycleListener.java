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
package com.braintribe.wire.api.scope;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

/**
 * LifecycleListeners can be used to call specific lifecycle methods on managed instances that are defined in interfaces or by annotations. 
 * The calling of LifecycleListeners is triggered by {@link InstanceHolder} and {@link WireScope} associated with a {@link Managed} annotated method. 
 * LifecycleListeners can be configured on a {@link WireContext} during {@link WireSpace#onLoaded(com.braintribe.wire.api.context.WireContextConfiguration)}
 * 
 * @see WireContextConfiguration#addLifecycleListener(LifecycleListener)
 * @author dirk.scheffler
 *
 */
public interface LifecycleListener {
	/**
	 * This method will be called on a registered LifecycleListener right before a managed instance is returned from a {@link Managed} annotated method
	 * @param instanceHolder the holder that holds the managed instance
	 * @param instance The managed instance that has been constructed
	 */
	void onPostConstruct(InstanceHolder instanceHolder, Object instance);
	
	/**
	 * This method will be called on a registered LifecycleListener when the owning {@link WireScope} is closed 
	 * @param instanceHolder the holder that holds the managed instance
	 * @param instance The managed instance that will be destroyed
	 */
	void onPreDestroy(InstanceHolder instanceHolder, Object instance);
}
