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
package com.braintribe.model.processing.dmbrpc.client;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class DynamicMBeanProxy implements InvocationHandler {
	private ObjectName objectName;
	private MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
	
	public static <T> T create(Class<T> interfaceClass, String objectName) throws MalformedObjectNameException {
		DynamicMBeanProxy invocationHandler = new DynamicMBeanProxy(new ObjectName(objectName));
		@SuppressWarnings("unchecked")
		T service = (T)Proxy.newProxyInstance(DynamicMBeanProxy.class.getClassLoader(), new Class<?>[]{interfaceClass}, invocationHandler);
		return service;
	}
	
	public static <T> T create(Class<T> interfaceClass, ObjectName objectName) {
		DynamicMBeanProxy invocationHandler = new DynamicMBeanProxy(objectName);
		@SuppressWarnings("unchecked")
		T service = (T)Proxy.newProxyInstance(DynamicMBeanProxy.class.getClassLoader(), new Class<?>[]{interfaceClass}, invocationHandler);
		return service;
	}
	
	private DynamicMBeanProxy(ObjectName objectName) {
		this.objectName = objectName;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// shortcut delegation for object methods with local method of the proxy
		if (method.getDeclaringClass() == Object.class) {
			try {
				return method.invoke(this, args);
			} catch (UndeclaredThrowableException | InvocationTargetException e) {
				throw (e.getCause() != null) ? e.getCause() : e;
			}
		}

		try {
			return mbeanServer.invoke(objectName, method.getName(), args, buildSignature(method));
		} catch (MBeanException | ReflectionException e) {
			throw (e.getCause() != null) ? e.getCause() : e;
		}
	}

	private static String[] buildSignature(Method method) {
		Class<?>[] parameterTypes = method.getParameterTypes();
		String signatures[] = new String[parameterTypes.length];
		
		for (int i = 0; i < parameterTypes.length; i++) {
			signatures[i] = parameterTypes[i].getName();
		}
		
		return signatures;
	}
}
