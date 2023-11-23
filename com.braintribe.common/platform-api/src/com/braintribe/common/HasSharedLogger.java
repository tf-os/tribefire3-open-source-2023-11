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
package com.braintribe.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.braintribe.common.lcd.ImplementationIssueException;
import com.braintribe.logging.Logger;
import com.braintribe.utils.ReflectionTools;

/**
 * Helper class that makes it easy to let classes from a class hierarchy share the same <code>Logger</code> (from the sub class).
 * <p>
 * Example: let's assume we have a class called <code>DeleteFileRequestProcessor</code> which is responsible for deleting files as specified by a
 * certain request. The actual code for deleting the file is in that class. However, there is also code shared between all file-related processors and
 * that is implemented in super class <code>AbstractFileRequestProcessor</code>. Furthermore some common helpers for all requests, e.g. printing the
 * request description, is provided by <code>AbstractRequestProcessor</code>.
 * <p>
 * There are multiple options for how to do the logging in such as scenario:
 * <ul>
 * <li>Sub classes use logger from the super class: To do this, one just declares a <code>protected</code> <code>Logger</code> in the super class,
 * i.e. <code>AbstractRequestProcessor</code> and lets sub classes use it. This is straight-forward to implement, but one always sees the name of the
 * super class in the log, which is often not the intention.
 * <li>Each class uses its own <code>Logger</code>: Each class has its own <code>private</code> <code>Logger</code>. This is also easy and means that
 * one will see all the different class names in the log. (If that's what you want, stop reading now and just do it. There is nothing wrong about
 * it!)</li>
 * <li>Super classes use logger from sub class: That way general request processing methods from super types (such as printing request details) will
 * still be logged with the name of the sub class (<code>DeleteFileRequestProcessor</code>) in the log. One approach to achieve the third solution is
 * to define an abstract method <code>logger()</code> which returns the logger. This works, but all sub classes have to implement it.</li>
 * </ul>
 * <br>
 * The <code>HasSharedLogger</code> makes implementing the last approach more convenient. The common super class, in our example
 * <code>AbstractRequestProcessor</code> just has to extend <code>HasSharedLogger</code>. It can then use the <code>protected</code> {@link #logger}
 * for logging. The concrete sub types, in our case <code>DeleteFileRequestProcessor</code>, just declare a <code>private static</code> {@link Logger}
 * field named {@value #LOGGER_FIELD} (as usual). This is needed for logging from <code>static</code> methods. If that's not required, there is
 * nothing to do at all in the sub classes.
 *
 * @author michael.lafite
 */
public abstract class HasSharedLogger {

	/**
	 * The expected name for <code>static</code> <code>Logger</code> fields in sub classes.
	 */
	public static final String LOGGER_FIELD = "logger";

	/**
	 * Used by {@link #logger(Class)} and {@link #setLogger()}.
	 */
	private static Map<Class<? extends HasSharedLogger>, Logger> loggers = new HashMap<>();

	/**
	 * The <code>Logger</code> shared between classes in a class hierarchy. See {@link HasSharedLogger} for an example.
	 * <p>
	 * For non-<code>static</code> methods, one can always just use this field. For logging from <code>static</code> methods the sub classes may
	 * declare their own {@link Logger} field named {@value #LOGGER_FIELD}. In that case that <code>Logger</code> will automatically be re-used, i.e.
	 * this field will reference it. (This is done via reflection, but only once per type and therefore very fast.) <br>
	 * For logging from <code>static</code> methods in super classes see {@link #logger(Class)}.
	 */
	protected Logger logger;

	/**
	 * Invokes {@link #setLogger()}.
	 */
	protected HasSharedLogger() {
		setLogger();
	}

	/**
	 * This method allows for logging from <code>static</code> methods in super classes. Since one has to pass the <code>subClass</code>, this is a
	 * bit inconvenient though and there is also a slight performance impact for looking up the <code>Logger</code>.<br>
	 * Therefore, using this method should be the exception. If you (would) need it often, that probably indicates that {@link HasSharedLogger} is not
	 * a good fit for your use case.
	 */
	protected static Logger logger(Class<? extends HasSharedLogger> subClass) {
		return loggers.get(subClass);
	}

	/**
	 * Sets field {@link #logger}, either by re-using a {@link #LOGGER_FIELD field} from the sub class or by creating a new <code>Logger</code> for
	 * this class or by re-using the previously created <code>Logger</code> for this class (during instantiation of another instance of the same
	 * type).
	 */
	private void setLogger() {
		logger = loggers.get(getClass());
		if (logger == null) {
			synchronized (loggers) {
				if (logger == null) {
					Class<? extends HasSharedLogger> clazz = getClass();

					Field loggerField = ReflectionTools.getField(LOGGER_FIELD, clazz);
					if (loggerField == null || loggerField.getDeclaringClass().equals(HasSharedLogger.class)) {
						// logger field doesn't exist --> create logger for class
						logger = Logger.getLogger(clazz);
					} else {
						// logger field exists --> reuse this logger
						Object loggerFromSubclass = ReflectionTools.getFieldValue(loggerField, this);
						if (loggerFromSubclass instanceof Logger) {
							logger = (Logger) loggerFromSubclass;
						} else {
							throw new ImplementationIssueException("Field " + LOGGER_FIELD + " in class " + loggerField.getDeclaringClass().getName()
									+ " has type " + loggerField.getType().getName() + " instead of " + Logger.class.getName() + "!");
						}

					}
					// put logger into map to re-use the logger for multiple processor instances of the same type
					loggers.put(clazz, logger);
				}
			}
		}
	}

}
