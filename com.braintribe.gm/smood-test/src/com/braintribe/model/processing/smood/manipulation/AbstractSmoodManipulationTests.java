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
package com.braintribe.model.processing.smood.manipulation;

import static com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode.LOCAL;

import java.util.List;
import java.util.Set;

import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.manipulation.basic.tools.ManipulationTools;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.smood.population.SmoodIndexTools;
import com.braintribe.model.processing.smood.test.AbstractSmoodTests;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver;
import com.braintribe.model.processing.test.tools.meta.ManipulationDriver.NotifyingSessionRunnable;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class AbstractSmoodManipulationTests extends AbstractSmoodTests {

	protected final ManipulationDriver manipulationRecorder = new ManipulationDriver();
	protected boolean generateId = true;

	protected ManipulationTrackingMode defaultManipulationMode;

	protected Manipulation trackedManipulation;
	protected ManipulationTrackingMode trackedMode;
	protected ManipulationResponse response;

	protected void applyManipulations(NotifyingSessionRunnable r) {
		applyManipulations(defaultManipulationMode, r);
	}

	protected void applyManipulations(ManipulationTrackingMode mm, NotifyingSessionRunnable r) {
		trackManipulations(mm, r);
		applyTrackedManipulations();
	}

	protected void trackManipulations(ManipulationTrackingMode mm, NotifyingSessionRunnable r) {
		manipulationRecorder.setTrackingMode(mm);
		trackedMode = mm;
		trackedManipulation = manipulationRecorder.track(r);
	}

	private void applyTrackedManipulations() {
		ManipulationRequest mr = asRequest(trackedManipulation);

		try {
			response = smood.apply() //
					.generateId(generateId) //
					.localRequest(trackedMode == LOCAL) //
					.manifestUnkownEntities(manifestUnknownEntities()) //
					.request2(mr).getManipulationResponse();

		} catch (ModelAccessException e) {
			throw new RuntimeException("Something went wrong!", e);
		}
	}

	protected boolean manifestUnknownEntities() {
		return false;
	}

	protected ManipulationRequest asRequest(Manipulation m) {
		return ManipulationTools.asManipulationRequest(m);
	}

	protected EntityQueryResult executeQuery(EntityQuery eq) {
		return smood.queryEntities(eq);
	}

	protected void assertFindsByProperty(EntityType<?> et, String propName, String propValue) {
		EntityQuery eq = EntityQueryBuilder.from(et).where().property(propName).eq(propValue).done();
		EntityQueryResult result = executeQuery(eq);
		List<GenericEntity> entities = result.getEntities();
		BtAssertions.assertThat(entities).isNotEmpty();
	}

	protected void assertFindsByIndexedProperty(EntityType<?> et, String propName, String propValue) {
		GenericEntity entity = smood.getValueForIndex(SmoodIndexTools.indexId(et.getTypeSignature(), propName), propValue);
		BtAssertions.assertThat(entity).isInstanceOf(et.getJavaType());
	}

	protected void assertEntityCountForType(EntityType<?> et, int count) {
		Set<? extends GenericEntity> entities = smood.getEntitiesPerType(et);

		if (count == 0)
			BtAssertions.assertThat(entities).isNullOrEmpty();
		else
			BtAssertions.assertThat(entities).isNotNull().hasSize(count);
	}

}
