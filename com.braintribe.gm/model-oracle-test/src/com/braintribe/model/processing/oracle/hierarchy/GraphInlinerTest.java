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
package com.braintribe.model.processing.oracle.hierarchy;

import static com.braintribe.model.processing.oracle.model.ModelNames.ANIMAL_MODEL;
import static com.braintribe.model.processing.oracle.model.ModelNames.FARM_MODEL;
import static com.braintribe.model.processing.oracle.model.ModelNames.FISH_MODEL;
import static com.braintribe.model.processing.oracle.model.ModelNames.MAMMAL_MODEL;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.hierarchy.GraphInliner;
import com.braintribe.model.processing.oracle.model.ModelOracleModelProvider;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see GraphInliner
 * 
 * @author peter.gazdik
 */
public class GraphInlinerTest {

	private static GmMetaModel farmModel = ModelOracleModelProvider.farmModel();

	private List<GmMetaModel> inlinedModels;

	@Test
	public void testInliningOrder() throws Exception {
		inlinedModels = GraphInliner.inline(farmModel, GmMetaModel::getDependencies).list;

		assertModelNames(FARM_MODEL, MAMMAL_MODEL, FISH_MODEL, ANIMAL_MODEL, GenericModelTypeReflection.rootModelName);
	}

	private void assertModelNames(String... expectedNames) {
		List<String> actualNames = inlinedModels.stream().map(GmMetaModel::getName).collect(Collectors.toList());
		Assertions.assertThat(actualNames).containsExactly(expectedNames);
	}
}
