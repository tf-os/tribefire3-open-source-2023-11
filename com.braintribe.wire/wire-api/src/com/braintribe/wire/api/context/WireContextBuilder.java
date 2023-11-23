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

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.scope.CreationListener;
import com.braintribe.wire.api.scope.DefaultScope;
import com.braintribe.wire.api.scope.LifecycleListener;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;

public interface WireContextBuilder<S extends WireSpace> {
	/**
	 * Maps an individual contract class to an space implementation class
	 * @throws IllegalArgumentException
	 */
	WireContextBuilder<S> bindContract(Class<? extends WireSpace> wireSpaceContract, Class<? extends WireSpace> wireSpaceImplementation) throws IllegalArgumentException;
	
	/**
	 * Maps and individual contract class to a space instance to allow to pass instance data into the context
	 * Mind that the instance you give here will not be managed by this context. So don't expect that {@link Managed} annotated methods are processed
	 * in the way you expect it normally when the instance is created internally in this context and therefore don't use it there except the space comes from another context. 
	 * @throws IllegalArgumentException
	 */
	<E extends WireSpace> WireContextBuilder<S> bindContract(Class<E> wireSpaceContract, E wireSpaceInstance) throws IllegalArgumentException;
	
	/**
	 * Maps an individual contract class to a space implementation class described by its full qualified name.
	 * This helps to losely couple the implementation class if it is not on the classpath when setting up a context
	 * @throws IllegalArgumentException
	 */
	WireContextBuilder<S> bindContract(Class<? extends WireSpace> wireSpaceContract, String wireSpaceImplementation) throws IllegalArgumentException;
	
	/**
	 * Maps contracts from <i>${contractPackage}.*${contractSuffix}</i> to <i>${implementationPackage}.space.*${implementationSuffix}</i>
	 */
	WireContextBuilder<S> bindContracts(String contractPackage, String contractSuffix, String implementationPackage, String implementationSuffix);
	
	/**
	 * Maps contracts from <i>${contractPackage}.*Contract</i> to <i>${implementationPackage}.*Space</i>
	 */
	WireContextBuilder<S> bindContracts(String contractPackage, String implementationPackage);
	
	/**
	 * Registers a {@link WireModule} to support a lookup from {@link WireSpace} to {@link WireModule}
	 * @param module the Module to be registered.
	 */
	WireContextBuilder<S> registerModule(WireModule module);
	
	/**
	 * Returns true if the given contract class is mapped to a space class by some configured mapping 
	 */
	boolean isContractBound(Class<? extends WireSpace> wireSpaceContract);
	
	/**
	 * Maps contracts from from <i>${basePackage}.contract.*Contract</i> to <i>${basePackage}.space.*Space</i> 
	 */
	WireContextBuilder<S> bindContracts(String basePackage);
	
	WireContextBuilder<S> bindContracts(Class<? extends WireSpace> wireSpaceClass);
	
	WireContextBuilder<S> bindContracts(ContractSpaceResolver resolver);
	
	/**
	 * Links the built context to a parent context from which in could inherit contracts
	 */
	WireContextBuilder<S> parent(WireContext<?> wireContext);
	
	/**
	 * Configures the default {@link WireScope} that is the last resort when a factory method and the defining {@link WireSpace}
	 * said the scope is {@link DefaultScope}. If this method is not called the default {@link WireScope} for the built context
	 * will be a singleton scope.
	 * @deprecated use {@link #defaultScope(Scope)} instead. 
	 */
	@Deprecated
	WireContextBuilder<S> defaultScope(Class<? extends WireScope> scopeClass);

	WireContextBuilder<S> defaultScope(Scope scope);
	
	/**
	 * Tells Wire's classloader for which packages special class loading (bytecode weaving) must take place.
	 */
	WireContextBuilder<S> selectSpaceClassesByPackage(String... packageName);

	/**
	 * Configures a class name filter telling Wire's classloader for which classes special class loading (bytecode weaving) must take place.
	 */
	WireContextBuilder<S> selectSpaceClasses(Predicate<String> name);
	
	/**
	 * Configures the classloader to be used to load space classes from. This is usefull in cases of plugin systems which use an additional classloader and shared libs
	 */
	WireContextBuilder<S> loadSpacesFrom(ClassLoader classLoader);
	
	/**
	 * Configures the lookup expert that can be used to selectively share ScopeContextHolders by supplying a common map on basis of a {@link ScopeContext} 
	 */
	WireContextBuilder<S> shareScopeContexts(Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert);
	
	/**
	 * Instructs the WireContext that is being build to automatically load and initialize the given space with {@link WireSpace#onLoaded(WireContextConfiguration)}
	 */
	WireContextBuilder<S> autoLoad(Class<? extends WireSpace> spaceToAutoload);
	
	/**
	 * Configures the WireContext to use the given {@link LifecycleListener} in scoping
	 */
	WireContextBuilder<S> lifecycleListener(LifecycleListener lifecycleListener);
	
	/**
	 * Configures the WireContext to use the given {@link CreationListener}
	 */
	WireContextBuilder<S> creationListener(CreationListener creationListener);

	/**
	 * Actually constructs the {@link WireContext} from the given builder informations and returns it for usage.
	 */
	WireContext<S> build();

}
