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
package com.braintribe.spring.support.factory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.function.Supplier;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.springframework.jndi.JndiCallback;
import org.springframework.jndi.JndiTemplate;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;


/**
 * <p>
 * A {@link SpringSwitchRule} selector based on the existence of JNDI resources.
 * 
 */
public class JndiResourceSelector implements Supplier<Boolean> {

	private static final Logger log = Logger.getLogger(JndiResourceSelector.class);
	private static boolean treeLogged;

	private String jndiName;
	private Class<?> requiredType;
	private JndiTemplate jndiTemplate;
	private Properties environment;
	private boolean enableResultCaching = true;

	private Boolean cachedResult;

	@Override
	public Boolean get() throws RuntimeException {

		if (cachedResult != null) {
			return cachedResult;
		}

		Object resource = null;

		JndiTemplate template = getJndiTemplate();

		if (log.isDebugEnabled() && !treeLogged) {
			logJndiTree(template, LogLevel.DEBUG);
		}

		try {
			resource = template.lookup(jndiName, requiredType);
		} catch (NamingException e) {
			if (log.isDebugEnabled()) {
				log.debug("Resource not found [ " + jndiName + " ]: " + e.getMessage());
			}
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception during JNDI lookup: " + e.getMessage(), e);
		}

		boolean result = resource != null;

		if (enableResultCaching) {
			cachedResult = result;
		}

		return result;

	}

	/**
	 * <p>
	 * Sets the name of the JNDI resource to be checked for existence
	 * 
	 * @param jndiName
	 *            The name of the JNDI resource
	 */
	@Required
	@Configurable
	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	/**
	 * <p>
	 * Sets the expected type of the JNDI resource, if existent
	 * 
	 * @param requiredType
	 *            The expected type of the JNDI resource
	 */
	@Configurable
	public void setRequiredType(Class<?> requiredType) {
		this.requiredType = requiredType;
	}

	/**
	 * <p>
	 * Sets an reusable {@link JndiTemplate}, which this selector will use for looking JNDI resources up.
	 * 
	 * @param jndiTemplate
	 *            {@link JndiTemplate} to be used for looking JNDI resources up
	 */
	@Configurable
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiTemplate = jndiTemplate;
	}

	/**
	 * <p>
	 * Sets the context properties to be used for looking JNDI resources up, in case no {@link JndiTemplate} was
	 * configured.
	 * 
	 * @param environment
	 *            The context properties to be used for looking JNDI resources up, in case no {@link JndiTemplate} is
	 *            configured
	 */
	@Configurable
	public void setEnvironment(Properties environment) {
		this.environment = environment;
	}

	protected JndiTemplate getJndiTemplate() {

		if (jndiTemplate != null) {
			return jndiTemplate;
		}

		// No re-usable template was configured, we build a local instance, which isn't worth keeping.
		JndiTemplate template = new JndiTemplate();
		if (environment != null) {
			template.setEnvironment(environment);
		}

		return template;

	}

	protected void logJndiTree(JndiTemplate template, final LogLevel logLevel) {
		try {
			template.execute(new JndiCallback<Void>() {
				@Override
				public Void doInContext(Context ctx) throws NamingException {
					String tree = new JndiTreePrinter(ctx).printTree().result();
					if (tree != null && !tree.isEmpty()) {
						log.log(logLevel, "Reachable JNDI Tree:\n" + tree);
					}
					return null;
				}
			});
		} catch (Throwable t) {
			log.warn("Failed to build JNDI tree for logging: " + t.getClass() + ": " + t.getMessage());
			if (log.isTraceEnabled()) {
				log.trace("Error while building JNDI tree for logging", t);
			}
		} finally {
			treeLogged = true;
		}
	}

	/**
	 * <p>
	 * Builds a tree (String) representation of the JNDI registry, primarily for logging convenience.
	 * 
	 * <p>
	 * Has no 3rd party dependencies and can be moved to an utilities artifact.
	 * 
	 */
	public static class JndiTreePrinter {

		private StringWriter stringWriter;
		private PrintWriter writer;
		private Context context;

		public JndiTreePrinter(Context context) {
			this.context = context;
			this.stringWriter = new StringWriter();
			this.writer = new PrintWriter(stringWriter);
		}

		public String result() {
			try {
				writer.flush();
			} finally {
				writer.close();
			}
			return stringWriter.toString().trim();
		}

		public JndiTreePrinter printTree() {
			printEntries(getRoot(), 0);
			return this;
		}

		private void printEntries(String path, int depth) {
			NamingEnumeration<?> names = null;
			try {
				names = context.list(path);

				if (names == null) {
					return;
				}

				while (names.hasMore()) {

					NameClassPair next = (NameClassPair) names.next();

					String entryPath = (path == null || path.length() == 0 || !next.isRelative()) ? next.getName() : path + "/" + next.getName();

					writer.print(repeat('\t', depth));
					writer.print('/');
					writer.print(next.getName());
					writer.print(": ");
					writer.print(next.getClassName());
					writer.print(" (");
					writer.print(entryPath);
					writer.print(')');
					writer.println();

					printEntries(entryPath, depth + 1);

				}
			} catch (NamingException e) {
				// Ignoring expected error when trying to list leaf nodes
			} catch (Exception e) {
				log.warn("Failed to log " + names + ": " + e.getClass() + ": " + e.getMessage());
			} finally {
				close(names);
			}
		}

		/**
		 * <p>
		 * Returns the reachable root as allowed by the container.
		 * 
		 * <p>
		 * Some containers might allow access only via relative names (java:/comp/env is intrinsic) where other might
		 * allow absolute names only.
		 */
		private String getRoot() {
			try {
				if (context.list("").hasMore()) {
					return "";
				}
			} catch (Throwable t) {
				// Ignoring expected exception when empty prefix is not supported by the container
			}
			return "java:";
		}

		private static void close(NamingEnumeration<?> names) {
			if (names != null) {
				try {
					names.close();
				} catch (Exception e) {
					log.warn("Failed to close " + names + ": " + e.getClass() + ": " + e.getMessage());
				}
			}
		}

		private static char[] repeat(char c, int n) {
			if (n < 0)
				throw new IllegalArgumentException("Length must be greater than 0");
			char[] ca = new char[n];
			for (int i = 0; i < n; i++) {
				ca[i] = c;
			}
			return ca;
		}

	}

}
