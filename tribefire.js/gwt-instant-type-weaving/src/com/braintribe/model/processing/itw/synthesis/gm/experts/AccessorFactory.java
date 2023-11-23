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
package com.braintribe.model.processing.itw.synthesis.gm.experts;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author peter.gazdik
 */
public class AccessorFactory {

	private static final Lookup lookup = MethodHandles.lookup();

	public static <B, T> Function<B, T> getter(Class<?> ownerType, String getterName, Class<?> propertyType) {
		try {
			CallSite site = LambdaMetafactory.metafactory(lookup, //
					"apply", //
					MethodType.methodType(Function.class), //
					MethodType.methodType(Object.class, Object.class), //
					lookup.findVirtual(ownerType, getterName, MethodType.methodType(propertyType)), //
					MethodType.methodType(propertyType, ownerType) //
			);

			MethodHandle factory = site.getTarget();
			Function<B, T> getter = (Function<B, T>) factory.invokeExact();

			return getter;

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static <B, T> BiConsumer<B, T> setter(Class<?> ownerType, String setterName, Class<?> propertyType) {
		try {
			CallSite site = LambdaMetafactory.metafactory(lookup, //
					"accept", //
					MethodType.methodType(BiConsumer.class), //
					MethodType.methodType(void.class, Object.class, Object.class), //
					lookup.findVirtual(ownerType, setterName, MethodType.methodType(void.class, propertyType)), //
					MethodType.methodType(void.class, ownerType, propertyType) //
			);

			MethodHandle factory = site.getTarget();
			BiConsumer<B, T> setter = (BiConsumer<B, T>) factory.invokeExact();

			return setter;

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
