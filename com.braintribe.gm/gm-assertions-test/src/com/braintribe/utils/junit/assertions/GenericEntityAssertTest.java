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
package com.braintribe.utils.junit.assertions;

import static com.braintribe.utils.junit.assertions.GmAssertions.assertThat;
import static com.braintribe.utils.junit.assertions.GmAssertions.property;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;

public class GenericEntityAssertTest {

	@Test
	public void testPropertyIsAbsent() throws GmSessionException {
		final IncrementalAccess access = TestModelTestTools.newSmoodAccessMemoryOnly("test", TestModelTestTools.createTestModelMetaModel());
		final PersistenceGmSession generatingSession = TestModelTestTools.newSession(access);
		final String propertyName = "stringProperty";

		final SimpleEntity simpleEntity = generatingSession.create(SimpleEntity.T);
		generatingSession.commit();

		final PersistenceGmSession retrievingSession = TestModelTestTools.newSession(access);

		final TraversingCriterion tc = TC.create().pattern().entity(SimpleEntity.class).property(propertyName).close()
				.done();

		final EntityQuery query = EntityQueryBuilder.from(SimpleEntity.class).tc(tc).where().property("id")
				.eq(simpleEntity.getId()).done();
		final SimpleEntity retrieved = retrievingSession.query().entities(query).first();
		new GenericEntityAssert(retrieved).onProperty(propertyName).isAbsent();
		assertThat(retrieved).onProperty(propertyName).isAbsent();
		assertThat(property(retrieved, propertyName)).isAbsent();

		retrieved.getStringProperty();

		new GenericEntityAssert(retrieved).onProperty(propertyName).isNotAbsent();
		try {
			new GenericEntityAssert(retrieved).onProperty(propertyName).isAbsent();
			fail("Property '" + propertyName + "' of entity " + retrieved + " is absent!");
		} catch (final AssertionError e) {
			BtAssertions.assertThat(e.getMessage()).startsWith("Property '" + propertyName).endsWith("is not absent!");
		}
	}
}
