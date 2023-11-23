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
package com.braintribe.utils.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

public abstract class Generics {

	/**
	 * Resolves the value for a generic parameter from a declaring class in the context of a derived class given that the generics parameter is fully
	 * concretized.
	 *
	 * @param type
	 *            the concrete class that
	 * @param declaringType
	 *            the class that declares the generics parameter
	 * @param parameterName
	 *            the name of the generics parameter
	 * @return the concrete type that is assigned to the generics parameter.
	 */
	public static Type getGenericsParameter(Class<?> type, Class<?> declaringType, String parameterName) {
		Deque<Type> stack = new ArrayDeque<>();

		if (!findInheritancePath(stack, type, declaringType)) {
			throw new IllegalStateException("Type [" + type.getName() + "] does not inherit from: " + declaringType.getName());
		}

		for (Type curType : stack) {
			if (curType == type) {
				return findGenericParameterByname(type, parameterName);
			}

			ParameterizedType parameterizedType = (ParameterizedType) curType;

			Class<?> rawType = (Class<?>) parameterizedType.getRawType();

			int parameterIndex = indexOfGenericParameter(rawType, parameterName);

			if (parameterIndex == -1) {
				throw new IllegalStateException(
						"Class [" + curType.getTypeName() + "] is missing the claimed generics parameter named '" + parameterName + "'");
			}

			Type parameterValue = parameterizedType.getActualTypeArguments()[parameterIndex];

			if (parameterValue instanceof Class<?>) {
				return parameterValue;
			}

			if (parameterValue instanceof ParameterizedType) {
				return parameterValue;
			}

			if (parameterValue instanceof TypeVariable<?>) {
				parameterName = ((TypeVariable<?>) parameterValue).getName();
			}
		}

		throw new IllegalStateException("Type [" + type.getName() + "] does not inherit from: " + declaringType.getName());
	}

	private static Type findGenericParameterByname(Class<?> type, String parameterName) {
		for (TypeVariable<?> tv : type.getTypeParameters()) {
			if (tv.getName().equals(parameterName)) {
				return tv;
			}
		}

		throw new IllegalStateException("Class [" + type.getTypeName() + "] is missing the claimed generics parameter named '" + parameterName + "'");
	}

	private static boolean findInheritancePath(Deque<Type> stack, Type subType, Class<?> matchSuperType) {
		Class<?> clazz = getRawType(subType);

		stack.push(subType);

		if (clazz == matchSuperType) {
			return true;
		}

		Iterable<Type> superTypes = Stream.concat( //
				Stream.of(clazz.getGenericSuperclass()).filter(t -> t != null), //
				Stream.of(clazz.getGenericInterfaces()) //
		)::iterator;

		for (Type superType : superTypes) {
			if (findInheritancePath(stack, superType, matchSuperType)) {
				return true;
			}
		}

		stack.pop();

		return false;
	}

	private static Class<?> getRawType(Type subType) {
		if (subType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) subType).getRawType();
		}

		if (subType instanceof Class<?>) {
			return (Class<?>) subType;
		}

		throw new IllegalStateException("unexpected type in hierarchy: " + subType);
	}

	private static int indexOfGenericParameter(Class<?> rawType, String parameterName) {
		TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
		for (int i = 0; i < typeParameters.length; i++) {
			if (typeParameters[i].getName().equals(parameterName)) {
				return i;
			}
		}

		return -1;
	}

}
