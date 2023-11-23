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
package com.braintribe.model.processing.manipulation.basic.mindelta;

import org.junit.Assert;
import org.junit.Before;

import com.braintribe.model.generic.manipulation.AddManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.RemoveManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.manipulation.AbstractManipulationTest;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;

/**
 * @author peter.gazdik
 */
public class AbstractMinDeltaTest extends AbstractManipulationTest {

	protected TestEntity entity;
	private int counter;

	@Before
	public void prepareInstance() {
		entity = session.create(TestEntity.T);
	}

	protected void assertChangeValue(Object expected) {
		ChangeValueManipulation m = getNextManipulation(ChangeValueManipulation.T);
		assertEqual(m.getNewValue(), expected, "Wrong new value.");
	}

	protected void assertRemove(Object expected) {
		RemoveManipulation m = getNextManipulation(RemoveManipulation.T);
		assertEqual(m.getItemsToRemove(), expected, "Wrong remove value.");
	}

	protected void assertAdd(Object expected) {
		AddManipulation am = getNextManipulation(AddManipulation.T);
		assertEqual(am.getItemsToAdd(), expected, "Wrong add value.");
	}

	protected <T extends AtomicManipulation> T getNextManipulation(EntityType<T> entityType) {
		AtomicManipulation am = recordedManipulations.get(counter++);
		if (am.<T> entityType() != entityType) {
			Assert.fail(
					"Manipulation should have type: " + entityType.getShortName() + ", but was: " + am.entityType().getShortName());
		}
		return (T) am;
	}

}
