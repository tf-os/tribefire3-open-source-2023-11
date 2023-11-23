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
package jsinterop.utils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;

/**
 * @author peter.gazdik
 */
@JsType(namespace = "$tf.util")
public class Lambdas {

	// @formatter:off
	
	// Function

	@JsFunction @FunctionalInterface
	public static interface JsUnaryFunction<T, R> { R call(T t);	}
	public static <T, R> Function<T, R> toJFunction(JsUnaryFunction<T, R> jsLambda) { return jsLambda::call; }

	// BiFunction

	@JsFunction	@FunctionalInterface
	public static interface JsBiFunction<T, U, R> { R call(T t, U u); }
	public static <T, U, R> BiFunction<T, U, R> toJBiFunction(JsBiFunction<T, U, R> jsLambda) { return jsLambda::call; }

	// BinaryOperator

	@JsFunction	@FunctionalInterface
	public static interface JsBinaryOperator<T> { T call(T t, T u); }
	public static <T> BinaryOperator<T> toJBinaryOperator(JsBinaryOperator<T> jsLambda) { return jsLambda::call; }

	// Supplier

	@JsFunction	@FunctionalInterface
	public static interface JsSupplier<T> { T call(); }
	public static <T> Supplier<T> toJSupplier(JsSupplier<T> jsLambda) { return jsLambda::call; }

	// Consumer
	
	@JsFunction	@FunctionalInterface
	public static interface JsConsumer<T> { void call(T t); }
	public static <T> Consumer<T> toJConsumer(JsConsumer<T> jsLambda) { return jsLambda::call; }
	
	// BiConsumer

	@JsFunction	@FunctionalInterface
	public static interface JsBiConsumer<T, U> { void call(T t, U u); }
	public static <T, U> BiConsumer<T, U> toJBiConsumer(JsBiConsumer<T, U> jsLambda) { return jsLambda::call; }

	// Predicate

	@JsFunction	@FunctionalInterface
	public static interface JsPredicate<T> { boolean call(T t); }
	public static <T> Predicate<T> toJPredicate(JsPredicate<T> jsLambda) { return jsLambda::call; }

	// BiPredicate

	@JsFunction	@FunctionalInterface
	public static interface JsBiPredicate<T, U> { boolean call(T t, U u); }
	public static <T, U> BiPredicate<T, U> toJBiPredicate(JsBiPredicate<T, U> jsLambda) { return jsLambda::call; }

	// @formatter:on
}
