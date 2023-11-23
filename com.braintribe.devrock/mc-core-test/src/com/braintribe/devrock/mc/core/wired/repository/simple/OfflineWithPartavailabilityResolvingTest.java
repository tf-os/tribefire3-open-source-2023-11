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
package com.braintribe.devrock.mc.core.wired.repository.simple;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.devrock.mc.core.commons.utils.TestUtils;

public class OfflineWithPartavailabilityResolvingTest extends OfflineCompoundResolvingTest {
	private File initialPartAvailability = new File( input, "part-availability");

	@Override
	@Before
	public void before() {
		super.before();
		TestUtils.copy( initialPartAvailability, repo);
	}

	@Test
	public void runTest() throws Exception {
		resolvingTest(true);
	}
}
