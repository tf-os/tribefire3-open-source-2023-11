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
package com.braintribe.wire.impl.context;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.annotation.Scope;
import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.scope.CreationListener;
import com.braintribe.wire.api.scope.LifecycleListener;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.contract.ChainedContractSpaceResolver;
import com.braintribe.wire.impl.contract.InstanceContractResolution;
import com.braintribe.wire.impl.contract.MapBasedContractSpaceResolver;
import com.braintribe.wire.impl.contract.NameConventionContractSpaceResolver;
import com.braintribe.wire.impl.contract.StandardContractResolution;
import com.braintribe.wire.impl.scope.caller.CallerScope;
import com.braintribe.wire.impl.scope.prototype.PrototypeScope;
import com.braintribe.wire.impl.scope.referee.AggregateScope;
import com.braintribe.wire.impl.scope.singleton.SingletonScope;

public class WireContextBuilderImpl<S extends WireSpace> implements WireContextBuilder<S> {
	public static final String DEFAULT_CONTRACT_SUFFIX = "Contract";
	public static final String DEFAULT_SPACE_SUFFIX = "Space";

	private Map<Class<? extends WireSpace>, Supplier<ContractResolution>> mappings = new IdentityHashMap<>();
	private List<NameConventionContractSpaceResolver> nameConventionResolvers = new ArrayList<>();
	private List<ContractSpaceResolver> genericContractSpaceResolvers = new ArrayList<>();
	private Class<S> beanSpace;
	private WireContext<?> parentContext;
	private Class<? extends WireScope> defaultScope = SingletonScope.class;
	private Predicate<String> spaceClassesSelector;
	private ClassLoader spaceClassLoader;
	private NavigableMap<String, WireModule> modulesByBasePackage = new TreeMap<>();
	private Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert;
	private List<Class<? extends WireSpace>> spacesToAutoload;
	private List<LifecycleListener> lifecycleListeners = new ArrayList<>();
	private List<CreationListener> creationListeners = new ArrayList<>();
	
	public WireContextBuilderImpl(Class<S> beanSpace) {
		super();
		this.beanSpace = beanSpace;
	}

	@Override
	public <E extends WireSpace> WireContextBuilder<S> bindContract(Class<E> beanSpaceContract, E beanSpaceInstance) throws IllegalArgumentException {
		if (!beanSpaceContract.isInterface())
			throw new IllegalArgumentException("beanSpaceContract argument [" + beanSpaceContract + "] must be an interface but isn't");

		ContractResolution contractResolution = new InstanceContractResolution(beanSpaceInstance);

		mappings.put(beanSpaceContract, () -> contractResolution);

		return this;
	}

	@Override
	public WireContextBuilder<S> bindContract(Class<? extends WireSpace> beanSpaceContract, Class<? extends WireSpace> beanSpaceImplementation)
			throws IllegalArgumentException {
		if (!beanSpaceContract.isInterface())
			throw new IllegalArgumentException("beanSpaceContract argument [" + beanSpaceContract + "] must be an interface but isn't");

		if (!beanSpaceContract.isAssignableFrom(beanSpaceImplementation))
			throw new IllegalArgumentException("beanSpaceImplementation argument [" + beanSpaceImplementation + "] must implement beanSpaceContract ["
					+ beanSpaceContract + "] but doesn't");

		ContractResolution contractResolution = new StandardContractResolution(beanSpaceImplementation.getName());

		mappings.put(beanSpaceContract, () -> contractResolution);

		selectSpaceClass(beanSpaceImplementation.getName());

		return this;
	}

	@Override
	public WireContextBuilder<S> bindContract(Class<? extends WireSpace> beanSpaceContract, String beanSpaceImplementation)
			throws IllegalArgumentException {
		if (!beanSpaceContract.isInterface())
			throw new IllegalArgumentException("beanSpaceContract argument [" + beanSpaceContract + "] must be an interface but isn't");

		ContractResolution contractResolution = new StandardContractResolution(beanSpaceImplementation);

		mappings.put(beanSpaceContract, () -> contractResolution);

		selectSpaceClass(beanSpaceImplementation);
		return this;
	}

	@Override
	public WireContextBuilder<S> bindContracts(ContractSpaceResolver resolver) {
		genericContractSpaceResolvers.add(resolver);
		return this;
	}

	@Override
	public WireContextBuilder<S> bindContracts(String basePackage) {

		String contractPrefix = basePackage + ".contract.";
		String implPrefix = basePackage + ".space.";

		nameConventionResolvers
				.add(new NameConventionContractSpaceResolver(contractPrefix, DEFAULT_CONTRACT_SUFFIX, implPrefix, DEFAULT_SPACE_SUFFIX, true));

		selectSpaceClassesByPrefix(implPrefix);

		return this;
	}

	@Override
	public WireContextBuilder<S> bindContracts(Class<? extends WireSpace> wireSpace) {
		String packageName = wireSpace.getPackage().getName();
		String basePackage = packageName.substring(0, packageName.lastIndexOf('.'));
		return bindContracts(basePackage);
	}

	@Override
	public WireContextBuilder<S> bindContracts(String contractPackage, String contractSuffix, String implementationPackage,
			String implementationSuffix) {
		nameConventionResolvers.add(new NameConventionContractSpaceResolver(contractPackage + ".", contractSuffix, implementationPackage + ".",
				implementationSuffix, true));

		return selectSpaceClassesByPackage(implementationPackage);
	}

	@Override
	public WireContextBuilder<S> bindContracts(String contractPackage, String implementationPackage) {
		nameConventionResolvers.add(new NameConventionContractSpaceResolver(contractPackage + ".", DEFAULT_CONTRACT_SUFFIX,
				implementationPackage + ".", DEFAULT_SPACE_SUFFIX, true));

		return selectSpaceClassesByPackage(implementationPackage);
	}

	@Override
	public WireContextBuilder<S> registerModule(WireModule module) {
		String packageName = module.getClass().getPackage().getName();
		modulesByBasePackage.put(packageName, module);
		return this;
	}

	@Override
	public boolean isContractBound(Class<? extends WireSpace> beanSpaceContract) {
		for (NameConventionContractSpaceResolver nameConventionContractSpaceResolver : nameConventionResolvers) {
			if (nameConventionContractSpaceResolver.resolveContractSpace(beanSpaceContract) != null)
				return true;
		}

		return mappings.containsKey(beanSpaceContract);
	}

	@Override
	public WireContextBuilder<S> parent(WireContext<?> wireContext) {
		this.parentContext = wireContext;
		return this;
	}

	@Deprecated
	@Override
	public WireContextBuilder<S> defaultScope(Class<? extends WireScope> scopeClass) {
		this.defaultScope = scopeClass;
		return this;
	}

	@Override
	public WireContextBuilder<S> defaultScope(Scope scope) {
		Objects.requireNonNull(scope, "scope must not be null");
		switch (scope) {
			case prototype:
				this.defaultScope = PrototypeScope.class;
				break;
			case aggregate:
				this.defaultScope = AggregateScope.class;
				break;
			case singleton:
				this.defaultScope = SingletonScope.class;
				break;
			case caller:
				this.defaultScope = CallerScope.class;
				break;
			default:
				throw new IllegalArgumentException("Scope constant not supported: " + scope);
		}
		return this;
	}

	@Override
	public WireContextBuilder<S> loadSpacesFrom(ClassLoader classLoader) {
		this.spaceClassLoader = classLoader;
		return this;
	}

	@Override
	public WireContextBuilder<S> shareScopeContexts(Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert) {
		this.shareScopeContextsExpert = shareScopeContextsExpert;
		return this;
	}

	@Override
	public WireContextBuilder<S> autoLoad(Class<? extends WireSpace> space) {
		if (spacesToAutoload == null)
			spacesToAutoload = new ArrayList<>();

		spacesToAutoload.add(space);
		return this;
	}

	@Override
	public WireContext<S> build() {
		List<ContractSpaceResolver> resolvers = new ArrayList<>();

		resolvers.addAll(genericContractSpaceResolvers);

		if (!mappings.isEmpty()) {
			resolvers.add(new MapBasedContractSpaceResolver(mappings, true));
		}

		resolvers.addAll(nameConventionResolvers);

		ContractSpaceResolver contractSpaceResolver = new ChainedContractSpaceResolver(resolvers);
		WireContextImpl<S> wireContextImpl = new WireContextImpl<S>(parentContext, beanSpace, contractSpaceResolver);
		return configure(wireContextImpl);
	}

	@Override
	public WireContextBuilder<S> selectSpaceClasses(Predicate<String> selector) {
		if (spaceClassesSelector == null)
			spaceClassesSelector = selector;
		else
			spaceClassesSelector = spaceClassesSelector.or(selector);

		return this;
	}

	@Override
	public WireContextBuilder<S> selectSpaceClassesByPackage(String... packageName) {
		for (String currenPackageName : packageName) {
			String prefix = currenPackageName + '.';
			selectSpaceClassesByPrefix(prefix);
		}
		return this;
	}

	private void selectSpaceClassesByPrefix(String prefix) {
		selectSpaceClasses((String name) -> name.startsWith(prefix));
	}

	private void selectSpaceClass(String className) {
		selectSpaceClasses((String name) -> name.equals(className));
	}

	@Override
	public WireContextBuilder<S> lifecycleListener(LifecycleListener lifecycleListener) {
		lifecycleListeners.add(lifecycleListener);
		return this;
	}
	
	@Override
	public WireContextBuilder<S> creationListener(CreationListener creationListener) {
		creationListeners.add(creationListener);
		return this;
	}
	
	private WireContext<S> configure(WireContextImpl<S> wireContextImpl) {
		wireContextImpl.setDefaultScopeClass(defaultScope);
		if (spaceClassesSelector != null)
			wireContextImpl.setSpaceClassesSelector(spaceClassesSelector);

		if (spaceClassLoader != null) {
			wireContextImpl.setSpaceClassLoader(spaceClassLoader);
		}

		if (shareScopeContextsExpert != null) {
			wireContextImpl.setShareScopeContextsExpert(shareScopeContextsExpert);
		}

		if (spacesToAutoload != null) {
			wireContextImpl.setSpacesToAutoload(spacesToAutoload);
		}
		
		lifecycleListeners.forEach(wireContextImpl::addLifecycleListener);
		creationListeners.forEach(wireContextImpl::addCreationListener);
		
		wireContextImpl.setModulesByBasePackage(modulesByBasePackage);

		wireContextImpl.initialize();

		return wireContextImpl;
	}

}
