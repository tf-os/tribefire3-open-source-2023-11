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
package tribefire.extension.simple.deployables.access;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;

import tribefire.extension.simple.model.data.Company;
import tribefire.extension.simple.model.data.Person;

/**
 * Provides tests for {@link SimpleInMemoryAccess}.<br>
 * The main purpose of this class is to demonstrate how to easily write unit tests for an access.
 *
 * @author michael.lafite
 */
public class SimpleInMemoryAccessTest {

	/**
	 * Creates a new {@link SimpleInMemoryAccess} initialized with example data. Afterwards verifies that the example data is correct (by doing some
	 * queries). Finally the test also performs some manipulations and then again verifies the result via queries.
	 */
	@Test
	public void test() throws GmSessionException {
		// create access and example data
		SimpleInMemoryAccess access = new SimpleInMemoryAccess();
		access.setInitializeWithExampleData(true);
		access.postConstruct();

		// use a session to work with the access
		// this is a) more convenient and b) the same code can be used in an integration test
		PersistenceGmSession session = new BasicPersistenceGmSession(access);

		// query for company
		Company company = session.query().entities(EntityQueryBuilder.from(Company.class).done()).unique();
		// check some properties
		assertThat(company).isNotNull().hasPropertyValue(Company.name, "Acme").hasPropertyValue(Company.averageRevenue, new BigDecimal("1234567890"));
		assertThat(company.getCeo().getFirstName()).isEqualTo("Jack");

		// query for person entities and check results via comparison of first names
		assertThat(session.query().entities(EntityQueryBuilder.from(Person.class).done()).list()).hasSize(4).extracting(Person.firstName)
				.containsExactlyInAnyOrder("Jack", "Jim", "Jane", "John");

		// create a new person and persist it
		Person person = session.create(Person.T);
		person.setFirstName("Joan");
		session.commit();

		// query again and verify that new person was persisted successfully
		PersistenceGmSession otherSession = new BasicPersistenceGmSession(access);
		assertThat(otherSession.query().entities(EntityQueryBuilder.from(Person.class).done()).list()).hasSize(5).extracting(Person.firstName)
				.containsExactlyInAnyOrder("Jack", "Jim", "Jane", "John", "Joan");
	}

}
