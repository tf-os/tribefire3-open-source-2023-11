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
package com.braintribe.model.util.meta;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.nullSafe;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.AB_BC_ABC;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.B;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.BC;
import com.braintribe.testing.model.test.technical.features.multipleinheritance.C;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;

/**
 * Provides {@link NewMetaModelGeneration} related tests.
 * 
 * @author michael.lafite
 */
public class NewMmgTest_Original {

	/**
	 * Creates a meta model using the entity classes of the TestModel.
	 */
	@Test
	public void testClassBasedMetaModelGeneration() {
		createAndCheckModel(TestModelTestTools.types, TestModelTestTools.enums);
	}

	/**
	 * Creates a meta model for a specific type and its super types. Subtypes and other types (from the same model) will not be included.
	 */
	@Test
	public void typeAndSuperTypes() {
		List<EntityType<?>> entityTypes = Arrays.asList(BC.T, B.T, C.T);
		List<EntityType<?>> unexpectedEntityTypes = asList(AB_BC_ABC.T, SimpleEntity.T);

		createAndCheckModel(entityTypes, null, unexpectedEntityTypes, null);
	}

	/**
	 * Creates a meta model for a specific type and the entity types used as property types. Other types (from the same model) will not be included.
	 */
	@Test
	public void typeAndPropertyTypes() {
		List<EntityType<?>> entityTypes = Arrays.asList(ComplexEntity.T, SimpleEntity.T, AnotherComplexEntity.T);
		List<EntityType<?>> unexpectedEntityTypes = asList(BC.T, CollectionEntity.T);

		createAndCheckModel(entityTypes, null, unexpectedEntityTypes, null);
	}

	private static void createAndCheckModel(Collection<EntityType<?>> entityTypes, Collection<Class<? extends Enum<?>>> expectedEnumTypes) {

		createAndCheckModel(entityTypes, expectedEnumTypes, null, null);
	}

	private static void createAndCheckModel(Collection<EntityType<?>> entityTypes, Collection<Class<? extends Enum<?>>> expectedEnumTypes,
			Collection<EntityType<?>> unexpectedEntityTypes, Collection<Class<? extends Enum<?>>> unexpectedEnumTypes) {

		NewMetaModelGeneration metaModelGeneration = new NewMetaModelGeneration();

		GmMetaModel metaModel = metaModelGeneration.buildMetaModel("gm:Model", entityTypes);

		Set<String> entityTypeSignatures = getEntityTypeSignatures(metaModel);
		Set<String> enumTypeSignatures = getEnumTypeSignatures(metaModel);

		for (EntityType<?> expectedEntityType : nullSafe(entityTypes))
			assertThat(entityTypeSignatures).contains(expectedEntityType.getTypeSignature());

		for (Class<? extends Enum<?>> expectedEnumType : nullSafe(expectedEnumTypes))
			assertThat(enumTypeSignatures).contains(expectedEnumType.getName());

		for (EntityType<?> unexpectedEntityType : nullSafe(unexpectedEntityTypes))
			assertThat(entityTypeSignatures).doesNotContain(unexpectedEntityType.getTypeSignature());

		for (Class<? extends Enum<?>> unexpectedEnumType : nullSafe(unexpectedEnumTypes))
			assertThat(enumTypeSignatures).doesNotContain(unexpectedEnumType.getName());
	}

	private static Set<String> getEntityTypeSignatures(GmMetaModel metaModel) {
		return metaModel.entityTypes().map(GmType::getTypeSignature).collect(Collectors.toSet());
	}

	private static Set<String> getEnumTypeSignatures(GmMetaModel metaModel) {
		return metaModel.enumTypes().map(GmType::getTypeSignature).collect(Collectors.toSet());
	}

}
