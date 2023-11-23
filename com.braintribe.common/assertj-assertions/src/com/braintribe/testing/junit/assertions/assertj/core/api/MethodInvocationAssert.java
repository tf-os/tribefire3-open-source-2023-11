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
package com.braintribe.testing.junit.assertions.assertj.core.api;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.assertj.core.api.ThrowableAssert;

import com.braintribe.common.lcd.Constants;
import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.utils.ThrowableTools;

/**
 * Provides assertions related to method invocations which are passed as lambda expressions. This can e.g. be used to assert that executing method
 * <code>doSomething</code> takes less than <code>3</code> seconds and fails with an <code>IOException</code>:
 *
 * <pre>
 * assertThatExecuting(() -> doSomething()).fails().withException(IOException.class).afterLessThan(10, ChronoUnit.SECONDS);
 * </pre>
 *
 * Please note that this class provides multiple methods that actually do the same, for example, {@link #withException()} and
 * {@link #throwingException()} or {@link #inLessThan(long, ChronoUnit)} and {@link #afterLessThan(long, ChronoUnit)}. The simple reason is that
 * depending on the combination and order of the assertion methods certain method names just allow for creating more readable English sentences.
 *
 * <pre>
 * assertThatExecuting(() -> doSomething()).finishes().inLessThan(3, ChronoUnit.MILLIS).throwingException();
 * assertThatExecuting(() -> doSomething()).fails().withException().afterLessThan(3, ChronoUnit.MILLIS);
 * </pre>
 *
 * Further example usages:
 *
 * <pre>
 * assertThatExecuting(() -> doSomething()).succeeds();
 * assertThatExecuting(() -> doSomething()).fails();
 * assertThatExecuting(() -> doSomething()).finishes().throwing(IOException.class);
 * assertThatExecuting(() -> doSomething()).fails().with(IOException.class);
 * assertThatExecuting(() -> doSomething()).succeeds().inBetween(5, 20, ChronoUnit.MILLIS);
 * assertThatExecuting(() -> doSomething()).twice().succeeds();
 * assertThatExecuting(() -> doSomething()).nTimes(200).finishes().afterMoreThan(1, ChronoUnit.SECONDS);
 * assertThatExecuting(() -> doSomething()).tenTimes().fails().afterMoreThan(100, ChronoUnit.MILLIS).throwingExceptionWhich()
 * 		.isInstanceOf(RuntimeException.class).hasMessageContaining("not found");
 * </pre>
 *
 * @author michael.lafite
 *
 * @param <T>
 *            the type of the {@link #getReturnValue() return value} of the method invocation
 */
public class MethodInvocationAssert<T> {
	private Duration duration;
	private Throwable throwable;
	private Object executable;
	private T returnValue;
	private MethodInvocationSettings<T> settings;

	/**
	 * Similar to {@link Runnable}, but may throw an exception.
	 */
	@FunctionalInterface
	public interface ExecutableWithoutReturnValue {
		void execute() throws Throwable;
	}

	/**
	 * Same as {@link java.util.concurrent.Callable}, but since we use our own type for {@link ExecutableWithoutReturnValue}, we also use our own type
	 * here to be consistent.
	 */
	@FunctionalInterface
	public interface ExecutableWithReturnValue<T> {
		T execute() throws Throwable;
	}

	public MethodInvocationAssert(ExecutableWithReturnValue<T> executable) {
		this.executable = executable;
		this.settings = new MethodInvocationSettings<>(this);
	}

	public MethodInvocationAssert(ExecutableWithoutReturnValue executable) {
		this.executable = executable;
		this.settings = new MethodInvocationSettings<>(this);
	}

	/**
	 * Returns a {@link MethodInvocationSettings} instance used to configure method invocation settings via fluent API.
	 */
	public MethodInvocationSettings<T> configure() {
		return settings;
	}

	/**
	 * Returns the {@link Throwable} thrown by the method invocation (or <code>null</code>).
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * Returns the return value of the method invocation (or <code>null</code>).
	 */
	public T getReturnValue() {
		return returnValue;
	}

	/**
	 * Fails with the passed <code>message</code>.
	 */
	private void fail(String message) {
		throw new AssertionError("Method assertion failed: " + message);
	}

	/**
	 * Asserts that method execution takes less than the specified <code>amount</code> of time.
	 */
	public MethodInvocationAssert<T> inLessThan(long amount, ChronoUnit unit) {
		return checkDuration(null, amount, unit);
	}

	/**
	 * Asserts that method execution takes less than the specified <code>amount</code> of time.
	 */
	public MethodInvocationAssert<T> afterLessThan(long amount, ChronoUnit unit) {
		return inLessThan(amount, unit);
	}

	/**
	 * Asserts that method execution takes more than the specified <code>amount</code> of time.
	 */
	public MethodInvocationAssert<T> inMoreThan(long amount, ChronoUnit unit) {
		return checkDuration(amount, null, unit);
	}

	/**
	 * Asserts that method execution takes more than the specified <code>amount</code> of time.
	 */
	public MethodInvocationAssert<T> afterMoreThan(long amount, ChronoUnit unit) {
		return inMoreThan(amount, unit);
	}

	/**
	 * Asserts that method execution takes less than the specified <code>minAmount</code> and more than less than the specified <code>maxAmount</code>
	 * of time.
	 */
	public MethodInvocationAssert<T> inBetween(long minAmount, long maxAmount, ChronoUnit unit) {
		return checkDuration(minAmount, maxAmount, unit);
	}

	/**
	 * Asserts that method execution takes less than the specified <code>minAmount</code> and more than less than the specified <code>maxAmount</code>
	 * of time.
	 */
	public MethodInvocationAssert<T> afterBetween(long minAmount, long maxAmount, ChronoUnit unit) {
		return inBetween(minAmount, maxAmount, unit);
	}

	private MethodInvocationAssert<T> checkDuration(Long minAmount, Long maxAmount, ChronoUnit unit) {
		// TODO: pretty print duration
		if (minAmount != null) {
			Duration minDuration = Duration.of(minAmount, unit);
			if (duration.compareTo(minDuration) < 0) {
				fail("Execution took " + duration.getNano() + " nano seconds which is less than expected minimumm " + minDuration.getNano() + ".");
			}
		}
		if (maxAmount != null) {
			Duration maxDuration = Duration.of(maxAmount, unit);
			if (duration.compareTo(maxDuration) > 0) {
				fail("Execution took " + duration.getNano() + " nano seconds which is more than expected maximum " + maxDuration.getNano() + ".");
			}
		}
		return this;
	}

	/**
	 * Asserts that method fails with a {@link Throwable} of the specified type.
	 */
	public MethodInvocationAssert<T> with(Class<? extends Throwable> throwable) {
		return throwing(throwable);
	}

	/**
	 * Asserts that method fails with an {@link Exception}.
	 */
	public MethodInvocationAssert<T> withException() {
		return throwingException();
	}

	/**
	 * Asserts that method fails with a {@link RuntimeException}.
	 */
	public MethodInvocationAssert<T> withUncheckedException() {
		return throwingUncheckedException();
	}

	/**
	 * Asserts that method fails with an {@link Error}.
	 */
	public MethodInvocationAssert<T> withError() {
		return throwingError();
	}

	/**
	 * Asserts that method execution throws a {@link Throwable} of the specified type.
	 */
	public MethodInvocationAssert<T> throwing(Class<? extends Throwable> throwable) {
		checkStatus(Constants.ERROR);
		checkThrowable(throwable);
		return this;
	}

	/**
	 * Asserts that method execution throws an {@link Exception}.
	 */
	public MethodInvocationAssert<T> throwingException() {
		return throwing(Exception.class);
	}

	/**
	 * Asserts that method execution throws a {@link RuntimeException}.
	 */
	public MethodInvocationAssert<T> throwingUncheckedException() {
		return throwing(RuntimeException.class);
	}

	/**
	 * Asserts that method execution throws an {@link Error}.
	 */
	public MethodInvocationAssert<T> throwingError() {
		return throwing(Error.class);
	}

	/**
	 * Asserts that method execution throws a {@link Throwable}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends Throwable> throwingThrowableWhich() {
		// assert that a Throwable is thrown
		throwing(Throwable.class);
		return new ThrowableAssert<>(throwable);
	}

	/**
	 * Asserts that method execution throws an {@link Exception}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends Exception> throwingExceptionWhich() {
		// assert that an Exception is thrown
		throwingException();
		return new ThrowableAssert<>((Exception) throwable);
	}

	/**
	 * Asserts that method execution throws a {@link RuntimeException}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends RuntimeException> throwingUncheckedExceptionWhich() {
		// assert that an unchecked Exception is thrown
		throwingUncheckedException();
		return new ThrowableAssert<>((RuntimeException) throwable);
	}

	/**
	 * Asserts that method execution throws an {@link Error}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends Error> throwingErrorWhich() {
		// assert that an Error is thrown
		throwingError();
		return new ThrowableAssert<>((Error) throwable);
	}

	/**
	 * Asserts that method execution throws a {@link Throwable}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends Throwable> withThrowableWhich() {
		return throwingThrowableWhich();
	}

	/**
	 * Asserts that method execution throws an {@link Exception}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends Exception> withExceptionWhich() {
		return throwingExceptionWhich();
	}

	/**
	 * Asserts that method execution throws a {@link RuntimeException}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends RuntimeException> withUncheckedExceptionWhich() {
		return throwingUncheckedExceptionWhich();
	}

	/**
	 * Asserts that method execution throws an {@link Error}, which can be further checked via the returned {@link ThrowableAssert}.
	 */
	public ThrowableAssert<? extends Error> withErrorWhich() {
		return throwingErrorWhich();
	}

	private void checkThrowable(Class<? extends Throwable> expectedSuperType) {
		if (!expectedSuperType.isInstance(throwable)) {
			fail("Invocation failed with " + throwable.getClass().getName() + ", although an instance of " + expectedSuperType.getClass().getName()
					+ " was expected!\n" + ThrowableTools.getStackTraceString(throwable));
		}
	}

	/**
	 * Makes sure the status matches the <code>expectedStatus</code>
	 */
	private void checkStatus(Boolean expectedStatus) {
		if (this.throwable == null && Constants.ERROR.equals(expectedStatus)) {
			fail("Execution unexpectedly succeeded, i.e. didn't throw any Exception."
					+ ((executable instanceof ExecutableWithReturnValue) ? (" Return value: " + returnValue) : ""));
		}
		if (this.throwable != null && Constants.SUCCESS.equals(expectedStatus)) {
			fail("Execution unexpectedly failed with " + throwable.getClass().getName() + "! "
					+ ((throwable.getMessage() != null) ? " Message: " + throwable.getMessage() : "<no message>"));
		}
	}

	/**
	 * Executes the method and afters {@link #checkStatus(Boolean) checks the status} (against <code>expectedStatus</code>).
	 */
	private MethodInvocationAssert<T> execute(Boolean expectedStatus) {
		if (!settings.sequential) {
			throw new NotImplementedException("Parallel execution not supported yet.");
		}

		final long durationAsNanos;
		final long startTime = System.nanoTime();
		for (int i = 0; i < settings.times; i++) {
			try {
				if (executable instanceof ExecutableWithReturnValue) {
					returnValue = ((ExecutableWithReturnValue<T>) executable).execute();
				} else {
					((ExecutableWithoutReturnValue) executable).execute();
				}
			} catch (Throwable t) {
				if (this.throwable == null) {
					this.throwable = t;
				} else {
					// keep first exception
				}
			}
		}
		final long endTime = System.nanoTime();
		durationAsNanos = endTime - startTime;

		duration = Duration.of(durationAsNanos, ChronoUnit.NANOS);

		checkStatus(expectedStatus);

		return this;
	}

	/**
	 * Used to fluently configure method invocation settings and then trigger the method execution via {@link #finishes()}, {@link #succeeds()}
	 * or{@link #fails()}.
	 *
	 * @author michael.lafite
	 *
	 * @param <U>
	 *            the type of the return value. See generic type of {@link MethodInvocationAssert}.
	 *
	 * @see MethodInvocationAssert#configure()
	 */
	public static class MethodInvocationSettings<U> {
		private int times = 1;
		private boolean sequential = true;
		private MethodInvocationAssert<U> methodInvocationAssert;

		private MethodInvocationSettings(MethodInvocationAssert<U> methodInvocationAssert) {
			this.methodInvocationAssert = methodInvocationAssert;
		}

		/**
		 * Specifies to execute the method just once. This is the default.
		 *
		 * @see #nTimes(int)
		 */
		public MethodInvocationSettings<U> once() {
			this.times = 1;
			return this;
		}

		/**
		 * Specifies to execute the method twice.
		 *
		 * @see #nTimes(int)
		 */
		public MethodInvocationSettings<U> twice() {
			this.times = 2;
			return this;
		}

		/**
		 * Specifies to execute the method <code>10</code> times.
		 *
		 * @see #nTimes(int)
		 */
		public MethodInvocationSettings<U> tenTimes() {
			this.times = 10;
			return this;
		}

		/**
		 * Specifies how many times to execute the method.
		 */
		public MethodInvocationSettings<U> nTimes(int n) {
			if (n < 0) {
				throw new IllegalArgumentException("Cannot execute " + n + " times!");
			}
			this.times = n;
			return this;
		}

		/**
		 * Specifies to execute the method sequentially. This is the default. This setting only makes sense when executing the method
		 * {@link #nTimes(int) more than once}. <br>
		 * Note that concurrent execution isn't supported yet, but this will be added in future versions of this class. Let us know if that would be
		 * useful for you!
		 */
		public MethodInvocationSettings<U> sequentially() {
			this.sequential = true;
			return this;
		}

		// TODO: implement
		// /**
		// * Specifies to execute the method concurrently. This setting only makes sense when
		// * executing the method {@link #nTimes(int) more than once}.
		// * @see #sequentially()
		// */
		// public MethodInvocationSettings<U> concurrently() {
		// this.sequential = false;
		// return this;
		// }

		/**
		 * Triggers method execution which may succeed or fail.
		 *
		 * @see #succeeds()
		 * @see #fails()
		 */
		public MethodInvocationAssert<U> finishes() {
			return methodInvocationAssert.execute(Constants.SUCCESS_OR_ERROR);
		}

		/**
		 * Triggers method execution and expect it to succeed.
		 *
		 * @see #finishes()
		 * @see #fails()
		 */
		public MethodInvocationAssert<U> succeeds() {
			return methodInvocationAssert.execute(Constants.SUCCESS);
		}

		/**
		 * Triggers method execution and expect it to fail.
		 *
		 * @see #finishes()
		 * @see #succeeds()
		 */
		public MethodInvocationAssert<U> fails() {
			return methodInvocationAssert.execute(Constants.ERROR);
		}
	}

}
