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
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Tests for BasicPersistenceGmSession
 */
public class RefreshBasicPersistenceGmSessionTest {

	BasicPersistenceGmSession session1;
	BasicPersistenceGmSession session2;

	private static final String TOP = "top";
	private static final String BEST_FRIEND = "best-friend";
	private static final String FRIEND1 = "friend1";
	private static final String FRIEND2 = "friend2";

	@Before
	public void setup() throws Exception {
		SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly("refresh.test.access", MetaModelTools.provideRawModel(Person.T, Flag.T));

		session1 = new BasicPersistenceGmSession(access);
		session2 = new BasicPersistenceGmSession(access);

		preparePersons();
	}

	@Test
	public void refreshSimpleProperty() throws Exception {
		Person top1 = queryByName(TOP, session1);

		Person top2 = queryByName(TOP, session2);
		top2.setName("top2");
		session2.commit();

		session1.query().entity(top1).refresh();
		assertThat(top1.getName()).isEqualTo("top2");
	}

	@Test
	public void refreshEntityProperty() throws Exception {
		Person top1 = queryByName(TOP, session1);
		assertThat(top1.getBestFriend()).isNotNull();
		
		Person top2 = queryByName(TOP, session2);
		top2.setBestFriend(null);
		session2.commit();
		
		session1.query().entity(top1).refresh();
		assertThat(top1.getBestFriend()).isNull();

		session1.query().entity(top1).refresh();
		assertThat(top1.getBestFriend()).isNull();
	}
	
	private <T extends HasName> T queryByName(String name, PersistenceGmSession session) throws GmSessionException {
		EntityQuery query = EntityQueryBuilder.from(HasName.T).where().property("name").eq(name).done();
		return session.query().entities(query).first();
	}

	private void preparePersons() throws GmSessionException {
		Person top = newPerson(TOP);
		top.setBestFriend(newPerson(BEST_FRIEND));
		top.setFriendSet(asSet(newPerson(FRIEND1), newPerson(FRIEND2)));

		session1.commit();
	}

	private Person newPerson(String name) {
		Person p = session1.create(Person.T);
		p.setName(name);
		return p;
	}

}
