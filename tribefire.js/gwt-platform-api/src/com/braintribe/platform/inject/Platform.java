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
package com.braintribe.platform.inject;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public abstract class Platform {

	private static Properties platformProperties;

	static {

		initPlatformProperties();

	}

	public static void initPlatformProperties() {

		try {
			Enumeration<URL> resources = Platform.class.getClassLoader().getResources("jvmPlatformImplementations");

			platformProperties = new Properties();

			while (resources.hasMoreElements()) {

				URL url = resources.nextElement();

				Properties properties = new Properties();
				properties.load(new InputStreamReader(url.openStream(), "UTF-8"));

				platformProperties.putAll(properties);
			}

		} catch (Exception e) {
			throw new PlatformRuntimeException("Initialisation of platform failed", e);
		}
	}

	public static <T> T create(Class<T> clazz) {

		try {
			String className = platformProperties.getProperty(clazz.getName());
			Class<?> delegateClass;

			delegateClass = Class.forName(className);

			if (!clazz.isAssignableFrom(delegateClass)) {
				throw new PlatformRuntimeException(
						"Wrong Platform configuration. Class defined by '" + delegateClass + "' property must implement the '" + clazz + "'");
			}

			T newInstance = delegateClass.asSubclass(clazz).getDeclaredConstructor().newInstance();
			return newInstance;

		} catch (Exception e) {
			throw new PlatformRuntimeException("Class " + clazz + " could not be instantiated", e);
		}

	}

}
