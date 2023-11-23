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
package tribefire.platform.impl.deployment;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

import com.braintribe.logging.Logger;

public class DynamicDeployableProxy<T> implements InvocationHandler {
	
	private static final Logger log = Logger.getLogger(DynamicDeployableProxy.class);
	
	private Class<T> serviceInterface;
	private T defaultDelegate;
	private Supplier<T> dynamicDelegateProvider;
	
	private DynamicDeployableProxy(Class<T> serviceInterface, T defaultDelegate, Supplier<T> dynamicDelegateProvider) {
		this.serviceInterface = serviceInterface;
		this.defaultDelegate = defaultDelegate;
		this.dynamicDelegateProvider = dynamicDelegateProvider;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		T delegate = (dynamicDelegateProvider != null) ? dynamicDelegateProvider.get() : defaultDelegate;
		
		if (delegate == null) {
			delegate = defaultDelegate;
		}
		
		if (log.isTraceEnabled()) {
			if (delegate == defaultDelegate) {
				log.trace("Invoking [ "+serviceInterface+" ] proxy with default delegate [ "+delegate+" ]");
			} else {
				log.trace("Invoking [ "+serviceInterface+" ] proxy with dynamic delegate [ "+delegate+" ]");
			}
		}
		
		try {
			return method.invoke(delegate, args);
		} catch (UndeclaredThrowableException | InvocationTargetException ex) {
			// avoid unnecessary exception wrapping 
			throw ex.getCause();
		}
	}
	
	public static <T> T create(Class<T> serviceInterface, T defaultDelegate, Supplier<T> dynamicDelegateProvider) {
		DynamicDeployableProxy<T> invocationHandler = new DynamicDeployableProxy<T>(serviceInterface, defaultDelegate, dynamicDelegateProvider);
		@SuppressWarnings("unchecked")
		T proxy = (T)Proxy.newProxyInstance(DynamicDeployableProxy.class.getClassLoader(), new Class<?>[]{serviceInterface}, invocationHandler);
		return proxy;
	}
	
	public static <T> T create(Class<T> serviceInterface, T defaultDelegate) {
		return create(serviceInterface, defaultDelegate, null);
	}
	
}
