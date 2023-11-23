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
package com.braintribe.model.processing.oracle.empty;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EnumConstantOracle;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.PropertyOracle;
import com.braintribe.model.processing.meta.oracle.empty.EmptyModelOracle;
import com.braintribe.model.processing.oracle.model.ModelNames;
import com.braintribe.model.processing.oracle.model.ModelOracleModelProvider;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.model.processing.oracle.model.basic.animal.Pet;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * @author peter.gazdik
 */
public class EmptyModelOracleTests {

	protected static ModelOracle emptyOracle = EmptyModelOracle.INSTANCE;
	protected static ModelOracle normalOracle = new BasicModelOracle(
			new NewMetaModelGeneration().buildMetaModel(ModelNames.ANIMAL_MODEL, ModelOracleModelProvider.animalTypes));

	@Test
	public void emptyEntityOracle() throws Exception {
		EntityTypeOracle entityTypeOracle = emptyOracle.findEntityTypeOracle(Dog.T);

		assertThat(entityTypeOracle).isNotNull();
		assertThat(entityTypeOracle.getProperties()).isNotNull();
		assertThat(entityTypeOracle.findProperty("name")).isNotNull();
		assertThat(entityTypeOracle.findProperty("name").asGmProperty()).isNull();
	}

	@Test
	public void emptyPropertyOracle() throws Exception {
		PropertyOracle propertyOracle = emptyOracle.getEntityTypeOracle(GenericEntity.T).findProperty(Pet.name);

		assertThat(propertyOracle).isNotNull();
		assertThat(propertyOracle.asGmProperty()).isNull();
	}

	@Test
	public void emptyPropertyOracle_FromNonEmptyEntityOracle() throws Exception {
		PropertyOracle propertyOracle = normalOracle.getEntityTypeOracle(GenericEntity.T).findProperty(Pet.name);

		assertThat(propertyOracle).isNull();
	}

	@Test
	public void emptyEnumOracle() throws Exception {
		EnumTypeOracle enumTypeOracle = emptyOracle.findEnumTypeOracle(Gender.class);

		assertThat(enumTypeOracle).isNotNull();
		assertThat(enumTypeOracle.getConstants()).isNotNull();
		assertThat(enumTypeOracle.findConstant("male")).isNotNull();
		assertThat(enumTypeOracle.findConstant("male").asGmEnumConstant()).isNull();
	}

	@Test
	public void emptyConstantOracle() throws Exception {
		EnumConstantOracle constantOracle = emptyOracle.getEnumTypeOracle(Gender.class).findConstant("nonExistent");

		assertThat(constantOracle).isNotNull();
		assertThat(constantOracle.asGmEnumConstant()).isNull();
	}

	@Test
	public void emptyConstantOracle_FromNonEmptyEnumOracle() throws Exception {
		EnumConstantOracle constantOracle = normalOracle.getEnumTypeOracle(Gender.class).findConstant("nonExistent");

		assertThat(constantOracle).isNull();
	}

}
