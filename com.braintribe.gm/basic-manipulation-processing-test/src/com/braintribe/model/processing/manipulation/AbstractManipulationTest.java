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
package com.braintribe.model.processing.manipulation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.fest.assertions.Assertions;
import org.junit.Before;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.NotifyingSessionRunnable;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.SessionRunnable;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;
import com.braintribe.utils.lcd.CollectionTools;

/**
 * 
 */
public abstract class AbstractManipulationTest {
	protected ManipulationDriver manipulationDriver;
	protected BasicPersistenceGmSession session;
	protected IncrementalAccess access;
	protected CompoundManipulation recordedManipulation;
	protected List<AtomicManipulation> recordedManipulations;

	private static final String NAME = "ent";

	@Before
	public void setup() {
		access = new Smood(EmptyReadWriteLock.INSTANCE);
		manipulationDriver = new ManipulationDriver(access);
		session = manipulationDriver.newSession();
	}

	protected void record(NotifyingSessionRunnable r) {
		recordedManipulation = manipulationDriver.track(r, session);
		recordedManipulations = recordedManipulation.inline();

		recordedManipulation.setCompoundManipulationList(cast(recordedManipulations));
	}

	protected static <T> T cast(Object o) {
		return (T) o;
	}

	protected void apply(SessionRunnable r) {
		ManipulationRequest request = prepare(r);
		apply(request);
	}

	protected void apply(ManipulationRequest request) {
		try {
			access.applyManipulation(request);

		} catch (ModelAccessException e) {
			throw new RuntimeException("[TEST INITIALIZATION ERRO] error while applying manipulations", e);
		}
	}

	protected ManipulationRequest prepare(SessionRunnable r) {
		return manipulationDriver.dryRunAsRequest(r);
	}

	protected void assertEqual(Object value, Object expected, String as) {
		if (value instanceof Collection) {
			assertEqual((Collection<?>) expected, (Collection<?>) expected, as);

		} else if (value instanceof Map) {
			assertEqual((Map<?, ?>) expected, (Map<?, ?>) expected, as);
		}
	}

	protected void assertEqual(Collection<?> c1, Collection<?> c2, String as) {
		if (c2 == null) {
			Assertions.assertThat(c1).isNull();
		} else {
			Assertions.assertThat(c1).as(as).isEqualTo(c2);
		}
	}

	protected void assertEqual(Map<?, ?> actual, Map<?, ?> expected, String as) {
		BtAssertions.assertThat(actual).as(as).isNotNull();

		assertEqual(actual.keySet(), expected.keySet(), as + " wrong keys");
		assertEqual(actual.values(), expected.values(), as + " wrong values");
	}

	protected TestEntity queryDefualtEntity() {
		return queryDefualtEntity(manipulationDriver.newSession());
	}

	protected TestEntity createDefaultEntity(PersistenceGmSession session) {
		return createEntity(session, NAME);
	}

	protected TestEntity createEntity(PersistenceGmSession session, String name) {
		TestEntity entity = session.create(TestEntity.T);
		entity.setName(name);

		return entity;
	}

	protected TestEntity queryDefualtEntity(PersistenceGmSession session) {
		try {
			EntityQuery query = EntityQueryBuilder.from(TestEntity.class).where().property("name").eq(NAME).done();
			List<GenericEntity> entities = session.query().entities(query).list();
			return CollectionTools.isEmpty(entities) ? null : (TestEntity) entities.get(0);
		} catch (Exception e) {
			throw new RuntimeException("error while querying default test entity", e);
		}
	}

}
