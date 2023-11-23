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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;

import tribefire.extension.messaging.model.comparison.AddEntries;
import tribefire.extension.messaging.model.comparison.CollectionType;
import tribefire.extension.messaging.model.comparison.ComparisonResult;
import tribefire.extension.messaging.model.comparison.Diff;
import tribefire.extension.messaging.model.comparison.DiffType;
import tribefire.extension.messaging.service.utils.PropertyByProperty;
import tribefire.extension.messaging.test.comparison.model.Complex;
import tribefire.extension.messaging.test.comparison.model.ComplexWithCollectionOfSimple;
import tribefire.extension.messaging.test.comparison.model.Simple;
import tribefire.extension.messaging.test.comparison.model.SimpleWithCollectionOfPrimitives;

public class TestHelper {
	protected static final String partition_fld = "partition";
	protected static final String old_val = "old value";
	protected static final String new_val = "!!! changed value !!!";

	protected static ComparisonResult testDiffType(DiffType diffType, boolean listedOnly, int expected, int unexpected) {
		Set<String> toVisit = Set.of(partition_fld);
		Simple before = getSimple("1", old_val);
		Simple after = getSimple("1", new_val);

		ComparisonResult comparisonResult = new PropertyByProperty(diffType, listedOnly, toVisit, AddEntries.NONE).checkEquality(before, after);

		assertEquals(expected, comparisonResult.getExpectedDiffs().size());
		assertEquals(unexpected, comparisonResult.getUnexpectedDiffs().size());
		return comparisonResult;
	}

	protected static void assertDiff(String path, boolean changed, Object expectedBefore, Object expectedAfter, Supplier<List<Diff>> supplier) {
		// assert is present in list, extract, compare key values
		assertTrue(String.format("Property %s is missing in expected DiffList!", path), diffListContainsPath(supplier, path));
		Diff expected = filterDiffByPath(supplier, path, expectedBefore);
		assertEquals(path, changed, expected.getValuesDiffer());
		assertEquals(path, expectedBefore, expected.getOldValue());
		assertEquals(path, expectedAfter, expected.getNewValue());
	}

	protected static void assertUnexpectedFroListSet(ComparisonResult comparisonResult, boolean isSet) {
		assertDiff("ROOT.globalId", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.id", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.mapPrimitive", false, null, null, comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.name", false, "name", "name", comparisonResult::getUnexpectedDiffs);
		assertDiff("ROOT.partition", false, null, null, comparisonResult::getUnexpectedDiffs);
		if (isSet) {
			assertDiff("ROOT.listPrimitive", false, null, null, comparisonResult::getUnexpectedDiffs);
		} else {
			assertDiff("ROOT.setPrimitive", false, null, null, comparisonResult::getUnexpectedDiffs);
		}
	}

	protected static ComparisonResult testSemiComplexSimpleCollection(int expected, int unexpected, CollectionType type, boolean sameValues) {
		Set<String> toVisit = Set.of(type == CollectionType.LIST ? "listPrimitive" : "setPrimitive");

		SimpleWithCollectionOfPrimitives before = getSimpleWithCollectionPrimitives(type, 1, 2, 3);
		int[] values = sameValues ? new int[] { 2, 3, 1 } : new int[] { 2, 3, 4 };
		SimpleWithCollectionOfPrimitives after = getSimpleWithCollectionPrimitives(type, values);
		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.ALL, false, toVisit, AddEntries.NONE).checkEquality(before, after);

		assertEquals(expected, comparisonResult.getExpectedDiffs().size());
		assertEquals(unexpected, comparisonResult.getUnexpectedDiffs().size());
		return comparisonResult;
	}

	protected static ComparisonResult testSemiComplexMap(int expected, int unexpected, DiffType diffType, boolean listedOnly, boolean removed,
			boolean added, boolean edited) {
		Set<String> toVisit = Set.of("mapPrimitive");

		SimpleWithCollectionOfPrimitives before = SimpleWithCollectionOfPrimitives.T.create();
		before.setMapPrimitive(Map.of("1", 1, "2", 2));
		before.setName("name");
		before.setPartition(old_val);

		SimpleWithCollectionOfPrimitives after = SimpleWithCollectionOfPrimitives.T.create();
		Map<String, Integer> afterMap = new HashMap<>();
		afterMap.put("1", 1);
		if (!removed) {
			afterMap.put("2", edited ? 4 : 2);
		}

		if (added) {
			afterMap.put("3", 3);
		}

		after.setMapPrimitive(afterMap);
		after.setName("name");
		after.setPartition(new_val);

		ComparisonResult comparisonResult = new PropertyByProperty(diffType, listedOnly, toVisit, AddEntries.NONE).checkEquality(before, after);

		assertEquals(expected, comparisonResult.getExpectedDiffs().size());
		assertEquals(unexpected, comparisonResult.getUnexpectedDiffs().size());
		return comparisonResult;
	}

	protected static ComparisonResult testComplex(int expected, int unexpected, boolean listedOnly, boolean sameEmbeddedObj, boolean sameValues) {
		Set<String> toVisit = Set.of("name", "simple.partition");

		Complex before = getComplex("1", old_val);
		Complex after = getComplex(sameEmbeddedObj ? "1" : "2", sameValues ? old_val : new_val);
		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.ALL, listedOnly, toVisit, AddEntries.NONE).checkEquality(before, after);

		assertEquals(expected, comparisonResult.getExpectedDiffs().size());
		assertEquals(unexpected, comparisonResult.getUnexpectedDiffs().size());
		return comparisonResult;
	}

	protected static ComparisonResult testComplexWithCollection(int expected, int unexpected, boolean listedOnly, CollectionType collectionType,
			Set<String> toVisit, boolean added, boolean removed, boolean edited) {
		ComplexWithCollectionOfSimple before = getComplexWithCollection(collectionType, false, false, false);
		ComplexWithCollectionOfSimple after = getComplexWithCollection(collectionType, added, removed, edited);

		ComparisonResult comparisonResult = new PropertyByProperty(DiffType.ALL, listedOnly, toVisit, AddEntries.NONE).checkEquality(before, after);

		assertEquals(expected, comparisonResult.getExpectedDiffs().size());
		assertEquals(unexpected, comparisonResult.getUnexpectedDiffs().size());
		return comparisonResult;
	}

	protected static ComplexWithCollectionOfSimple getComplexWithCollection(CollectionType collectionType, boolean added, boolean removed,
			boolean edited) {
		ComplexWithCollectionOfSimple complex = ComplexWithCollectionOfSimple.T.create();
		switch (collectionType) {
			case LIST -> complex.setListSimple(simpleList(added, removed, edited));
			case SET -> complex.setSetSimple(new HashSet<>(simpleList(added, removed, edited)));
			case MAP -> complex.setMapSimple(simpleMap(added, removed, edited));
		}

		return complex;
	}

	private static List<Simple> simpleList(boolean added, boolean removed, boolean edited) {
		List<Simple> list = new ArrayList<>();
		list.add(getSimple("1", old_val));
		if (added) {
			list.add(getSimple("3", old_val));
		}
		if (!removed) {
			list.add(getSimple("2", edited ? new_val : old_val));
		}
		return list;
	}

	private static Map<String, Simple> simpleMap(boolean added, boolean removed, boolean edited) {
		Map<String, Simple> map = new HashMap<>();
		map.put("1", getSimple("1", old_val));
		if (added) {
			map.put("3", getSimple("3", old_val));
		}
		if (!removed) {
			map.put("2", getSimple("2", edited ? new_val : old_val));
		}

		return map;
	}

	private static Diff filterDiffByPath(Supplier<List<Diff>> supplier, String path, Object expectedBefore) {
		return supplier.get().stream()
				.filter(s -> s.getPropertyPath().equals(path)
						&& (s.getOldValue() == expectedBefore || (expectedBefore != null && s.getOldValue().equals(expectedBefore))))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Could not Extract Element for Diff with path: " + path));
	}

	private static boolean diffListContainsPath(Supplier<List<Diff>> supplier, String path) {
		return supplier.get().stream().map(Diff::getPropertyPath).anyMatch(s -> s.equals(path));
	}

	protected static Complex getComplex(String id, String partition) {
		Complex complex = Complex.T.create();
		complex.setName("name");
		complex.setSimple(getSimple(id, partition));
		return complex;
	}

	protected static Complex getComplex(String id, String name, Simple simple) {
		Complex c = Complex.T.create();
		c.setId(id);
		c.setName(name);
		c.setSimple(simple);
		return c;
	}

	protected static SimpleWithCollectionOfPrimitives getSimpleWithCollectionPrimitives(CollectionType type, int... values) {
		SimpleWithCollectionOfPrimitives simpleWithCollectionOfPrimitives = SimpleWithCollectionOfPrimitives.T.create();
		simpleWithCollectionOfPrimitives.setName("name");
		switch (type) {
			case SET -> simpleWithCollectionOfPrimitives.setSetPrimitive(Arrays.stream(values).boxed().collect(Collectors.toSet()));
			case LIST -> simpleWithCollectionOfPrimitives.setListPrimitive(Arrays.stream(values).boxed().toList());
			default -> throw new IllegalArgumentException("Collection type " + type.name() + " not supported by this method!");
		}
		return simpleWithCollectionOfPrimitives;
	}

	protected static Simple getSimple(String id, String partition) {
		Simple o = Simple.T.create();
		o.setId(id);
		o.setPartition(partition);
		return o;
	}

	protected static String printAsJson(Object o) {
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		GmSerializationOptions options = GmSerializationOptions.deriveDefaults().outputPrettiness(OutputPrettiness.high).build();
		return marshaller.encode(o, options);
	}

	private TestHelper() {
	}
}
