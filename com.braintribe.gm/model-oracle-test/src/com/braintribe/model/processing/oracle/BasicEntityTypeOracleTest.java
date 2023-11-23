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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.info.GmEntityTypeInfo;
import com.braintribe.model.processing.meta.oracle.BasicEntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.oracle.model.basic.animal.Animal;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.model.processing.oracle.model.extended.Farm;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see EntityTypeOracle
 * @see BasicEntityTypeOracle
 * 
 * @author peter.gazdik
 */
public class BasicEntityTypeOracleTest extends AbstractOracleTest {

	@Test
	public void getCorrectGmType() throws Exception {
		GmEntityType gmType = getEntityOracle(Dog.T).asGmType();

		Assertions.assertThat(gmType).isNotNull();
		Assertions.assertThat(gmType.getTypeSignature()).isEqualTo(Dog.T.getTypeSignature());
	}

	@Test
	public void getCorrectType() throws Exception {
		EntityType<?> entityType = getEntityOracle(Dog.T).asType();

		Assertions.assertThat(entityType).isNotNull();
		Assertions.assertThat(entityType.getTypeSignature()).isEqualTo(Dog.T.getTypeSignature());
	}

	@Test
	public void isDeclared() throws Exception {
		Assertions.assertThat(getEntityOracle(Farm.T).isDeclared()).isTrue();
		Assertions.assertThat(getEntityOracle(Animal.T).isDeclared()).isFalse();
	}

	@Test
	public void getCorrectGmEntityTypeInfos() throws Exception {
		// The infos are actually covered by the next test which collects meta-data, so no need to assert much.
		List<GmEntityTypeInfo> gmEntityTypeInfos = getEntityOracle(Animal.T).getGmEntityTypeInfos();
		Assertions.assertThat(gmEntityTypeInfos).hasSize(4);
	}

	@Test
	public void getCorrectMetaData() throws Exception {
		assertOrigins(getEntityOracle(Animal.T).getMetaData(), FARM, MAMMAL, FISH, ANIMAL);
	}

	@Test
	public void getCorrectQualifiedMetaData() throws Exception {
		assertQualifiedOrigins(getEntityOracle(Animal.T).getQualifiedMetaData(), FARM, MAMMAL, FISH, ANIMAL);
		assertQualifiedOwnerIds(getEntityOracle(Animal.T).getQualifiedMetaData(), //
				entityOId(Animal.T, FARM_MODEL), entityOId(Animal.T, MAMMAL_MODEL), entityOId(Animal.T, FISH_MODEL), entityId(Animal.T));
	}

	@Test
	public void getCorrectMetaData_ExcludesInherited() throws Exception {
		assertOrigins(getEntityOracle(Dog.T).getMetaData(), FARM);
	}

	@Test
	public void hasProperty() throws Exception {
		EntityTypeOracle dogOracle = getEntityOracle(Dog.T);
		// declared
		Assertions.assertThat(dogOracle.hasProperty("breed")).isTrue();
		// overridden
		Assertions.assertThat(dogOracle.hasProperty(GENDER)).isTrue();
		// inherited, not overridden
		Assertions.assertThat(dogOracle.hasProperty(WEIGHT)).isTrue();
		// not-existing
		Assertions.assertThat(dogOracle.hasProperty("madeUp")).isFalse();
	}

	@Test
	public void getInitializer() throws Exception {
		EntityTypeOracle dogOracle = getEntityOracle(Dog.T);
		
		assertThat(dogOracle.getProperty("name").getInitializer()).isNull();
		assertThat(dogOracle.getProperty("initialized").getInitializer()).isEqualTo("Mammal");
	}

}
