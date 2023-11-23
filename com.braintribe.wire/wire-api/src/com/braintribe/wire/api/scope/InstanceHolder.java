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

/**
 * InstanceHolders need to be implemented by custom scopes in order to support the scoped identity management 
 * for {@link Managed} annotated methods. InstanceHolders are used by Wire's bytecode enriching to store the actual
 * managed instance according to the {@link WireScope} it stems from. Via {@link #publish(Object)} an
 * InstanceHolder also enables eager access to a managed instance when cyclic dependencies occur. 
 * @author dirk.scheffler
 *
 */
public interface InstanceHolder extends InstanceQualification {
	
	/**
	 * @return the actual instance managed by the holder
	 */
	Object get();
	
	/**
	 * Will be called by Wire's bytecode enhancements from {@link Managed} annotated methods right after the first
	 * assignment of the freshly created instance just before properties are applied to it 
	 */
	void publish(Object instance);
	
	/**
	 * Will be called by Wire's bytecode enhancements from {@link Managed} annotated methods right before it will be returned
	 * to the caller 
	 */
	void onPostConstruct(Object instance);
	
	/**
	 * Will be called by the according {@link WireScope} when the lifecycle of the managed instance is closing
	 */
	void onDestroy();
	
	/**
	 * Will be called by Wire's bytecode enhancements from {@link Managed} annotated methods just in the beginning of the method
	 * to make it threadsafe in the case the according {@link WireScope} supports that threadsafety.
	 */
	boolean lockCreation();
	
	/**
	 * Will be called by Wire's bytecode enhancements from {@link Managed} annotated methods at the end of the method
	 * to make it threadsafe in the case the according {@link WireScope} supports that threadsafety.
	 */
	void unlockCreation();
	
	
	void onCreationFailure(Throwable t);

	/**
	 * Returns the {@link InstanceConfiguration} that can be used to give addition information for the InstanceHolder. To actually
	 * address this method from a {@link Managed} annotated method you have to call {@link InstanceConfiguration#currentInstance()} which
	 * will be replaced by {@link #config()} by the Wire's bytecode enhancements.
	 */
	InstanceConfiguration config();
	
	/**
	 * @return the {@link WireScope} the InstanceHolder belongs to
	 */
	WireScope scope();
}
