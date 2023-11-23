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
package tribefire.platform.impl.gmql;

import static tribefire.platform.impl.gmql.GmqlProcessorTestCommons.createPerson;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.query.api.shortening.QueryShorteningRuntimeException;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.testing.model.test.demo.person.Person;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.platform.impl.gmql.wire.GmqlProcessorTestWireModule;
import tribefire.platform.impl.gmql.wire.contract.GmqlProcessorTestContract;

public class GmqlProcessorTest {

	protected WireContext<GmqlProcessorTestContract> context;
	protected Evaluator<ServiceRequest> evaluator;
	protected GmqlProcessorTestContract testContract;

	@Before
	public void setup() {
		context = Wire.context(GmqlProcessorTestWireModule.INSTANCE);
		testContract = context.contract();
		evaluator = testContract.evaluator();
		assertSetUpPerson();
	}
	
	@After
	public void after() {
		context.shutdown();
	}
	

	private <T extends QueryResult> T evaluate(GmqlRequest request) {
		return (T) request.eval(evaluator).get();
	}

	@Test
	public void testStatement() {
		GmqlRequest request = getRequest("select p from " + Person.class.getName() + " p");
		// when
		SelectQueryResult response = evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getResults();

		// assert
		Assert.assertNotNull(persons);
		Assertions.assertThat(persons.get(0).getFirstName()).isEqualTo("Foo");
	}

	@Test
	public void testStatementWithEntitySimpleName() {

		// give
		GmqlRequest request = getRequest("select p from Person p");
		SelectQueryResult response = evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getResults();

		// assert
		Assert.assertNotNull(persons);
		Assertions.assertThat(persons.get(0).getFirstName()).isEqualTo("Foo");
	}

	@Test(expected = QueryShorteningRuntimeException.class)
	public void testStatementWithEntitySimpleNameNotExists() {

		// give
		GmqlRequest request = getRequest("select p from FooBar p");
		SelectQueryResult response = evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getResults();

		// assert
		Assert.assertNotNull(persons);
		Assertions.assertThat(persons.get(0).getFirstName()).isEqualTo("Foo");
	}

	@Test
	public void testSelectQueryResultMultipleEntities() {
		Person bar = createPerson("Bar");
		testContract.smood().registerEntity(bar, true);

		// give
		GmqlRequest request = getRequest("select p from " + Person.class.getName() + " p");

		// when
		SelectQueryResult response = evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getResults();

		// assert
		Assert.assertNotNull(persons);
		Assert.assertEquals(2, persons.size());
	}

	@Test
	public void testEntityQueryResultIdProperty() {
		// give
		GmqlRequest request = getRequest("from " + Person.class.getName() + " p where p.id = 1L");
		
		// when
		EntityQueryResult response = evaluate(request);
		
		// when
		List<Person> persons = (List<Person>) (List<?>) response.getEntities();

		// assert
		Assert.assertNotNull(persons);
		Assertions.assertThat(persons.get(0).getFirstName()).isEqualTo("Foo");
	}

	@Test
	public void testEntityQueryResultIdPropertySimpleName() {
		// give
		GmqlRequest request = getRequest("from Person p where p.id = 1L");
		// when
		EntityQueryResult response = evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getEntities();

		// assert
		Assert.assertNotNull(persons);
		Assertions.assertThat(persons.get(0).getFirstName()).isEqualTo("Foo");
	}

	@Test
	public void testEntityQueryResultIdPropertyNotExistingId() {
		// when
		// give
		GmqlRequest request = getRequest("from " + Person.class.getName() + " p where p.id = 9L");

		// when
		EntityQueryResult response = evaluate(request);
		// assert
		Assert.assertEquals(0, response.getEntities().size());
	}

	@Test
	public void testEntityQueryResultStringProperty() {
		// give
		GmqlRequest request = getRequest("from " + Person.class.getName() + " p where p.firstName like 'Foo'");

		// when
		EntityQueryResult response = evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getEntities();

		// assert
		Assert.assertNotNull(persons);
		Assertions.assertThat(persons.get(0).getFirstName()).isEqualTo("Foo");
	}

	@Test
	public void testEntityQueryResultStringPropertyNoResults() {
		// give
		GmqlRequest request = getRequest("from " + Person.class.getName() + " p where p.firstName like 'NoEXists'");

		// when
		EntityQueryResult response = evaluate(request);
		// assert
		Assert.assertEquals(0, response.getEntities().size());
	}

	@Test
	public void testPropertyQueryResult() {
		// give

		GmqlRequest request = getRequest("property firstName of reference(" + Person.class.getName() + ", 1L)");

		// when
		PropertyQueryResult response = (PropertyQueryResult) evaluate(request);

		// assert
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getPropertyValue());
		Assert.assertEquals("Foo", response.getPropertyValue());
	}

	@Test
	public void testPropertyQueryResultSimpleName() {
		// give

		GmqlRequest request = getRequest("property firstName of reference(Person, 1L)");

		// when
		PropertyQueryResult response = (PropertyQueryResult) evaluate(request);

		// assert
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getPropertyValue());
		Assert.assertEquals("Foo", response.getPropertyValue());
	}

	@Test(expected = NotFoundException.class)
	public void testPropertyQueryResultNotFoundException() {
		// give
		GmqlRequest request = getRequest("property firstName of reference(" + Person.class.getName() + ", 9L)");

		// when
		evaluate(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncorrectGmqlStatement() {

		GmqlRequest request = createRequest();
		request.setStatement("select a from " + Person.class.getName() + " p");

		// when
		evaluate(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIncorrectStartOfQueryStatement() {
		// give

		GmqlRequest request = createRequest();
		request.setStatement("somethingelse p from " + Person.class.getName() + " p");

		// when
		evaluate(request);
	}

	@Test
	public void testGmqlStatementNoResults() {
		// give

		GmqlRequest request = getRequest("select p from " + Person.class.getName() + " p");

		// when
		SelectQueryResult response = (SelectQueryResult) evaluate(request);
		List<Person> persons = (List<Person>) (List<?>) response.getResults();

		// assert
		Assert.assertNotNull(persons);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRequestMissingServiceDomain() {

		// when
		GmqlRequest request = GmqlRequest.T.create();
		evaluate(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRequestMissingStatement() {
		// when
		GmqlRequest request = getRequest("");
		evaluate(request);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRequestStatementNull() {
		// when
		GmqlRequest request = getRequest(null);
		evaluate(request);
	}

	private void assertSetUpPerson() {
		Person person1 = testContract.smood().getEntitiesPerType(Person.T).iterator().next();
		Person person2 = testContract.smood().getEntity(Person.T, person1.getId());
		// assert
		Assertions.assertThat(person2).isEqualTo(person1);
		Assertions.assertThat(person2.getFirstName()).isEqualTo("Foo");

		Object person3 = testContract.smood().getEntity(person1.reference());
		// assert
		Assertions.assertThat(person3).isEqualTo(person1);
	}

	private GmqlRequest getRequest(String statement) {
		GmqlRequest request = createRequest();
		request.setStatement(statement);
		return request;
	}
	private GmqlRequest createRequest() {
		GmqlRequest request = GmqlRequest.T.create();
		request.setAccessId("test.access");
		return request;
	}

}
