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

import java.util.Map;

import org.junit.Test;

import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.manipulation.testdata.manipulation.TestEntity;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * @see ChangeMapWithMinDelta
 */
public class ChangeMapWithMinDeltaTest extends AbstractMinDeltaTest {

	private static Property property = TestEntity.T.getProperty("intMap");

	private Map<Integer, Integer> oldMap;
	private Map<Integer, Integer> newMap;

	@Test
	public void noChange_Empty() throws Exception {
		oldMap();
		newMap();

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		BtAssertions.assertThat(recordedManipulations).hasSize(0);
	}

	@Test
	public void noChange_NonEmpty() throws Exception {
		oldMap(1, 10);
		newMap(1, 10);

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		BtAssertions.assertThat(recordedManipulations).hasSize(0);
	}

	@Test
	public void changeAllValues() throws Exception {
		oldMap(1, 10, 2, 20, 3, 30);
		newMap(4, 40, 5, 50, 6, 60);

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		assertChangeValue(newMap);
	}

	@Test
	public void onlyRemove() throws Exception {
		oldMap(1, 10, 2, 20, 3, 30);
		newMap(1, 10, 2, 20);

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		assertRemove(asMap(3, 30));
	}

	@Test
	public void onlyAdd() throws Exception {
		oldMap(1, 10, 2, 20, 3, 30);
		newMap(1, 10, 2, 20, 3, 30, 4, 40);

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		assertAdd(asMap(4, 40));
	}

	@Test
	public void onlyReplace() throws Exception {
		oldMap(1, 10, 2, 20, 3, 30);
		newMap(1, 10, 2, 20, 3, 300);
		
		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		assertAdd(asMap(3, 300));
	}
	
	
	@Test
	public void mixOfAll() throws Exception {
		oldMap(1, 10, 2, 20, 3, 30, 4, 40);
		newMap(1, 10, 2, 20, 4, 400, 5, 50);

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		assertRemove(asMap(3, 30));
		assertAdd(asMap(4, 400, 5, 50));
	}
	
	@Test
	public void mixOfAll_CvmIsBetter() throws Exception {
		oldMap(1, 10, 2, 20, 3, 30);
		newMap(1, 10, 3, 300, 4, 40);

		record(session -> ChangeMapWithMinDelta.apply(entity, property, oldMap, newMap));
		assertChangeValue(newMap);
	}

	private void oldMap(Integer... values) {
		entity.setIntMap(asMap((Object[]) values));
		oldMap = entity.getIntMap();
	}

	private void newMap(Integer... values) {
		newMap = asMap((Object[]) values);
	}

}
