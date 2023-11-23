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

import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.info.GmEnumTypeInfo;
import com.braintribe.model.processing.meta.oracle.BasicEnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.model.processing.oracle.model.extended.Color;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see EnumTypeOracle
 * @see BasicEnumTypeOracle
 * 
 * @author peter.gazdik
 */
public class BasicEnumTypeOracleTest extends AbstractOracleTest {

	@Test
	public void getCorrectGmType() throws Exception {
		GmEnumType gmType = getEnumOracle(Gender.class).asGmType();

		Assertions.assertThat(gmType).isNotNull();
		Assertions.assertThat(gmType.getTypeSignature()).isEqualTo(Gender.class.getName());
	}

	@Test
	public void getCorrectType() throws Exception {
		EnumType enumType = getEnumOracle(Gender.class).asType();

		Assertions.assertThat(enumType).isNotNull();
		Assertions.assertThat(enumType.getTypeSignature()).isEqualTo(Gender.class.getName());
	}

	@Test
	public void isDeclared() throws Exception {
		Assertions.assertThat(getEnumOracle(Color.class).isDeclared()).isTrue();
		Assertions.assertThat(getEnumOracle(Gender.class).isDeclared()).isFalse();
	}

	@Test
	public void getCorrectGmEnumTypeInfos() throws Exception {
		// The infos are actually covered by the next test which collects meta-data, so no need to assert much.
		List<GmEnumTypeInfo> gmEnumTypeInfos = getEnumOracle(Gender.class).getGmEnumTypeInfos();
		Assertions.assertThat(gmEnumTypeInfos).hasSize(4);
	}

	@Test
	public void getCorrectMetaData() throws Exception {
		assertOrigins(getEnumOracle(Gender.class).getMetaData(), FARM, MAMMAL, FISH, ANIMAL);
	}

	@Test
	public void getCorrectQualifiedMetaData() throws Exception {
		assertQualifiedOrigins(getEnumOracle(Gender.class).getQualifiedMetaData(), FARM, MAMMAL, FISH, ANIMAL);
		assertQualifiedOwnerIds(getEnumOracle(Gender.class).getQualifiedMetaData(), //
				enumOId(Gender.class, FARM_MODEL), enumOId(Gender.class, MAMMAL_MODEL), enumOId(Gender.class, FISH_MODEL), enumId(Gender.class));
	}

}
