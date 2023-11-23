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
package com.braintribe.wire.test.contractaggr.wire;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.test.contractaggr.wire.contract.ContractAggregationTestContract;

public class ContractAggregationTest {

	@Test
	public void testAggregation() throws Exception {
		try (WireContext<ContractAggregationTestContract> context = Wire.context(ContractAggregationTestWireModule.INSTANCE)) {
			
			ContractAggregationTestContract contract = context.contract();
			
			System.out.println(contract.toString());
			System.out.println(contract.hashCode());
			
			Assertions.assertThat(contract.value1()).isEqualTo("value1");
			Assertions.assertThat(contract.value2()).isEqualTo("value2");
			
		}
	}

}
