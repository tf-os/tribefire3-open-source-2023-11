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

import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.processing.meta.oracle.BasicEntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.EntityTypeProperties;
import com.braintribe.model.processing.meta.oracle.EnumTypeConstants;
import com.braintribe.model.processing.meta.oracle.EnumTypeOracle;
import com.braintribe.model.processing.oracle.model.basic.animal.Gender;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * @see EntityTypeProperties
 * @see BasicEntityTypeProperties
 * 
 * @author peter.gazdik
 */
public class BasicEnumTypeConstantsTest extends AbstractOracleTest {

	private final EnumTypeOracle genderOracle = oracle.getEnumTypeOracle(Gender.class);
	private final EnumTypeConstants enumTypeConstants = genderOracle.getConstants();
	private Set<String> constantNames;

	@Test
	public void getAllProperties() throws Exception {
		collectPropertyNames(enumTypeConstants.asGmEnumConstants());
		assertNames("M", "F", "H");
	}

	@Test
	public void getFilteredProperties() throws Exception {
		collectPropertyNames(enumTypeConstants.filter(gmConstant -> gmConstant.getName().contains("F")).asGmEnumConstants());
		assertNames("F");
	}

	private void collectPropertyNames(Stream<GmEnumConstant> gmEnumConstants) {
		constantNames = gmEnumConstants.map(GmEnumConstant::getName).collect(Collectors.toSet());
	}

	private void assertNames(String... expected) {
		Assertions.assertThat(constantNames).containsOnly(expected);
	}
}
