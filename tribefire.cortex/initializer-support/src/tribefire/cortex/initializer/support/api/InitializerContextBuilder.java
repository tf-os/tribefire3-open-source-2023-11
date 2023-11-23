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
package tribefire.cortex.initializer.support.api;

import java.util.function.Supplier;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireTerminalModule;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.InitializerContextImpl;
import tribefire.cortex.initializer.support.wire.InitializerSupportWireModule;

public interface InitializerContextBuilder {

	/**
	 * Method for building {@link InitializerContext} that includes building of wire context which wires the initializer spaces.
	 */
	static <S extends WireSpace> WiredInitializerContext<S> build(WireTerminalModule<S> initializerModule, String initializerId,
			PersistenceInitializationContext context, WireContext<?> parentContext) {

		return build(initializerModule, () -> initializerId, context, parentContext);
	}

	/**
	 * Method for building {@link InitializerContext} that includes building of wire context which wires the initializer spaces.
	 */
	static <S extends WireSpace> WiredInitializerContext<S> build(WireTerminalModule<S> initializerModule, Supplier<String> initializerIdSupplier,
			PersistenceInitializationContext context, WireContext<?> parentContext) {

		InitializerSupportWireModule<S> iswm = new InitializerSupportWireModule<>(initializerModule, initializerIdSupplier, context, parentContext);
		WireContext<S> wireContext = Wire.context(iswm);

		return new InitializerContextImpl<S>(context.getSession(), wireContext);
	}

}
