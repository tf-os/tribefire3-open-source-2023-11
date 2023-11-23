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
package com.braintribe.wire.test.aggregate;

import java.util.Set;

import org.assertj.core.api.Assertions;

import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.test.aggregate.payload.TestContext;
import com.braintribe.wire.test.aggregate.payload.TestNode;
import com.braintribe.wire.test.aggregate.wire.AggregateScopeTestWireModule;
import com.braintribe.wire.test.aggregate.wire.contract.AggregateScopeTestContract;

public class AggregateTest {
	
	//@Test
	public void ExpectedToFail_refereeScopeTest() throws Exception {
		try (WireContext<AggregateScopeTestContract> context = Wire.context(AggregateScopeTestWireModule.INSTANCE)) {
			TestContext testContext = new TestContext("one");
			TestNode refereeObject = context.contract().root(testContext);
			
//			context.contract().sub1();
			
			TestNode sub = refereeObject.getNext();
			TestNode altSub = refereeObject.getAltNext();
			TestNode extraSub = refereeObject.getExtraNext();
			TestNode sub1 = sub.getNext();
			TestNode sub2 = sub1.getNext();
			TestNode sub1Candidate = sub2.getNext();
			
			Assertions.assertThat(sub1).isSameAs(sub1Candidate);
			Assertions.assertThat(sub).isSameAs(altSub);
			Assertions.assertThat(sub).isNotSameAs(extraSub);
			Assertions.assertThat(extraSub.getName()).isEqualTo("subextra");
			
			
			context.close(testContext);
			
			Set<Object> destroyedInstances = context.contract().monitor().getDestroyedInstances();
			
			Assertions.assertThat(destroyedInstances).contains(refereeObject, sub, sub1, sub2);
		}
	}
}
