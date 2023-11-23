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
package com.braintribe.testing.test.logging;

import org.junit.Test;

import com.braintribe.logging.Logger;

/**
 * Provides simple test methods which just log a few messages. Purpose of this class in combination with {@link LoggingTest_B} is to verify that
 * logging from multiple test classes/methods doesn't interfere in any way, e.g. that logs end up in the wrong test report or that after logging in
 * one class logging in the other class doesn't work anymore or bugs like that. Note that tests currently don't contain any assertions, i.e. logs have
 * to checked manually.
 *
 * @author michael.lafite
 */
public class LoggingTest_A {

	private static Logger logger = Logger.getLogger(LoggingTest_A.class);

	@Test
	public void test1() {
		logger.info("[LoggingTest_A.test1] test INFO message 1");
		System.out.println("[LoggingTest_A.test1] test System.out message 1");
		System.err.println("[LoggingTest_A.test1] test System.err message 1");
		logger.info("[LoggingTest_A.test1] test INFO message 2");
		System.out.println("[LoggingTest_A.test1] test System.out message 2");
		System.err.println("[LoggingTest_A.test1] test System.err message 2");
	}

	@Test
	public void test2() {
		logger.info("[LoggingTest_A.test2] test INFO message 1");
		System.out.println("[LoggingTest_A.test2] test System.out message 1");
		System.err.println("[LoggingTest_A.test2] test System.err message 1");
		logger.info("[LoggingTest_A.test2] test INFO message 2");
		System.out.println("[LoggingTest_A.test2] test System.out message 2");
		System.err.println("[LoggingTest_A.test2] test System.err message 2");
	}
}
