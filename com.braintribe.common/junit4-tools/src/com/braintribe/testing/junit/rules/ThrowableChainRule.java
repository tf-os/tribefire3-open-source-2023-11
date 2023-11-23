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
package com.braintribe.testing.junit.rules;

import java.io.IOException;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * This rule provides a convenient way to specify the chain of {@link Throwable}s expected to be thrown by a test.
 * <p>
 * For example, if one expects a {@link RuntimeException} {@link Throwable#getCause() caused} by {@link IOException} (i.e. somewhere in the code there
 * is something like <code>throw new RuntimeException(ioException)}</code> one may use this rule and specify these exceptions and their order.
 */
public class ThrowableChainRule implements MethodRule {
	private final Class<? extends Throwable>[] throwables;

	@SafeVarargs
	public ThrowableChainRule(final Class<? extends Throwable>... exceptions) {
		this.throwables = exceptions;
	}

	@Override
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				ThrowableChain chain = method.getAnnotation(ThrowableChain.class);

				Class<? extends Throwable>[] throwablesForMethod = (chain == null) ? throwables : chain.value();

				try {
					base.evaluate();

				} catch (final Throwable t) {
					if (isEmpty(throwablesForMethod)) {
						throw t;
					}

					checkMatchesExpectedChain(t, throwablesForMethod);
					return;
				}

				if (!isEmpty(throwablesForMethod)) {
					throw new AssertionError("Exception was expected to be thrown, but the method exited in standard way.");
				}
			}

			private boolean isEmpty(final Object[] array) {
				return array == null || array.length == 0;
			}

			private void checkMatchesExpectedChain(Throwable t, final Class<? extends Throwable>[] expectedThrowables) {
				final Throwable originalT = t;

				for (final Class<? extends Throwable> throwableClass : expectedThrowables) {

					if (!(throwableClass.isInstance(t))) {
						/* I am not using AssertionError here, because in that case I couldn't append original Throwable as cause. */
						throw new Error("Exception does not match expected chain. Expected: " + throwableClass.getName() + ", but found: "
								+ (t == null ? "null" : t.getClass().getName()) + ". See causing exception for entire chain.", originalT);
					}

					t = t.getCause();
				}
			}
		};
	}

}
