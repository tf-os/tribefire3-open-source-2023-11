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
package com.braintribe.model.generic;

import java.io.InputStream;
import java.util.Properties;

import com.braintribe.logging.Logger;
import com.braintribe.model.generic.reflection.GenericModelException;

/**
 * Provides the correct implementation of {@link GmPlatform}. This is the default JVM implementation.
 * <p>
 * As of 22.2.2013 (when this was created) the intended usage was that the {@link GMF} is provided with the right
 * implementation of this interface via {@linkplain GmPlatformProvider} at the startup automatically. For this there the
 * static method {@link #provide()} should be used. As stated before, this is the default JVM implementation for such
 * provider and different platforms are expected to implement (emulate) their own version of this class (i.e. such with
 * exact same name and methods).
 */
public class GmPlatformProvider {

	private static final String GMF_PROPS_RESOURCE_NAME = "META-INF/gm.properties";
	private static final String PLATFORM_IMPL_PROPERTY = "platform.implementation";

	private static final Logger logger = Logger.getLogger(GmPlatformProvider.class);

	private static GmPlatform plattform;

	public static GmPlatform provide() {
		if (plattform == null) {

			try {
				String gmfDelegateClassName = null;

				if (System.getenv().containsKey(PLATFORM_IMPL_PROPERTY)) {
					gmfDelegateClassName = System.getenv().get(PLATFORM_IMPL_PROPERTY);
					logger.debug("Found " + PLATFORM_IMPL_PROPERTY + " setting in environment.");

				} else if (null != System.getProperty(PLATFORM_IMPL_PROPERTY)) {
					gmfDelegateClassName = System.getProperty(PLATFORM_IMPL_PROPERTY);
					logger.debug("Found " + PLATFORM_IMPL_PROPERTY + " setting in system properties.");

				} else {
					logger.debug("Try to load " + PLATFORM_IMPL_PROPERTY + " from META-INF.");

					InputStream is = GenericEntity.class.getClassLoader().getResourceAsStream(GMF_PROPS_RESOURCE_NAME);

					if (is == null) {
						is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GMF_PROPS_RESOURCE_NAME);
					}

					if (is == null)
						throw new GenericModelException(
								"Cannot initialize GmPlatform - file not found 'gm.properties'. This usually happens when gm-core4-jvm is not on the classpath. "
										+ "If it shouldn't be on the classpath and you wanted to set the 'platform.implementation' property"
										+ " as environment variable or as a JVM argument, well, it didn't work.");

					try {
						Properties props = new Properties();
						props.load(is);

						gmfDelegateClassName = props.getProperty(PLATFORM_IMPL_PROPERTY);

					} catch (Exception e) {
						throw new RuntimeException("Error while loading properties", e);
					} finally {
						is.close();
					}
				}
				Class<?> delegateClass = Class.forName(gmfDelegateClassName);

				if (!GmPlatform.class.isAssignableFrom(delegateClass)) {
					throw new RuntimeException("Wrong GM configuration. Class defined by '" + PLATFORM_IMPL_PROPERTY
							+ "' property must implement the '" + GmPlatform.class.getName() + "'");
				}

				plattform = delegateClass.asSubclass(GmPlatform.class).getDeclaredConstructor().newInstance();

			} catch (Exception e) {
				throw new RuntimeException("Error while initializing GmPlatform", e);
			}
		}

		return plattform;
	}
}
