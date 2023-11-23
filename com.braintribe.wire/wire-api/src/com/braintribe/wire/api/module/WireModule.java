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
package com.braintribe.wire.api.module;

import java.util.Collections;
import java.util.List;

import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.space.WireSpace;

/**
 * A WireModule helps to build a reproducible wire context
 * 
 * @author Dirk Scheffler
 * */
public interface WireModule {
	default List<WireModule> dependencies() { 
		return Collections.emptyList(); 
	}
	
	/**
	 * This method is being called when the module is applied to some context because it was a transitive
	 * dependency of the {@link WireTerminalModule} with which the {@link WireContext} was been built.
	 * 
	 * @param contextBuilder on this builder the module can configure its aspects
	 */
	default void configureContext(WireContextBuilder<?> contextBuilder) {
		contextBuilder.bindContracts(getClass().getPackage().getName());
	}
}
