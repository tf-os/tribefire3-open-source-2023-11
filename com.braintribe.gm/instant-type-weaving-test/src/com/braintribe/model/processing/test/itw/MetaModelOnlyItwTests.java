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
package com.braintribe.model.processing.test.itw;

import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.entityType;
import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.metaModel;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.build.GmEntityBuilder;
import com.braintribe.model.processing.test.itw.build.GmEnumBuilder;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class MetaModelOnlyItwTests extends ImportantItwTestSuperType {

	private static final String SUPER_PERSON_TS = "com.braintribe.model.processing.test.itw.SuperPerson";
	private static final String PERSON_TS = "com.braintribe.model.processing.test.itw.Person";
	private static final String SUB_PERSON_TS = "com.braintribe.model.processing.test.itw.SubPerson";
	private static final String SIZE_TS = "com.braintribe.model.processing.test.itw.Size";

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private GenericEntity currentEntity;
	private EntityType<GenericEntity> currentEntityType;
	private EnumType currentEnumType;

	@Test
	public void testSimpleEntity() throws Exception {
		GmMetaModel gmMetaModel = createMetaModel();
		gmMetaModel.deploy();

		checkInterfaceContainsEntityTypeField();
		checkEnumContainsEnumTypeField();

		loadNewEnhancedInstance(SUPER_PERSON_TS);
		setProperty("superAge", 10);
		checkProperty("superAge", 10);

		// tests with person instance
		loadNewEnhancedInstance(PERSON_TS);
		setProperty("superAge", 10);
		checkProperty("superAge", 10);
		setProperty("age", 10);
		checkProperty("age", 10);
		// changing entity type, but entity instance remains
		loadEntityType(SUPER_PERSON_TS);
		setProperty("superAge", 20);
		checkProperty("superAge", 20);

		// tests with sub-person instance
		loadNewEnhancedInstance(SUB_PERSON_TS);
		setProperty("superAge", 10);
		checkProperty("superAge", 10);
		setProperty("age", 10);
		checkProperty("age", 10);
		setProperty("subAge", 10);
		checkProperty("subAge", 10);
		// changing entity type, but entity instance remains
		loadEntityType(PERSON_TS);
		setProperty("age", 20);
		checkProperty("age", 20);
		setProperty("superAge", 20);
		checkProperty("superAge", 20);
		// changing entity type, but entity instance remains
		loadEntityType(SUPER_PERSON_TS);
		setProperty("superAge", 30);
		checkProperty("superAge", 30);
	}

	private void checkInterfaceContainsEntityTypeField() throws Exception {
		loadEntityType(PERSON_TS);
		Class<GenericEntity> javaType = currentEntityType.getJavaType();
		BtAssertions.assertThat(javaType.getName()).isEqualTo(PERSON_TS);

		// if the field didn't exist, this would throw an exception
		javaType.getDeclaredField("T");
	}

	private void checkEnumContainsEnumTypeField() throws Exception {
		loadEnumType(SIZE_TS);
		Class<?> javaType = currentEnumType.getJavaType();
		BtAssertions.assertThat(javaType.getName()).isEqualTo(SIZE_TS);
		BtAssertions.assertThat(javaType.isEnum()).isTrue();

		javaType.getDeclaredField("T");
	}

	private void setProperty(String propertyName, Object value) {
		currentEntityType.getProperty(propertyName).set(currentEntity, value);
	}

	private void checkProperty(String propertyName, Object expected) {
		Object actual = currentEntityType.getProperty(propertyName).get(currentEntity);
		BtAssertions.assertThat(actual).isEqualTo(expected);
	}

	private void loadNewEnhancedInstance(String signature) {
		loadEntityType(signature);
		currentEntity = currentEntityType.create();
	}

	private void loadEntityType(String signature) {
		currentEntityType = typeReflection.getType(signature);
	}

	private void loadEnumType(String signature) {
		currentEnumType = typeReflection.getType(signature);
	}

	private static GmMetaModel createMetaModel() {
		GmEntityType ge = entityType(GenericEntity.class.getName());
		ge.setIsAbstract(true);

		GmSimpleType intType = MetaModelBuilder.integerType();

		GmMetaModel metaModel = metaModel();
		GmEntityType superType = ge;
		GmEntityBuilder gmEntityBuilder;

		gmEntityBuilder = new GmEntityBuilder(SUPER_PERSON_TS);
		gmEntityBuilder.addSuper(superType).setIsAbstract(false);
		gmEntityBuilder.addProperty("superAge", intType);
		superType = gmEntityBuilder.addToMetaModel(metaModel);

		gmEntityBuilder = new GmEntityBuilder(PERSON_TS);
		gmEntityBuilder.addSuper(superType).setIsAbstract(false);
		gmEntityBuilder.addProperty("age", intType);
		superType = gmEntityBuilder.addToMetaModel(metaModel);

		gmEntityBuilder = new GmEntityBuilder(SUB_PERSON_TS);
		gmEntityBuilder.addSuper(superType).setIsAbstract(false);
		gmEntityBuilder.addProperty("subAge", intType);
		superType = gmEntityBuilder.addToMetaModel(metaModel);

		GmEnumBuilder gmEnumBuilder = new GmEnumBuilder(SIZE_TS);
		gmEnumBuilder.addConstant("small").addConstant("medium").addConstant("big");
		gmEnumBuilder.addToMetaModel(metaModel);

		return metaModel;
	}

	protected <T extends GenericEntity> T instantiate(Class<T> beanClass) {
		EntityType<T> entityType = typeReflection.getEntityType(beanClass);
		return entityType.create();
	}

	protected <T extends GenericEntity> T instantiatePlain(Class<T> beanClass) {
		EntityType<T> entityType = typeReflection.getEntityType(beanClass);
		return entityType.createPlain();
	}

}
