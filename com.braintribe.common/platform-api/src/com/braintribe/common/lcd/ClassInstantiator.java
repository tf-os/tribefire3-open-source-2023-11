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
package com.braintribe.common.lcd;

/**
 * Provides methods to create new instances of a specified class. The purpose of this class is to avoid dependencies, for example when no Generic
 * Model features except entity creation are required.
 *
 * @param <T>
 *            super class for all types for which entity creation is supported. Note that this does not necessarily mean that creation is supported
 *            for all sub types!
 *
 * @author michael.lafite
 */
public interface ClassInstantiator<T> {

	/**
	 * Returns a new instance of the specified class. Please read the documentation of the implementing class to check which classes can be
	 * instantiated.
	 *
	 * @throws ClassInstantiationException
	 *             if the implementing class cannot create a new instance of the specified class (for whatever reason).
	 */
	<U extends T> U instantiate(Class<U> clazz) throws ClassInstantiationException;

	/**
	 * @see #instantiate(Class)
	 */
	T instantiate(String className) throws ClassInstantiationException;

	public static class ClassInstantiationException extends AbstractUncheckedBtException {

		private static final long serialVersionUID = -3959480387603696574L;

		public ClassInstantiationException(final String message, final Throwable cause) {
			super(message, cause);
		}

		public ClassInstantiationException(final String message) {
			super(message);
		}
	}
}
