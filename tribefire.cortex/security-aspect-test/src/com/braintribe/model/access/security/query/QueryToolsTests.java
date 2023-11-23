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
package com.braintribe.model.access.security.query;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static org.fest.assertions.Assertions.assertThat;

import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.access.security.query.entities.Description;
import com.braintribe.model.access.security.query.entities.Person;
import com.braintribe.model.access.security.query.entities.Task;
import com.braintribe.model.access.security.testdata.manipulation.EntityWithPropertyConstraints;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.category.KnownIssue;

/**
 * @see QueryTools
 */
public class QueryToolsTests {

	private SourcesDescriptor sources;

	// #######################################
	// ## . . . . EntityQuery sources . . . ##
	// #######################################

	@Test
	public void findsDirectSource() {
		EntityQuery query = EntityQueryBuilder.from(Person.T).done();

		assertSourceTypes(query, Person.class);
	}

	@Test
	public void findsDirectSourceWithConditions() {
		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("PersonName").done();

		assertSourceTypes(query, Person.class);
	}

	@Test
	public void findsPropertySource() {
		EntityType<EntityWithPropertyConstraints> entityType = GMF.getTypeReflection().getEntityType(EntityWithPropertyConstraints.class);
		EntityWithPropertyConstraints task = entityType.create();

		SelectQuery query = new SelectQueryBuilder().from(Person.T, "p").where().property("p", "task").eq(task).done();

		assertSourceTypes(query, Person.class, Task.class);
		assertImplicitSources(Person.class, "task");
	}

	@Test
	public void findsPropertyChainSources() {
		SelectQuery query = new SelectQueryBuilder().from(Person.T, "p").where().property("p", "task.description").eq(null).done();

		assertSourceTypes(query, Person.class, Task.class, Description.class);
		assertImplicitSources(Person.class, "task", Person.class, "task.description");
	}

	@Test
	public void findsPropertySource_Entity() {
		EntityType<EntityWithPropertyConstraints> entityType = GMF.getTypeReflection().getEntityType(EntityWithPropertyConstraints.class);
		EntityWithPropertyConstraints task = entityType.create();

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("task").eq(task).done();

		assertSourceTypes(query, Person.class, Task.class);
		assertImplicitSourcesWithDefaultSource("task");
	}

	@Test
	public void findsPropertyChainSources_Entity() {
		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("task.description").eq(null).done();

		assertSourceTypes(query, Person.class, Task.class, Description.class);
		assertImplicitSourcesWithDefaultSource("task", "task.description");
	}

	@Test
	public void findsJoinSource() {
		Query query = new SelectQueryBuilder().from(Person.T, "p").from(Task.class, "t").join("t", "description", "d").done();

		assertSourceTypes(query, Person.class, Task.class, Description.class);
	}

	@Test
	public void ignoresSimpleCollectionJoinSource() {
		Query query = new SelectQueryBuilder().from(Person.T, "p").join("p", "nickNames", "n").done();

		assertSourceTypes(query, Person.class);
	}

	@Test
	public void findsCollectionJoinSource() {
		Query query = new SelectQueryBuilder().from(Person.T, "p").join("p", "tasks", "t").done();

		assertSourceTypes(query, Person.class, Task.class);
	}

	/**
	 * I am just forcing a PropertyOperand with <tt>null</tt> as propertyName (with that entitySignature), because there
	 * was a bug that this was throwing NPE. (The entitySignature function will have PropertyOperand as it's operand,
	 * with "propertyName" being <tt>null</tt>.)
	 */
	@Test
	public void findsSourceWithoutNpe() {
		Query query = new SelectQueryBuilder().from(Person.T, "p").select().entitySignature().entity("p").done();

		assertSourceTypes(query, Person.class);
	}

	// #######################################
	// ## . . . PropertyQuery sources . . . ##
	// #######################################

	@Test
	public void simpleProperty() {
		Query query = PropertyQueryBuilder.forProperty(Person.T, 10, "name").done();

		assertSourceTypes(query, Person.class);
	}

	// TODO this also fails currently, see "tmp workaround" in QueryVisitor
	@Category(KnownIssue.class)
	// @Test
	public void EXPTECTED_TO_FAIL_entityProperty() {
		Query query = PropertyQueryBuilder.forProperty(Person.T, 10, "task").done();

		assertSourceTypes(query, Person.class, Task.class);
	}

	@Test
	public void entityCollectionProperty() {
		Query query = PropertyQueryBuilder.forProperty(Person.T, 10, "tasks").orderBy("taskName").done();

		assertSourceTypes(query, Person.class, Task.class);
	}

	// ####################################################################################################
	// ####################################################################################################

	private void assertSourceTypes(Query query, @SuppressWarnings("rawtypes") Class... classes) {
		findSources(query);

		/* We use Object[] (rather than Set<String>), because that's the only thing the "Assertions" library accepts */
		Object[] expectedTypes = extractExpectedTypes(classes);
		Set<String> actualTypes = extractActualTypes();

		assertThat(actualTypes).as("Wrong source types detected.").containsOnly(expectedTypes);
	}

	private void findSources(Query query) {
		sources = QueryTools.findQuerySources(query);
	}

	private Set<String> extractActualTypes() {
		return sources.getSourceTypes().stream() //
				.map(EntityType::getTypeSignature) //
				.collect(Collectors.toSet());
	}

	@SuppressWarnings("rawtypes")
	private Object[] extractExpectedTypes(Class... classes) {
		Object[] expectedTypes = new Object[classes.length];
		int counter = 0;
		for (Class<?> clazz : classes)
			expectedTypes[counter++] = clazz.getName();

		return expectedTypes;
	}

	/**
	 * Expects an array of objects which consists of pairs <class, propertyChain>.
	 * 
	 * NOTE that this only if source of a {@link PropertyOperand} is an instance of {@link From}.
	 */
	private void assertImplicitSources(Object... impSourceEntries) {
		if ((impSourceEntries.length & 1) == 1)
			throw new RuntimeException("Error in test. Even number of elements expected, but " + impSourceEntries.length + " were given");

		/* Returned sources are strings in form: "${typeSignature}#${propertyChain}" */
		Object[] expectedImplicitSources = extractExpectedImplicitSources(impSourceEntries);
		Set<String> actualImplicitSources = extractActualImplicitSources();

		assertThat(actualImplicitSources).as("Wrong implicit sources detected.").containsOnly(expectedImplicitSources);
	}

	/** Expects an array of objects which consists of pairs <class, propertyChain>. */
	private Object[] extractExpectedImplicitSources(Object... impSourceEntries) {
		Object[] result = new Object[impSourceEntries.length >> 1];

		int counter = 0;
		int added = 0;
		while (counter < impSourceEntries.length) {
			String sourceTypeSignature = ((Class<?>) impSourceEntries[counter++]).getName();
			String propChain = (String) impSourceEntries[counter++];

			result[added++] = sourceTypeSignature + "#" + propChain;
		}
		return result;
	}

	/** Only works if source of a {@link PropertyOperand} is an instance {@link From}. */
	private Set<String> extractActualImplicitSources() {
		Set<String> result = newSet();

		for (Entry<PropertyOperand, EntityType<?>> entry : sources.implicitSources()) {
			PropertyOperand po = entry.getKey();
			String sourceTypeSignature = ((From) po.getSource()).getEntityTypeSignature();

			result.add(sourceTypeSignature + "#" + po.getPropertyName());
		}

		return result;
	}

	private void assertImplicitSourcesWithDefaultSource(String... propertyPaths) {
		/* Returned sources are strings in form: "${typeSignature}#${propertyChain}" */
		Set<String> actualImplicitSources = extractActualImplicitSourcesWithDefaultSource();

		assertThat(actualImplicitSources).as("Wrong implicit sources detected.").containsOnly((Object[]) propertyPaths);
	}

	private Set<String> extractActualImplicitSourcesWithDefaultSource() {
		Set<String> result = newSet();

		for (Entry<PropertyOperand, EntityType<?>> entry : sources.implicitSources()) {
			assertThat(entry.getKey().getSource()).isNull();

			result.add(entry.getKey().getPropertyName());
		}

		return result;
	}

}
