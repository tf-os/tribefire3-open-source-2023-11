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
package com.braintribe.model.processing.deployment.processor.bidi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.processing.deployment.processor.bidi.data.Company;
import com.braintribe.model.processing.deployment.processor.bidi.data.Person;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class BidiStateChangeProcessor_1_N_Test extends AbstractBidiScpTests {

	// ######################################
	// ## . . . adding to collection . . . ##
	// ######################################

	@Test
	public void worksForNewEntities() throws Exception {
		apply(session -> {
			Company company = newCompany(session);
			Person p1 = newPerson(session);
			Person p2 = newPerson(session);

			p1.setEmployerCompany(company);
			p2.setEmployerCompany(company);

			p1.setEmployerCompanyList(company);
			p2.setEmployerCompanyList(company);
		});

		assertCompanyWithTwoPersons();
	}

	@Test
	public void worksWithBothExisting() throws Exception {
		prepare(session -> {
			newPerson(session, "p1");
			newPerson(session, "p2");
			newCompany(session);
		});

		apply(session -> {
			Company company = queryFirst(Company.class, session);
			Person p1 = personByName("p1", session);
			Person p2 = personByName("p2", session);

			p1.setEmployerCompany(company);
			p2.setEmployerCompany(company);

			p1.setEmployerCompanyList(company);
			p2.setEmployerCompanyList(company);
		});

		assertCompanyWithTwoPersons();
	}

	@Test
	public void reassignExistingValue() throws Exception {
		prepareCompanyWithTwoEmployees();

		apply(session -> {
			Person p1 = personByName("p1", session);
			p1.setEmployerCompany(p1.getEmployerCompany());
		});

		assertCompanyWithTwoPersons();
	}

	private void assertCompanyWithTwoPersons() {
		assertLinked(queryAll(Person.class), queryFirst(Company.class));
	}

	// ######################################
	// ## . . removing from collection . . ##
	// ######################################

	@Test
	public void removingFromCollection() throws Exception {
		prepareCompanyWithTwoEmployees();

		apply(session -> {
			Person p1 = personByName("p1", session);
			p1.setEmployerCompany(null);
			p1.setEmployerCompanyList(null);
		});

		Person p2 = personByName("p2");
		Company c = queryFirst(Company.class);

		assertLinked(Arrays.asList(p2), c);
	}

	@Test
	public void removingAllInstancesFromList() throws Exception {
		prepareCompanyWithTwoEmployees();

		prepare(session -> {
			Person p1 = personByName("p1", session);
			Company c = companyByName("c", session);

			c.getEmployeeList().add(p1);
		});

		apply(session -> {
			Person p1 = personByName("p1", session);
			p1.setEmployerCompany(null);
			p1.setEmployerCompanyList(null);
		});

		Person p2 = personByName("p2");
		Company c = queryFirst(Company.class);

		assertLinked(Arrays.asList(p2), c);
	}

	@Test
	public void linkingWithNewUnlinksOld_List() throws Exception {
		prepare(session -> {
			Person p = newPerson(session, "p");
			Company c1 = newCompany(session, "c1");
			@SuppressWarnings("unused")
			Company c2 = newCompany(session, "c2");

			p.setEmployerCompanyList(c1);
			c1.getEmployeeList().add(p);
		});

		apply(session -> {
			Person p = personByName("p", session);
			Company c2 = companyByName("c2", session);

			c2.getEmployeeList().add(p);
		});

		Person p = personByName("p");
		Company c1 = companyByName("c1");
		Company c2 = companyByName("c2");

		BtAssertions.assertThat(p.getEmployerCompanyList()).isEqualTo(c2);
		BtAssertions.assertThat(c1.getEmployeeList()).isEmpty();
		BtAssertions.assertThat(c2.getEmployeeList()).containsOnly(p);
	}

	@Test
	public void linkingWithNewUnlinksOld_Set() throws Exception {
		prepare(session -> {
			Person p = newPerson(session, "p");
			Company c1 = newCompany(session, "c1");
			@SuppressWarnings("unused")
			Company c2 = newCompany(session, "c2");

			p.setEmployerCompany(c1);
			c1.getEmployeeSet().add(p);
		});

		apply(session -> {
			Person p = personByName("p", session);
			Company c2 = companyByName("c2", session);

			c2.getEmployeeSet().add(p);
		});

		Person p = personByName("p");
		Company c1 = companyByName("c1");
		Company c2 = companyByName("c2");

		BtAssertions.assertThat(p.getEmployerCompany()).isEqualTo(c2);
		BtAssertions.assertThat(c1.getEmployeeSet()).isEmpty();
		BtAssertions.assertThat(c2.getEmployeeSet()).containsOnly(p);
	}

	// ######################################
	// ## . . . . . . helpers . . . . . . .##
	// ######################################

	private void prepareCompanyWithTwoEmployees() {
		prepare(session -> {
			Person p1 = newPerson(session, "p1");
			Person p2 = newPerson(session, "p2");
			Company c = newCompany(session, "c");

			p1.setEmployerCompany(c);
			p2.setEmployerCompany(c);

			p1.setEmployerCompanyList(c);
			p2.setEmployerCompanyList(c);

			addEmployee(c, p1);
			addEmployee(c, p2);
		});
	}

	private void addEmployee(Company c, Person p) {
		c.getEmployeeSet().add(p);
		c.getEmployeeList().add(p);
	}

	private void assertLinked(List<Person> employees, Company company) {
		assertEquals(employees.size(), company.getEmployeeSet().size());
		assertEquals(employees.size(), company.getEmployeeList().size());

		assertTrue(company.getEmployeeSet().containsAll(employees));
		BtAssertions.assertThat(company.getEmployeeList()).containsOnly(employees.toArray());
		assertTrue(company.getEmployeeList().containsAll(employees));

		for (Person p : employees) {
			assertEquals(company, p.getEmployerCompany());
			assertEquals(company, p.getEmployerCompanyList());
		}
	}
}
