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

import com.braintribe.wire.api.scope.CreationListener;
import com.braintribe.wire.api.scope.LifecycleListener;
import com.braintribe.wire.api.space.WireSpace;

/**
 * This interface allows a {@link WireSpace} to influence the way a {@link WireContext} works. 
 * It is passed to the {@link WireSpace#onLoaded(WireContextConfiguration)} method in order to allow for eager configuration. 
 * @author dirk.scheffler
 *
 */
public interface WireContextConfiguration {
	/**
	 * configures a {@link LifecycleListener} that will be called on post construction and on destruction of managed instances
	 */
	void addLifecycleListener(LifecycleListener listener);
	
	/**
	 * removes a configured {@link LifecycleListener} from this context.
	 */
	void removeLifecycleListener(LifecycleListener listener);
	
	/**
	 * configures a {@link CreationListener} that will be called before and after construction of managed instances
	 */
	void addCreationListener(CreationListener listener);

	/**
	 * removes a configured {@link CreationListener} from this context.
	 */
	void removeCreationListener(CreationListener listener);
}
