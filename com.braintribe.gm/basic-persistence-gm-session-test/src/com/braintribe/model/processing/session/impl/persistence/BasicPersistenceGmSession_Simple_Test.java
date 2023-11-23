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

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.AbstractAccess;
import com.braintribe.model.access.AccessBase;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Tests for BasicPersistenceGmSession
 */
public class BasicPersistenceGmSession_Simple_Test {

	SmoodAccess smoodAccess;
	BasicPersistenceGmSession session;

	@Before
	public void setup() throws Exception {
		smoodAccess = GmTestTools.newSmoodAccessMemoryOnly("testAccess", MetaModelTools.provideRawModel(Person.T, Flag.T));

		renewSession();
	}

	@Test
	public void creationAwareness() throws Exception {
		Person p = session.create(Person.T);
		assertCreated(p, true, true);

		session.getTransaction().undo(1);
		assertCreated(p, true, false);

		session.getTransaction().redo(1);
		assertCreated(p, true, true);

		session.deleteEntity(p);
		assertCreated(p, true, false);

		session.getTransaction().undo(1);
		assertCreated(p, true, true);

		p.setId(1L);
		assertCreated(p, true, true);

		session.commit();
		assertCreated(p, false, false);
	}

	private void assertCreated(Person p, boolean created, boolean willPersist) {
		assertThat(session.getTransaction().created(p)).isEqualTo(created);
		assertThat(session.getTransaction().willPersist(p)).isEqualTo(willPersist);
	}

	@Test
	public void entityQueryCache() throws Exception {
		newPerson("p4");

		EntityQuery query = EntityQueryBuilder.from(Person.T).where().property("name").eq("p4").done();

		Person person = session.query().entities(query).first();
		assertThat(person).isNull();

		person = session.queryCache().entities(query).first();
		assertThat(person).isNotNull();

		session.commit();

		person = session.query().entities(query).first();
		assertThat(person).isNotNull();
	}

	@Test
	public void deletion() throws Exception {
		newPerson("p4");
		session.commit();

		Person p = queryByName("p4");
		assertThat(p).isNotNull();

		session.deleteEntity(p);

		p = queryByName("p4");
		assertThat(p).isNull();
	}

	@Test
	public void deletionIgnoringReferences_WorksIfNotReferenced() throws Exception {
		Person p4 = newPerson("p4");
		session.commit();

		renewSession();

		p4 = queryByName("p4");
		assertThat(p4).isNotNull();

		session.deleteEntity(p4, DeleteMode.ignoreReferences);
		session.commit();
	}

	@Test
	public void deletionIgnoringReferencesFailsIfReferenced() throws Exception {
		Person p4 = newPerson("p4");
		Person p5 = newPerson("p5");
		p4.setBestFriend(p5);
		session.commit();

		renewSession();

		p5 = queryByName("p5");
		assertThat(p5).isNotNull();

		try {
			session.deleteEntity(p5, DeleteMode.ignoreReferences);
			session.commit();
			fail("Exception was expected, p5 cannot be deleted as it is referenced by p4.bestFriend!!!");

		} catch (Exception e) {
			// empty
		}
	}

	@Test
	public void instantiationRollback() throws Exception {
		NestedTransaction transaction = session.getTransaction().beginNestedTransaction();
		Person p = session.create(Person.T);

		assertThat(p.session()).isSameAs(session);

		transaction.rollback();

		assertThat(p.session()).isNull();
	}

	@Test
	public void initializedValueIsPersisted() throws Exception {
		session.create(Flag.T, "flag1");
		session.commit();

		Flag flag = smoodAccess.getDatabase().findEntityByGlobalId("flag1");
		assertThat(flag.getInitializedValue()).isTrue();
	}

	@Test
	public void findLocalOrBuildShallow() throws Exception {
		Person p1 = (Person) session.query().entity(Person.T.getTypeSignature(), 99L).findLocalOrBuildShallow();
		assertThat(p1).isNotNull();

		// @formatter:off
		EntityQuery eq = EntityQueryBuilder
				.from(Person.T)
				.where()
					.property("id").eq(99L)
			.done();
		// @formatter:on

		// If findLocalOrBuildShallow fails, this will not be found
		Person p2 = session.queryCache().entities(eq).first();
		assertThat(p2).isSameAs(p1);

		// Now let's test that setting the absent property doesn't destroy it
		p2.setGlobalId("p-1");

		Person p3 = session.queryCache().findEntity("p-1");
		assertThat(p3).isSameAs(p2);
	}

	/**
	 * There was a bug - there was no default TC for an Object property, so a method returned null, which was interpreted as if "default" placeholder
	 * was not known. Fixed by applying the default TC for GenericEntity in that case, in case of {@link AbstractAccess}. The new and unused
	 * {@link AccessBase} would pick TC based on the type of the value.
	 */
	@Test
	public void queryObjectPropertyWithDefaultTC() throws Exception {
		Person otherP = newPerson("otherP");

		Person p = newPerson("p");
		p.setObject(otherP);
		session.commit();

		PropertyQuery pq = PropertyQueryBuilder.forProperty(Person.T, p.getId(), "object").tc(TC.placeholder("default")).done();
		Object o = session.query().property(pq).value();

		assertThat(o).isEqualTo(otherP);
	}

	@Test
	public void malformedStringQuery() throws Exception {
		String query = "select p fom " + Person.class.getName() + " where p.name = 'p1'"; // from keyword spelled wrong

		String message = null;
		try {
			session.query().select(query).list();
		} catch (GmSessionRuntimeException e) {
			message = e.getMessage();
		}

		assertThat(message).as("No exception thrown.").isNotNull();
		assertThat(message).as("Unexpected error message").contains("could not be parsed to a valid query");

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

	private BasicPersistenceGmSession renewSession() {
		return session = new BasicPersistenceGmSession(smoodAccess);
	}

}
