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
package com.braintribe.config.configurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import com.braintribe.config.configurator.ConfiguratorPriority.Level;
import com.braintribe.logging.Logger;
import com.braintribe.utils.lcd.StopWatch;

/**
 * <p>
 * A {@link Configurator} which triggers the configuration of other {@link Configurator}(s) defined in the classpath.
 * 
 * <p>
 * This configurator works by:
 * 
 * <ul>
 * <li>Searching for configurator files ({@code META-INF/.configurator}) in the classpath;</li>
 * <li>Instantiating the {@link Configurator}(s) defined, by their line-separated fully qualified names, in such
 * files;</li>
 * <li>Invoking {@link Configurator#configure()} in the instantiated configurators.</li>
 * </ul>
 * 
 */
public class ClasspathConfigurator implements Configurator {

	private ClassLoader classLoader;

	private String configuratorFile = "META-INF/.configurator";

	private ConfiguratorContext context;

	private static final Logger log = Logger.getLogger(ClasspathConfigurator.class);

	public ClasspathConfigurator() {
		classLoader = getClass().getClassLoader();
	}

	public ClasspathConfigurator(ConfiguratorContext context) {
		this();
		this.context = context;
	}

	@Override
	public void configure() throws ConfiguratorException {

		StopWatch stopWatch = new StopWatch();
		stopWatch.intermediate(context.getServletContextPath());
		
		List<Configurator> configurators = loadConfigurators();
		
		stopWatch.intermediate("Load Configurators");

		sortConfiguratorsByPrio(configurators);

		stopWatch.intermediate("Sort");
		
		for (Configurator configurator : configurators) {

			log.trace(() -> "Configuring " + configurator + " ... ");

			configurator.configure();
			
			stopWatch.intermediate("Configurator "+configurator.toString());

			log.debug(() -> "Configured " + configurator);

		}

		log.trace(() -> "configure: "+stopWatch);
	}

	private static void sortConfiguratorsByPrio(List<Configurator> configurators) {
		Collections.sort(configurators, new Comparator<Configurator>() {
			@Override
			public int compare(Configurator c1, Configurator c2) {
				if (c1 == null && c2 == null) {
					return 0;
				}
				if (c1 == null) {
					return -1;
				}
				if (c2 == null) {
					return 1;
				}
				if (c1.equals(c2)) {
					return 0;
				}

				ConfiguratorPriority c1p = c1.getClass().getAnnotation(ConfiguratorPriority.class);
				ConfiguratorPriority c2p = c2.getClass().getAnnotation(ConfiguratorPriority.class);

				int level = priority(c1p).compareTo(priority(c2p));

				if (level != 0) {
					return level;
				}

				return Integer.compare(order(c1p), order(c2p));

			}

			private ConfiguratorPriority.Level priority(ConfiguratorPriority configuratorPriority) {
				if (configuratorPriority == null) {
					return Level.normal;
				}
				return configuratorPriority.value();
			}

			private int order(ConfiguratorPriority configuratorPriority) {
				if (configuratorPriority == null) {
					return 0;
				}
				return configuratorPriority.order();
			}

		});

	}

	private List<Configurator> loadConfigurators() throws ConfiguratorException {

		Enumeration<URL> resources = null;
		List<Configurator> configurators = new ArrayList<Configurator>();

		try {
			log.debug(() -> "Reading configurator resources [ " + configuratorFile + " ] ");
			resources = classLoader.getResources(configuratorFile);
		} catch (IOException e) {
			throw new ConfiguratorException("Failed to enumerate configurator resources: " + e.getMessage(), e);
		}

		int filesFound = 0;

		while (resources.hasMoreElements()) {
			filesFound++;
			URL resourceUrl = resources.nextElement();

			log.debug(() -> "Found configurator file [ " + resourceUrl + " ] ");

			configurators.addAll(loadConfigurators(resourceUrl));
		}

		if (log.isDebugEnabled() && filesFound == 0) {
			log.debug("No configurator resources found (" + configuratorFile + ") ");
		}

		return configurators;

	}

	private List<Configurator> loadConfigurators(URL configuratorResourceFile) throws ConfiguratorException {

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(configuratorResourceFile.openStream()));
		} catch (IOException e) {
			throw new ConfiguratorException("Failed to open stream to [ " + configuratorResourceFile + " ]"
					+ (e.getMessage() != null && !e.getMessage().isEmpty() ? ": " + e.getMessage() : ""), e);
		}

		List<String> configuratorClasses = new ArrayList<String>();

		String configuratorClassName;
		try {
			while ((configuratorClassName = in.readLine()) != null) {
				configuratorClassName = configuratorClassName.trim();
				if (!configuratorClassName.isEmpty()) {
					configuratorClasses.add(configuratorClassName);
				}
			}
		} catch (IOException e) {
			throw new ConfiguratorException("Failed to read from [ " + configuratorResourceFile + " ]"
					+ (e.getMessage() != null && !e.getMessage().isEmpty() ? ": " + e.getMessage() : ""), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.warn(() -> "Failed to close [ " + configuratorResourceFile + " ] reader: " + e.getMessage(), e);
				}
			}
		}

		if (configuratorClasses.isEmpty()) {
			log.debug(() -> "No configurator class names found in [ " + configuratorResourceFile + " ]");
			return Collections.<Configurator> emptyList();
		}

		return loadConfigurators(configuratorClasses);

	}

	private List<Configurator> loadConfigurators(List<String> configuratorClasses) throws ConfiguratorException {

		List<Configurator> configurators = new ArrayList<Configurator>(configuratorClasses.size());

		for (String configuratorClassName : configuratorClasses) {
			configurators.add(initializeConfigurator(configuratorClassName));
		}

		return configurators;

	}

	private Configurator initializeConfigurator(String configuratorClassName) {

		Class<? extends Configurator> configuratorClass = loadClass(configuratorClassName);

		Configurator configurator = instantiateConfigurator(configuratorClass);

		if (context != null && configurator instanceof ContextAware) {
			try {
				((ContextAware) configurator).setContext(context);
			} catch (Exception e) {
				throw new ConfiguratorException("Failed to initialize context for configurator of type [ " + configuratorClass.getName() + " ]"
						+ (e.getMessage() != null && !e.getMessage().isEmpty() ? ": " + e.getMessage() : ""), e);
			}
		}

		log.trace(() -> "Initialized configurator " + configurator);
		return configurator;

	}

	private Class<? extends Configurator> loadClass(String configuratorClassName) {

		log.trace(() -> "Loading configurator of type " + configuratorClassName + " ...");

		Class<?> clazz;
		try {
			clazz = Class.forName(configuratorClassName);
			log.trace(() -> "Loaded conifgurator of type " + configuratorClassName);
		} catch (Exception e) {
			throw new ConfiguratorException("Failed to load configurator of type [ " + configuratorClassName + " ]"
					+ (e.getMessage() != null && !e.getMessage().isEmpty() ? ": " + e.getMessage() : ""), e);
		}

		if (!Configurator.class.isAssignableFrom(clazz)) {
			throw new ConfiguratorException("Class " + clazz + " is not a " + Configurator.class.getName());
		}

		return (Class<? extends Configurator>) clazz;

	}

	private Configurator instantiateConfigurator(Class<? extends Configurator> configuratorClass) {

		log.trace(() -> "Instantiating configurator of type " + configuratorClass.getName() + " ...");

		Configurator configurator;
		try {
			configurator = configuratorClass.getDeclaredConstructor().newInstance();
			log.trace(() -> "Instantiated configurator of type " + configuratorClass.getName());
			
		} catch (Exception e) {
			throw new ConfiguratorException("Failed to instantiate configurator of type [ " + configuratorClass + " ]"
					+ (e.getMessage() != null && !e.getMessage().isEmpty() ? ": " + e.getMessage() : ""), e);
		}

		return configurator;

	}
	

}
