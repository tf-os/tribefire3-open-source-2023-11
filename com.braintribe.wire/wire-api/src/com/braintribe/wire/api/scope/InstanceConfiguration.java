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
import com.braintribe.wire.api.space.WireSpace;

/**
 * An InstanceConfiguration stands for the additional configuration than can be made on a managed instance in construction.
 * If you are within a {@link Managed} annotated method in a {@link WireSpace} you can easily get the managed instance
 * to the according InstanceConfiguration by calling {@link #currentInstance()}. 
 * @author dirk.scheffler
 */
public interface InstanceConfiguration {
	/**
	 * Configures a callback method that will be called on destruction of the managed instance associated with the InstanceConfiguration
	 */
	void onDestroy(Runnable function);
	void closeOnDestroy(AutoCloseable closable);
	
	InstanceQualification qualification();
	
	/**
	 * This call will give access to the InstanceConfiguration (facet of {@link InstanceHolder}) to allow for further configuration.
	 * 
	 * The call is intentionally implemented by an exception here as the calling of it in {@link Managed} annotated methods
	 * will be replaced by the Wire's bytecode enricher to return the current {@link InstanceConfiguration} associated
	 * with the {@link InstanceHolder} that is managing the identity of the constructed instance. 
	 * @return
	 */
	static InstanceConfiguration currentInstance() { 
		throw new UnsupportedOperationException("this method call should have been replaced by Wire's bytecode enriching");
	};
}
