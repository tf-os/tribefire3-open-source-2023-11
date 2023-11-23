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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.processing.deployment.processor.bidi.data.Company;
import com.braintribe.model.processing.deployment.processor.bidi.data.Person;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class BidiStateChangeProcessor_N_N_Test extends AbstractBidiScpTests {

	// ######################################
	// ## . . . adding to collection . . . ##
	// ######################################

	@Test
	public void worksForNewEntities() throws Exception {
		apply(session -> {
			Company company = newCompany(session);

			company.getOwnersFriends().add(newPerson(session));
			company.getOwnersFriends().add(newPerson(session));
		});

		assertCompanyWithTwoOwnersFriends();
	}

	@Test
	public void worksWithBothExisting() throws Exception {
		prepare(session -> {
			newPerson(session);
			newCompany(session);
		});

		apply(session -> {
			Company company = queryFirst(Company.class, session);
			Person person = queryFirst(Person.class, session);

			company.getOwnersFriends().add(newPerson(session));
			company.getOwnersFriends().add(person);
		});

		assertCompanyWithTwoOwnersFriends();
	}

	private void assertCompanyWithTwoOwnersFriends() {
		assertLinked(queryAll(Person.class), queryAll(Company.class));
	}

	// ######################################
	// ## . . . clearing collection . . . .##
	// ######################################

	@Test
	public void clearingCollectionWorks() throws Exception {
		prepareCompanyWithTwoOwnersFriends();

		apply(session -> {
			Company company = queryFirst(Company.class, session);
			company.getOwnersFriends().clear();
		});

		for (Person person : queryAll(Person.class)) {
			BtAssertions.assertThat(person.getFriendsCompanies()).isEmpty();
		}
	}

	@Test
	public void settingEmptyCompanyWorks() throws Exception {
		prepareCompanyWithTwoOwnersFriends();

		apply(session -> {
			Company company = queryFirst(Company.class, session);
			company.setOwnersFriends(new HashSet<Person>());
		});

		for (Person person : queryAll(Person.class)) {
			BtAssertions.assertThat(person.getFriendsCompanies()).isEmpty();
		}

	}

	// ######################################
	// ## . . removing from collection . . ##
	// ######################################

	@Test
	public void removingFromCollection() throws Exception {
		prepareCompanyWithTwoOwnersFriends();

		apply(session -> {
			Company company = queryFirst(Company.class, session);
			Person person = personByName("p1", session);

			assertTrue(company.getOwnersFriends().remove(person));
		});

		Person p1 = personByName("p1");
		Person p2 = personByName("p2");
		Company c = queryFirst(Company.class);

		assertLinked(Arrays.asList(p2), Arrays.asList(c));
		BtAssertions.assertThat(p1.getFriendsCompanies()).isEmpty();
	}

	@Test
	public void reassignExistingValue() throws Exception {
		prepareCompanyWithTwoOwnersFriends();

		// Create request with add of an element that is already there
		// we remove+add, so it's tracked, and then remove the RemoveManipulation  
		ManipulationRequest mr = manipulationDriver.dryRunAsRequest(session -> {
			Person p = personByName("p1", session);
			Set<Company> fcs = p.getFriendsCompanies();
			Company c = first(fcs);
			fcs.remove(c);
			fcs.add(c);
		});

		// Removing the RemoveManipulation
		((CompoundManipulation) mr.getManipulation()).getCompoundManipulationList().remove(0);

		apply(mr);

		assertCompanyWithTwoOwnersFriends();
	}

	// ######################################
	// ## . . . . . . helpers . . . . . . .##
	// ######################################

	private void prepareCompanyWithTwoOwnersFriends() {
		prepare(session -> {
			Person p1 = newPerson(session, "p1");
			Person p2 = newPerson(session, "p2");
			Company c = newCompany(session);

			c.getOwnersFriends().add(p1);
			c.getOwnersFriends().add(p2);
			p1.getFriendsCompanies().add(c);
			p2.getFriendsCompanies().add(c);
		});
	}

	private void assertLinked(List<Person> persons, List<Company> companies) {
		for (Company company : companies) {
			assertEquals(persons.size(), company.getOwnersFriends().size());

			assertTrue(company.getOwnersFriends().containsAll(persons));

			for (Person p : persons)
				assertTrue(p.getFriendsCompanies().contains(company));
		}
	}
}
