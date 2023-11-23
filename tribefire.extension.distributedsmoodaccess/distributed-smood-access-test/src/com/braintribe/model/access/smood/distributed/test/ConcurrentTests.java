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
package com.braintribe.model.access.smood.distributed.test;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.access.smood.distributed.test.concurrent.tester.AbstractSmoodDbAccessTest;
import com.braintribe.testing.category.Slow;
import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class ConcurrentTests extends TestBase {

	@Test
	@Category(Slow.class)
	public void executeConcurrentTests() {
		
		List<AbstractSmoodDbAccessTest> testers = configuration.concurrentTesters();
		if (testers != null) {
			for (AbstractSmoodDbAccessTest tester : testers) {
				try {
					tester.executeTest();
				} catch(Exception e) {
					throw new AssertionError("Tester "+tester+" failed.", e);
				}
			}
		}
		
	}
}
