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
package com.braintribe.model.processing.oracle;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.info.GmEnumConstantInfo;
import com.braintribe.model.processing.meta.oracle.BasicEnumConstantOracle;
import com.braintribe.model.processing.meta.oracle.EnumConstantOracle;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see EnumConstantOracle
 * @see BasicEnumConstantOracle
 * 
 * @author peter.gazdik
 */
public class BasicEnumConstantOracleTest extends AbstractOracleTest {

	@Test
	public void getCorrectGmEnumConstant() throws Exception {
		GmEnumConstant gmEnumConstant = getEnumOracle(Gender.class).getConstant(Gender.M).asGmEnumConstant();

		Assertions.assertThat(gmEnumConstant).isNotNull();
		Assertions.assertThat(gmEnumConstant.getName()).isEqualTo("M");
		Assertions.assertThat(gmEnumConstant.getDeclaringType().getTypeSignature()).isEqualTo(Gender.class.getName());
	}

	@Test
	public void getCorrectEnumConstant() throws Exception {
		Enum<?> enumValue = getEnumOracle(Gender.class).getConstant(Gender.M).asEnum();

		Assertions.assertThat(enumValue).isNotNull();
		Assertions.assertThat(enumValue.name()).isEqualTo("M");
		Assertions.assertThat(enumValue.getDeclaringClass()).isSameAs(Gender.class);
	}

	@Test
	public void getCorrectGmEnumConstantsProperties() throws Exception {
		// The infos are actually covered by the next test which collects meta-data, so no need to assert much.
		List<GmEnumConstantInfo> gmEnumConstantInfos = getEnumOracle(Gender.class).getConstant(Gender.M).getGmEnumConstantInfos();
		Assertions.assertThat(gmEnumConstantInfos).hasSize(4);
	}

	@Test
	public void getCorrectMetaData() throws Exception {
		assertOrigins(getEnumOracle(Gender.class).getConstant(Gender.M).getMetaData(), FARM, MAMMAL, FISH, ANIMAL);
	}

	@Test
	public void getCorrectQualifiedMetaData() throws Exception {
		assertQualifiedOrigins(getEnumOracle(Gender.class).getConstant(Gender.M).getQualifiedMetaData(), FARM, MAMMAL, FISH, ANIMAL);
		assertQualifiedOwnerIds(getEnumOracle(Gender.class).getConstant(Gender.M).getQualifiedMetaData(), //
				constantOId(Gender.M, FARM_MODEL), //
				constantOId(Gender.M, MAMMAL_MODEL), //
				constantOId(Gender.M, FISH_MODEL), //
				constantId(Gender.M));
	}
}
