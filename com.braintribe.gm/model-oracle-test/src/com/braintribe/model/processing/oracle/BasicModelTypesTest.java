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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.processing.meta.oracle.BasicModelTypes;
import com.braintribe.model.processing.meta.oracle.ModelTypes;
import com.braintribe.model.processing.oracle.model.basic.animal.Animal;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.model.processing.oracle.model.basic.fish.Fish;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.model.processing.oracle.model.extended.Color;
import com.braintribe.model.processing.oracle.model.extended.Farm;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see ModelTypes
 * @see BasicModelTypes
 * 
 * @author peter.gazdik
 */
public class BasicModelTypesTest extends AbstractOracleTest {

	private final ModelTypes types = oracle.getTypes();
	private Set<String> signatures;

	@Test
	public void getAllTypes() throws Exception {
		collectSignatures(types.asGmTypes());

		assertNotContainsSignatures("object", "integer");
		assertSignatures(Animal.T, Fish.T, Dog.T, Farm.T);
		assertSignatures(Gender.class.getName());
	}

	@Test
	public void getDeclaredTypes() throws Exception {
		collectSignatures(types.onlyDeclared().asGmTypes());

		assertSignatures(Farm.T);
		assertSignatures(Color.class.getName());

		assertNotContainsSignatures(Animal.T, Fish.T, Dog.T);
		assertNotContainsSignatures(Gender.class.getName());
	}

	@Test
	public void getInheritedTypes() throws Exception {
		collectSignatures(types.onlyInherited().asGmTypes());

		assertSignatures(Animal.T, Fish.T, Dog.T);
		assertSignatures(Gender.class.getName());

		assertNotContainsSignatures(Farm.T);
		assertNotContainsSignatures(Color.class.getName());
	}

	@Test
	public void getOnlyEntities() throws Exception {
		collectSignatures(types.onlyEntities().asGmTypes());

		assertSignatures(Animal.T, Fish.T, Dog.T, Farm.T);
		assertNotContainsSignatures(Gender.class.getName(), Color.class.getName());
	}

	@Test
	public void getOnlyEnums() throws Exception {
		collectSignatures(types.onlyEnums().asGmTypes());

		assertSignatures(Gender.class.getName(), Color.class.getName());
		assertNotContainsSignatures(Animal.T, Fish.T, Dog.T, Farm.T);
	}

	private void collectSignatures(Stream<GmCustomType> gmTypes) {
		signatures = gmTypes.map(GmCustomType::getTypeSignature).collect(Collectors.toSet());
	}

	private void assertSignatures(EntityType<?>... ets) {
		Set<String> expected = Arrays.asList(ets).stream().map(EntityType::getTypeSignature).collect(Collectors.toSet());
		assertSignaturesHelper(expected);
	}

	private void assertSignatures(String... expected) {
		Assertions.assertThat(signatures).contains(expected);
	}

	private void assertSignaturesHelper(Iterable<String> expected) {
		Assertions.assertThat(signatures).containsAll(expected);
	}

	private void assertNotContainsSignatures(EntityType<?>... ets) {
		Set<String> expected = Arrays.asList(ets).stream().map(EntityType::getTypeSignature).collect(Collectors.toSet());
		assertNotContainsSignaturesHelper(expected);
	}

	private void assertNotContainsSignatures(String... expected) {
		Assertions.assertThat(signatures).doesNotContain(expected);
	}

	private void assertNotContainsSignaturesHelper(Iterable<String> expected) {
		Assertions.assertThat(signatures).doesNotContainAnyElementsOf(expected);
	}

}
