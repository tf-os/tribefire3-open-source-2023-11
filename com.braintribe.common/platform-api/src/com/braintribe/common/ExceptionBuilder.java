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

import java.lang.reflect.Constructor;

import com.braintribe.utils.ReflectionTools;

/**
 * <p>
 * Static helper for constructing Exceptions, abstracting reflection operations. GWT incompatible.
 *
 */
public class ExceptionBuilder {

	/**
	 * See {@link #createException(String, String, Throwable)}.
	 */
	public static Throwable createException(String typeSignature, String message) {
		return createException(typeSignature, message, null);
	}

	/**
	 * <p>
	 * Instantiates a {@link Throwable} based on the class name given by the {@code typeSignature} parameter.
	 *
	 * <p>
	 * The expected exception type to be returned must define constructors one of the following signatures:
	 *
	 * <ul>
	 * <li>{@code init(String, Throwable)}</li>
	 * <li>{@code init(String)}</li>
	 * <li>{@code init(Throwable)}</li>
	 * <li>{@code init()}</li>
	 * </ul>
	 */
	public static Throwable createException(String typeSignature, String message, Throwable cause) {

		Class<? extends Throwable> throwable = null;
		try {
			throwable = ReflectionTools.getClassForName(typeSignature, Throwable.class);
		} catch (Throwable t) {
			// reflection failures are suppressed, call returns java.lang.Exception.
			return createGenericException(typeSignature, message, cause);
		}

		@SuppressWarnings("unchecked")
		Constructor<? extends Throwable>[] constructors = (Constructor<? extends Throwable>[]) throwable.getConstructors();

		@SuppressWarnings("unchecked")
		Constructor<? extends Throwable>[] prioritizedConstructors = new Constructor[ThrowableInit.values().length];

		for (Constructor<? extends Throwable> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 2 && parameterTypes[0] == String.class && parameterTypes[1].isAssignableFrom(Throwable.class)) {
				prioritizedConstructors[ThrowableInit.STANDARD.ordinal()] = constructor;
				break;
			} else if (parameterTypes.length == 1 && parameterTypes[0] == String.class) {
				prioritizedConstructors[ThrowableInit.MESSAGE.ordinal()] = constructor;
			} else if (parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(Throwable.class)) {
				prioritizedConstructors[ThrowableInit.CAUSE.ordinal()] = constructor;
			} else if (parameterTypes.length == 0) {
				prioritizedConstructors[ThrowableInit.NO_ARGS.ordinal()] = constructor;
			}
		}

		Constructor<? extends Throwable> selectedConstructor = null;
		ThrowableInit throwableInit = null;
		for (int i = 0; i < prioritizedConstructors.length; i++) {
			selectedConstructor = prioritizedConstructors[i];
			if (selectedConstructor != null) {
				throwableInit = ThrowableInit.values()[i];
				break;
			}
		}

		Throwable result = null;

		if (throwableInit != null) {
			try {
				switch (throwableInit) {
					case STANDARD:
						result = selectedConstructor.newInstance(message, cause);
						break;
					case CAUSE:
						result = selectedConstructor.newInstance(cause);
						break;
					case MESSAGE:
						result = selectedConstructor.newInstance(message != null ? message : cause != null ? cause.getMessage() : "");
						if (cause != null) {
							initCause(result, cause);
						}
						break;
					case NO_ARGS:
						result = selectedConstructor.newInstance();
						if (cause != null) {
							initCause(result, cause);
						}
						break;
				}
			} catch (Exception e) {
				// newInstance() failures are suppressed, call returns java.lang.Exception.
			}
		}

		if (result != null) {
			return result;
		}

		return createGenericException(typeSignature, message, cause);

	}

	private static void initCause(Throwable throwable, Throwable cause) {
		if (cause != null) {
			try {
				throwable.initCause(cause);
			} catch (Throwable e) {
				// initCause() failures are suppressed
			}
		}
	}

	private static Exception createGenericException(String typeSignature, String message, Throwable cause) {
		if (message == null) {
			message = typeSignature;
		} else if (!message.startsWith(typeSignature)) {
			message = typeSignature + ": " + message;
		}
		return new Exception(message, cause);
	}

	private enum ThrowableInit {
		STANDARD,
		MESSAGE,
		CAUSE,
		NO_ARGS
	}

}
