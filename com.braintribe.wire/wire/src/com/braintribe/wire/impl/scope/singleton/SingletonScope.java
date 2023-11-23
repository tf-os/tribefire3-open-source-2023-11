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
package com.braintribe.wire.impl.scope.singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.braintribe.cfg.ScopeContext;
import com.braintribe.wire.api.context.InternalWireContext;
import com.braintribe.wire.api.context.ScopeContextHolders;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.InstanceHolderSupplier;
import com.braintribe.wire.api.scope.InstanceParameterization;
import com.braintribe.wire.api.scope.InstanceQualification;
import com.braintribe.wire.api.scope.WireScope;
import com.braintribe.wire.api.space.WireSpace;

public class SingletonScope extends AbstractSingletonScope {
	
	static Logger logger = Logger.getLogger(SingletonScope.class.getName());
	private static Object nullSubstitute = new Object();

	public static final SingletonScope INSTANCE = new SingletonScope();

	@Override
	public void close() throws Exception {
		synchronized (holders) {
			super.close();
		}
	}

	@Override
	public void appendBean(AbstractSingletonInstanceHolder singletonBeanHolder) {
		synchronized (holders) {
			super.appendBean(singletonBeanHolder);
		}
	}

	@Override
	public void attachContext(InternalWireContext context) {
		super.attachContext(context);
	}

	@Override
	public InstanceHolderSupplier createHolderSupplier(WireSpace managedSpace, String name, InstanceParameterization beanParameterization) {
		switch (beanParameterization) {
			case none:
				return new SingletonInstanceHolder(managedSpace, this, name, this::appendBean);
			case context:
				return new ScopeContextualizedSupplier(managedSpace, name);
			case params:
				return new ParameterizedSupplier(managedSpace, name);
			default:
				throw new IllegalArgumentException("unuspported BeanParameterization: " + beanParameterization);
		}
	}

	public class ParameterizedSupplier implements InstanceHolderSupplier {

		private String name;
		private WireSpace space;
		private Map<Object, SingletonInstanceHolder> parameterizedHolders;

		public ParameterizedSupplier(WireSpace space, String name) {
			this.space = space;
			this.name = name;
			this.parameterizedHolders = new ConcurrentHashMap<>();
		}

		@Override
		public InstanceHolder getHolder(Object context) {
			if (context == null)
				context = nullSubstitute;

			return parameterizedHolders.computeIfAbsent(context,
					k -> new SingletonInstanceHolder(space, SingletonScope.this, name, SingletonScope.this::appendBean));
		}
	}

	public class ScopeContextualizedSupplier implements InstanceHolderSupplier, InstanceQualification {

		private String name;
		private WireSpace space;

		public ScopeContextualizedSupplier(WireSpace space, String name) {
			this.space = space;
			this.name = name;
		}

		@Override
		public InstanceHolder getHolder(Object context) {
			ScopeContextHolders scopeContextSingletons = getContext().getScopeForContext((ScopeContext) context);
			return scopeContextSingletons.acquireHolder(this);
		}

		@Override
		public WireSpace space() {
			return space;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public WireScope scope() {
			return SingletonScope.this;
		}

	}
}
