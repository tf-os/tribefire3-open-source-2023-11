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

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @see ChangeSetWithMinDelta
 */
public class ChangeSetWithMinDeltaTest extends AbstractMinDeltaTest {

	private static Property property = TestEntity.T.getProperty("intSet");

	private Set<Integer> oldSet;
	private Set<Integer> newSet;

	@Test
	public void noChange_Empty() throws Exception {
		oldSet();
		newSet();

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		BtAssertions.assertThat(recordedManipulations).hasSize(0);
	}

	@Test
	public void noChange_NonEmpty() throws Exception {
		oldSet(1);
		newSet(1);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		BtAssertions.assertThat(recordedManipulations).hasSize(0);
	}

	@Test
	public void changeAllValues() throws Exception {
		oldSet(1, 2, 3, 4);
		newSet(5, 6, 7, 8);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		assertChangeValue(newSet);
	}

	@Test
	public void onlyRemove() throws Exception {
		oldSet(1, 2, 3, 4);
		newSet(1, 2, 3);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		assertRemove(asMap(4, 4));
	}

	@Test
	public void onlyAdd() throws Exception {
		oldSet(1, 2, 3, 4);
		newSet(1, 2, 3, 4, 5);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		assertAdd(asMap(5, 5));
	}

	@Test
	public void addMoreThanRemove() throws Exception {
		oldSet(1, 2, 3, 4);
		newSet(1, 2, 3, 5, 6);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		assertRemove(asMap(4, 4));
		assertAdd(asMap(5, 5, 6, 6));
	}

	@Test
	public void addFewerThanRemove() throws Exception {
		oldSet(1, 2, 3, 4, 5, 6);
		newSet(1, 2, 3, 4, 7);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		assertRemove(asMap(5, 6, 6, 6));
		assertAdd(asMap(7, 7));
	}

	@Test
	public void addFewerThanRemove_CvmIsBetter() throws Exception {
		oldSet(1, 2, 3, 4);
		newSet(1, 2, 5);

		record(session -> ChangeSetWithMinDelta.apply(entity, property, oldSet, newSet));
		assertChangeValue(newSet);
	}

	// #########################################
	// ## . . . . . . . Helpers . . . . . . . ##
	// #########################################

	private void oldSet(Integer... values) {
		entity.setIntSet(asSet(values));
		oldSet = entity.getIntSet();
	}

	private void newSet(Integer... values) {
		newSet = asSet(values);
	}

}
