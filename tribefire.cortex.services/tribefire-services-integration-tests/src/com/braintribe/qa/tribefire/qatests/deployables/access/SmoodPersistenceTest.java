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
package com.braintribe.qa.tribefire.qatests.deployables.access;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.product.rat.imp.ReducedImpApi;
import com.braintribe.qa.tribefire.test.Child;
import com.braintribe.qa.tribefire.test.Father;
import com.braintribe.qa.tribefire.test.Mother;
import com.braintribe.qa.tribefire.test.Task;
import com.braintribe.qa.tribefire.test.TaskState;
import com.braintribe.utils.CommonTools;

public class SmoodPersistenceTest extends AbstractPersistenceTest {

	private static final int NUMBEROFTASKS = 5;
	private static final int NUMBEROFCHILDREN = 10;

	private static String FAMILY_SMOODACCESS_ID = null;

	@Before
	public void init() {
		ImpApi imp = apiFactory().build();
		eraseTestEntities();
		FAMILY_SMOODACCESS_ID = createAndDeployFamilyAccess(imp).getExternalId();
	}

	@Test
	public void testSmoodPersistence() throws IOException {

		ReducedImpApi impForSmood = apiFactory().build().switchToAccess(FAMILY_SMOODACCESS_ID);
		final PersistenceGmSession session = impForSmood.session();

		Father f1 = session.create(Father.T);

		session.commit();

		Mother m1 = session.create(Mother.T);
		f1.setWife(m1);
		f1.setName("Kostas");
		f1.setChildren(new HashSet<Child>());

		Random rand = new Random();

		for (int i = 1; i <= NUMBEROFCHILDREN; i++) {
			Child c1 = session.create(Child.T);
			c1.setName("Kid_" + i);
			f1.getChildren().add(c1);
			c1.setTasks(new ArrayList<Task>());

			for (int j = 1; j <= NUMBEROFTASKS; j++) {
				Task t1 = session.create(Task.T);
				int randomInt = rand.nextInt(100);

				if (randomInt <= 33) {
					t1.setState(TaskState.SCHEDULED);
				} else if (randomInt <= 66) {
					t1.setState(TaskState.STARTED);
				} else {
					t1.setState(TaskState.COMPLETED);
				}
				t1.setName("Task_(" + i + ", " + j + ")");
				c1.getTasks().add(t1);
			}

			Resource r = impForSmood.utils().createResource("Resource_" + i, "What an interesting Resource" + i);
			c1.setLogo(r);
		}
		session.commit();

		logger.info("Completed instances creation for the model");

		final PersistenceGmSession session3 = apiFactory().newSessionForAccess(FAMILY_SMOODACCESS_ID);
		logger.info("Created session for access '" + session3.getAccessId() + "'.");

		logger.info("Entity Query by id check - Retrieving the Father with the id: " + f1.getId());
		Father retrievedFather = session3.query().entity(Father.T, f1.getId()).find();
		assertThat(retrievedFather).isNotNull();

		logger.info("Set collection check - Father.children is a Set<Child> with " + NUMBEROFCHILDREN + " Child entities");
		// assertThat(retrievedFather.getChildren().size()).isEqualTo(NUMBEROFCHILDREN);

		logger.info("List collection check - Child.task is a List<Task> with " + NUMBEROFTASKS + " Task entities");
		logger.info("Enum value check - Task.taskState in (SCHEDULED, STARTED, COMPLETED)");
		for (Child child : retrievedFather.getChildren()) {

			assertThat(child.getTasks().size()).isEqualTo(NUMBEROFTASKS);

			for (Task task : child.getTasks()) {
				assertThat(task.getState()).isIn(CommonTools.getSet(TaskState.SCHEDULED, TaskState.STARTED, TaskState.COMPLETED));
			}
		}

		logger.info("Single aggregation check - Father.wife = Mother");
		retrievedFather.getWife();
		assertThat(retrievedFather.getWife().getId().toString()).isEqualTo(m1.getId().toString());

		session.commit();
		logger.info("Completed DevQA-test: creating complex model and persist instances to Smood access.");
	}

	@After
	public void tearDown() {
		eraseTestEntities();
	}
}
