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
package tribefire.extension.messaging.test.comparison;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static tribefire.extension.messaging.test.comparison.TestHelper.assertDiff;
import static tribefire.extension.messaging.test.comparison.TestHelper.assertUnexpectedFroListSet;
import static tribefire.extension.messaging.test.comparison.TestHelper.getComplex;
import static tribefire.extension.messaging.test.comparison.TestHelper.getSimple;
import static tribefire.extension.messaging.test.comparison.TestHelper.new_val;
import static tribefire.extension.messaging.test.comparison.TestHelper.old_val;
import static tribefire.extension.messaging.test.comparison.TestHelper.partition_fld;
import static tribefire.extension.messaging.test.comparison.TestHelper.testComplex;
import static tribefire.extension.messaging.test.comparison.TestHelper.testComplexWithCollection;
import static tribefire.extension.messaging.test.comparison.TestHelper.testDiffType;
import static tribefire.extension.messaging.test.comparison.TestHelper.testSemiComplexMap;
import static tribefire.extension.messaging.test.comparison.TestHelper.testSemiComplexSimpleCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import tribefire.extension.messaging.model.comparison.AddEntries;
import tribefire.extension.messaging.model.comparison.CollectionType;
import tribefire.extension.messaging.model.comparison.ComparisonResult;
import tribefire.extension.messaging.model.comparison.Diff;
import tribefire.extension.messaging.model.comparison.DiffType;
import tribefire.extension.messaging.model.test.TestObject;
import tribefire.extension.messaging.service.utils.PropertyByProperty;
import tribefire.extension.messaging.test.comparison.model.Complex;
import tribefire.extension.messaging.test.comparison.model.ComplexWithCollectionOfSimple;
import tribefire.extension.messaging.test.comparison.model.Simple;

public class PropertyByPropertyTest {
	/**
	 * Intro: Used terms: 1. Simple object -> an object containing primitive fields only (no GenericEntities or
	 * Collections as fields) 2. SemiComplex object -> an object containing primitive fields or a Collection of
	 * primitives (no GenericEntities or Collections of GenericEntities as fields) 3. Complex object -> an object
	 * containing one or several complex fields (GenericEntities or Collections of GenericEntities as fields)
	 */

	// Test Diff Types -> simple objects comparison
	@Test
	public void diffType_All() {
		ComparisonResult comparisonResult = testDiffType(DiffType.ALL, false, 1, 2);
		assertDiff("ROOT.partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.id", false, "1", "1", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
	}

	@Test
	public void diffType_Changes() {
		ComparisonResult comparisonResult = testDiffType(DiffType.CHANGES_ONLY, false, 1, 0);
		assertDiff("ROOT.partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
	}

	@Test
	public void diffType_UnChanged() {
		ComparisonResult comparisonResult = testDiffType(DiffType.UN_CHANGED_ONLY, false, 0, 2);
		assertDiff("ROOT.id", false, "1", "1", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
	}

	// Visit listed properties only
	@Test
	public void visit_listed_only_true_simple() {
		ComparisonResult comparisonResult = testDiffType(DiffType.ALL, true, 1, 0);
		assertDiff("ROOT.partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
	}

	@Test
	public void visit_listed_only_true_complex_list() {
		ComplexWithCollectionOfSimple before = ComplexWithCollectionOfSimple.T.create();
		before.setListSimple(List.of(getSimple("1", old_val), getSimple("2", old_val)));

		ComplexWithCollectionOfSimple after = ComplexWithCollectionOfSimple.T.create();
		after.setListSimple(List.of(getSimple("1", old_val), getSimple("2", new_val)));

		Set<String> toVisit = Set.of("listSimple[1].partition");
		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.ALL, true, toVisit, AddEntries.NONE).checkEquality(before, after);
		assertEquals(1, comparisonResult.getExpectedDiffs().size());
		assertEquals(0, comparisonResult.getUnexpectedDiffs().size());
		assertDiff("ROOT.listSimple[1].partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
	}

	// Semi_complex diff list/set
	@Test
	public void semi_complex_list_unsorted_list_same_values() { // Here all list elements match, but are shuffled, so
																// elements are equal, but lists
																// are not
		ComparisonResult comparisonResult = testSemiComplexSimpleCollection(4, 6, CollectionType.LIST, true);

		assertDiff("ROOT.listPrimitive[0]", false, 1, 1, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive[1]", false, 2, 2, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive[2]", false, 3, 3, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive", true, null, null, comparisonResult::getExpectedDiffs);

		assertUnexpectedFroListSet(comparisonResult, false);
	}

	@Test
	public void semi_complex_unsorted_list_add_delete_values() {
		ComparisonResult comparisonResult = testSemiComplexSimpleCollection(5, 6, CollectionType.LIST, false);
		assertDiff("ROOT.listPrimitive[0]", true, 1, null, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive[1]", false, 2, 2, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive[2]", false, 3, 3, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive[2]", true, null, 4, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listPrimitive", true, null, null, comparisonResult::getExpectedDiffs);

		assertUnexpectedFroListSet(comparisonResult, false);
	}

	@Test
	public void semi_complex_set_same_values() { // Here as Set is not an ordered collection they are all same
		ComparisonResult comparisonResult = testSemiComplexSimpleCollection(4, 6, CollectionType.SET, true);

		assertDiff("ROOT.setPrimitive[0]", false, 1, 1, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive[1]", false, 2, 2, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive[2]", false, 3, 3, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive", false, null, null, comparisonResult::getExpectedDiffs);

		assertUnexpectedFroListSet(comparisonResult, true);
	}

	@Test
	public void semi_complex_set_add_delete_values() {
		ComparisonResult comparisonResult = testSemiComplexSimpleCollection(5, 6, CollectionType.SET, false);
		assertDiff("ROOT.setPrimitive[0]", true, 1, null, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive[1]", false, 2, 2, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive[2]", false, 3, 3, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive[2]", true, null, 4, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.setPrimitive", true, null, null, comparisonResult::getExpectedDiffs);

		assertUnexpectedFroListSet(comparisonResult, true);
	}

	// Semi_complex diff map
	@Test
	public void semi_complex_map_add_delete_values() {
		ComparisonResult comparisonResult = testSemiComplexMap(4, 6, DiffType.ALL, false, true, true, false);

		assertDiff("ROOT.mapPrimitive", true, null, null, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.mapPrimitive(key:1)", false, 1, 1, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.mapPrimitive(key:2)", true, 2, null, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.mapPrimitive(key:3)", true, null, 3, comparisonResult::getExpectedDiffs);

		assertDiff("ROOT.id", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.name", false, "name", "name", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.partition", true, old_val, new_val, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listPrimitive", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.setPrimitive", false, null, null, comparisonResult::getUnexpectedDiffs);
	}

	@Test
	public void semi_complex_map_element_diff() {
		ComparisonResult comparisonResult = testSemiComplexMap(2, 0, DiffType.CHANGES_ONLY, true, false, false, true);

		assertDiff("ROOT.mapPrimitive", true, null, null, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.mapPrimitive(key:2)", true, 2, 4, comparisonResult::getExpectedDiffs);
	}

	// Complex tests
	@Test
	public void complex_same_values() {
		ComparisonResult comparisonResult = testComplex(2, 5, false, true, true);
		assertDiff("ROOT.name", false, "name", "name", comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.simple.partition", false, old_val, old_val, comparisonResult::getExpectedDiffs);

		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.id", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.partition", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.simple.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.simple.id", false, "1", "1", comparisonResult::getUnexpectedDiffs);
	}

	// Complex with collections of simple tests
	@Test
	public void complex_list_add_delete() { // Here we add/delete second element, expect to see 1 record 'expected' as
											// second element is removed an
											// new is created
		Set<String> toVisit = Set.of("listSimple.partition");
		ComparisonResult comparisonResult = testComplexWithCollection(1, 10, false, CollectionType.LIST, toVisit, true, true, false);
		assertDiff("ROOT.listSimple[0].partition", false, old_val, old_val, comparisonResult::getExpectedDiffs);

		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.id", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[0].globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[0].id", false, "1", "1", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[1]", true, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[1]", true, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple", true, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.mapSimple", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.partition", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.setSimple", false, null, null, comparisonResult::getUnexpectedDiffs);
	}

	@Test
	public void complex_list_test_change_element_1() { // Here we expect 1 record, but for second element only as we
														// mention in 'toVisit' path
		Set<String> toVisit = Set.of("listSimple[1].partition");
		ComparisonResult comparisonResult = testComplexWithCollection(1, 11, false, CollectionType.LIST, toVisit, false, false, true);
		assertDiff("ROOT.listSimple[1].partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);

		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.id", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[0].globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[0].id", false, "1", "1", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[0].partition", false, old_val, old_val, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[1].globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple[1].id", false, "2", "2", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.listSimple", true, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.mapSimple", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.partition", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.setSimple", false, null, null, comparisonResult::getUnexpectedDiffs);
	}

	@Test
	public void complex_list_test_change_element_listed_only() { // Here we expect 2 records, for elements mentioned in
																	// 'toVisit (Visitor mechanism is
																	// used!)
		Set<String> toVisit = Set.of("listSimple[0].id", "listSimple[1].partition");
		ComparisonResult comparisonResult = testComplexWithCollection(2, 0, true, CollectionType.LIST, toVisit, false, false, true);
		assertDiff("ROOT.listSimple[1].partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.listSimple[0].id", false, "1", "1", comparisonResult::getExpectedDiffs);
	}

	@Test
	public void complex_map_test_change_element_listed_only() { // Here we expect 2 records, for elements mentioned in
																// 'toVisit (Visitor mechanism is
																// used!)
		Set<String> toVisit = Set.of("mapSimple(1).id", "mapSimple(2).partition");
		ComparisonResult comparisonResult = testComplexWithCollection(2, 0, true, CollectionType.MAP, toVisit, false, false, true);
		assertDiff("ROOT.mapSimple(2).partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.mapSimple(1).id", false, "1", "1", comparisonResult::getExpectedDiffs);
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void complex_set_test_change_element_listed_only() { // Here we expect 2 records, for elements mentioned in
																// 'toVisit (Visitor mechanism is
																// used!)
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Unsupported collection type for extraction by index!");
		Set<String> toVisit = Set.of("setSimple(1).id", "setSimple(2).partition");
		testComplexWithCollection(2, 0, true, CollectionType.SET, toVisit, false, false, true);
	}

	// other
	@Test
	public void simple_two_different_objects() { // here change in partition makes no diff as the whole entry was
													// changed (id:1 -> id:2)
		Set<String> toVisit = Set.of(partition_fld);
		Simple before = getSimple("1", "1");
		Simple after = getSimple("2", "2");

		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.CHANGES_ONLY, false, toVisit, AddEntries.NEW).checkEquality(before,
				after);

		assertEquals(0, comparisonResult.getExpectedDiffs().size());
		assertEquals(2, comparisonResult.getUnexpectedDiffs().size());

		Diff diff1 = comparisonResult.getUnexpectedDiffs().get(1);
		assertNull(diff1.getNewValue());
		assertNull(diff1.getOldValue());
		assertEquals("ROOT", diff1.getPropertyPath());
		assertEquals("Value was removed", diff1.getDescription());

		Diff diff2 = comparisonResult.getUnexpectedDiffs().get(0);
		assertNotNull(diff2.getNewValue());
		assertNull(diff2.getOldValue());
		assertEquals("ROOT", diff2.getPropertyPath());
		assertEquals("Value was added", diff2.getDescription());
	}

	@Test
	public void simple_listed_only() { // visit listed properties only test for simple single entry
		Set<String> toVisit = Set.of("partition");
		Simple before = getSimple("1", "1");
		Simple after = getSimple("1", "2");

		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.ALL, true, toVisit, AddEntries.NONE).checkEquality(before, after);

		assertEquals(1, comparisonResult.getExpectedDiffs().size());
		assertEquals(0, comparisonResult.getUnexpectedDiffs().size());
		assertDiff("ROOT.partition", true, "1", "2", comparisonResult::getExpectedDiffs);
	}

	@Test
	public void complex_simple_property_diff() {// no extraction, so expect diff in parent and in child
		Set<String> toVisit = Set.of("name", "simple.partition");

		Complex before = getComplex("id1", "name1", getSimple("id2", old_val));
		Complex after = getComplex("id1", "name2", getSimple("id2", new_val));

		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.CHANGES_ONLY, false, toVisit, AddEntries.NONE).checkEquality(before,
				after);
		assertTrue(comparisonResult.getOldValue() instanceof Complex && comparisonResult.getNewValue() instanceof Complex);
		assertEquals(2, comparisonResult.getExpectedDiffs().size());
		assertEquals(0, comparisonResult.getUnexpectedDiffs().size());
		assertDiff("ROOT.name", true, "name1", "name2", comparisonResult::getExpectedDiffs);
		assertDiff("ROOT.simple.partition", true, old_val, new_val, comparisonResult::getExpectedDiffs);
	}

	@Test
	public void test_object() {
		Set<String> toVisit = Set.of("name", "embeddedObject");
		TestObject before = storeTestObject();
		TestObject after = storeTestObject();
		after.setName("NEW_NAME");

		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.ALL, false, toVisit, AddEntries.NONE).checkEquality(before, after);
		assertEquals(1, comparisonResult.getExpectedDiffs().size());
	}

	protected TestObject storeTestObject() {
		TestObject emb_obj = getTestObject("EMB_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject list1_obj = getTestObject("LIST1_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject list2_obj = getTestObject("LIST2_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject map1_obj = getTestObject("MAP1_OBJ", null, new HashMap<>(), new ArrayList<>());
		TestObject map2_obj = getTestObject("MAP2_OBJ", null, new HashMap<>(), new ArrayList<>());
		return getTestObject("ROOT_OBJ", emb_obj, Map.of(map1_obj.getName(), map1_obj, map2_obj.getName(), map2_obj), List.of(list1_obj, list2_obj));
	}

	private TestObject getTestObject(String name, TestObject embedded, Map<String, TestObject> map, List<TestObject> list) {
		TestObject object = TestObject.T.create();
		object.setName(name);
		object.setId(name);
		object.setGlobalId(name);
		object.setEmbeddedObject(embedded);
		object.setObjectMap(map);
		object.setObjectList(list);
		return object;
	}
}
