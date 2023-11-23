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
package com.braintribe.utils.junit.core.rules;

import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;

public class ConcurrentRuleTests {

	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule(2);

	@Test
	public void demoWithDefalutValue() {
		System.out.println("Run by thread: " + Thread.currentThread().getName());
	}

	@Test
	@Concurrent(5)
	public void demoWithCustomValue() {
		System.out.println("Run by thread: " + Thread.currentThread().getName());
	}

	@Test(expected = NumberFormatException.class)
	public void demoWithExpectedException() {
		/* This just shows that the "expected" exception is handled by JUnit framework correctly, even if we use this (or any other) custom rule. */

		// Wouldn't it be fun if this was actually parsable in future versions of java?
		int value = Integer.parseInt("forty-two");
		// should be unreachable
		fail("What?! Integer (unexpectedly) could be parsed: " + value);
	}

}
