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

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.query.EntityQuery;

/**
 * Tests for BasicPersistenceGmSession
 */
public class TransientPersistenceGmSessionTest {

	TransientPersistenceGmSession session;

	@Before
	public void setup() throws Exception {
		session = new TransientPersistenceGmSession();

		preparePersons();
	}
	
	@Test
	public void testTransientQuery() throws Exception {
		
		String name = "p1";
		Person p1 = queryPerson(name);
		assertThat(p1.getName()).isEqualTo(name);
		
	}


	protected Person getP1() throws Exception {
		return session.query().entity(Person.T, new Long(1)).require();
	}

	protected PersistentEntityReference getP1Reference() throws Exception {
		return (PersistentEntityReference) getP1().reference();
	}

	protected void preparePersons() throws GmSessionException {
		Person p1 = newPerson("p1");
		Person p2 = newPerson("p2");
		Person p3 = newPerson("p3");
		p1.setFriendSet(new HashSet<Person>(Arrays.asList(p2, p3)));
	}

	private Person queryPerson(String name) throws GmSessionException {
		EntityQuery query = EntityQueryBuilder.from(Person.class).where().property("name").eq(name).done();
		return session.query().entities(query).first();
	}

	protected Person newPerson(String name) {
		Person p = session.create(Person.T);
		p.setName(name);
		return p;
	}

}
