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
package com.braintribe.qa.tribefire.qatests.helpers.comparison;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.test.tools.comparison.PropertyByProperty;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.qa.tribefire.qatests.deployables.access.AbstractPersistenceTest;
import com.braintribe.qa.tribefire.test.Child;
import com.braintribe.qa.tribefire.test.Father;
import com.braintribe.qa.tribefire.test.Mother;
import com.braintribe.qa.tribefire.test.Task;
import com.braintribe.utils.lcd.CommonTools;

public class ComparisonTest extends AbstractPersistenceTest {

	PersistenceGmSession session;

	@Before
	@After
	public void tearDown() {
		eraseTestEntities();
	}

	@Test
	public void testEquals() {
		logger.info("Starting DevQA-test: testing PBP...");

		ImpApi imp = apiFactory().build();
		CollaborativeSmoodAccess testAccess = createAndDeployFamilyAccess(imp);
		session = apiFactory().buildSessionFactory().newSession(testAccess.getExternalId());

		Set<Object> a = new HashSet<>();
		Set<Object> b = new HashSet<>();

		Set<Child> c1 = new HashSet<>();
		Set<Child> c2 = new HashSet<>();

		Mother m1 = session.create(Mother.T);
		Mother m2 = session.create(Mother.T);

		Father f1 = session.create(Father.T);
		Father f2 = session.create(Father.T);
		m1.setHusband(f1);
		m2.setHusband(f2);

		f1.setWife(m1);
		f2.setWife(m2);

		Task t1 = session.create(Task.T);
		Task t2 = session.create(Task.T);

		for (int i = 0; i < 5; i++) {
			Child child = session.create(Child.T);
			child.setName("orphan" + i);
			child.setTasks(CommonTools.getList(t1));

			Child child2 = session.create(Child.T);
			child2.setName("orphan" + i);
			child2.setTasks(CommonTools.getList(t2));

			c1.add(child);
			c2.add(child2);
		}

		m1.setChildren(c1);
		f1.setChildren(c1);
		m2.setChildren(c2);
		f2.setChildren(c2);

		a.add("1");
		a.add(m1);
		b.add("1");
		b.add(m2);

		PropertyByProperty.checkEquality(m1, m2).assertThatEqual();

		// no ids before commit - should be equal
		session.commit();

		PropertyByProperty.checkEquality(m1, m2).assertThatNotEqual();
		PropertyByProperty.checkEquality(m1, m2, "globalId").assertThatNotEqual();
		PropertyByProperty.checkEquality(m1, m2, "id", "globalId").assertThatEqual();
		PropertyByProperty.checkEqualityExcludingIds(m1, m2).assertThatEqual();

		// should have different ids after commit

	}
}
