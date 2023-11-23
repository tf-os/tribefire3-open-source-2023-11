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

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * This rule says how many times a given test will be run (serial, all runs are made by one thread). The default value is set through the constructor
 * ( {@link #LoopRule(int)}, but each individual test may define it's own value using the {@link Loop} annotation.
 */
public class LoopRule implements MethodRule {
	private final int loops;

	public LoopRule(final int loops) {
		if (loops < 1) {
			throw new IllegalArgumentException("Number of loops must be a positive number!");
		}

		this.loops = loops;
	}

	@Override
	public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				final Loop loop = method.getAnnotation(Loop.class);

				int loopsForMethod = loop == null ? loops : loop.value();

				if (loopsForMethod < 1) {
					throw new IllegalArgumentException("Number of loops must be a positive number!");
				}

				while (--loopsForMethod >= 0) {
					base.evaluate();
				}
			}
		};
	}

}
