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
package com.braintribe.testing.model.test.testtools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.AB_BC_but_not_ABC;
import com.braintribe.testing.model.test.technical.limits.ManyValuesEnum;

/**
 * Provides tests for {@link TestModelTestTools}.
 *
 * @author michael.lafite
 *
 */
public class TestModelTestToolsTest {

	@Test
	public void testCreateTestModelMetaModel() {
		GmMetaModel model = TestModelTestTools.createTestModelMetaModel();

		assertThat(model.getTypes()).extracting("typeSignature").contains(SimpleEntity.class.getName(), ComplexEntity.class.getName(),
				AB_BC_but_not_ABC.class.getName());

		assertThat(model.getTypes()).extracting("typeSignature").contains(SimpleEnum.class.getName(), ManyValuesEnum.class.getName());
	}

}
