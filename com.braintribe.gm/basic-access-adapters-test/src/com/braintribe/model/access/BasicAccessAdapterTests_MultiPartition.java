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
package com.braintribe.model.access;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.impls.ConditionApplyingAccess;
import com.braintribe.model.access.impls.IndexingAccess;
import com.braintribe.model.access.impls.PopulationProvidingAccess;
import com.braintribe.model.access.model.BasicAccesAdapterTestModelProvider;
import com.braintribe.model.access.model.Book;
import com.braintribe.model.access.model.Library;
import com.braintribe.model.access.model.NoPartitionLibrary;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.lcd.CollectionTools2;

/**
 * This tests some simple use-cases for three different implementations of {@link BasicAccessAdapter}. One that only
 * provides entire population, one that is able to filter by conditions and one that also provides index-access.
 * 
 * @see PopulationProvidingAccess
 * @see ConditionApplyingAccess
 * @see IndexingAccess
 */
@RunWith(Parameterized.class)
public class BasicAccessAdapterTests_MultiPartition {

	private static final String ACCESS_ID = "BaaTestAccess";
	private static final String DEFAULT_PARTITION = ACCESS_ID;
	private static final String OTHER_PARTITION = "_" + ACCESS_ID;

	private static Library la, lb, _la;
	private static NoPartitionLibrary npl;
	private static Book ba1, ba2, bb1, bb2;

	// ##########################################
	// ## . . Static Access Configuration . . .##
	// ##########################################

	private static Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> accesses = configureAccesses();

	private static Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> configureAccesses() {
		Smood dataSource = configureSmood();

		Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> result = newMap();

		registerAccess(new PopulationProvidingAccess(dataSource), result);
		registerAccess(new ConditionApplyingAccess(dataSource, false), result);
		registerAccess(new IndexingAccess(dataSource, false), result);

		return result;
	}

	private static void registerAccess(BasicAccessAdapter access, Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> result) {
		result.put(access.getClass(), access);

		// let's set an access id, cause there was a reported bug that queries containing references do not work in this
		// case
		access.setAccessId(ACCESS_ID);
		access.setIgnorePartitions(false);
		access.setMetaModelProvider(BasicAccesAdapterTestModelProvider.INSTANCE);
	}

	private static Smood configureSmood() {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(BasicAccesAdapterTestModelProvider.INSTANCE.get());
		smood.setIgnorePartitions(false);

		smood.setPartitions(CollectionTools2.asSet(DEFAULT_PARTITION, OTHER_PARTITION));

		la = newLibrary("la");
		lb = newLibrary("lb");

		ba1 = newBook("000001", "ba1", 100, la);
		ba2 = newBook("000002", "ba2", 200, la);
		bb1 = newBook("000003", "bb1", 300, lb);
		bb2 = newBook("000004", "bb2", 400, lb);

		npl = newNonPartitionLibrary("npl");
		
		smood.initialize(Arrays.asList(la, lb, ba1, ba2, bb1, bb2, npl));

		_la = newLibrary("_la");
		_la.setId(la.getId());
		_la.setPartition("_" + la.getPartition());

		smood.registerEntity(_la, false);

		return smood;
	}

	private static Library newLibrary(String name) {
		Library result = Library.T.create(name);
		result.setPartition(ACCESS_ID);
		result.setName(name);

		return result;
	}

	private static NoPartitionLibrary newNonPartitionLibrary(String name) {
		NoPartitionLibrary result = NoPartitionLibrary.T.create(name);
		result.setName(name);

		return result;
	}

	
	private static Book newBook(String isin, String title, int pages, Library library) {
		Book result = Book.T.create(title);
		result.setPartition(ACCESS_ID);
		result.setTitle(title);
		result.setPages(pages);
		result.setLibrary(library);
		library.getBookByIsbn().put(isin, result);
		library.getBookSet().add(result);
		library.getBookList().add(result);

		return result;
	}

	// ##########################################
	// ## . . . . Test Parameters Setup . . . .##
	// ##########################################

	@Parameters
	public static Collection<?> data() {
		return Arrays.asList(new Object[][] { { PopulationProvidingAccess.class }, { ConditionApplyingAccess.class }, { IndexingAccess.class } });
	}

	private final BasicAccessAdapter access;

	public BasicAccessAdapterTests_MultiPartition(Class<? extends BasicAccessAdapter> accessClass) {
		this.access = accesses.get(accessClass);
	}

	// ##########################################
	// ## . . . . . . Actual Tests . . . . . . ##
	// ##########################################

	private List<GenericEntity> entities;

	@Test
	public void findEntitiesByType() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.class).done());
		assertFoundEntities(ba1, ba2, bb1, bb2);

		runQuery(EntityQueryBuilder.from(Library.class).done());
		assertFoundEntities(la, lb, _la);
	}

	@Test
	public void findEntities_SimplePropertyFilter() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.class).where().property("pages").lt(350).done());
		assertFoundEntities(ba1, ba2, bb1);
	}

	@Test
	public void findEntities_SimplePropertyFilter_PossiblyIndexed() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.class).where().property("title").eq("bb1").done());
		assertFoundEntities(bb1);
	}

	@Test
	public void findEntities_EntityPropertyFilter_PossiblyIndexed() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.class).where().property("library").eq().entity(la).done());
		assertFoundEntities(ba1, ba2);
	}

	@Test
	public void findEntities_PossibleIndexChain() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.class).where().property("library.id").eq(la.getId()).done());
		assertFoundEntities(ba1, ba2);
	}

	@Test
	public void findEntities_Paginated() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.class).orderBy("pages").limit(2).done());
		assertFoundEntities(ba1, ba2);

		runQuery(EntityQueryBuilder.from(Book.class).orderBy("pages").paging(2, 1).done());
		assertFoundEntities(ba2, bb1);

		runQuery(EntityQueryBuilder.from(Book.class).orderBy("pages").paging(2, 2).done());
		assertFoundEntities(bb1, bb2);
	}

	@Test
	public void testCollectionContainsQuery() throws Exception {
		runQuery(EntityQueryBuilder.from(Library.class).where().property("bookSet").contains().entity(ba1).done());
		assertFoundEntities(la);

		runQuery(EntityQueryBuilder.from(Library.class).where().property("bookList").contains().entity(bb1).done());
		assertFoundEntities(lb);
	}

	// ##########################################
	// ## . . . . . Select queries . . . . . . ##
	// ##########################################

	@Test
	public void selectEntityByReference() throws Exception {
		PersistentEntityReference ref = la.reference();
		GenericEntity resolvedEntity = access.getEntity(ref);

		assertThat(resolvedEntity.getGlobalId()).isNotNull().isEqualTo(la.getGlobalId());
	}

	@Test
	public void selectEntities_EntityReferenceFilter() throws Exception {
		PersistentEntityReference ref = la.reference();

		// @formatter:off
		SelectQuery query = query()
				.from(Book.class, "b")
				.where()
					.property("b", "library").eq().entityReference(ref)
				.done();
		// @formatter:on

		runQuery(query);

		assertFoundEntities(ba1, ba2);
	}

	// ##########################################
	// ## . . Partition Assigned when null . . ##
	// ##########################################

	@Test
	public void checkPartition_EntityQuery() throws Exception {
		runQuery(EntityQueryBuilder.from(NoPartitionLibrary.class).where().property("name").eq(npl.getName()).done());

		assertFoundEntities(npl);
		checkPartitionAssigned();
	}

	@Test
	public void checkPartition_SelectQuery() throws Exception {
		// @formatter:off
		SelectQuery query = query()
				.from(NoPartitionLibrary.T, "l")
				.where()
					.property("l", "name").eq(npl.getName())
				.done();
		// @formatter:on

		runQuery(query);

		assertFoundEntities(npl);
		checkPartitionAssigned();
	}

	// ##########################################
	// ## . . . . Assertions and Helpers. . . .##
	// ##########################################

	private void runQuery(EntityQuery query) {
		try {
			entities = access.queryEntities(query).getEntities();

		} catch (ModelAccessException e) {
			throw new RuntimeException("Query evaluation failed", e);
		}
	}

	private static SelectQueryBuilder query() {
		return new SelectQueryBuilder();
	}

	private void runQuery(SelectQuery query) {
		try {
			entities = (List<GenericEntity>) (List<?>) access.query(query).getResults();

		} catch (ModelAccessException e) {
			throw new RuntimeException("Query evaluation failed", e);
		}
	}

	private void checkPartitionAssigned() {
		for (GenericEntity entity : entities) {
			assertThat(entity.getPartition()).isEqualTo(ACCESS_ID);
		}
	}

	private void assertFoundEntities(GenericEntity... expectedEntities) {
		assertThat(globalIds(entities)).isEqualTo(globalIds(asList(expectedEntities)));
	}

	private Set<String> globalIds(Collection<GenericEntity> entities) {
		return entities.stream().map(GenericEntity::getGlobalId).collect(Collectors.toSet());
	}

}
