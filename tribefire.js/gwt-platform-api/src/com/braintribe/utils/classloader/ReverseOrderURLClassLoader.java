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
package com.braintribe.utils.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import com.braintribe.logging.Logger;

/**
 * An {@link URLClassLoader} extension which searches for {@link Class}es and resources locally before looking for them
 * in the parent {@link ClassLoader}. The exception being classes/resources available via Java "platform" ClassLoader
 * (Java 9 terminology), which are not overridden on this level.
 * 
 */
public class ReverseOrderURLClassLoader extends URLClassLoader {

	private static final Logger log = Logger.getLogger(ReverseOrderURLClassLoader.class);
	private static final Object CLASS_FILE_SUFFIX = ".class";

	private volatile boolean closed = false;

	private final ClassLoader platformClassLoader;
	private final SecurityManager securityManager;

	public ReverseOrderURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		this.platformClassLoader = resolvePlatformClassLoader();
		this.securityManager = getSecurityManager();

		log.trace(() -> this.getClass().getSimpleName() + " created with parent loader: " + parent);
	}

	// PGA: PlatformClassLoader and Security stuff inspired by Tomcat's handling

	private ClassLoader resolvePlatformClassLoader() {
		ClassLoader j = String.class.getClassLoader();
		if (j == null) {
			j = getSystemClassLoader();
			while (j.getParent() != null) {
				j = j.getParent();
			}
		}
		return j;
	}

	private SecurityManager getSecurityManager() {
		SecurityManager securityManager1 = System.getSecurityManager();
		if (securityManager != null)
			refreshPolicy();

		return securityManager1;
	}

	/** Refresh the system policy file, to pick up eventual changes. */
	protected void refreshPolicy() {
		try {
			// The policy file may have been modified to adjust permissions, so we're reloading it when loading or
			// reloading a Context
			Policy policy = Policy.getPolicy();
			policy.refresh();
		} catch (AccessControlException e) {
			// Some policy files may restrict this, even for the core, so this exception is ignored
		}
	}

	/**
	 * Loads the class with the specified {@code name}.
	 * <p>
	 * Unlike the default implementation of this method, the {@link #loadClass(String) <tt>loadClass</tt>} method on the
	 * parent class loader is called last.
	 * <p>
	 * In this implementation, classes are looked up for in the following order:
	 * <ol>
	 * <li>Invoke {@link #findLoadedClass(String)} to check if the class has already been loaded.</li>
	 * <li>Invoke the {@link #findClass(String)} method to find the class.</li>
	 * <li>Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method on the parent class loader. If the parent is
	 * {@code null} the class loader built-in to the virtual machine is used, instead.</li>
	 * </ol>
	 * <p>
	 * If the class was found using the above steps, and the {@code resolve} flag is {@code true}, this method will then
	 * invoke the {@link #resolveClass(Class)} method on the resulting {@link Class} object.
	 *
	 * @see ClassLoader#loadClass(String, boolean)
	 */
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		log.trace(() -> "Invoking " + getClass().getSimpleName() + ".loadClass(\"" + name + "\", " + resolve + ")");

		Class<?> result = findLoadedClass(name);
		if (result != null)
			return finishLoadClass(result, resolve, "java.lang.ClassLoader.findLoadedClass(String)");

		log.trace(() -> "Class not found with java.lang.ClassLoader.findLoadedClass(String)");

		result = loadWithJavaPlatformClassLoaderIfRelevant(name, resolve);
		if (result != null)
			return finishLoadClass(result, false, "java.net.URLClassLoader.super.loadClass(String, boolean)");

		try {
			result = findClass(name);
			if (result != null)
				return finishLoadClass(result, resolve, "java.net.URLClassLoader.findClass(String)");

		} catch (ClassNotFoundException e) {

			log.trace(() -> "Class not found with java.net.URLClassLoader.findClass(String)");
		}

		try {
			result = super.loadClass(name, resolve);
			if (result != null)
				return finishLoadClass(result, false, "java.net.URLClassLoader.loadClass(String, boolean)");

		} catch (ClassNotFoundException e1) {
			// ignored
		}

		log.trace(() -> "Failed to load class " + name + ". URLs:\n" + describeURLState());
		throw new ClassNotFoundException(name);
	}

	private Class<?> finishLoadClass(Class<?> result, boolean resolve, String method) {
		log.trace(() -> "Found class " + result.getName() + " with " + method);

		if (resolve)
			resolveClass(result);

		return result;
	}

	protected Class<?> loadWithJavaPlatformClassLoaderIfRelevant(String name, boolean resolve) {
		String resourceName = binaryNameToPath(name, false);

		boolean tryJavaPlatformClassLoader;
		try {
			/* Use getResource as it won't trigger an expensive ClassNotFoundException if the resource is not available
			 * from the Java SE class loader. However (see https://bz.apache.org/bugzilla/show_bug.cgi?id=58125 for
			 * details) when running under a security manager in rare cases this call may trigger a
			 * ClassCircularityError. See https://bz.apache.org/bugzilla/show_bug.cgi?id=61424 for details of how this
			 * may trigger a StackOverflowError Given these reported errors, catch Throwable to ensure any other edge
			 * cases are also caught */
			tryJavaPlatformClassLoader = loadPlatformResource(resourceName) != null;

		} catch (Throwable t) {
			// Swallow all exceptions apart from those that must be re-thrown
			handleThrowable(t);

			/* The getResource() trick won't work for this class. We have to try loading it directly and accept that we
			 * might get a CNF Exception. */
			tryJavaPlatformClassLoader = true;
		}

		if (!tryJavaPlatformClassLoader)
			return null;

		try {
			Class<?> clazz = platformClassLoader.loadClass(name);
			if (clazz != null) {
				if (resolve)
					resolveClass(clazz);
				return clazz;
			}

		} catch (ClassNotFoundException e) {
			// ignore
		}

		return null;
	}

	private URL loadPlatformResource(String resourceName) {
		if (securityManager != null) {
			PrivilegedAction<URL> dp = () -> platformClassLoader.getResource(resourceName);
			return AccessController.doPrivileged(dp);

		} else {
			return platformClassLoader.getResource(resourceName);
		}
	}
	/**
	 * Checks whether the supplied Throwable is one that needs to be re-thrown and swallows all others.
	 * 
	 * @param t
	 *            the Throwable to check
	 */
	public static void handleThrowable(Throwable t) {
		if (t instanceof ThreadDeath)
			throw (ThreadDeath) t;

		if (t instanceof StackOverflowError)
			// Swallow silently - it should be recoverable
			return;

		if (t instanceof VirtualMachineError)
			throw (VirtualMachineError) t;

		// All other instances of Throwable will be silently swallowed
	}

	private String binaryNameToPath(String binaryName, boolean withLeadingSlash) {
		// 1 for leading '/', 6 for ".class"
		StringBuilder path = new StringBuilder(7 + binaryName.length());
		if (withLeadingSlash)
			path.append('/');

		path.append(binaryName.replace('.', '/'));
		path.append(CLASS_FILE_SUFFIX);
		return path.toString();
	}

	@Override
	public URL getResource(String name) {
		log.trace(() -> "Invoking " + getClass().getSimpleName() + ".getResource(\"" + name + "\")");

		URL url = findResource(name);
		if (url != null) {
			if (log.isTraceEnabled())
				log.trace("Resource [ " + url + " ] found with java.net.URLClassLoader.findResource(\"" + name + "\")");
			return url;
		}

		log.trace(() -> "Resource not found with java.net.URLClassLoader.findResource(\"" + name + "\")");

		url = super.getResource(name);
		if (url == null)
			log.trace(() -> "Failed to find resource: " + name);
		else if (log.isTraceEnabled())
			log.trace("Resource [ " + url + " ] found with java.jang.ClassLoader.getResource(\"" + name + "\")");

		return url;
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		log.trace(() -> "Invoking " + getClass().getSimpleName() + ".getResources(\"" + name + "\")");

		Enumeration<URL> localUrls = findResources(name);

		Enumeration<URL> parentUrls = null;
		if (getParent() != null)
			parentUrls = getParent().getResources(name);

		return new URLEnumeration(name, localUrls, parentUrls);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		// As per documentation, overriding {@link #getResource(String)} should be enough
		// to influence the search order of this method.
		// We override simply to add some logging around super.getResourceAsStream(String)

		log.trace(() -> "Invoking " + getClass().getSimpleName() + ".getResourceAsStream(\"" + name + "\")");

		InputStream is = super.getResourceAsStream(name);

		if (log.isTraceEnabled()) {
			if (is == null)
				log.trace(() -> "No InputStream opened with java.net.URLClassLoader.getResourceAsStream(\"" + name + "\")");
			else
				log.trace(() -> "InputStream [ " + is + " ] opened with java.net.URLClassLoader.getResourceAsStream(\"" + name + "\")");
		}

		return is;
	}

	@Override
	public void close() throws IOException {
		try {
			super.close();
		} finally {
			closed = true; // We override simply to set our closed flag
		}
	}

	public boolean isClosed() {
		return closed;
	}

	/**
	 * <p>
	 * An {@link Enumeration} of local and parent URL(s) as required per {@link #getResources(String)}.
	 * 
	 * <p>
	 * If adds some logging distinct local from parent resources.
	 * 
	 */
	protected static class URLEnumeration implements Enumeration<URL> {

		private final String name;
		private final Enumeration<URL> localUrls;
		private final Enumeration<URL> parentUrls;

		protected URLEnumeration(String resource, Enumeration<URL> prioritary, Enumeration<URL> secondary) {
			name = resource;
			localUrls = prioritary != null ? prioritary : Collections.<URL> emptyEnumeration();
			parentUrls = secondary != null ? secondary : Collections.<URL> emptyEnumeration();
		}

		@Override
		public boolean hasMoreElements() {
			return localUrls.hasMoreElements() | parentUrls.hasMoreElements();
		}

		@Override
		public URL nextElement() {
			if (localUrls.hasMoreElements()) {
				URL url = localUrls.nextElement();
				log.trace(() -> "Returning element provided by java.net.URLClassLoader.findResource(\"" + name + "\"): [ " + url + " ]");
				return url;
			}

			if (parentUrls.hasMoreElements()) {
				URL url = parentUrls.nextElement();
				log.trace(() -> "Returning element provided by the parent getResources(\"" + name + "\"): [ " + url + " ]");
				return url;
			}

			throw new NoSuchElementException();
		}

	}

	private String describeURLState() {
		URL[] urls = getURLs();

		int total = urls != null ? urls.length : 0;
		if (total == 0)
			return "<empty>";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < total; i++)
			sb.append("\n\t").append('[').append(i).append("]: ").append(urls[i]);

		return sb.toString();
	}

}
