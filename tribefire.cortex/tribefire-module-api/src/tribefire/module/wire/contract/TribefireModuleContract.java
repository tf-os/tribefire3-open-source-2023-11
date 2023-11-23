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
package tribefire.module.wire.contract;

import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.api.WireContractBindingBuilder;

/**
 * This interface is the core configuration of a given module, a point where this module provides (registers on the platform) extensions implemented
 * by the module.
 * <p>
 * These methods are called during module-loading phase of an application bootstrap. For more information on the order of binding and module-loading
 * lifecycle see {@link #onBeforeBinding()}.
 * 
 * <h2>Available Imports</h2>
 * 
 * The corresponding space can import various types of contracts:
 * <ul>
 * <li>{@link TribefirePlatformContract} (or a platform-specific sub-type like TribefireWebPlatformContract) - same instance is imported to all the
 * modules
 * <li>{@link ModuleReflectionContract} - module-specific instance is imported
 * <li>{@link ModuleResourcesContract} - module-specific instance is imported
 * <li>Any contract bound from a different module via it's {@link #bindWireContracts(WireContractBindingBuilder)} method, assuming this other module
 * is a dependency of our module.
 * </ul>
 * 
 * @author peter.gazdik
 */
public interface TribefireModuleContract extends WireSpace {

	/**
	 * Lifecycle method called immediately before a module is bound (explained below). It can be used to perform some internal initialization once
	 * it's clear all the dependencies were initialized.
	 * <p>
	 * <h3>Module loading steps</h3>
	 * 
	 * Module loading is done in two phases:
	 * <ul>
	 * <li><b>Wiring</b> - wire contracts are bound by invoking {@link #bindWireContracts(WireContractBindingBuilder)} for all the modules</li>
	 * <li><b>Binding</b> - all the other bind methods are invoked</li>
	 * </ul>
	 * In both cases it is guaranteed that a dependency module is bound before it's depender, and the binding of each module is surrounded by our two
	 * lifecycle methods.
	 * <p>
	 * <b>Example:</b> Consider a project with two modules, <tt>A</tt> and <tt>B</tt>, and <tt>A</tt> depends on <tt>B</tt>. The module-loading
	 * performs these steps in this order:
	 * 
	 * <pre>
	 * [Wiring]
	 * 
	 * [Wiring of B]
	 * B.bindContracts
	 *
	 * [Wiring of A]
	 * A.bindContracts
	 *
	 * [Binding]
	 * 
	 * [Binding of B]
	 * B.onBeforeBinding
	 * B.bindHardwired
	 * B.bindInitializers
	 * B.bindDeployables
	 * B.onAfterBinding
	 *
	 * [Binding of A]
	 * A.onBeforeBinding
	 * A.bindHardwired
	 * A.bindInitializers
	 * A.bindDeployables
	 * A.onAfterBinding
	 * </pre>
	 */
	default void onBeforeBinding() {
		// Optional
	}

	/**
	 * Lifecycle method called immediately after a module is bound.
	 * <p>
	 * See {@link #onBeforeBinding()} for more details.
	 */
	default void onAfterBinding() {
		// Optional
	}

	/**
	 * Binds implementations for wire contracts which other modules can import.
	 * <p>
	 * This serves as a mechanism for creating custom extension points of a module.
	 * <p>
	 * For this to work the custom contract(s) MUST be defined in a separate API artifact, because the depender will need it as a dependency on it's
	 * classpath. The depender will also need this module (which binds this contract) as an asset-only dependency in order to guarantee that these two
	 * are loaded in the correct order - binder (dependency) first, importer (depender) after that.
	 * 
	 * @param bindings
	 *            a {@link WireContractBindingBuilder} which offers a way for a module to bind a space to a contract, which can then be imported by
	 *            another module.
	 */
	default void bindWireContracts(WireContractBindingBuilder bindings) {
		// Optional
	}

	/**
	 * This method is the place for the code that binding hardwired functional components.
	 * <p>
	 * The actual binding is supposed to be done via a {@link HardwiredDeployablesContract} (exactly this one or it's extension from some module-api
	 * like <tt>HardwiredMarshallersContract</tt> or <tt>WebPlatformHardwiredDeployablesContract</tt>).
	 * <p>
	 * <b>NOTE that biding using these contracts is only guaranteed to work if done within this method. Doing it anywhere else is probably too
	 * late.</b>
	 * <p>
	 * The main advantage of hardwired deployables is that you can register a specific expert directly without defining it's own denotation type. This
	 * makes sense as long as the expert isn't meant to be configured dynamically.
	 * <p>
	 * <b>Example:</b>
	 * 
	 * <pre>
	 * public class MyModuleSpace implements TribefireModuleContract {
	 * 	&#64;Import
	 * 	private HardwiredDeployablesContract hardwiredDeployables;
	 * 
	 * 	public void bindHardwired() {
	 * 		// URL: tribefire-services/api/v1/en/SayHello
	 * 		hardwiredDeployables.bindOnServiceDomain("en", "Domain EN") //
	 * 				.serviceProcessor("en.sayHello", "English Hello Sayer", SayHello.T, (ctx, request) -> "Hello!!!");
	 * 
	 * 		// URL: tribefire-services/api/v1/de/SayHello
	 * 		hardwiredDeployables.bindOnServiceDomain("de", "Domain DE") //
	 * 				.serviceProcessor("de.sayHello", "German Hello Sayer", SayHello.T, (ctx, request) -> "Hallo!!!");
	 * 	}
	 * }
	 * </pre>
	 */
	default void bindHardwired() {
		// Optional
	}

	/**
	 * @param bindings
	 *            {@link InitializerBindingBuilder} for registering {@link DataInitializer}s for various CSA based accesses.
	 */
	default void bindInitializers(InitializerBindingBuilder bindings) {
		// Optional
	}

	/**
	 * @param bindings
	 *            a {@link DenotationBindingBuilder} specific for given module
	 */
	default void bindDeployables(DenotationBindingBuilder bindings) {
		// Optional
	}

}
