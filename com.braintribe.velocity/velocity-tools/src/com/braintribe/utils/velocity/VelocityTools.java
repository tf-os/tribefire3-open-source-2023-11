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
package com.braintribe.utils.velocity;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.GenericRuntimeException;
import com.braintribe.utils.MapTools;

/**
 * This class provides <code>Velocity</code> related utility methods.
 * 
 * @author michael.lafite
 */
public class VelocityTools {

	/**
	 * Default template name (e.g. used by {@link #evaluate(VelocityEngine, VelocityContext, String)}.
	 */
	public static final String DEFAULT_TEMPLATE_NAME = "VelocityTemplate";

	protected VelocityTools() {
		// nothing to do
	}

	/**
	 * Invokes {@link #newVelocityEngine(Map)} with {@link #getDefaultVelocityEngineProperties() default properties}.
	 * 
	 * @return An initialized VelocityEngine
	 */
	public static VelocityEngine newVelocityEngine() {
		return newVelocityEngine(getDefaultVelocityEngineProperties(), true);
	}

	/**
	 * Invokes {@link #newVelocityEngine(Map, boolean)} with {@link #getDefaultVelocityEngineProperties() default
	 * properties}.
	 * 
	 * @return An uninitialized VelocityEngine
	 */
	public static VelocityEngine newUninitializedVelocityEngine() {
		return newVelocityEngine(getDefaultVelocityEngineProperties(), false);
	}

	/**
	 * Returns a default (initialized) VelocityEngine that is configured to load resources from the classpath.
	 * 
	 * @return An initialized VelocityEngine
	 */
	public static VelocityEngine newResourceLoaderVelocityEngine(boolean initialize) {
		VelocityEngine engine = newVelocityEngine(getDefaultVelocityEngineProperties(), false);
		List<String> loaderList = new ArrayList<String>();
		loaderList.add("classpath");
		loaderList.add("string");
		engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, loaderList);
		engine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		engine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
		engine.setProperty("resource.loader.string.repository.class", StringResourceRepositoryImpl.class.getName());

		if (initialize) {
			engine.init();
		}
		return engine;
	}

	/**
	 * Returns a default (initialized) VelocityEngine that is configured to load resources from the classpath.
	 * 
	 * @return An initialized VelocityEngine
	 */
	public static VelocityEngine newMultiLoaderVelocityEngine(File templatesDirectory, boolean initialize) {
		VelocityEngine engine = newVelocityEngine(getDefaultVelocityEngineProperties(), false);
		List<String> loaderList = new ArrayList<String>();
		loaderList.add("classpath");
		loaderList.add("file");
		loaderList.add("string");
		engine.setProperty(RuntimeConstants.RESOURCE_LOADERS, loaderList);
		engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatesDirectory.getAbsolutePath());
		engine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		engine.setProperty("resource.loader.string.class", StringResourceLoader.class.getName());
		engine.setProperty("resource.loader.string.repository.class", StringResourceRepositoryImpl.class.getName());
		if (initialize) {
			engine.init();
		}
		return engine;
	}

	/**
	 * Returns a default (initialized) VelocityEngine that is configured to load resources from a provided file directory.
	 * 
	 * @return An initialized VelocityEngine
	 */
	public static VelocityEngine newFileSystemLoaderVelocityEngine(File templatesDirectory, boolean initialize) {
		VelocityEngine engine = newVelocityEngine(getDefaultVelocityEngineProperties(), false);
		engine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatesDirectory.getAbsolutePath());
		if (initialize) {
			engine.init();
		}
		return engine;
	}
	/**
	 * Creates a new {@link VelocityContext} and puts the passed <code>keyAndValuePairs</code> into it.
	 */
	public static VelocityContext newVelocityContext(final Object... keyAndValuePairs) {
		final Map<String, Object> contextEntries = MapTools.getParameterizedMap(String.class, Object.class, keyAndValuePairs);
		final VelocityContext context = new VelocityContext();

		for (final Entry<String, Object> entry : contextEntries.entrySet()) {
			context.put(entry.getKey(), entry.getValue());
		}

		return context;
	}

	/**
	 * Gets a new (initialized) {@link VelocityEngine} instance.
	 * 
	 * @param properties
	 *            the engine properties (see {@link #getDefaultVelocityEngineProperties()}).
	 * @return the new engine instance
	 */
	public static VelocityEngine newVelocityEngine(final Map<String, Object> properties) {
		return newVelocityEngine(properties, true);
	}

	/**
	 * Gets a new {@link VelocityEngine} instance.
	 * 
	 * @param properties
	 *            the engine properties (see {@link #getDefaultVelocityEngineProperties()}).
	 * @return the new engine instance
	 */
	public static VelocityEngine newVelocityEngine(final Map<String, Object> properties, boolean initialize) {
		final VelocityEngine engine = new VelocityEngine();
		for (final Entry<String, Object> property : properties.entrySet()) {
			engine.setProperty(property.getKey(), property.getValue());
		}
		if (initialize) {
			engine.init();
		}
		return engine;
	}

	/**
	 * Gets the default velocity engine properties:<br/>
	 * <ul>
	 * <li>The strict settings {@link RuntimeConstants#STRICT_MATH}, {@link RuntimeConstants#RUNTIME_REFERENCES_STRICT} and
	 * {@link RuntimeConstants#RUNTIME_REFERENCES_STRICT_ESCAPE} are enabled.</li>
	 * <li>Input and output encoding are set to <code>UTF-8</code>.</li>
	 * </ul>
	 * <p/>
	 * Attention: settings may change and new properties may be added in the future (overriding the Velocity defaults).
	 * Therefore, if you rely on a specific setting, you should either explicitly override that setting or not use these
	 * method at all.
	 * 
	 * @return the default properties (in a <code>Map</code>).
	 */
	public static Map<String, Object> getDefaultVelocityEngineProperties() {
		final Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(RuntimeConstants.STRICT_MATH, true);
		properties.put(RuntimeConstants.RUNTIME_REFERENCES_STRICT, true);
		properties.put(RuntimeConstants.RUNTIME_REFERENCES_STRICT_ESCAPE, true);
		properties.put(RuntimeConstants.INPUT_ENCODING, Constants.ENCODING_UTF8_NIO);
		properties.put(RuntimeConstants.PARSER_POOL_SIZE, 5); // Default: 20
		return properties;
	}

	/**
	 * Invokes {@link #evaluate(VelocityEngine, VelocityContext, String, String)} using {@link #DEFAULT_TEMPLATE_NAME} (
	 * {@value #DEFAULT_TEMPLATE_NAME}) as template name.
	 */
	public static String evaluate(final VelocityEngine engine, final VelocityContext context, final String template) {
		return evaluate(engine, context, template, DEFAULT_TEMPLATE_NAME);
	}

	/**
	 * Evaluates the <code>template</code> using the passed <code>engine</code> and <code>context</code>. This is just a
	 * convenience method that deals with all possible exceptions and returns the result as a string (i.e. no Writer
	 * handling).
	 */
	public static String evaluate(final VelocityEngine engine, final VelocityContext context, final String template, final String templateName) {
		final String defaultErrorMessagePrefix = "Error while evaluating velocity template!";

		final StringWriter stringWriter = new StringWriter();
		try {
			engine.evaluate(context, stringWriter, templateName, template);
		} catch (final ParseErrorException e) {
			throw new GenericRuntimeException(defaultErrorMessagePrefix, e);
		} catch (final MethodInvocationException e) {
			throw new GenericRuntimeException(defaultErrorMessagePrefix, e);
		} catch (final ResourceNotFoundException e) {
			throw new GenericRuntimeException(defaultErrorMessagePrefix, e);
		}
		final String result = stringWriter.toString();
		return result;
	}
}
