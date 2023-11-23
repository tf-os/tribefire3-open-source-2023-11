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
package com.braintribe.wire.impl.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>
 * This class serves as namespace for a number of methods that should be used to contextualize, wrap and stringify exceptions in a cleaner way than the standard
 * way that is used by the Java community.
 * <p>
 * The commonly used strategy of exception handling has the disadvantage that it always wraps exceptions in order to add contextual information from
 * higher call sites. It also often forces to wrap in order to pass on checked exceptions that are not announced by the throws clause of the catching method.
 * Such repeated wrappings lead to obscure stacktraces which treat the important root cause with less priority than all its wrappings. This is especially
 * annoying when trying to react on specific exceptions because they could be hidden by wrappings and therefore require extra scanning in the chain of causes.
 * Also the rendering confuses with truncated stack frames in order to avoid the inherent redundancy. 
 * This redundancy is not only useless but also costly as each new constructed wrapper exception involves a determination of the stack
 * frames which is an expensive reflective operation.   
 * 
 * <p>
 * In order to solve all the issues that are made up by the common strategy we introduce a new strategy that is supported by this class.
 * 
 * <p>
 * The new strategy avoids unnecessary wrappings by using dynamically acquired suppressed exceptions to store additional contextual information
 * from higher call sites in method {@link #contextualize(Throwable, String)}. Wrapping exceptions should only be done if really needed in case
 * of undeclared checked exceptions by using one of the following methods:
 * <ul>
 *  <li>{@link #unchecked(Throwable, String)}
 *  <li>{@link #unchecked(Throwable, String, BiFunction)}
 *  <li>{@link #uncheckedAndContextualize(Throwable, String, Function)}
 * </ul>
 * 
 * <p>
 * This class offers stringification of exceptions in a different way than {@link Throwable#printStackTrace()}. It only renders the root cause
 * and its stack frames and joins contextual information from wrappers and informations from suppressed exceptions on those:
 * <ul>
 *  <li>{@link #stringify(Throwable)}
 *  <li>{@link #stringify(Throwable, Appendable)}
 * </ul>
 * 
 * 
 * @author Christina Wilpernig
 * @author Dirk Scheffler
 */
public abstract class Exceptions {

	/** Similar to {@link #unchecked(Throwable, String)}, but without specifying extra context. */
	public static <T extends Throwable> RuntimeException unchecked(T t) {
		if (t instanceof RuntimeException)
			return (RuntimeException) t;
		
		if (t instanceof Error) 
			throw (Error) t;
		
		// UncheckedException
		return removeFirstFrame(new RuntimeException(t));
	}

	/**
	 * Ensures that for the given exception an unchecked exception is being returned. This exception is then ready to be re-thrown from any method. 
	 * For this, 2 different situations have to be considered:
	 * <ol>
	 * 	<li>The given exception is either an {@link Error} or a {@link RuntimeException} which can be returned directly as they are already unchecked.
	 * 	In that case the given context is added to the {@link ThrowableContext} which is managed as a suppressed exception. 
	 *  <li>The given exception is neither an {@link Error} nor a {@link RuntimeException}. In that case a {@link RuntimeException} is constructed from
	 *  the given context and exception and is being returned.
	 * </ol>
	 * 
	 * @param t the original exception
	 * @param context the context which should enrich the exception structure.
	 */
	public static <E extends Throwable> RuntimeException unchecked(E t, String context) {
		if (t instanceof RuntimeException)
			return (RuntimeException)contextualize(t, context, 3);
		
		if (t instanceof Error) 
			throw contextualize((Error)t, context, 2);
		
		// UncheckedException
		return removeFirstFrame(new RuntimeException(context, t));
	}

	/**
	 * <p>
	 * Ensures that for the given exception an unchecked exception is being returned. This exception is then ready to be re-thrown from any method. 
	 * For this, 2 different situations have to be considered:
	 * <ol>
	 * 	<li>The given exception is either an {@link Error} or a {@link RuntimeException} which can be returned directly as they are already unchecked.
	 * 	In that case the given context is added to the {@link ThrowableContext} which is managed as a suppressed exception. 
	 *  <li>The given exception is neither an {@link Error} nor a {@link RuntimeException}. In that case a {@link RuntimeException} is constructed from
	 *  the given context and exception via the given exceptionFactory and is being returned.
	 * </ol>
	 * 
	 * @param t the original exception
	 * @param context the context which should enrich the exception structure.
	 * @param exceptionFactory the factory which can construct a derivate of a {@link RuntimeException} when required due to the nature of the original exception
	 */
	public static <E extends Throwable> RuntimeException unchecked(E t, String context, BiFunction<String, ? super E, ? extends RuntimeException> exceptionFactory) {
		if (t instanceof RuntimeException) {
			return (RuntimeException)contextualize(t, context, 2);
		}
		else if (t instanceof Error) {
			throw contextualize((Error)t, context, 2);
		}

		return removeFirstFrame(exceptionFactory.apply(context, t));
	}
	
	/**
	 * <p>
	 * Ensures that for the given exception an unchecked exception is being returned. This exception is then ready to be re-thrown from any method. 
	 * For this, 2 different situations have to be considered:
	 * <ol>
	 * 	<li>The given exception is either an {@link Error} or a {@link RuntimeException} which can be returned directly as they are already unchecked.
	 *  <li>The given exception is neither an {@link Error} nor a {@link RuntimeException}. In that case a {@link RuntimeException} is constructed from
	 *  the given exception via the given exceptionFactory and is being returned.
	 * </ol>
	 * 
	 * In both cases the given context is added to the {@link ThrowableContext} which is managed as a suppressed exception.
	 * 
	 * @param t the original exception
	 * @param context the context which should enrich the exception structure.
	 * @param exceptionFactory the factory which can construct a derivate of a {@link RuntimeException} when required due to the nature of the original exception
	 */
	public static <E extends Throwable> RuntimeException uncheckedAndContextualize(E t, String context, Function<? super E, ? extends RuntimeException> exceptionFactory) {
		if (t instanceof RuntimeException) {
			return (RuntimeException)contextualize(t, context, 2);
		}
		else if (t instanceof Error) {
			throw contextualize((Error)t, context, 2);
		}
		else {
			contextualize(t, context, 2);
			return removeFirstFrame(exceptionFactory.apply(t));
		}
	}
	
	/**
	 * The given context is added to the {@link ThrowableContext} which is managed as a suppressed exception. The given exception is
	 * then type-safely returned as is and can be re-thrown by the calling method if its throws clause is compatible to the exception. If that
	 * is not the case you should consider using any of the following methods:
	 * 
	 * <ul>
	 * <li>{@link #unchecked(Throwable, String)}
	 * <li>{@link #unchecked(Throwable, String, BiFunction)}
	 * <li>{@link #uncheckedAndContextualize(Throwable, String, Function)}
	 * </ul>
	 * 
	 * @param t the original exception
	 * @param context the context being added to the {@link ThrowableContext}
	 */
	public static <E extends Throwable> E contextualize(E t, String context) {
		return contextualize(t, context, 3);
	}
	
	private static <E extends Throwable> E contextualize(E t, String msg, int framesToRemove) {
	
		ThrowableContext ctx = new ThrowableContext(msg);
		t.addSuppressed(ctx);
		
		return t;
	}

	private static RuntimeException removeFirstFrame(RuntimeException e) {
		StackTraceElement originalTrace[] = e.getStackTrace();
		
		if (originalTrace == null || originalTrace.length == 0)
			return e; 
		
		int traceLength = originalTrace.length - 1;
		StackTraceElement trace[] = new StackTraceElement[traceLength];
		System.arraycopy(originalTrace, 1, trace, 0, traceLength);
		e.setStackTrace(trace);
		return e;
	}
}
