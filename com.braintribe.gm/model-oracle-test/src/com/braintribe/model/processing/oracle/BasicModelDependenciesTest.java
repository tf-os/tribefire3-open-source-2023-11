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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.BasicModelDependencies;
import com.braintribe.model.processing.meta.oracle.ModelDependencies;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see ModelDependencies
 * @see BasicModelDependencies
 * 
 * @author peter.gazdik
 */
public class BasicModelDependenciesTest extends AbstractOracleTest {

	private final ModelDependencies types = oracle.getDependencies();
	private List<String> names;

	@Test
	public void getDirectDependencies() throws Exception {
		collectNames(types.asGmMetaModels());

		assertNames(ANIMAL_MODEL, MAMMAL_MODEL, FISH_MODEL);
	}

	@Test
	public void getTransitiveDependencies() throws Exception {
		collectNames(types.transitive().asGmMetaModels());

		assertNames(MAMMAL_MODEL, FISH_MODEL, ANIMAL_MODEL, GenericModelTypeReflection.rootModelName);
	}

	@Test
	public void getTransitiveDependenciesAndSelf() throws Exception {
		collectNames(types.transitive().includeSelf().asGmMetaModels());

		assertNames(FARM_MODEL, MAMMAL_MODEL, FISH_MODEL, ANIMAL_MODEL, GenericModelTypeReflection.rootModelName);
	}

	private void collectNames(Stream<GmMetaModel> models) {
		names = models.map(GmMetaModel::getName).collect(Collectors.toList());
	}

	private void assertNames(String... expected) {
		Assertions.assertThat(names).containsOnly(expected);
	}

}
