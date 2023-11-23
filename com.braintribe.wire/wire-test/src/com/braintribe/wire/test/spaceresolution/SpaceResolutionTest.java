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
package com.braintribe.wire.test.spaceresolution;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.test.spaceresolution.wire.ProxyResolutionTestModule;
import com.braintribe.wire.test.spaceresolution.wire.contract.AlternativeSpaceResolutionFirstContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.ContractResolutionFirstContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.ExampleProxyContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.ProxyResolutionTestContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.SpaceResolutionContract;
import com.braintribe.wire.test.spaceresolution.wire.contract.SpaceResolutionFirstContract;


public class SpaceResolutionTest {
	
	@Test
	public void contractVsSpaceResolution() throws RuntimeException {

		WireContext<SpaceResolutionContract> context = Wire
				.contextWithStandardContractBinding(SpaceResolutionContract.class)
				.bindContract(AlternativeSpaceResolutionFirstContract.class, "com.braintribe.wire.test.spaceresolution.wire.space.SpaceResolutionFirstSpace")
				.build();

		SpaceResolutionContract contract = context.contract();
		
		ContractResolutionFirstContract importedByContract1 = contract.importedByContract1();
		ContractResolutionFirstContract importedBySpace1 = contract.importedBySpace1();
		SpaceResolutionFirstContract importedByContract2 = contract.importedByContract2();
		SpaceResolutionFirstContract importedBySpace2 = contract.importedBySpace2();
		AlternativeSpaceResolutionFirstContract alternativeImportedByContract2 = contract.importedByAlterativeContract2();

		String instanceFromContract1 = importedByContract1.instance();
		String instanceFromSpace1 = importedBySpace1.instance();
		String instanceFromContract2 = importedByContract2.instance();
		String instanceFromSpace2 = importedBySpace2.instance();
		String instanceFromAlternativeContract2 = alternativeImportedByContract2.instance();
		
		assertThat(importedByContract1).isSameAs(importedBySpace1);
		assertThat(instanceFromContract1).isSameAs(instanceFromSpace1);
		assertThat(importedByContract2).isSameAs(importedBySpace2);
		assertThat(instanceFromContract2).isSameAs(instanceFromSpace2);
		assertThat(instanceFromContract2).isSameAs(instanceFromAlternativeContract2);
		
		context.shutdown();
	}
	
	@Test
	public void testProxyContractResolution() {
		WireContext<ProxyResolutionTestContract> context = Wire.context(ProxyResolutionTestModule.INSTANCE);

		ProxyResolutionTestContract contract = context.contract();
		
		ExampleProxyContract example = contract.example();
		assertThat(example.one()).isEqualTo("one");
		assertThat(example.two()).isEqualTo("two");
		assertThat(example.three()).isEqualTo("three");
		
		context.shutdown();
	}
}
