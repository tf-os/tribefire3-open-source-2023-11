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

import java.util.NoSuchElementException;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.space.WireSpace;

/**
 * A WireContext manages a set of {@link WireSpace ManagedSpaces}. There is a master {@link WireSpace} that transitively {@link Import imports}
 * other {@link WireSpace ManagedSpaces} that together make up the set of spaces. Using the Wire library one can build WireContext with a {@link WireContextBuilder}
 * coming from com.braintribe.wire.api.Wire.context(Class&lt;S&gt;) 
 * @author dirk.scheffler
 *
 * @param <S> the type of the master {@link WireSpace contract}
 */
public interface WireContext<S extends WireSpace> extends AutoCloseable {
	
	/**
	 * @return the space for the master {@link WireSpace contract} of this context
	 */
	S contract();
	
	/**
	 * @return The space for the given {@link WireSpace contract} of this context.
	 * @param wireContractClass the class of the contract to be resolved
	 * @throws NoSuchElementException if the contract is not mapped to a space in this context.
	 */
	<C extends WireSpace> C contract(Class<C> wireContractClass);

	/**
	 * finds spaces that are transitively found in the {@link #contract() master} {@link WireSpace}
	 * @param wireSpace The type of the requested {@link WireSpace}
	 * @return The space identified by the type
	 */
	<T extends WireSpace> T findContract(Class<T> wireSpace);
	
	/**
	 * finds the module that hosts the given {@link WireSpace} if there is any otherwise null is returned.
	 */
	<T extends WireSpace> WireModule findModuleFor(Class<T> wireSpace);
	
	/**
	 * Shuts down the context and therefore closes scopes which will lead to destruction of the related managed instances.
	 */
	void shutdown();
	
	@Override
	default void close() {
		shutdown();
	}
	
	void close(ScopeContext scopeContext);
	
	InstancePath currentInstancePath();
	

	/**
	 * @deprecated use {@link #contract()} instead
	 */
	@Deprecated
	default S beans() {
		return contract();
	}
	

}
