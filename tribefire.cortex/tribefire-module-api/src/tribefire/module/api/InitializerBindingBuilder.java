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
package tribefire.module.api;

import java.io.File;
import java.util.function.Function;

import com.braintribe.model.csa.DynamicInitializer;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;

/**
 * BindingBuilder which allows a module to bind a an initializer for one or more accesses.
 * <p>
 * All the methods have an additive effect, i.e. when multiple {@link DataInitializer}s are configured for the same access, all of them are executed
 * on bootstrap.
 * 
 * <h3>Declaring affected accesses</h3>
 * 
 * Every module must declare which accesses it initializes in it's asset.man file. Example:
 * 
 * <pre>
 * $nature=!com.braintribe.model.asset.natures.TribefireModule()
 * .accessIds=('access.example', 'access.another','access.oneMore')
 * </pre>
 * 
 * This is needed so the setup process can add this module to the corresponding access' initializers configuration.
 * <p>
 * When it comes to order, initializers from one module are executed in their binding order, and initializers from different modules (or any other
 * assets) respect the asset-dependency graph - dependencies first, "dependers" later.
 *
 * @author peter.gazdik
 */
public interface InitializerBindingBuilder {

	/**
	 * Adds an initializer to an access given by it's externalId.
	 */
	void bind(String accessId, DataInitializer initializer);

	/**
	 * Adds an initializer for all the accesses the module was configured for (see at the top).
	 */
	void bind(DataInitializer initializer);

	/**
	 * Binds a dynamic initializer factory of given module. The input file for the function is the folder which contains the
	 * DynamicInitializerInput content. See documentation for com.braintribe.model.asset.natures.DynamicInitializerInput (platform-asset-model).
	 * 
	 * @see DynamicInitializer
	 */
	void bindDynamicInitializerFactory(Function<File, DataInitializer> initializerFactory);

}
