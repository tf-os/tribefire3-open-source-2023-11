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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.BasicTypeHierarchy;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.meta.oracle.TypeHierarchy;
import com.braintribe.model.processing.meta.oracle.TypeHierarchy.Order;
import com.braintribe.model.processing.oracle.model.basic.animal.Animal;
import com.braintribe.model.processing.oracle.model.basic.mammal.Dog;
import com.braintribe.model.processing.oracle.model.basic.mammal.Husky;
import com.braintribe.model.processing.oracle.model.basic.mammal.Mammal;
import com.braintribe.model.processing.oracle.model.basic.mammal.PurebredDog;
import com.braintribe.model.processing.oracle.model.basic.mammal.Tiger;
import com.braintribe.model.processing.oracle.model.extended.Mutant;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.lcd.StringTools;

/**
 * This tests that retrieving sub/super types for given {@link GmEntityType} works when starting with {@link ModelOracle} (or {@link BasicModelOracle}
 * to be precise).
 * 
 * Every test starts with retrieving the {@link TypeHierarchy} object, from which we then pick types using {@link TypeHierarchy#asGmTypes()} , and
 * then extract signatures. For these signatures we do our assertions, which in case of supertypes also check that these signatures are sorted
 * correctly.
 * 
 * @see TypeHierarchy
 * @see BasicTypeHierarchy
 * @see BasicModelOracle
 * 
 * @author peter.gazdik
 */
public class BasicTypeHierarchyTest extends AbstractOracleTest {

	private static EntityTypeOracle mammalOracle = getEntityOracle(Mammal.T);

	private List<String> actualShortNames;

	@Test
	public void testSimplySuperTypes() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes());

		assertRelatedTypes_ThisOrder(Animal.T, StandardIdentifiable.T);
	}

	@Test
	public void testSuperTypesAndBase() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().includeBaseType());

		assertContainsBase();
		assertRelatedTypes_ThisOrder(Animal.T, StandardIdentifiable.T);
	}

	@Test
	public void testSuperTypesIncludeSelf() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().includeSelf());

		assertRelatedTypes_ThisOrder(Mammal.T, Animal.T, StandardIdentifiable.T);
	}

	@Test
	public void testIncludeSelfIgnoredWhenOnlyAbstract() throws Exception {
		loadSignatures(mammalOracle.getSubTypes().transitive().onlyInstantiable().includeSelf());

		assertRelatedTypes(Mutant.T, Husky.T, Tiger.T);
	}

	@Test
	public void testIncludeSelfIgnoredForceOverridesAbstract() throws Exception {
		loadSignatures(mammalOracle.getSubTypes().transitive().onlyInstantiable().includeSelfForce());

		assertRelatedTypes(Mammal.T, Mutant.T, Husky.T, Tiger.T);
	}

	@Test
	public void testSuperTypesTransitivelyIncludeSelf() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().transitive().includeSelf());

		assertRelatedTypes_ThisOrder(Mammal.T, Animal.T, GenericEntity.T, StandardIdentifiable.T);
	}

	@Test
	public void testSimplySubTypes() throws Exception {
		loadSignatures(mammalOracle.getSubTypes());

		assertRelatedTypes(Dog.T, Tiger.T);
	}

	@Test
	public void testSubTypesAndBase() throws Exception {
		loadSignatures(mammalOracle.getSubTypes().includeBaseType());

		/* No base of course, we want sub-types */
		assertRelatedTypes(Dog.T, Tiger.T);
	}

	@Test
	public void testInstantiableSubTypes() throws Exception {
		loadSignatures(mammalOracle.getSubTypes().transitive().onlyInstantiable());

		assertRelatedTypes(Husky.T, Tiger.T, Mutant.T);
	}

	@Test
	public void testAbstractSubTypes() throws Exception {
		loadSignatures(mammalOracle.getSubTypes().transitive().onlyAbstract());

		assertRelatedTypes(Dog.T, PurebredDog.T);
	}

	// Ordered

	@Test
	public void testSuper_IncludeSelf_SubFirst_Sorted() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().sorted(Order.subFirst).includeSelf());

		assertRelatedTypes_ThisOrder(Mammal.T, Animal.T, StandardIdentifiable.T);
	}

	@Test
	public void testSuper_IncludeSelf_SuperFirst_Sorted() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().sorted(Order.superFirst).includeSelf());

		assertRelatedTypes(StandardIdentifiable.T, Animal.T, Mammal.T);
		assertRelatedType(2, Mammal.T);
	}

	@Test
	public void testSub_IncludeSelf_SuperFirst_Sorted() throws Exception {
		loadSignatures(mammalOracle.getSubTypes().includeSelf().sorted(Order.subFirst));

		assertRelatedTypes(Dog.T, Tiger.T, Mammal.T);
		assertRelatedType(2, Mammal.T);
	}

	@Test
	public void testSuper_Transitive_IncludeSelf_Sorted() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().transitive().includeSelf().sorted(Order.subFirst));

		assertRelatedTypes_ThisOrder(Mammal.T, Animal.T, StandardIdentifiable.T, GenericEntity.T);
	}

	@Test
	public void testSuper_Transitive_IncludeSelf_Sorted_Reverse() throws Exception {
		loadSignatures(mammalOracle.getSuperTypes().transitive().includeSelf().sorted(Order.superFirst));

		assertRelatedTypes(Mammal.T, Animal.T, StandardIdentifiable.T, GenericEntity.T);
		assertRelatedType(0, GenericEntity.T);
		assertRelatedType(3, Mammal.T);
	}

	// ########################################
	// ## . . . . . . . Helpers . . . . . . .##
	// ########################################

	private void loadSignatures(TypeHierarchy hierarchy) {
		actualShortNames = toSignatures(hierarchy.asGmTypes());
	}

	private List<String> toSignatures(Collection<GmType> gmTypes) {
		List<String> result = newList(gmTypes.size());
		for (GmType gmType : gmTypes)
			result.add(toShortName(gmType.getTypeSignature()));

		return result;
	}

	private String toShortName(String typeSignature) {
		return StringTools.findSuffix(typeSignature, ".");
	}

	// ########################################
	// ## . . . . . . Assertions . . . . . . ##
	// ########################################

	private void assertRelatedTypes_ThisOrder(EntityType<?>... entityTypes) {
		if (actualShortNames.size() != entityTypes.length)
			Assert.fail("The number of elements does not match. Expected: " + toShortNames(entityTypes) + " actual: " + actualShortNames);

		int counter = 0;
		for (String actualShortName : actualShortNames) {
			EntityType<?> et = entityTypes[counter];

			String expectedShortName = et.getShortName();
			if (!actualShortName.equals(expectedShortName))
				Assert.fail("Wrong typ on position '" + counter + "' Expected: " + et.getTypeSignature() + ", Actual: " + actualShortName);

			counter++;
		}
	}

	protected void assertRelatedType(int index, EntityType<?> entityType) {
		String actual = actualShortNames.get(index);
		String expected = entityType.getShortName();
		if (!actual.equals(expected))
			Assert.fail("Wrong typ on position '" + index + "' Expected: " + expected + ", Actual: " + actual);
	}

	private void assertRelatedTypes(EntityType<?>... entityTypes) {
		if (actualShortNames.size() != entityTypes.length)
			Assert.fail("The number of elements does not match. Expected: " + toShortNames(entityTypes) + " actual: " + actualShortNames);

		Set<String> actuals = newSet(actualShortNames);

		for (EntityType<?> et : entityTypes) {
			String expectedShortName = et.getShortName();
			if (!actuals.remove(expectedShortName))
				Assert.fail("Type not found in related types: " + et.getTypeSignature());
		}
	}

	private List<String> toShortNames(EntityType<?>... entityTypes) {
		return Stream.of(entityTypes).map(EntityType::getShortName).collect(Collectors.toList());
	}

	private void assertContainsBase() {
		Assertions.assertThat(actualShortNames).endsWith(BaseType.INSTANCE.getTypeSignature());
		actualShortNames.remove(actualShortNames.size() - 1); // let's remove the last element
	}

}
