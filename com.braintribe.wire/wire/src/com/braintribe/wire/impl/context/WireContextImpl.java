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

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.EnrichedWireSpace;
import com.braintribe.wire.api.ImportField;
import com.braintribe.wire.api.ImportFieldRecorder;
import com.braintribe.wire.api.context.InstancePath;
import com.braintribe.wire.api.context.InternalWireContext;
import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.scope.CreationListener;
import com.braintribe.wire.api.scope.DefaultScope;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.LifecycleListener;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.compile.WireManagedSpaceFactory;
import com.braintribe.wire.impl.lifecycle.MulticastLifecycleListener;
import com.braintribe.wire.impl.lifecycle.NoopLifecycleListener;
import com.braintribe.wire.impl.lifecycle.StandardLifecycleListener;
import com.braintribe.wire.impl.scope.singleton.ScopeContextSingletons;
import com.braintribe.wire.impl.scope.singleton.SingletonScope;
import com.braintribe.wire.impl.util.Exceptions;

public class WireContextImpl<S extends WireSpace> implements WireContext<S>, InternalWireContext, WireContextConfiguration {
	private static final Logger logger = Logger.getLogger(WireContextImpl.class.getName());
	private final Map<Class<? extends WireScope>, WireScope> scopes = new IdentityHashMap<>();
	private final Map<Class<? extends WireSpace>, WireSpace> beanSpaces = new IdentityHashMap<>();
	private S mainContract;
	private LifecycleListener listener = StandardLifecycleListener.INSTANCE;
	private final MulticastLifecycleListener multicastLifecycleListener = new MulticastLifecycleListener();
	private ContractSpaceResolver contractSpaceResolver;
	private WireContext<?> parentContext;
	private Class<? extends WireScope> defaultScopeClass = SingletonScope.class;
	private WireManagedSpaceFactory wireBeanSpaceFactory;
	private Predicate<String> spaceClassesSelector = (String name) -> false;
	private Class<S> rootBeanSpaceClass;
	private ClassLoader spaceClassLoader;
	private final ThreadLocal<Deque<InstanceHolder>> stackLocal = ThreadLocal.withInitial(ArrayDeque::new);
	private NavigableMap<String, WireModule> modulesByBasePackage = Collections.emptyNavigableMap();
	private final Map<ScopeContext, ScopeContextHolders> scopeContextHoldersMap = new ConcurrentHashMap<>();
	private Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert;
	private List<Class<? extends WireSpace>> spacesToAutoload = Collections.emptyList();
	private List<CreationListener> creationListeners = new ArrayList<>();

	public WireContextImpl(Class<S> beanSpace, ContractSpaceResolver contractSpaceResolver) {
		this(null, beanSpace, contractSpaceResolver);
	}

	public WireContextImpl(WireContext<?> parentContext, Class<S> beanSpace, ContractSpaceResolver contractSpaceResolver) {
		this.parentContext = parentContext;
		this.rootBeanSpaceClass = beanSpace;
		this.contractSpaceResolver = contractSpaceResolver;
	}

	@Configurable
	public void setSpacesToAutoload(List<Class<? extends WireSpace>> spacesToAutoload) {
		this.spacesToAutoload = spacesToAutoload;
	}

	public void setShareScopeContextsExpert(Function<ScopeContext, Map<ScopeContext, ScopeContextHolders>> shareScopeContextsExpert) {
		this.shareScopeContextsExpert = shareScopeContextsExpert;
	}

	public void setSpaceClassLoader(ClassLoader spaceClassLoader) {
		this.spaceClassLoader = spaceClassLoader;
	}

	public void initialize() {
		if (spaceClassLoader != null) {
			wireBeanSpaceFactory = new WireManagedSpaceFactory(this, spaceClassesSelector, spaceClassLoader);
		} else {
			wireBeanSpaceFactory = new WireManagedSpaceFactory(this, spaceClassesSelector);

		}

		logger.finest(() -> "Loading wire bean spaces.");
		// load main contract
		mainContract = resolveSpace(rootBeanSpaceClass);

		// load additional spaces
		for (Class<? extends WireSpace> space : spacesToAutoload) {
			resolveSpace(space);
		}

		logger.finest(() -> "Loaded wire bean spaces.");
	}

	public void setDefaultScopeClass(Class<? extends WireScope> defaultScopeClass) {
		this.defaultScopeClass = defaultScopeClass;
	}

	@Override
	public S contract() {
		return mainContract;
	}

	@Override
	public <C extends WireSpace> C contract(Class<C> contractClass) {
		Objects.requireNonNull(contractClass, "Argument contractClass may not be null");

		if (!contractClass.isInterface())
			throw new IllegalArgumentException("Argument contractClass [" + contractClass + "] has to be an interface but isn't");

		return new ContextualizedSpaceResolution(false).getSpace(contractClass);
	}

	@Override
	public <T extends WireSpace> T resolveSpace(Class<T> beanSpaceClass) {
		return new ContextualizedSpaceResolution(false).getSpace(beanSpaceClass);
	}

	@Override
	public <T extends WireSpace> T findContract(Class<T> contractClass) {
		Objects.requireNonNull(contractClass, "Argument contractClass may not be null");

		if (!contractClass.isInterface())
			throw new IllegalArgumentException("Argument contractClass [" + contractClass + "] has to be an interface but isn't");

		return new ContextualizedSpaceResolution(true).getSpace(contractClass);
	}

	private class ContextualizedSpaceResolution {
		private final boolean lenient;
		private List<WireSpace> createdSpaces;

		public ContextualizedSpaceResolution(boolean lenient) {
			super();
			this.lenient = lenient;
		}

		public <T extends WireSpace> T getSpace(Class<T> beanSpaceClass) {
			synchronized (beanSpaces) {
				T wireSpace = getSpaceRecursive(beanSpaceClass, lenient);

				if (createdSpaces != null) {
					for (WireSpace createdSpace : createdSpaces) {
						logger.finer(() -> "Loading wire bean space " + createdSpace.getClass().getName());
						createdSpace.onLoaded(WireContextImpl.this);
					}
					createdSpaces = null;
				}

				return wireSpace;
			}
		}

		private <T extends WireSpace> T getSpaceRecursive(Class<T> beanSpaceClass, boolean lenient) {
			T wireSpace = (T) beanSpaces.get(beanSpaceClass);

			if (wireSpace == null) {
				wireSpace = (T) resolveSpace(beanSpaceClass, lenient);
			}

			return wireSpace;
		}

		private WireSpace resolveSpace(Class<? extends WireSpace> beanSpaceClass, boolean lenient) {
			if (beanSpaceClass.isInterface()) {
				return resolveContract(beanSpaceClass, lenient);
			} else {
				return resolveImplementation(beanSpaceClass);
			}
		}

		private WireSpace resolveSpaceByClassName(String spaceClassName) {
			Class<WireSpace> spaceClass = wireBeanSpaceFactory.acquireSpaceClass(spaceClassName);
			return getSpaceRecursive(spaceClass, false);
		}

		private WireSpace resolveContract(Class<? extends WireSpace> beanSpaceClass, boolean lenient) {
			ContractResolution resolution = contractSpaceResolver.resolveContractSpace(WireContextImpl.this, beanSpaceClass);

			WireSpace beanSpace = null;

			if (resolution != null)
				beanSpace = resolution.resolve(this::resolveSpaceByClassName);
			else if (parentContext != null)
				beanSpace = parentContext.findContract(beanSpaceClass);

			if (beanSpace == null) {
				if (!lenient)
					throw new IllegalStateException("Contract " + beanSpaceClass + " is not mapped");

				return null;
			}

			beanSpaces.put(beanSpaceClass, beanSpace);

			return beanSpace;
		}

		private WireSpace resolveImplementation(Class<? extends WireSpace> beanSpaceClass) {
			if ((beanSpaceClass.getModifiers() & Modifier.ABSTRACT) != 0)
				throw new IllegalStateException("Cannot instantiate abstract Wire Space: " + beanSpaceClass.getName());
			
			try {
				WireSpace beanSpace = wireBeanSpaceFactory.createInstance(beanSpaceClass.getName());

				// put eager reference
				beanSpaces.put(beanSpaceClass, beanSpace);

				for (ImportField importField : getImportFields(beanSpace)) {
					Class<?> fieldType = importField.type();
					Object injectValue = null;

					if (WireSpace.class.isAssignableFrom(fieldType)) {
						injectValue = getSpaceRecursive(fieldType.asSubclass(WireSpace.class), false);
					} else if (WireScope.class.isAssignableFrom(fieldType)) {
						injectValue = getScope(fieldType.asSubclass(WireScope.class));
					} else if (WireContext.class.isAssignableFrom(fieldType)) {
						injectValue = WireContextImpl.this;
					}

					if (injectValue != null) {
						importField.set(injectValue);
					}
				}

				notifyLoadedImplementation(beanSpace);
				return beanSpace;
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while building BeanSpace for type " + beanSpaceClass, IllegalStateException::new);
			}

		}

		private void notifyLoadedImplementation(WireSpace space) {
			if (createdSpaces == null) {
				createdSpaces = new ArrayList<>();
			}

			createdSpaces.add(space);
		}

	}

	private static List<ImportField> getImportFields(WireSpace space) {
		List<ImportField> importFields = Collections.emptyList();

		if (space instanceof EnrichedWireSpace) {
			EnrichedWireSpace enrichedWireSpace = (EnrichedWireSpace) space;

			ImportFieldRecorder recorder = new ImportFieldRecorder(enrichedWireSpace);
			enrichedWireSpace.__listImportFields(recorder);

			importFields = recorder.getImportFields();
		}

		return importFields;
	}

	@Override
	public WireScope getScope(Class<? extends WireScope> scopeClass) {
		if (scopeClass == DefaultScope.class)
			scopeClass = defaultScopeClass;

		WireScope scope = scopes.get(scopeClass);

		if (scope == null) {
			try {
				scope = scopeClass.getConstructor().newInstance();
				scope.attachContext(this);
			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while constructing scope of type " + scopeClass, IllegalStateException::new);
			}
			scopes.put(scopeClass, scope);
		}

		return scope;
	}

	@Override
	public void shutdown() {
		for (WireScope scope : scopes.values()) {
			try {
				scope.close();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Error while closing scope", e);
			}
		}
	}

	private Map<ScopeContext, ScopeContextHolders> getScopeSingletonsMap(ScopeContext scopeContext) {
		if (shareScopeContextsExpert != null) {
			Map<ScopeContext, ScopeContextHolders> map = shareScopeContextsExpert.apply(scopeContext);
			if (map != null) {
				return map;
			}
		}

		return scopeContextHoldersMap;
	}

	@Override
	public ScopeContextHolders getScopeForContext(ScopeContext scopeContext) {
		return getScopeSingletonsMap(scopeContext).computeIfAbsent(scopeContext, k -> new ScopeContextSingletons());
	}

	@Override
	public void close(ScopeContext scopeContext) {
		ScopeContextHolders singletons = getScopeSingletonsMap(scopeContext).remove(scopeContext);
		singletons.close();
	}

	@Override
	public void onPreDestroy(InstanceHolder beanHolder, Object bean) {
		try {
			listener.onPreDestroy(beanHolder, bean);
		} catch (Exception e) {
			throw new RuntimeException("Error while destroying bean " + bean, e);
		}
	}

	@Override
	public void onPostConstruct(InstanceHolder beanHolder, Object bean) {
		try {
			listener.onPostConstruct(beanHolder, bean);
		} catch (Exception e) {
			throw new RuntimeException("Error while constructing bean " + bean, e);
		}
	}

	@Override
	public boolean lockCreation(InstanceHolder beanHolder) {
		boolean result = beanHolder.lockCreation();

		if (result) {
			push(beanHolder);
			onBeforeCreation(beanHolder);
		}

		return result;
	}

	private void onBeforeCreation(InstanceHolder beanHolder) {
		int size = creationListeners.size();
		for (int i = 0; i < size; i++) {
			creationListeners.get(i).onBeforeCreate(beanHolder);
		}
	}

	@Override
	public void unlockCreation(InstanceHolder beanHolder) {
		beanHolder.unlockCreation();
		onAfterCreation(beanHolder);
		pop();
	}

	private void onAfterCreation(InstanceHolder beanHolder) {
		int size = creationListeners.size();
		for (int i = 0; i < size; i++) {
			creationListeners.get(i).onAfterCreate(beanHolder);
		}
	}

	private void push(InstanceHolder beanHolder) {
		Deque<InstanceHolder> stack = stackLocal.get();
		stack.push(beanHolder);
	}

	private void pop() {
		Deque<InstanceHolder> stack = stackLocal.get();
		stack.pop();
		if (stack.isEmpty())
			stackLocal.remove();
	}

	@Override
	public InstancePath currentInstancePath() {
		Deque<InstanceHolder> stack = stackLocal.get();

		if (stack.isEmpty()) {
			stackLocal.remove();
			return InstancePath.empty();
		} else {
			return new DequeInstancePath(stack);
		}
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		if (this.listener == NoopLifecycleListener.INSTANCE) {
			this.listener = listener;
		} else if (this.listener == multicastLifecycleListener) {
			multicastLifecycleListener.add(listener);
		} else {
			multicastLifecycleListener.add(this.listener);
			multicastLifecycleListener.add(listener);
			this.listener = multicastLifecycleListener;
		}
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		multicastLifecycleListener.remove(listener);

		if (multicastLifecycleListener.isEmpty()) {
			this.listener = NoopLifecycleListener.INSTANCE;
		}
	}
	
	@Override
	public void addCreationListener(CreationListener listener) {
		creationListeners.add(listener);
	}
	
	@Override
	public void removeCreationListener(CreationListener listener) {
		creationListeners.remove(listener);
	}

	public void setSpaceClassesSelector(Predicate<String> spaceClassesSelector) {
		this.spaceClassesSelector = spaceClassesSelector;
	}

	@Configurable
	public void setModulesByBasePackage(NavigableMap<String, WireModule> modulesByBasePackage) {
		this.modulesByBasePackage = modulesByBasePackage;
	}

	@Override
	public <T extends WireSpace> WireModule findModuleFor(Class<T> wireSpace) {
		return modulesByBasePackage.computeIfAbsent(wireSpace.getPackage().getName(), n -> {
			Entry<String, WireModule> floorEntry = modulesByBasePackage.floorEntry(wireSpace.getName());

			if (floorEntry == null)
				return null;

			return floorEntry.getValue();
		});

	}
}
