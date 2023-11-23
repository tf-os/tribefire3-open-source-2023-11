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

import static com.braintribe.utils.junit.assertions.BtAssertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.fest.assertions.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.generic.value.NullDescriptor;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.test.itw.build.GmEntityBuilder;
import com.braintribe.model.processing.test.itw.entity.Color;
import com.braintribe.model.processing.test.itw.entity.InitializedEntity;
import com.braintribe.model.processing.test.itw.entity.InitializedSubEntity;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class InitializedEntityItwTests extends ImportantItwTestSuperType {

	private static final long SECOND_IN_MILLIS = 1000;

	@Test
	public void plainInstance() {
		checkStandard(InitializedEntity.T.createPlain());
	}

	@Test
	public void enhancedInstance() {
		checkStandard(InitializedEntity.T.create());
	}

	private void checkStandard(InitializedEntity e) {
		assertThat(e.getStringValue()).isEqualTo("hello");
		assertThat(e.getIntValue()).isEqualTo(99);
		assertThat(e.getLongValue()).isEqualTo(11L);
		assertThat(e.getFloatValue()).isEqualTo(123f);
		assertThat(e.getDoubleValue()).isEqualTo(-123D);
		assertThat(e.getBigFloatValue()).isEqualTo(1.0e30f);
		assertThat(e.getBigDoubleValue()).isEqualTo(-1.0e30d);
		assertThat(e.getDecimalValue()).isEqualTo(new BigDecimal("99889988.00"));
		assertThat(e.getBooleanValue()).isTrue();
		assertThat(e.getEnumValue()).isEqualTo(Color.green);
		assertThat(e.getEnumShort()).isEqualTo(Color.green);
		assertThat(e.getListValue()).isEqualTo(asList("one", "two"));
		assertThat(e.getSetValue()).isEqualTo(asSet("one", "two"));
		assertThat(e.getEnumSetValue()).isEqualTo(asSet(Color.green));
		assertThat(e.getMapValue()).isEqualTo(asMap(1, "one", 2, "two"));
		// 10 seconds should be safe even for DevQA
		assertThat(new Date().getTime() - e.getDateValue().getTime()).isLessThan(10 * SECOND_IN_MILLIS);
		assertThat(e.getUninitializedDateValue()).isNull();
	}

	@Test
	public void enhancedInstance_Sub() {
		InitializedSubEntity e = InitializedSubEntity.T.create();

		assertThat(e.getIntValue()).isEqualTo(88); // overridden
		assertThat(e.getLongValue()).isEqualTo(0); // overridden with 0 as nothing was stated, but this is default
		assertThat(e.getNewLongValue()).isEqualTo(0L); // new property which is primitive
		assertThat(e.getBooleanValue()).isTrue(); // inherited despite being re-declared
		assertThat(e.getDateValue()).isNull(); // overridden with null as nothing was stated, but this is default
		assertThat(e.getStringValue()).isEqualTo("hello"); // inherited
		assertThat(e.getFloatValue()).isEqualTo(123f); // inherited
	}

	@Test
	public void reflectionProperty() {
		assertPropertyInitializer(InitializedEntity.T, "stringValue", "hello");
		assertPropertyInitializer(InitializedEntity.T, "intValue", 99);
		assertPropertyInitializer(InitializedSubEntity.T, "intValue", 88);

		// declaring type is sub-type, firstDeclaringType is super-type
		assertDeclaringType(InitializedSubEntity.T, "intValue", InitializedSubEntity.T, InitializedEntity.T);
	}

	private void assertPropertyInitializer(EntityType<?> et, String propertyName, Object expectedInitializer) {
		Property p = et.getProperty(propertyName);
		assertThat(p.getInitializer()).isEqualTo(expectedInitializer);
	}

	private void assertDeclaringType(EntityType<?> et, String propertyName, EntityType<?> declaringType, EntityType<?> firstDeclaringType) {
		Property p = et.getProperty(propertyName);
		assertThat(p.getDeclaringType()).isSameAs(declaringType);
		assertThat(p.getFirstDeclaringType()).isSameAs(firstDeclaringType);
	}

	@Test
	public void testGeneratedInterfaceHasAnnotation() throws Exception {
		final String ENTITY_SIGNATURE = "test.Entity";
		final String SUB_ENTITY_SIGNATURE1 = "test.SubEntity1";
		final String SUB_ENTITY_SIGNATURE2 = "test.SubEntity2";
		final String SUB_ENTITY_SIGNATURE3 = "test.SubEntity3";

		GmMetaModel rootModel = NewMetaModelGeneration.rootModel().getMetaModel();
		GmMetaModel metaModel = new NewMetaModelGeneration().buildMetaModel("gm:ItwTestModel", Collections.emptySet());
		ModelOracle rootOracle = new BasicModelOracle(rootModel);
		GmSimpleType stringType = rootOracle.findGmType(SimpleTypes.TYPE_STRING);
		GmEntityType generitEntityType = rootOracle.findGmType(GenericEntity.T);

		// Create entity with property name (String) and default value "bob"
		GmEntityBuilder builder = new GmEntityBuilder(ENTITY_SIGNATURE);
		builder.addSuper(generitEntityType);
		builder.addProperty("name", stringType, "bob");
		builder.addToMetaModel(metaModel);

		GmEntityBuilder subBuilder;
		subBuilder = new GmEntityBuilder(SUB_ENTITY_SIGNATURE1);
		subBuilder.addSuper(builder);
		subBuilder.addProperty("name", stringType, "joe");
		subBuilder.addToMetaModel(metaModel);

		subBuilder = new GmEntityBuilder(SUB_ENTITY_SIGNATURE2);
		subBuilder.addSuper(builder);
		subBuilder.addProperty("name", stringType, NullDescriptor.T.create());
		subBuilder.addToMetaModel(metaModel);

		subBuilder = new GmEntityBuilder(SUB_ENTITY_SIGNATURE3);
		subBuilder.addSuper(builder);
		subBuilder.addToMetaModel(metaModel);

		// Ensure types
		metaModel.deploy();

		// Check correct annotation is there on the getter
		assertPropertyInitializer(ENTITY_SIGNATURE, "'bob'");
		assertPropertyInitializer(SUB_ENTITY_SIGNATURE1, "'joe'");
		assertPropertyInitializer(SUB_ENTITY_SIGNATURE2, "null");
		assertPropertyMethodNotDeclared(SUB_ENTITY_SIGNATURE3);
	}

	private void assertPropertyInitializer(String signature, String initializerString) throws Exception {
		Class<?> clazz = loadItwClass(signature);
		Method m = clazz.getDeclaredMethod("getName");
		Initializer initializer = m.getAnnotation(Initializer.class);

		BtAssertions.assertThat(clazz).isInterface();
		BtAssertions.assertThat(initializer).isNotNull();
		BtAssertions.assertThat(initializer.value()).isEqualTo(initializerString);
	}

	private void assertPropertyMethodNotDeclared(String signature) throws Exception {
		Class<?> clazz = loadItwClass(signature);
		try {
			clazz.getDeclaredMethod("getName");
			Assert.fail("Method 'getName' should not be declared for: " + signature);

		} catch (NoSuchMethodException ignored) {
			// this is what we want
		}
	}

	private Class<?> loadItwClass(String signature) throws ClassNotFoundException {
		return Class.forName(signature, false, GenericEntity.T.getClass().getClassLoader());
	}

}
