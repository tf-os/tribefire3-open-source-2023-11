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
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.addProperty;
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.enumConstant;
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.enumType;
import static com.braintribe.model.processing.test.itw.tools.MetaModelItwTools.newGmEntityType;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesis;
import com.braintribe.model.processing.itw.synthesis.gm.GenericModelTypeSynthesisException;
import com.braintribe.model.processing.test.itw.entity.AnotherTestEntity;
import com.braintribe.model.processing.test.itw.entity.AsStringBase;
import com.braintribe.model.processing.test.itw.entity.AsStringDerivate;
import com.braintribe.model.processing.test.itw.entity.TestEntity;
import com.braintribe.model.processing.test.itw.entity.TestRecursiveToStringSelectiveInfo;
import com.braintribe.model.processing.test.itw.entity.TestSubEntity;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class BasicItwTests extends ImportantItwTestSuperType {

	@Test
	public void testPlainEntity() {
		TestEntity instance = TestEntity.T.createPlain();

		runSimpleTest(instance);
		runReflectionTest(instance);
	}

	@Test
	public void testEnhancedEntity() {
		TestEntity instance = TestEntity.T.create();

		runSimpleTest(instance);
		runReflectionTest(instance);
		runEnhancedEntityTest(instance);
	}

	private static void runSimpleTest(TestEntity entity) {
		entity.setAge(454);
		entity.setAgeO(999);
		entity.setDouble(6.67d);

		assertThat(entity.getAge()).as("Wrong int value").isEqualTo(454);
		assertThat(entity.getAgeO()).as("Wrong Integer value").isEqualTo(999);
		assertThat(entity.getDouble()).as("Wrong double value").isEqualTo(6.67d);

		entity.setStrings(new HashSet<String>());
		assertThat(entity.getStrings()).as("Wrong set value").isEmpty();
	}

	private static void runReflectionTest(TestEntity entity) {
		TestEntity.T.findProperty("age").set(entity, 454);
		TestEntity.T.findProperty("ageO").set(entity, 999);
		TestEntity.T.findProperty("double").set(entity, 6.67);

		assertThat(entity.getAge()).as("Wrong int value").isEqualTo(454);
		assertThat(entity.getAgeO()).as("Wrong Integer value").isEqualTo(999);
		assertThat(entity.getDouble()).as("Wrong double value").isEqualTo(6.67d);

		TestEntity.T.findProperty("strings").set(entity, new HashSet<String>());
		assertThat(entity.getStrings()).as("Wrong set value").isEmpty();
	}

	private void runEnhancedEntityTest(TestEntity instance) {
		EnhancedEntity ee = (EnhancedEntity) instance;

		long rid = instance.runtimeId();

		instance.setDouble(5d);
		BtAssertions.assertThat(ee.toSelectiveInformation())
				.isEqualTo("Hi " + TestEntity.class.getName() + " 5.0 ~" + rid + " " + rid + " ${N/A} Low2");
		instance.setId(99L);
		BtAssertions.assertThat(ee.toSelectiveInformation()).isEqualTo("Hi " + TestEntity.class.getName() + " 5.0 99 " + rid + " ${N/A} Low2");

		BtAssertions.assertThat(ee.toString()).isEqualTo("Hi " + TestEntity.class.getSimpleName() + " 5.0  ${N/A} Low2");
	}

	@Test
	public void testToStringToSelectiveInfoOnSubEntity() {
		AnotherTestEntity ano = AnotherTestEntity.T.create();
		ano.setName("ANO");

		TestSubEntity e = TestSubEntity.T.create();
		e.setAge(99);
		e.setAnotherEntity(ano);

		BtAssertions.assertThat(e.toSelectiveInformation()).isEqualTo("SubEntity %{N/A} 99 ANO");
		BtAssertions.assertThat(e.toString()).isEqualTo("SubEntity %{N/A} 99 ANO");
	}
	
	@Test
	public void testToStringAsStringDelegation() {
		AsStringBase asb = AsStringBase.T.create();
		asb.setName("foo");
		
		BtAssertions.assertThat(asb.asString()).isEqualTo("foo");
		BtAssertions.assertThat(asb.toString()).isEqualTo(asb.asString());
		
		AsStringDerivate asd = AsStringDerivate.T.create();
		asd.setName("bar");
		
		BtAssertions.assertThat(asd.asString()).isEqualTo("bar");
		BtAssertions.assertThat(asd.toString()).isEqualTo("Derived [bar]");
	}

	@Test
	public void testRecursiveToStringToSelectiveInfoOnSubEntity() {
		TestRecursiveToStringSelectiveInfo parent = TestRecursiveToStringSelectiveInfo.T.create();
		parent.setName("PARENT");

		TestRecursiveToStringSelectiveInfo child = TestRecursiveToStringSelectiveInfo.T.create();
		child.setName("CHILD");
		child.setParent(parent);

		BtAssertions.assertThat(child.toString()).isEqualTo(":PARENT:CHILD");
		BtAssertions.assertThat(child.toSelectiveInformation()).isEqualTo("/PARENT/CHILD");
	}

	@Test
	public void testSimpleEntityReflection_Direct() {
		GenericEntity entity = TestEntity.T.create();

		Property setProp = TestEntity.T.findProperty("strings");
		setProp.setDirectUnsafe(entity, null);

		assertThat(setProp.<Object> getDirectUnsafe(entity)).as("Retrieving directly should return null!").isNull();
		assertThat((Set<?>) setProp.get(entity)).as("Retrieving with AOP shold ensure collection not null!").isEmpty();
		assertThat((Set<?>) setProp.getDirectUnsafe(entity))
				.as("Retrieving directly should return non-null. Value should have been stored in previous step.").isEmpty();
	}

	@Test
	public void testDependencyOnOtherEntity() {
		AnotherTestEntity anotherEntity = AnotherTestEntity.T.create();
		anotherEntity.setName("Another name");

		TestEntity entity = TestEntity.T.create();

		entity.setAge(454);
		entity.setAgeO(999);
		entity.setAnotherEntity(anotherEntity);

		assertEquals("Wrong int value", 454, entity.getAge());
		assertEquals("Wrong Integer value", Integer.valueOf(999), entity.getAgeO());
		assertNotNull("AnotherEntity should NOT be null!", entity.getAnotherEntity());
		assertEquals("Wrong AnotherEntity name", "Another name", entity.getAnotherEntity().getName());
	}

	protected void print(GenericEntity anotherEntity) {
		printEnhanced((EnhancedEntity) anotherEntity);
	}

	protected void printEnhanced(EnhancedEntity ee) {
		System.out.println();
		System.out.println("Selective information:" + ee.toSelectiveInformation());
		System.out.println("ToString:" + ee);
	}

	@Test
	public void testEnum() throws GenericModelTypeSynthesisException, Exception {
		GmEnumType get = enumType("Days");
		List<String> dayNames = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

		get.setConstants(createConstants(get, dayNames));

		GenericModelTypeSynthesis gmts = GenericModelTypeSynthesis.standardInstance();

		EnumType et = (EnumType) gmts.ensureType(get);
		Class<?> enumClass = et.getJavaType();

		assertEquals("Wrong enum names", dayNames, getValues(enumClass));
		assertEquals("Wrong member returned with 'valueOf' method.", "WEDNESDAY", getWednesday(enumClass).name());
	}

	private static List<GmEnumConstant> createConstants(GmEnumType enumType, List<String> names) {
		List<GmEnumConstant> result = newList();

		for (String name : names)
			result.add(enumConstant(enumType, name));

		return result;
	}

	private static List<String> getValues(Class<?> enumClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method m = enumClass.getMethod("values");
		Enum<?>[] values = (Enum[]) m.invoke(null);
		assertNotNull("Enum 'values' should NOT be null", values);

		List<String> foundNames = new ArrayList<>();
		for (Enum<?> o : values) {
			foundNames.add(o.name());
		}

		return foundNames;
	}

	private static Enum<?> getWednesday(Class<?> enumClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method m = enumClass.getMethod("valueOf", String.class);
		Enum<?> w = (Enum<?>) m.invoke(null, "WEDNESDAY");
		assertNotNull("Method 'valueOf' seems not to be wroking.", w);

		return w;
	}

	/**
	 * just checking if everything goes OK, especially if both classes are loaded OK (this may be buggy as they depend on each other)
	 */
	@Test
	public void testProtoGmoEntitiesDependingOnEachOther() throws Exception {
		GmEntityType ge = entityType(GenericEntity.T.getTypeSignature());
		ge.setIsAbstract(true);

		GmEntityType e1 = newGmEntityType("itw.test.E1", ge);
		GmEntityType e2 = newGmEntityType("itw.test.E2", ge);

		addProperty(e1, "friendOf1", e2);
		addProperty(e2, "friendOf2", e1);
		GmSimpleType intType = MetaModelBuilder.integerType();
		addProperty(e1, "age", intType);

		GenericModelTypeSynthesis gmts = GenericModelTypeSynthesis.standardInstance();

		EntityType<GenericEntity> et1 = gmts.ensureEntityType(e1);
		EntityType<GenericEntity> et2 = gmts.ensureEntityType(e2);
		GenericEntity enhancedInstance1 = et1.create();
		GenericEntity enhancedInstance2 = et2.create();

		int IFACE_POSITION = 0;
		// expected: [itw.text.E?, iw.text.E?-weak, EnhancedEntity] => position is => 1 <=
		// System.out.println(Arrays.toString(enhancedInstance1.getClass().getInterfaces()));
		Class<?> iface1 = enhancedInstance1.getClass().getInterfaces()[IFACE_POSITION];
		Class<?> iface2 = enhancedInstance2.getClass().getInterfaces()[IFACE_POSITION];

		assertEquals("Test expects other iface on pos " + IFACE_POSITION, "itw.test.E1", iface1.getName());
		assertEquals("Test expects other iface on pos " + IFACE_POSITION, "itw.test.E2", iface2.getName());

		Method m1 = enhancedInstance1.getClass().getMethod("setFriendOf1", iface2);
		Method m2 = enhancedInstance2.getClass().getMethod("setFriendOf2", iface1);

		m1.invoke(enhancedInstance1, enhancedInstance2);
		m2.invoke(enhancedInstance2, enhancedInstance1);
	}

	@Test
	public void testRuntimeId() {
		TestEntity ifaceEntity = TestEntity.T.create();

		long initialId = ifaceEntity.runtimeId();

		// now the value is initialized
		assertThat(ifaceEntity.runtimeId()).isEqualTo(initialId);

		// now the value is just read
		assertThat(ifaceEntity.runtimeId()).isEqualTo(initialId);
	}

	@Test
	public void testFlags() {
		EnhancedEntity enhEntity = (EnhancedEntity) TestEntity.T.create();
		assertThat(enhEntity.flags()).isEqualTo(0);
		enhEntity.assignFlags(88);
		assertThat(enhEntity.flags()).isEqualTo(88);
	}

	@Test(expected = IllegalArgumentException.class)
	public void typeSafetyInPlace() {
		TestEntity entity = TestEntity.T.create();
		Property ageProperty = entity.entityType().getProperty("ageO");
		ageProperty.setDirect(entity, "INTEGER");
	}

}
