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
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import com.braintribe.model.access.impls.PopulationProvidingPartitionRemovingAccess;
import com.braintribe.model.access.model.BasicAccesAdapterTestModelProvider;
import com.braintribe.model.access.model.Book;
import com.braintribe.model.access.model.Library;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.query.eval.set.EvalFilteredSet;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.EntityQuery;

/**
 * This tests some simple use-cases for three different implementations of {@link BasicAccessAdapter}. One that only
 * provides entire population, one that is able to filter by conditions and one that also provides index-access.
 * 
 * @see PopulationProvidingAccess
 * @see ConditionApplyingAccess
 * @see IndexingAccess
 */
@RunWith(Parameterized.class)
public class BaaQueryTests {

	private static final String accessId = "baa.test.access";

	private static Library la, lb;
	private static Book ba1, ba2, bb1, bb2;

	// ##########################################
	// ## . . Static Access Configuration . . .##
	// ##########################################

	@Parameters
	public static Collection<?> data() {
		return Arrays.asList(new Object[][] { { PopulationProvidingAccess.class }, { PopulationProvidingPartitionRemovingAccess.class },
				{ ConditionApplyingAccess.class }, { IndexingAccess.class } });
	}

	private static Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> accesses = configureAccesses();

	private static Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> configureAccesses() {
		Smood dataSource = configureSmood();

		Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> result = newMap();

		registerAccess(new PopulationProvidingAccess(dataSource), result);
		registerAccess(new PopulationProvidingPartitionRemovingAccess(dataSource), result);
		registerAccess(new ConditionApplyingAccess(dataSource, true), result);
		registerAccess(new IndexingAccess(dataSource, true), result);

		return result;
	}

	private static void registerAccess(BasicAccessAdapter access, Map<Class<? extends BasicAccessAdapter>, BasicAccessAdapter> result) {
		access.setMetaModelProvider(BasicAccesAdapterTestModelProvider.INSTANCE);
		access.setAccessId(accessId);

		result.put(access.getClass(), access);
	}

	private static Smood configureSmood() {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(BasicAccesAdapterTestModelProvider.INSTANCE.get());

		la = newLibrary("la");
		lb = newLibrary("lb");

		ba1 = newBook("000001", "ba1", 100, la);
		ba2 = newBook("000002", "ba2", 200, la);
		bb1 = newBook("000003", "bb1", 300, lb);
		bb2 = newBook("000004", "bb2", 400, lb);

		smood.initialize(Arrays.asList(la, lb, ba1, ba2, bb1, bb2));

		return smood;
	}

	private static Library newLibrary(String name) {
		Library result = Library.T.create(name);
		result.setPartition(accessId);
		result.setName(name);
		result.setBookByIsbn(new HashMap<String, Book>());
		result.setBookSet(new HashSet<Book>());
		result.setBookList(new ArrayList<Book>());

		return result;
	}

	private static Book newBook(String isin, String title, int pages, Library library) {
		Book result = Book.T.create(title);
		result.setPartition(accessId);
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

	private final BasicAccessAdapter access;

	public BaaQueryTests(Class<? extends BasicAccessAdapter> accessClass) {
		this.access = accesses.get(accessClass);
	}

	// ##########################################
	// ## . . . . . . Actual Tests . . . . . . ##
	// ##########################################

	private List<GenericEntity> entities;

	@Test
	public void findEntitiesByType() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.T).done());
		assertFoundEntities(ba1, ba2, bb1, bb2);

		runQuery(EntityQueryBuilder.from(Library.T).done());
		assertFoundEntities(la, lb);
	}

	@Test
	public void findEntities_SimplePropertyFilter() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.T).where().property("pages").lt(350).done());
		assertFoundEntities(ba1, ba2, bb1);
	}

	@Test
	public void findEntities_SimplePropertyFilter_PossiblyIndexed() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.T).where().property("title").eq("bb1").done());
		assertFoundEntities(bb1);
	}

	/**
	 * There was a bug (DEVCX-988) that disjunction was not delegated to the repository, but handled inside of a
	 * {@link EvalFilteredSet}. This was fixed in the query planner, which has the tests for the actual query plan, and
	 * here we double-check that it rally works.
	 */
	@Test
	public void findEntities_SimpleDisjunctionFilter() throws Exception {
		// @formatter:off
		runQuery(EntityQueryBuilder.from(Book.T)
				.where()
					.disjunction()
						.property("title").eq("bb1")
						.property("title").eq("bb2")
					.close()
				.done());
		// @formatter:on

		assertFoundEntities(bb1, bb2);
	}

	@Test
	public void findEntities_EntityPropertyFilter_PossiblyIndexed() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.T).where().property("library").eq().entity(la).done());
		assertFoundEntities(ba1, ba2);
	}

	@Test
	public void findEntities_PossibleIndexChain() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.T).where().property("library.id").eq(la.getId()).done());
		assertFoundEntities(ba1, ba2);
	}

	@Test
	public void findEntities_Paginated() throws Exception {
		runQuery(EntityQueryBuilder.from(Book.T).orderBy("pages").limit(2).done());
		assertFoundEntities(ba1, ba2);

		runQuery(EntityQueryBuilder.from(Book.T).orderBy("pages").paging(2, 1).done());
		assertFoundEntities(ba2, bb1);

		runQuery(EntityQueryBuilder.from(Book.T).orderBy("pages").paging(2, 2).done());
		assertFoundEntities(bb1, bb2);
	}

	@Test
	public void testCollectionContainsQuery() throws Exception {
		runQuery(EntityQueryBuilder.from(Library.T).where().property("bookSet").contains().entity(ba1).done());
		assertFoundEntities(la);

		runQuery(EntityQueryBuilder.from(Library.T).where().property("bookList").contains().entity(bb1).done());
		assertFoundEntities(lb);
	}

	@Test
	public void inEntities() throws Exception {
		runQuery(EntityQueryBuilder.from(Library.T).where().entity(EntityQueryBuilder.DEFAULT_SOURCE).inEntities(asSet(la)).done());
		assertFoundEntities(la);
	}

	@Test
	public void in_Using_ANY_PARTITION() throws Exception {
		EntityReference ref = la.reference();
		ref.setRefPartition(EntityReference.ANY_PARTITION);

		runQuery(EntityQueryBuilder.from(Library.T).where().entity(EntityQueryBuilder.DEFAULT_SOURCE).in(asSet(ref)).done());
		assertFoundEntities(la);
	}

	// ##########################################
	// ## . . . . Assertions and Helpers. . . .##
	// ##########################################

	private void assertFoundEntities(GenericEntity... expectedEntities) {
		assertThat(globalIds(entities)).isEqualTo(globalIds(asList(expectedEntities)));
	}

	private Set<String> globalIds(Collection<GenericEntity> entities) {
		return entities.stream().map(GenericEntity::getGlobalId).collect(Collectors.toSet());
	}

	private void runQuery(EntityQuery query) {
		try {
			entities = access.queryEntities(query).getEntities();

		} catch (ModelAccessException e) {
			throw new RuntimeException("Query evaluation failed", e);
		}
	}

}
