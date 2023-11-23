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
package com.braintribe.model.access.smart.test.manipulation;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.discriminator.DiscriminatorEntityA;
import com.braintribe.model.processing.query.smart.test.model.smart.discriminator.SmartDiscriminatorType1;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @author peter.gazdik
 */
public class DiscriminatorEntity_ManipulationTests extends AbstractManipulationsTests {

	@Test
	public void testInstantiation() throws Exception {
		SmartDiscriminatorType1 entity = newEntity(SmartDiscriminatorType1.T);
		entity.setName("e1");
		commit();

		DiscriminatorEntityA delegate = selectByProperty(DiscriminatorEntityA.class, "name", "e1", smoodA);
		BtAssertions.assertThat(delegate).isNotNull();
		BtAssertions.assertThat(delegate.getDiscriminator()).isEqualTo(SmartDiscriminatorType1.DISC_TYPE1);
	}

}
