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
package com.braintribe.model.processing.session.impl.persistence;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.enhance.ManipulationTrackingPropertyAccessInterceptor;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.notifying.interceptors.ManipulationTracking;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Tests for BasicPersistenceGmSession
 */
public class BasicPersistenceGmSession_ExistingPopulation_Test {

	SmoodAccess smoodAccess;
	BasicPersistenceGmSession session;

	@Before
	public void setup() throws Exception {
		smoodAccess = GmTestTools.newSmoodAccessMemoryOnly("testAccess", MetaModelTools.provideRawModel(Person.T, Flag.T));

		session = new BasicPersistenceGmSession(smoodAccess);

		preparePersons();
	}

	/** There was a bug one day that a case like this would not work... */
	@Test
	public void collectionPropertyHandling() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("p1").done();
		Person p1 = session.query().entities(query).unique();

		for (Person friend : p1.getFriendSet()) {
			assertThat(friend.<Object> getId()).as("id was not set automatically for collection values").isNotNull();
		}
	}

	@Test
	public void settingPropertyToCurrentValueNotTracked_IfPaiConfigured() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("p1").done();
		Person p1 = session.query().entities(query).unique();

		assertThat(session.getTransaction().getManipulationsDone()).isEmpty();
		
		// set the right pai
		ManipulationTrackingPropertyAccessInterceptor goodPai = new ManipulationTrackingPropertyAccessInterceptor();
		goodPai.ignoredNoChangeAssignments = true;
		session.interceptors().replace(ManipulationTracking.class, goodPai);
		
		p1.setName("p1");
		
		Person.T.getProperty("name").setVd(p1, GMF.absenceInformation());
		
		assertThat(session.getTransaction().getManipulationsDone()).isEmpty();
	}

	@Test
	public void entityQuery_List() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("p1").done();
		List<Person> persons = session.query().entities(query).list();

		assertThat(persons.size()).as("One resulting person expected.").isEqualTo(1);
	}

	@Test
	public void entityQuery_First() throws Exception {
		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("p1").done();
		Person person = session.query().entities(query).first();

		assertThat(person).as("No person found.").isNotNull();
	}

	@Test
	public void selectQuery() throws Exception {
		SelectQuery query = new SelectQueryBuilder().from(Person.T, "p").where().property("p", "name").eq("p1").done();
		List<Person> persons = session.query().select(query).list();

		assertThat(persons.size()).as("One resulting person expected.").isEqualTo(1);
	}

	@Test
	public void propertyQuery() throws Exception {
		PropertyQuery query = PropertyQueryBuilder.forProperty(getP1().reference(), "friendSet").done();
		HashSet<Person> persons = session.query().property(query).value();

		assertThat(persons.size()).as("Two best friends expected.").isEqualTo(2);
	}

	@Test
	public void conditionalPropertyQuery() throws Exception {
		PropertyQuery query = PropertyQueryBuilder.forProperty(getP1().reference(), "friendSet").where().property("name").eq("p2").done();
		Set<Person> persons = session.query().property(query).value();

		assertThat(persons.size()).as("One best friend expected with name 'p2'.").isEqualTo(1);
	}

	/** Test all kind of queries using the generic way to pass a Query to the withQuery method of the session. **/
	@Test
	public void variousQueries() throws Exception {
		// test passing EntityQuery
		EntityQuery eq = EntityQueryBuilder.from(Person.T).where().property("name").eq("p1").done();
		List<Person> eqr = session.query().abstractQuery(eq).list();

		assertThat(eqr).hasSize(1);

		// test passing PropertyQuery
		PropertyQuery pq = PropertyQueryBuilder.forProperty(getP1().reference(), "friendSet").done();
		Set<Person> pqr = session.query().abstractQuery(pq).value();

		assertThat(pqr).hasSize(2);

		// test passing SelectQuery
		SelectQuery sq = new SelectQueryBuilder().from(Person.T, "p").where().property("p", "name").eq("p1").done();
		List<Person> sqr = session.query().abstractQuery(sq).list();

		assertThat(sqr).hasSize(1);
	}

	@Test
	public void stringBasedSelectQueryWithVariables() throws Exception {
		String query = "select p from " + Person.T.getTypeSignature() + " p where p.name = :personName";
		List<Person> persons = session.query().select(query).setVariable("personName", "p1").list();

		assertThat(persons.size()).as("One resulting person expected.").isEqualTo(1);
	}

	@Test
	public void stringBasedEntityQueryWithVariables() throws Exception {
		String query = "from " + Person.T.getTypeSignature() + " p where p.name = :name";
		List<Person> persons = session.query().entities(query).setVariable("name", "p1").value();

		assertThat(persons.size()).as("One person with name 'p1' expected.").isEqualTo(1);
	}

	private Person getP1() throws Exception {
		return queryByName("p1");
	}

	private void preparePersons() throws GmSessionException {
		Person p1 = newPerson("p1");
		Person p2 = newPerson("p2");
		Person p3 = newPerson("p3");
		p1.setFriendSet(asSet(p2, p3));
		session.commit();
	}

	private <T extends HasName> T queryByName(String name) throws GmSessionException {
		EntityQuery query = EntityQueryBuilder.from(HasName.T).where().property("name").eq(name).done();
		return session.query().entities(query).first();
	}

	private Person newPerson(String name) {
		Person p = session.create(Person.T);
		p.setName(name);
		return p;
	}

}
