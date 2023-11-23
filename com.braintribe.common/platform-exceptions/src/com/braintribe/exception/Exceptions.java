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
package com.braintribe.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
	
	/**
	 * Traverses the given exception along {@link Throwable#getCause()} to find the {@link Throwable} that has no further cause 
	 * and returns it as the root cause.
	 */
	public static Throwable getRootCause(Throwable t) {
		return getRootCause(t, null);
	}
	
	/**
	 * Traverses the given exception along {@link Throwable#getCause()} to find the {@link Throwable} that has no further cause 
	 * and returns it as the root cause. Any exception being visited by the method is notified to the given visitor.
	 */
	public static Throwable getRootCause(Throwable t, Consumer<Throwable> visitor) {
		while (true) {
			if (visitor != null) visitor.accept(t);
			
			Throwable cause = t.getCause();
			
			if (cause == null) {
				return t;
			}
			else {
				t = cause;
			}
		}
	}
	
	private static <E extends Throwable> E contextualize(E t, String msg, int framesToRemove) {
		
		ThrowableContext ctx = new ThrowableContext(msg);
		
		StackTraceElement frames[] = Thread.currentThread().getStackTrace();
		StackTraceElement[] stackTrace = new StackTraceElement[frames.length - framesToRemove];
		System.arraycopy(frames, framesToRemove, stackTrace, 0, frames.length - framesToRemove);
		ctx.reducedStackTraceElements = stackTrace;
	
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
	
	/**
	 * Renders the given exception as a string using a different approach than {@link Throwable#printStackTrace()}. It only renders the root cause
	 * and its stack frames and joins contextual information from wrappers and the {@link ThrowableContext} from the suppressed exceptions. The rendering
	 * is directly applied to the given appendable (e.g. java.io.Writer, java.lang.StringBuilder) to support stream-like processing.
	 */
	public static void stringify(Throwable t, Appendable appendable) {
		ExceptionsStringifier.stringify(t, appendable);
	}
	
	/**
	 * Renders the given exception as a string using a different approach than {@link Throwable#printStackTrace()}. It only renders the root cause
	 * and its stack frames and joins contextual information from wrappers and the {@link ThrowableContext} from the suppressed exceptions.
	 * @return the rendered exception
	 */
	public static String stringify(Throwable t) {
		return ExceptionsStringifier.stringify(t);
	}
	
	
	protected static class FrameInfo {
		public String stackTraceElementString;
		public List<String> msg = new ArrayList<>();
		
		public FrameInfo(StackTraceElement element) {
			StringBuilder sb = new StringBuilder(element.getClassName());
			sb.append('.');
			sb.append(element.getMethodName());
			if (element.isNativeMethod()) {
				sb.append("(Native Method)");
			} else {
				String fileName = element.getFileName();
				int lineNumber = element.getLineNumber();
				if (fileName != null && lineNumber >= 0) {
					sb.append('(');
					sb.append(fileName);
					sb.append(':');
					sb.append(lineNumber);
					sb.append(')');
				} else if (fileName != null) {
					sb.append('(');
					sb.append(fileName);
					sb.append(')');					
				} else {
					sb.append("(Unknown Source)");
				}
			}
			this.stackTraceElementString = sb.toString();
		}
		public static void renderStackTraceElement(Appendable sb, StackTraceElement element) throws IOException {
			sb.append(element.getClassName());
			sb.append('.');
			sb.append(element.getMethodName());
			if (element.isNativeMethod()) {
				sb.append("(Native Method)");
			} else {
				String fileName = element.getFileName();
				int lineNumber = element.getLineNumber();
				if (fileName != null && lineNumber >= 0) {
					sb.append('(');
					sb.append(fileName);
					sb.append(':');
					sb.append(intToString(lineNumber));
					sb.append(')');
				} else if (fileName != null) {
					sb.append('(');
					sb.append(fileName);
					sb.append(')');					
				} else {
					sb.append("(Unknown Source)");
				}
			}
		}
	}
	
	private static String[] intRepresentations;
	static {
		int prefetch = 1024;
		intRepresentations = new String[prefetch];
		for (int i=0; i<prefetch; ++i) {
			intRepresentations[i] = Integer.toString(i);
		}
	}
	protected static String intToString(int value) {
		if (value >= 0 && value < intRepresentations.length) {
			return intRepresentations[value];
		}
		return Integer.toString(value);
	}
	private static String[] intRadix36Representations;
	static {
		int prefetch = 64;
		intRadix36Representations = new String[prefetch];
		for (int i=0; i<prefetch; ++i) {
			intRadix36Representations[i] = Integer.toString(i, 36);
		}
	}
	protected static String intToStringRadix36(int value) {
		if (value < intRadix36Representations.length) {
			return intRadix36Representations[value];
		}
		return Integer.toString(value, 36);
	}
	
	
	public static ThrowableNormalizer normalizer(Throwable t) {
		return new ThrowableNormalizer(t);
	}
	
	public static String getEnsuredMessage(Throwable e) {
		String message = e.getMessage();
		
		if (message == null || message.isEmpty()) {
			StackTraceElement stackTraceElement = e.getStackTrace()[0];
			
			String className = stackTraceElement.getClassName();
			int lastNameDelimiter = className.lastIndexOf('.');
			
			if (lastNameDelimiter != -1) {
				className = className.substring(lastNameDelimiter + 1);
			}
			
			message = e.getClass().getSimpleName() + " occurred in " 
					+ className
					+ '.' + stackTraceElement.getMethodName()
					+ " line " + stackTraceElement.getLineNumber();
		}
		
		return message;
	}

	
}
