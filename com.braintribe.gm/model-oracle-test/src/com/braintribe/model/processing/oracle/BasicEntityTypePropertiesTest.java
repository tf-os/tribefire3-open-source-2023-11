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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.meta.oracle.BasicEnumTypeConstants;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.EntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.EnumTypeConstants;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see EnumTypeConstants
 * @see BasicEnumTypeConstants
 * 
 * @author peter.gazdik
 */
public class BasicEntityTypePropertiesTest extends AbstractOracleTest {

	private final EntityTypeOracle dogOracle = oracle.getEntityTypeOracle(Dog.T);
	private final EntityTypeProperties entityTypeProperties = dogOracle.getProperties();
	private Set<String> propertyNames;

	@Test
	public void getAllProperties() throws Exception {
		collectPropertyNames(entityTypeProperties.asGmProperties());
		assertNames("breed", "mammalClass", "name", "gender", "weight", "initialized", "partition", "globalId", "id", "petNumber");
	}

	@Test
	public void getDeclaredProperties() throws Exception {
		collectPropertyNames(entityTypeProperties.onlyDeclared().asGmProperties());
		assertNames("breed");
	}

	@Test
	public void getInheritedProperties() throws Exception {
		collectPropertyNames(entityTypeProperties.onlyInherited().asGmProperties());
		assertNames("mammalClass", "name", "gender", "weight", "initialized", "partition", "globalId", "id", "petNumber");
	}

	@Test
	public void getFilteredProperties() throws Exception {
		collectPropertyNames(entityTypeProperties.filter(gmProperty -> gmProperty.getName().contains("er")).asGmProperties());
		assertNames("gender", "petNumber");
	}

	private void collectPropertyNames(Stream<GmProperty> gmProperties) {
		propertyNames = gmProperties.map(GmProperty::getName).collect(Collectors.toSet());
	}

	private void assertNames(String... expected) {
		Assertions.assertThat(propertyNames).containsOnly(expected);
	}
}
