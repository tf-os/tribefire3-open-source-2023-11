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

import java.util.List;
import java.util.Set;

import org.junit.Test;

import tribefire.extension.messaging.model.comparison.CollectionType;
import tribefire.extension.messaging.service.utils.EntryCleaner;
import tribefire.extension.messaging.test.comparison.model.Complex;
import tribefire.extension.messaging.test.comparison.model.ComplexWithCollectionOfSimple;
import tribefire.extension.messaging.test.comparison.model.Simple;
import tribefire.extension.messaging.test.comparison.model.SimpleWithCollectionOfPrimitives;

public class EntryCleanerTest {
	@Test
	public void simple() {
		Simple simple = TestHelper.getSimple("1", "1");
		Set<String> path = Set.of("partition");

		Simple result = (Simple) EntryCleaner.cleanEntry(path, simple);
		assertNotNull(result.getPartition());
		assertNull(result.getGlobalId());
		assertNull(result.getId());
	}

	@Test
	public void simpleWithCollectionPrimitive() {
		SimpleWithCollectionOfPrimitives simple = TestHelper.getSimpleWithCollectionPrimitives(CollectionType.LIST, 1, 2, 3);
		Set<String> path = Set.of("name", "listPrimitive");

		SimpleWithCollectionOfPrimitives result = (SimpleWithCollectionOfPrimitives) EntryCleaner.cleanEntry(path, simple);
		assertNotNull(result.getName());
		assertNotNull(result.getListPrimitive());
		assertEquals(3, result.getListPrimitive().size());
		assertTrue(List.of(1, 2, 3).containsAll(result.getListPrimitive()));
		assertNull(result.getGlobalId());
		assertNull(result.getId());
		assertTrue(result.getMapPrimitive().isEmpty());
		assertTrue(result.getSetPrimitive().isEmpty());
		assertNull(result.getPartition());
	}

	@Test
	public void complex() {
		Complex complex = TestHelper.getComplex("1", "1");
		Set<String> path = Set.of("name", "simple.partition");

		Complex result = (Complex) EntryCleaner.cleanEntry(path, complex);
		assertNotNull(result.getName());
		assertNotNull(result.getSimple());
		assertNotNull(result.getSimple().getPartition());

		assertNull(result.getId());
		assertNull(result.getGlobalId());
		assertNull(result.getPartition());

		assertNull(result.getSimple().getId());
		assertNull(result.getSimple().getGlobalId());
	}

	@Test
	public void complex2() {
		ComplexWithCollectionOfSimple complex = TestHelper.getComplexWithCollection(CollectionType.LIST, false, false, false);
		Set<String> path = Set.of("listSimple.partition");

		ComplexWithCollectionOfSimple result = (ComplexWithCollectionOfSimple) EntryCleaner.cleanEntry(path, complex);
		assertNotNull(result.getListSimple());
		assertNotNull(result.getListSimple().get(0).getPartition());
		assertNotNull(result.getListSimple().get(1).getPartition());

		assertNull(result.getId());
		assertNull(result.getGlobalId());
		assertNull(result.getPartition());
		assertTrue(result.getSetSimple().isEmpty());
		assertTrue(result.getMapSimple().isEmpty());
	}
}
