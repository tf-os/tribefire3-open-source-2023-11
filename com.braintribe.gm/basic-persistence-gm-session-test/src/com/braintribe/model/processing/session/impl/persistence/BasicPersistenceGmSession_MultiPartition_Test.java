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

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.model.tools.MetaModelTools;
import com.braintribe.model.processing.session.test.data.Flag;
import com.braintribe.model.processing.session.test.data.Person;
import com.braintribe.testing.tools.gm.GmTestTools;

/**
 * Tests for BasicPersistenceGmSession
 */
public class BasicPersistenceGmSession_MultiPartition_Test {

	SmoodAccess smoodAccess;
	BasicPersistenceGmSession session;

	@Before
	public void setup() throws Exception {
		smoodAccess = GmTestTools.newSmoodAccessMemoryOnly("testAccess", MetaModelTools.provideRawModel(Person.T, Flag.T));
		smoodAccess.setPartitions(asSet("p1", "p2"));

		session = new BasicPersistenceGmSession(smoodAccess);
	}

	/**
	 * There was a bug that internally in the session this would lead to an EntityQuery with a condition "where
	 * partition = '*'", because that's the value of the reference created internally when no partition is given (see
	 * {@link EntityReference#ANY_PARTITION}).
	 * <p>
	 * This was then fix to not include the partition in the query when this is the actual value, assuming the
	 * underlying access would not need the value anyway.
	 * <p>
	 * A better fix would probably be to just create a query like this:
	 * {@code select e from <ref.typeSignature> where e = ref}. Will probably be done in a future version of TF, don't
	 * want to break stuff right now.
	 */
	@Test
	public void refresh() throws Exception {
		Person p = createPerson("John", "p1");
		session.commit();

		Person p2 = (Person) session.query().entity(p.reference()).refresh();
		assertThat(p2).isSameAs(p);

		// Assuming a Person can only have partition "p1", this should work properly
		Person p3 = session.query().entity(Person.T, p.getId()).refresh();
		assertThat(p3).isSameAs(p);
	}

	private Person createPerson(String name, String partition) {
		Person p = session.create(Person.T);
		p.setName(name);
		p.setPartition(partition);
		return p;
	}

}
