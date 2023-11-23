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
package com.braintribe.utils.genericmodel;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEnum;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;

/**
 * This class tests the {@link GMCoreTools#getDescription(java.util.Map)} against different mapping key-value
 * combinations.
 * 
 */
public class GmCoreToolsGetDescriptionTest {

	private ComplexEntity complexEntity = null;
	private SimpleEntity simpleEntity = null;
	private ComplexEntity complexEntity2 = null;
	private SimpleEntity simpleEntity2 = null;

	/**
	 * When models change, obviously respective descriptions change too. In this case all affected tests must be
	 * updated. By setting this field to true, the files which the tests normally check against are actually (re)created
	 * during the tests. In this case, make sure to compare the files carefully before committing them. Also revert the
	 * temporary field value change, i.e. this field MUST always be set to <code>false</code> in version control!
	 */
	private static final boolean RECREATE_TEST_FILES = false;

	@Before
	public void initObjects() {
		final long complexEntityId = 100;
		final long simpleEntityId = 200;
		final long complexEntity2Id = 300;
		final long simpleEntity2Id = 400;

		this.simpleEntity = SimpleEntity.T.create();
		this.simpleEntity.setBooleanProperty(false);
		this.simpleEntity.setId(simpleEntityId);
		this.simpleEntity.setStringProperty("simpleEntityTestString");

		this.simpleEntity2 = SimpleEntity.T.create();
		this.simpleEntity2.setBooleanProperty(false);
		this.simpleEntity2.setId(simpleEntity2Id);
		this.simpleEntity2.setStringProperty("simpleEntity2TestString");

		this.complexEntity = ComplexEntity.T.create();
		this.complexEntity.setBooleanProperty(true);
		this.complexEntity.setId(complexEntityId);
		this.complexEntity.setIntegerProperty(123);
		this.complexEntity.setSimpleEnum(SimpleEnum.THREE);
		this.complexEntity.setStringProperty("testString");
		this.complexEntity.setSimpleEntityProperty(this.simpleEntity);

		this.complexEntity2 = ComplexEntity.T.create();
		this.complexEntity2.setBooleanProperty(true);
		this.complexEntity2.setId(complexEntity2Id);
		this.complexEntity2.setIntegerProperty(123);
		this.complexEntity2.setSimpleEnum(SimpleEnum.THREE);
		this.complexEntity2.setStringProperty("testString");
		this.complexEntity2.setSimpleEntityProperty(this.simpleEntity);
	}

	@Test
	public void sortTest() {
		final HashMap<Object, Object> mapOfEntities = new HashMap<Object, Object>();
		mapOfEntities.put("2bEntity", this.complexEntity2);
		mapOfEntities.put("2aEntity", this.simpleEntity2);
		mapOfEntities.put(this.simpleEntity2, this.simpleEntity);
		mapOfEntities.put(this.simpleEntity, this.simpleEntity2);
		mapOfEntities.put(this.complexEntity, this.simpleEntity2);
		mapOfEntities.put(this.complexEntity2, this.simpleEntity);
		mapOfEntities.put("1Entity", this.complexEntity2);
		mapOfEntities.put("Sally", this.complexEntity2);
		assertEqualDescriptions("sortedEntities.txt", mapOfEntities);
	}

	@Test
	public void testGetDescriptionOfSimpleEntity() {
		assertEqualDescriptions("simpleEntity.txt", simpleEntity);
	}

	@Test
	public void testGetDescriptionOfMapStringToEntities() {
		final HashMap<String, GenericEntity> mapOfEntities = new HashMap<String, GenericEntity>();
		mapOfEntities.put("complexEntity", this.complexEntity2);
		mapOfEntities.put("simpleEntity", this.simpleEntity2);

		assertEqualDescriptions("stringToEntities.txt", mapOfEntities);
	}

	@Test
	public void testGetDescriptionOfMapSimpleEntityToSimpleEntity() {
		final HashMap<GenericEntity, GenericEntity> mapOfEntities = new HashMap<GenericEntity, GenericEntity>();
		mapOfEntities.put(this.simpleEntity, this.simpleEntity2);
		assertEqualDescriptions("simpleEntityToSimpleEntity.txt", mapOfEntities);
	}

	@Test
	public void testGetDescriptionOfMapSimpleEntityToComplexEntity() {

		final HashMap<GenericEntity, GenericEntity> mapOfEntities = new HashMap<GenericEntity, GenericEntity>();
		mapOfEntities.put(this.simpleEntity, this.complexEntity2);
		assertEqualDescriptions("simpleEntityToComplexEntity.txt", mapOfEntities);
	}

	@Test
	public void testGetDescriptionOfMapComplexEntityToSimpleEntity() {
		final HashMap<GenericEntity, GenericEntity> mapOfEntities = new HashMap<GenericEntity, GenericEntity>();
		mapOfEntities.put(this.complexEntity, this.simpleEntity2);
		assertEqualDescriptions("complexEntityToSimpleEntity.txt", mapOfEntities);
	}

	@Test
	public void testGetDescriptionOfMapComplexEntityToComplexEntity() {
		final HashMap<GenericEntity, GenericEntity> mapOfEntities = new HashMap<GenericEntity, GenericEntity>();
		mapOfEntities.put(this.complexEntity, this.complexEntity2);
		assertEqualDescriptions("complexEntityToComplexEntity.txt", mapOfEntities);
	}

	@Test
	public void testGetDescriptionOfMapEntitiesToString() {
		final HashMap<GenericEntity, String> mapOfEntities = new HashMap<GenericEntity, String>();
		mapOfEntities.put(this.complexEntity2, "complexEntity");
		mapOfEntities.put(this.simpleEntity2, "simpleEntity");
		assertEqualDescriptions("entitiesToString.txt", mapOfEntities);
	}

	private static void assertEqualDescriptions(String pathToExpectedDescription, Object entity) {
		String actual = StringTools.normalizeLineSeparators(GMCoreTools.getDescriptionForObject(entity), "\n");

		File fileWithExpectedDescription = new File("res-junit/GmCoreToolsGetDescriptionTest/" + pathToExpectedDescription);

		if (RECREATE_TEST_FILES) {
			FileTools.writeStringToFile(fileWithExpectedDescription, actual);
		}

		String expected = StringTools.normalizeLineSeparators(FileTools.readStringFromFile(fileWithExpectedDescription), "\n");

		assertThat(actual).isEqualToWithVerboseErrorMessage(expected);
	}

}
