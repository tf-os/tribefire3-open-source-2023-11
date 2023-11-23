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
package com.braintribe.wire.api.space;

import java.util.function.Consumer;

import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.reflect.EmptyWireSpaceReflection;
import com.braintribe.wire.api.reflect.WireSpaceReflection;

/**
 * WireSpace subclasses are the namespaces for managed instance factory methods.
 * Such subclasses may be interfaces and in this case the WireSpace is a contract.
 * If the subclass is a class it really implements factory methods which are to be
 * annotated with the {@link Managed} annotation then.
 * 
 * The factory methods follow a certain pattern.
 * 
 * <pre>
 * <code>
 * 		
 *  {@literal @}Managed 
 *  public TypeOfInstance instanceName()
 *    // right here will be the lock and identity management done by Wire's bytecode enrichment
 *  
 *    // creation with constructor or other ways
 *    TypeOfInstance instance = new TypeOfInstance();
 *    
 *    // right here will be the eager publishing done by Wire's bytecode enrichment
 * 
 *    // preparation of the instance
 *    instance.setX("foo");
 *    instance.setY("bar");
 *    instance.addZ(1);
 *    instance.addZ(2);
 *    instance.addZ(3);
 * 
 *    // right here will be the post contruct notification done by Wire's bytecode enrichment
 *    
 *    // return the prepared
 *    return instance;
 *    
 *    // right here will be the unlocking done by Wire's bytecode enrichment
 *  }
 * </code>
 * </pre>
 * 
 * @author dirk.scheffler
 *
 */
public interface WireSpace {
	/**
	 * This method will be called right after the WireSpace was created and its imports were loaded.
	 * A WireSpace can configure additional expertise on the {@link WireContextConfiguration}. 
	 */
	default void onLoaded(@SuppressWarnings("unused") WireContextConfiguration configuration) {
		//intentionally left empty
	}
	
	default WireSpaceReflection reflect() {
		return EmptyWireSpaceReflection.INSTANCE;
	}
	
}
