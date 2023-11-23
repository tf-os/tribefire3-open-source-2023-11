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
package com.braintribe.model.processing.vde.impl.root;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SessionAspect;
import com.braintribe.model.processing.vde.evaluator.impl.root.EntityReferenceVde;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.model.processing.vde.impl.misc.Person;
import com.braintribe.model.processing.vde.test.VdeTest;
import com.braintribe.model.util.meta.NewMetaModelGeneration;

/**
 * Provides tests for {@link EntityReferenceVde}.
 *
 */
public class EntityReferenceVdeTest extends VdeTest {

	public static VDGenerator $ = new VDGenerator();

	@Test
	public void testPersistentEntityReference() throws Exception {
		Smood x = new Smood(EmptyReadWriteLock.INSTANCE);
		x.setMetaModel(new NewMetaModelGeneration().buildMetaModel("gm:VdeTestModel", asList(Person.T)));

		BasicPersistenceGmSession session = new BasicPersistenceGmSession(x);
		Person entity = session.create(Person.T);
		entity.setId(1l);
		session.commit();

		EntityReference ref = entity.reference();
		Object result = evaluateWith(SessionAspect.class, session, ref);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Person.class);
		assertThat(((Person) result).<Long> getId()).isEqualTo(1L);
	}

	@Test(expected = VdeRuntimeException.class)
	public void testPersistentEntityReferenceFail() throws Exception {
		Smood x = new Smood(EmptyReadWriteLock.INSTANCE);

		BasicPersistenceGmSession session = new BasicPersistenceGmSession(x);

		PersistentEntityReference ref = $.persistentEntityReference();
		Object result = evaluateWith(SessionAspect.class, session, ref);
		assertThat(result).isNotNull();
		assertThat(result).isInstanceOf(Person.class);
		assertThat(((Person) result).<Long> getId()).isEqualTo(1L);
	}
}
