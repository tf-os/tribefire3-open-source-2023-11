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
package com.braintribe.model.processing.deployment.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.ReflectionTools;

/**
 * This proxy class allows to loosely bind to a deployed component of some deployable. If the deployable is currently not deployed an explicit or auto default delegate will be used
 * @author dirk.scheffler
 *
 */
public class DeployedComponentProxy implements InvocationHandler {

	private DeployRegistry deployRegistry;
	private Deployable deployable;
	private Object defaultDelegate;
	private EntityType<? extends Deployable> componentType;

	public static <I> I create(Class<I> componentInterface, DeployRegistry deployRegistry, Deployable deployable, EntityType<? extends Deployable> componentType) {
		return create(componentInterface, deployRegistry, deployable, componentType, null);
	}

	public static <I> I create(Class<I> componentInterface, DeployRegistry deployRegistry, Deployable deployable, EntityType<? extends Deployable> componentType, Object defaultDelegate) {
		DeployedComponentProxy deployedComponentProxy = new DeployedComponentProxy(deployRegistry, deployable, defaultDelegate, componentType);
		@SuppressWarnings("unchecked")
		I implementation = (I) Proxy.newProxyInstance(componentInterface.getClassLoader(), new Class<?>[] { componentInterface }, deployedComponentProxy);
		return implementation;
	}

	private DeployedComponentProxy(DeployRegistry deployRegistry, Deployable deployable, Object defaultDelegate, EntityType<? extends Deployable> componentType) {
		super();
		this.deployRegistry = deployRegistry;
		this.deployable = deployable;
		this.defaultDelegate = defaultDelegate;
		this.componentType = componentType;
	}

	private Object getDelegate() {
		DeployedUnit deployedUnit = deployRegistry.resolve(deployable);

		if (deployedUnit != null) {
			Object delegate = deployedUnit.getComponent(componentType);
			return delegate;
		}

		return defaultDelegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getDeclaringClass() == Object.class)
			return method.invoke(this, args);
		else {
			Object delegate = getDelegate();

			if (delegate != null)
				try {
					return method.invoke(delegate, args);
				} catch (InvocationTargetException e) {
					throw e.getCause() != null ? e.getCause() : e;
				}
			else {
				return ReflectionTools.getDefaultValue(method.getReturnType());
			}
		}
	}

}
