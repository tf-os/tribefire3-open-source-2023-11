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

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.model.processing.session.test.data.SpecialPerson;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Tests for BasicPersistenceGmSession
 */
public class AdvancedBasicPersistenceGmSessionTest {

	private static final String ACCESS_ID = "test";

	Smood smood;
	BasicPersistenceGmSession session;

	Function<EntityType<?>, GenericEntity> constructor = EntityType::create;

	@Before
	public void setup() throws Exception {
		GmMetaModel model = MetaModelTools.provideRawModel(Person.T, SpecialPerson.T, Flag.T);
		SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly(ACCESS_ID, model);

		smood = access.getDatabase();
		session = new BasicPersistenceGmSession(access);
	}

	@Test
	public void resolvingEntityWhenIdNotUniqueInHierarchy() throws Exception {
		// Create instance of Person and it's sub-type SpecialPerson
		Person p1 = newPerson("p1");
		SpecialPerson sp1 = newSpecialPerson("sp1");

		// set the same id to both
		p1.setId(1l);
		sp1.setId(1L);

		smood.initialize(asList(p1, sp1));

		// get the person by reference (this was throwing an exception before the bug was fixed)
		Person p = (Person) session.query().entity(p1.reference()).find();

		assertThat(p instanceof SpecialPerson).isFalse();
	}

	@Test
	public void requireDoesUpdateEntityWhenNotAttachedYet() throws Exception {
		// 1:
		// Create Person and initialize Smood with it
		Person accessPerson = newPerson("Original Name");
		accessPerson.setId("id-1");
		accessPerson.setPartition(ACCESS_ID);

		smood.initialize(accessPerson);

		// 2:
		// Query Person, get the copy of what is in the access
		Person sessionPerson = session.query().entity(accessPerson).require();

		assertThat(accessPerson).isNotSameAs(sessionPerson);
		assertThat(accessPerson.getName()).isEqualTo(sessionPerson.getName());

		// 3:
		// Change the value in the access, note that require will not update our sessionPerson
		accessPerson.setName("Changed Name");

		sessionPerson = session.query().entity(accessPerson).require();
		assertThat(sessionPerson.getName()).isEqualTo("Original Name");

		// 4:
		// refresh() will now update our sessionPerson
		sessionPerson = session.query().entity(accessPerson).refresh();
		assertThat(sessionPerson.getName()).isEqualTo("Changed Name");
	}

	protected Person newPerson(String name) {
		Person p = newInstance(Person.T);
		p.setName(name);
		return p;
	}

	protected SpecialPerson newSpecialPerson(String name) {
		SpecialPerson p = newInstance(SpecialPerson.T);
		p.setName(name);
		return p;
	}

	private <T extends GenericEntity> T newInstance(EntityType<T> et) {
		return (T) constructor.apply(et);
	}

}
