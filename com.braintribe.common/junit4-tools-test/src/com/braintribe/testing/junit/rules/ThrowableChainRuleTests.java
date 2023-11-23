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

import org.junit.Rule;
import org.junit.Test;

public class ThrowableChainRuleTests {

	@Rule
	public ThrowableChainRule exceptionChainRule = new ThrowableChainRule(Exception.class, IllegalArgumentException.class);

	@Test
	public void defaultThrowableChain() throws Exception {
		throw new Exception(new IllegalArgumentException());
	}

	@Test
	@ThrowableChain({ Exception.class, RuntimeException.class })
	public void customChainSpecifiedByAnnotation() throws Exception {
		throw new Exception(new RuntimeException());
	}

	@Test
	@ThrowableChain({})
	public void noExceptionExpected() {
		// empty
	}

	@Test(expected = RuntimeException.class)
	@ThrowableChain({})
	public void justShowingHowItIntegratesWithJunit() throws Exception {
		/* This test passes, because this method exits with RuntimeException, but the JUnit runner expects that (expected=RuntimeException.class), so
		 * it "eats" the exception. Therefore, since our rule is configured to expect no exception at all, the test passes.
		 *
		 * If you remove the @ThrowableChain thing, the test will fail telling you no exception was thrown, even though it should have been. (Just try
		 * it.) */
		throw new RuntimeException();
	}

}
