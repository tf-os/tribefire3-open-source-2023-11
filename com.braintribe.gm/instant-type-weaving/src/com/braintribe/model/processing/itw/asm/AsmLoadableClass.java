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
package com.braintribe.model.processing.itw.asm;

/**
 * 
 */
public abstract class AsmLoadableClass extends AsmClass {

	byte[] bytes;
	Class<?> loadedClass;

	public AsmLoadableClass(String name, AsmClassPool classPool) {
		super(name, classPool);
	}

	/**
	 * JLS $12.7 says: A class or interface may be unloaded if and only if its defining class loader may be reclaimed by the garbage collector as
	 * discussed in $12.6
	 * 
	 * This means, once we load our class with our ClassLoader, we may delete the bytes as the class will be loaded at least as long as the
	 * ClassLoader -> there will never be any need to load it again with the same ClassLoader.
	 */
	public synchronized void notifyLoaded(Class<?> loadedClass) {
		this.bytes = null;
		this.loadedClass = loadedClass;
	}

	@Override
	public Class<?> getJavaType() {
		if (loadedClass == null)
			throw new IllegalStateException("Cannot get javaType, this class wasn't loaded yet: " + name);

		return loadedClass;
	}

	/** We need this to be public in case we want to do something with the bytes, like storing them in a file. */
	public byte[] getBytes() {
		return bytes;
	}

	@Override
	public final boolean isPrimitive() {
		return false;
	}

	@Override
	public final String toString() {
		return getClass().getSimpleName() + "[" + name + "]";
	}

}
