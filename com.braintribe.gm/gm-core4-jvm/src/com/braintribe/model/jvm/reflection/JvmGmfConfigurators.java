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
package com.braintribe.model.jvm.reflection;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;

import com.braintribe.config.configurator.Configurator;
import com.braintribe.logging.Logger;

/**
 * This class is responsible for running all the GMF configurators on the classpath, which are registered from text-files on the classpath (path:
 * {@value #GMF_CONFIGURATOR_LOCATION}). It scans all the configurator files, which are expected to contain one class name per line. Each of these
 * classes is also expected to be an implementation of {@link Configurator} interface.
 * <p>
 * The whole initialization is done in the static initializer for this class, and should be triggered from the outside by calling the static
 * {@link #triggerClassLoading()} method.
 * 
 * @see Configurator
 * 
 * @author peter.gazdik
 */
public class JvmGmfConfigurators {

	private static final String GMF_CONFIGURATOR_LOCATION = "META-INF/gmf.configurator";

	private static final ClassLoader classLoader = JvmGenericModelTypeReflection.getClassLoader();

	private static final Logger log = Logger.getLogger(JvmGmfConfigurators.class);

	static {
		loadConfigurators();
	}

	private static void loadConfigurators() {
		Enumeration<URL> declarationUrls = null;

		try {
			declarationUrls = classLoader.getResources(GMF_CONFIGURATOR_LOCATION);
		} catch (IOException e) {
			log.error("Error while retrieving configurer files (gm.configurer) on classpath of classloader: " + classLoader, e);
			return;
		}

		while (declarationUrls.hasMoreElements())
			processUrl(declarationUrls.nextElement());
	}

	private static void processUrl(URL url) {
		try (Scanner scanner = new Scanner(url.openStream())) {
			while (scanner.hasNextLine())
				configure(scanner.nextLine(), url);

		} catch (Exception e) {
			log.error("Error while parsing configurators from " + url, e);
		}
	}

	private static void configure(String configuratorClass, URL originUrl) {
		configuratorClass = configuratorClass.trim();
		if (configuratorClass.isEmpty())
			return;

		Class<?> clazz = getClassSafe(configuratorClass);
		if (clazz == null) {
			logError("Class not found: " + configuratorClass, originUrl);
			return;
		}

		if (!Configurator.class.isAssignableFrom(clazz)) {
			logError("Class is not a Configurator: " + configuratorClass, originUrl);
			return;
		}

		Configurator configurator = newConfigurator(clazz, originUrl);
		if (configurator == null)
			return;

		try {
			configurator.configure();

		} catch (Exception e) {
			log.error("Error while configuring " + configuratorClass + ". Url:" + originUrl, e);
			return;
		}

		log.debug("Successfully applied configurator " + configuratorClass + ". Url: " + originUrl);
	}

	private static Class<?> getClassSafe(String configurator) {
		try {
			return Class.forName(configurator, false, classLoader);
		} catch (ClassNotFoundException e) {
			log.error("Cconfigurer " + configurator + " class not found.");
			return null;
		}
	}

	private static Configurator newConfigurator(Class<?> clazz, URL originUrl) {
		try {
			return clazz.asSubclass(Configurator.class).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			log.error("Error while instantiating configurator " + clazz.getName() + ". Url:" + originUrl, e);
			return null;
		}
	}

	private static void logError(String message, URL originUrl) {
		log.error("[CONFIGURATOR] " + message + ". Url: " + originUrl);
	}

	/** Dummy method that trigger class-Loading and initialization of this class. */
	public static void triggerClassLoading() {
		// DO NOT DELETE THIS METHOD!
	}

}
