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
package com.braintribe.model.processing.vde.impl.query;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.bvd.convert.ToReference;
import com.braintribe.model.bvd.convert.ToSet;
import com.braintribe.model.bvd.query.Query;
import com.braintribe.model.bvd.query.ResultConvenience;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SessionAspect;
import com.braintribe.model.processing.vde.evaluator.impl.root.EntityReferenceVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.impl.misc.Name;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.test.VdeTest;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * Provides tests for {@link EntityReferenceVde}.
 *
 */
public class QueryVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator();

	@Test
	public void testQueryListWithEntityQuery() throws Exception {
		BasicPersistenceGmSession session = prepareSession();

		String expectedLastName = "Doe";
		//@formatter:off
		EntityQuery q = 
			EntityQueryBuilder
				.from(Person.T)
				.where()
					.property("name.last").eq("Doe")
				.done();
		//@formatter:on
		
		Query queryVd = $.query();
		queryVd.setQuery(q);
		queryVd.setResultConvenience(ResultConvenience.list);
		
		
		Object result = evaluateWith(SessionAspect.class, session, queryVd);
		assertListResult(result, 2, p -> assertPerson(p, expectedLastName));
	}
	
	@Test
	public void testQueryListWithEntityQueryGmql() throws Exception {
		BasicPersistenceGmSession session = prepareSession();

		String expectedLastName = "Doe";
		String q = "from "+Person.T.getTypeSignature()+" where name.last = 'Doe'";
		
		Query queryVd = $.query();
		queryVd.setQuery(q);
		queryVd.setResultConvenience(ResultConvenience.list);
		
		Object result = evaluateWith(SessionAspect.class, session, queryVd);
		assertListResult(result, 2, p -> assertPerson(p, expectedLastName));
	}
	
	@Test
	public void testQueryListWithEntityQueryAsSet() throws Exception {
		BasicPersistenceGmSession session = prepareSession();

		String expectedLastName = "Doe";
		//@formatter:off
		EntityQuery q = 
			EntityQueryBuilder
				.from(Person.T)
				.where()
					.property("name.last").eq("Doe")
				.done();
		//@formatter:on
		
		Query queryVd = $.query();
		queryVd.setQuery(q);
		queryVd.setResultConvenience(ResultConvenience.list);
		
		ToSet toSet = $.toSet();
		toSet.setOperand(queryVd);
		
		Object result = evaluateWith(SessionAspect.class, session, toSet);
		assertSetResult(result, 2, p -> assertPerson(p, expectedLastName));
	}
	
	@Test
	public void testQueryUniqueWithEntityQuery() throws Exception {
		BasicPersistenceGmSession session = prepareSession();

		String expectedLastName = "Doe";
		//@formatter:off
		EntityQuery q = 
			EntityQueryBuilder
				.from(Person.T)
				.where()
				.conjunction()
					.property("name.first").eq("John")
					.property("name.last").eq("Doe")
				.close()	
				.done();
		//@formatter:on
		
		Query queryVd = $.query();
		queryVd.setQuery(q);
		queryVd.setResultConvenience(ResultConvenience.unique);
		
		Object result = evaluateWith(SessionAspect.class, session, queryVd);
		assertUniqueResult(result, p -> assertPerson(p, expectedLastName));
	}
	
	@Test
	public void testQueryListWithEntityQueryAsReferences() throws Exception {
		BasicPersistenceGmSession session = prepareSession();

		//@formatter:off
		EntityQuery q = 
			EntityQueryBuilder
				.from(Person.T)
				.where()
					.property("name.last").eq("Doe")
				.done();
		//@formatter:on
		
		Query queryVd = $.query();
		queryVd.setQuery(q);
		queryVd.setResultConvenience(ResultConvenience.list);
		
		ToReference referenceVd = ToReference.T.create();
		referenceVd.setOperand(queryVd);
		
		Object result = evaluateWith(SessionAspect.class, session, referenceVd);
		assertListResult(result, 2, p -> assertThat(p).isInstanceOf(PersistentEntityReference.class));
	}
	
	

	private void assertUniqueResult(Object result, Consumer<Object> elementAssertion) {
		assertThat(result).isNotNull();
		elementAssertion.accept(result);
	}
	
	private void assertListResult(Object result, int expectedSize, Consumer<Object> elementAssertion) {
		assertCollectionnResult(result, expectedSize, List.class, elementAssertion);
	}

	private void assertSetResult(Object result, int expectedSize, Consumer<Object> elementAssertion) {
		assertCollectionnResult(result, expectedSize, Set.class, elementAssertion);
	}

	private void assertCollectionnResult(Object result, int expectedSize, Class<?> collectionClass, Consumer<Object> elementAssertion) {
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(collectionClass);
		Collection<?> resultList = (Collection<?>) result;
		assertThat(resultList.size()).isEqualTo(expectedSize);
		
		//@formatter:off
		resultList
			.stream()
			.forEach(elementAssertion);
		//@formatter:on
	}

	private Person assertIsPerson(Object o) {
		assertThat(o).isInstanceOf(Person.class);
		return (Person) o;
	}

	private void assertPerson(Object o, String expectedLastName) {
		Person p = assertIsPerson(o);
		assertThat(p).isNotNull();
		assertThat(p.getName()).isNotNull();
		assertThat(p.getName().getLast()).isEqualTo(expectedLastName);
	}

	private BasicPersistenceGmSession prepareSession() {
		Smood x = new Smood(EmptyReadWriteLock.INSTANCE);
		x.setMetaModel(new NewMetaModelGeneration().buildMetaModel("gm:VdeTestModel", asList(Person.T)));

		BasicPersistenceGmSession session = new BasicPersistenceGmSession(x);
		
		person(session, 1, "John", "Doe");
		person(session, 2, "Jane", "Doe");
		
		session.commit();
		return session;
	}

	private Person person(PersistenceGmSession session, long id, String firstName, String lastName) {
		Name name = session.create(Name.T);
		name.setFirst(firstName);
		name.setLast(lastName);
		
		Person p = session.create(Person.T);
		p.setId(id);
		p.setName(name);
		return p;
	}

	
}
