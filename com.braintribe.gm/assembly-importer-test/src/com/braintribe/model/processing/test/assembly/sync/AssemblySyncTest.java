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
package com.braintribe.model.processing.test.assembly.sync;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.assembly.sync.impl.AssemblyImporter;
import com.braintribe.model.processing.test.assembly.sync.model.AssemblyNode;
import com.braintribe.model.processing.traversing.engine.impl.clone.MinImpactPropertyTransferExpert;

/**
 * Tests for {@link AssemblyImporter}
 * 
 * Every test has the following structure:
 * 
 * <ol>
 * <li>Create data to already existing in the DB with the dbSession - {@link AbstractAssemblySyncTest#dbSession}, which
 * ends with commit - {@link AbstractAssemblySyncTest#commitToDb()}.</li>
 * <li>Add data to the empty existing assembly node - {@link AbstractAssemblySyncTest#assembly}</li>
 * <li>run the actual import with test parameters</li>
 * <li>assert result, which was assigned to importedAssembly field -
 * {@link AbstractAssemblySyncTest#importedAssembly}</li>
 * </ol>
 */
public class AssemblySyncTest extends AbstractAssemblySyncTest {

	@Test
	public void syncWithEmptySession() throws Exception {
		prepareDb(f -> {
			/* Do nothing */
		});

		prepareExport(f -> {
			setNeighbors(assembly, f.create("leaf1"), f.create("leaf2"));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		assertNodes(importedAssembly.getNeighbors(), "leaf1", "leaf2");
	}

	@Test
	public void changeName() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			setNeighbors(dbRoot, f.create("leaf1"), f.create("leaf2"));
		});

		prepareExport(f -> {
			setNeighbors(assembly, newExportNode("leaf1", "Renamed1"), newExportNode("leaf2", "Renamed2"));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		assertNodes(importedAssembly.getNeighbors(), "Renamed1", "Renamed2");
	}

	@Test
	public void doNothingForAbsentProperty() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			setNeighbors(dbRoot, f.create("leaf1"), f.create("leaf2"));
		});

		prepareExport(f -> {
			Property neighborsProperty = assembly.entityType().getProperty(AssemblyNode.neighbors);
			neighborsProperty.setAbsenceInformation(assembly, GMF.absenceInformation());
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		assertNodes(importedAssembly.getNeighbors(), "leaf1", "leaf2");
	}

	@Test
	public void changeName_IgnoreExternals() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			setNeighbors(dbRoot, f.create("leaf1"), f.create("leaf2"));
		});

		prepareExport(f -> {
			setNeighbors(assembly, newExportNode("leaf1", "Renamed1"), newExportNode("leaf2", "Renamed2"));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, g -> g.getGlobalId().equals("leaf2"));

		assertNodes(importedAssembly.getNeighbors(), "Renamed1", "leaf2");
	}

	@Test
	public void changeCollections() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			dbRoot.setIntList(asList(1, 2, 3));
			dbRoot.setIntSet(asSet(1, 2, 3));
			dbRoot.setIntMap(asMap(1, 10, 2, 20, 3, 30));
		});

		prepareExport(f -> {
			assembly.setIntList(asList(4, 5, 6));
			assembly.setIntSet(asSet(4, 5, 6));
			assembly.setIntMap(asMap(4, 40, 5, 50, 6, 60));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		assertThat(importedAssembly.getIntList()).containsExactly(4, 5, 6);
		assertThat(importedAssembly.getIntSet()).containsOnly(4, 5, 6);
		assertThat(importedAssembly.getIntMap()).isEqualTo(asMap(4, 40, 5, 50, 6, 60));
	}

	@Test
	public void changeCollections_NoOpWhenEqal() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			dbRoot.setIntList(asList(1, 2, 3));
			dbRoot.setIntSet(asSet(1, 2, 3));
			dbRoot.setIntMap(asMap(1, 10, 2, 20, 3, 30));
		});

		prepareExport(f -> {
			assembly.setIntList(asList(1, 2, 3));
			assembly.setIntSet(asSet(1, 2, 3));
			assembly.setIntMap(asMap(1, 10, 2, 20, 3, 30));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL, new ExceptionThrowingCollectionTransferExpert());

		assertThat(importedAssembly.getIntList()).containsExactly(1, 2, 3);
		assertThat(importedAssembly.getIntSet()).containsOnly(1, 2, 3);
		assertThat(importedAssembly.getIntMap()).isEqualTo(asMap(1, 10, 2, 20, 3, 30));
	}

	static class ExceptionThrowingCollectionTransferExpert extends MinImpactPropertyTransferExpert {
		@Override
		protected CollectionHandler<?, ?> newHandlerFor(CollectionType type) {
			return new CollectionHandler<CollectionType, Object>() {
				@Override
				protected void findMinimalWayToModifyCollection(Object oldValue) {
					throw new UnsupportedOperationException("This method should not be called in a test!");
				}
			};
		}
	}
	
	@Test
	public void changeCollection_ObjectProperty() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			AssemblyNode listOwner = f.create("list");
			AssemblyNode setOwner = f.create("set");
			AssemblyNode mapOwner = f.create("map");
			setNeighbors(dbRoot, listOwner, setOwner, mapOwner);

			listOwner.setObject(asList(1, 2, 3));
			setOwner.setObject(asSet(1, 2, 3));
			mapOwner.setObject(asMap(1, 10, 2, 20, 3, 30));
		});

		prepareExport(f -> {
			AssemblyNode listOwner = f.create("list");
			AssemblyNode setOwner = f.create("set");
			AssemblyNode mapOwner = f.create("map");
			setNeighbors(assembly, listOwner, setOwner, mapOwner);

			listOwner.setObject(asList(4, 5, 6));
			setOwner.setObject(asSet(4, 5, 6));
			mapOwner.setObject(asMap(4, 40, 5, 50, 6, 60));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		List<AssemblyNode> neighbors = importedAssembly.getNeighbors();
		assertNodes(neighbors, "list", "set", "map");

		AssemblyNode listOwner = neighbors.get(0);
		AssemblyNode setOwner = neighbors.get(1);
		AssemblyNode mapOwner = neighbors.get(2);

		assertThat(listOwner.getObject()).isInstanceOf(List.class).isEqualTo(asList(4, 5, 6));
		assertThat(setOwner.getObject()).isInstanceOf(Set.class).isEqualTo(asSet(4, 5, 6));
		assertThat(mapOwner.getObject()).isInstanceOf(Map.class).isEqualTo(asMap(4, 40, 5, 50, 6, 60));
	}

	@Test
	public void changeCollection_ObjectProperty_ChangeType() throws Exception {
		prepareDb(f -> {
			AssemblyNode dbRoot = f.create(ROOT_NAME);
			AssemblyNode listOwner = f.create("list");
			AssemblyNode setOwner = f.create("set");
			AssemblyNode mapOwner = f.create("map");
			setNeighbors(dbRoot, listOwner, setOwner, mapOwner);

			listOwner.setObject("list");
			setOwner.setObject("set");
			mapOwner.setObject("map");
		});

		prepareExport(f -> {
			AssemblyNode listOwner = f.create("list");
			AssemblyNode setOwner = f.create("set");
			AssemblyNode mapOwner = f.create("map");
			setNeighbors(assembly, listOwner, setOwner, mapOwner);

			listOwner.setObject(asList(4, 5, 6));
			setOwner.setObject(asSet(4, 5, 6));
			mapOwner.setObject(asMap(4, 40, 5, 50, 6, 60));
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		List<AssemblyNode> neighbors = importedAssembly.getNeighbors();
		assertNodes(neighbors, "list", "set", "map");

		AssemblyNode listOwner = neighbors.get(0);
		AssemblyNode setOwner = neighbors.get(1);
		AssemblyNode mapOwner = neighbors.get(2);

		assertThat(listOwner.getObject()).isInstanceOf(List.class).isEqualTo(asList(4, 5, 6));
		assertThat(setOwner.getObject()).isInstanceOf(Set.class).isEqualTo(asSet(4, 5, 6));
		assertThat(mapOwner.getObject()).isInstanceOf(Map.class).isEqualTo(asMap(4, 40, 5, 50, 6, 60));
	}

	/**
	 * This tests the use-case when we are importing an entity that was only created on the session, but not committed
	 * yet. In such case the importer must synchronize the entity again.
	 */
	@Test
	public void syncEntityEvenIfItExistsInSession() throws Exception {
		prepareDb(f -> {
			// empty
		});

		prepareExport(f -> {
			setNeighbors(assembly, f.create("leaf1"), f.create("leaf2"));
		});

		prepareSession(f -> {
			f.create(ROOT_NAME);
		});

		runImport(NO_INCLUDE_ENVELOPE, NO_ENVELOPE, NO_EXTERNAL);

		assertNodes(importedAssembly.getNeighbors(), "leaf1", "leaf2");
	}

	protected void assertNodes(List<AssemblyNode> nodes, String... names) {
		assertThat(nodes).hasSize(names.length);
		int i = 0;
		for (AssemblyNode node : nodes) {
			String expectedName = names[i++];

			assertThat(node.getName()).isEqualTo(expectedName);
		}
	}

}
