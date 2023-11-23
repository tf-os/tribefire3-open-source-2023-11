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
package com.braintribe.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A class loader which tries to load classes from the child {@link URLClassLoader} first and only then from the parent.
 * See {@link #ChildFirstClassLoader(URL[], ClassLoader)}.
 */
public class ChildFirstClassLoader extends ClassLoader {

	/**
	 * The child class loader. It's needed because method <code>findClass</code> is not public in {@link URLClassLoader} .
	 */
	private final ChildURLClassLoader childUrlClassLoader;

	/**
	 * Classes are cached to avoid {@link LinkageError}.
	 */
	private final Map<String, Class<?>> cachedClasses = new HashMap<>();
	private final ReentrantReadWriteLock cacheRwl = new ReentrantReadWriteLock();

	/**
	 * Creates a new <code>ChildFirstClassLoader</code> instance.
	 *
	 * @param childUrlClassLoaderUrls
	 *            the urls for the child {@link URLClassLoader} to be created.
	 * @param parentClassLoader
	 *            the parent class loader.
	 */
	public ChildFirstClassLoader(final URL[] childUrlClassLoaderUrls, final ClassLoader parentClassLoader) {
		super(parentClassLoader);
		this.childUrlClassLoader = new ChildURLClassLoader(childUrlClassLoaderUrls, parentClassLoader);
	}

	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

		cacheRwl.readLock().lock();
		try {
			Class<?> clazz = this.cachedClasses.get(name);
			if (clazz == null) {

				// Must release read lock before acquiring write lock
				cacheRwl.readLock().unlock();
				cacheRwl.writeLock().lock();
				try {
					// Recheck state because another thread might have
					// acquired write lock and changed state before we did.
					clazz = this.cachedClasses.get(name);
					if (clazz == null) {
						try {
							clazz = this.childUrlClassLoader.findClass(name);
						} catch (final ClassNotFoundException e) {
							clazz = super.loadClass(name, resolve);
						}
						this.cachedClasses.put(name, clazz);
					}
					// Downgrade by acquiring read lock before releasing write lock
					cacheRwl.readLock().lock();
				} finally {
					cacheRwl.writeLock().unlock(); // Unlock write, still hold read
				}
			}

			return clazz;
		} finally {
			cacheRwl.readLock().unlock();
		}

	}

	/**
	 * A {@link URLClassLoader} which sets the parent to <code>null</code>. The only method which uses the parent is the
	 * overridden method {@link #findClass(String)}, which tries to load from child first and then falls back to parent. The
	 * visiblity is changed to <code>public</code> to be able to access it from other classes.
	 */
	private class ChildURLClassLoader extends URLClassLoader {
		private final ClassLoader realParent;

		public ChildURLClassLoader(final URL[] urls, final ClassLoader realParent) {
			super(urls, null);

			this.realParent = realParent;
		}

		@Override
		public Class<?> findClass(final String name) throws ClassNotFoundException {
			Class<?> clazz = ChildFirstClassLoader.this.cachedClasses.get(name);
			if (clazz == null) {
				try {
					// first try to use the URLClassLoader findClass
					clazz = super.findClass(name);
				} catch (final ClassNotFoundException e) {
					// if that fails, we ask our real parent classloader to load the class (we give up)
					clazz = this.realParent.loadClass(name);
				}
				ChildFirstClassLoader.this.cachedClasses.put(name, clazz);
			}
			return clazz;
		}
	}
}
