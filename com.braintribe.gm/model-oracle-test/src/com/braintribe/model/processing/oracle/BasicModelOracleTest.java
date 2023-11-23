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

import static com.braintribe.model.generic.reflection.Model.modelGlobalId;

import org.junit.Test;

import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;

/**
 * @see ModelOracle
 * @see BasicModelOracle
 * 
 * @author peter.gazdik
 */
public class BasicModelOracleTest extends AbstractOracleTest {

	@Test
	public void getCorrectMetaData() throws Exception {
		assertOrigins(oracle.getMetaData(), FARM, MAMMAL, FISH, ANIMAL);
	}

	@Test
	public void getCorrectQualifiedMetaData() throws Exception {
		assertQualifiedOrigins(oracle.getQualifiedMetaData(), FARM, MAMMAL, FISH, ANIMAL);
		assertQualifiedOwnerIds(oracle.getQualifiedMetaData(), modelGlobalId(FARM_MODEL), modelGlobalId(MAMMAL_MODEL), modelGlobalId(FISH_MODEL),
				modelGlobalId(ANIMAL_MODEL));
	}

}
