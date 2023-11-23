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
package com.braintribe.transport.messaging.dbm.mbean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * <p>
 * Generic {@link InvocationHandler} for MBeans.
 * <p>
 * 
 * Unlike {@link javax.management.JMX#newMBeanProxy(MBeanServerConnection, ObjectName, Class)}, the target MBean
 * instance does not have to implement the interface expected for the resulting proxy.
 */
public class MBeanProxy implements InvocationHandler {

	private ObjectName objectName;
	private MBeanServerConnection mbeanServer;

	private MBeanProxy(ObjectName objectName, MBeanServerConnection mbeanServer) {
		this.objectName = objectName;
		this.mbeanServer = mbeanServer;
	}

	public static <T> T create(Class<T> interfaceClass, ObjectName objectName, MBeanServerConnection mbeanServer) {
		return create(interfaceClass, objectName, mbeanServer, MBeanProxy.class.getClassLoader());
	}

	public static <T> T create(Class<T> interfaceClass, ObjectName objectName, MBeanServerConnection mbeanServer, ClassLoader cl) {
		MBeanProxy invocationHandler = new MBeanProxy(objectName, mbeanServer);
		@SuppressWarnings("unchecked")
		T service = (T) Proxy.newProxyInstance(cl, new Class<?>[] { interfaceClass }, invocationHandler);
		return service;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// shortcut delegation for object methods with local method of the proxy
		if (method.getDeclaringClass() == Object.class) {
			return method.invoke(this, args);
		}

		return mbeanServer.invoke(objectName, method.getName(), args, buildSignature(method));
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
