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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.impls.SimpleSmoodifiedBaaAccess;
import com.braintribe.model.access.model.BasicAccesAdapterTestModelProvider;
import com.braintribe.model.access.model.Book;
import com.braintribe.model.access.model.Library;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;

/**
 * 
 */
public class BaaManipulationTests {

	private static final String ACCESS_ID = "baa.test.access";
	private SimpleSmoodifiedBaaAccess access;
	private PersistenceGmSession session;

	// ##########################################
	// ## . . Static Access Configuration . . .##
	// ##########################################

	@Before
	public void initAccess() {
		access = new SimpleSmoodifiedBaaAccess(ACCESS_ID, BasicAccesAdapterTestModelProvider.INSTANCE.get());
		refreshSession();
	}

	private void refreshSession() {
		if (session != null)
			session.commit();
		session = new BasicPersistenceGmSession(access);
	}

	// ##########################################
	// ## . . . . . . Actual Tests . . . . . . ##
	// ##########################################

	@Test
	public void createEntity() throws Exception {
		Library library = session.create(Library.T);
		library.setName("Eagleton Library");

		session.commit();

		Assertions.assertThat(library.<Long> getId()).isNotNull();
		Assertions.assertThat(library.getPartition()).isNotNull();
	}

	@Test
	public void createEntityWithId() throws Exception {
		Library library = createLibrary(99L, "Eagleton Library");
		refreshSession();

		library = getById(Library.T, 99L);
		Assertions.assertThat(library.getPartition()).isNotNull();
	}

	@Test
	public void createEntityWithPartition() throws Exception {
		Library library = session.create(Library.T);
		library.setName("Eagleton Library");
		library.setPartition(ACCESS_ID);
		refreshSession();

		Assertions.assertThat(library.<Long> getId()).isNotNull();

		library = getById(Library.T, library.getId());
		Assertions.assertThat(library.getPartition()).isEqualTo(ACCESS_ID);
	}

	@Test
	public void createEntityWithIdAndPartition() throws Exception {
		Library library = createLibrary(99L, "Eagleton Library");
		library.setPartition(ACCESS_ID);
		refreshSession();

		library = getById(Library.T, library.getId());
		Assertions.assertThat(library.getPartition()).isEqualTo(ACCESS_ID);
		Assertions.assertThat(library.<Long> getId()).isNotNull();
	}

	@Test
	public void createEntitiesWithId() throws Exception {
		Library l1 = createLibrary(5L, "L1");
		Library l2 = createLibrary(6L, "L2");
		session.commit();

		Assertions.assertThat(l1.<Long> getId()).isNotNull();
		Assertions.assertThat(l1.getPartition()).isNotNull();
		Assertions.assertThat(l2.<Long> getId()).isNotNull();
		Assertions.assertThat(l2.getPartition()).isNotNull();
	}

	@Test
	public void editEntities_Simple() throws Exception {
		createLibrary(1L, "Eagleton Library");
		refreshSession();

		Library library = getById(Library.T, 1L);
		Assertions.assertThat(library.getName()).isEqualTo("Eagleton Library");

		library.setName("Pawnee Library");
		refreshSession();

		library = getById(Library.T, 1L);
		Assertions.assertThat(library.getName()).isEqualTo("Pawnee Library");

	}

	@Test
	public void editEntities_List() throws Exception {
		createLibrary(1L, "Library");
		createBook(1L, "Book1");
		createBook(2L, "Book2");
		createBook(3L, "Book3");
		refreshSession();

		Library library = getById(Library.T, 1L);
		Book book1 = getById(Book.T, 1L);
		Book book2 = getById(Book.T, 2L);
		Book book3 = getById(Book.T, 3L);

		library.getBookList().addAll(asList(book1, book2, book3));
		refreshSession();

		library = getById(Library.T, 1L);
		Assertions.assertThat(library.getBookList()).hasSize(3);
	}

	@Test
	public void editEntities_editingSimpleDoesNotAffectCollections() throws Exception {
		createLibrary(1L, "Library");
		createBook(1L, "Book1");
		createBook(2L, "Book2");
		createBook(3L, "Book3");
		refreshSession();

		Library library = getById(Library.T, 1L);
		Book book1 = getById(Book.T, 1L);
		Book book2 = getById(Book.T, 2L);
		Book book3 = getById(Book.T, 3L);

		library.getBookList().addAll(asList(book1, book2, book3));
		refreshSession();

		library = getById(Library.T, 1L);
		library.setName("New Library");
		refreshSession();

		library = getById(Library.T, 1L);
		Assertions.assertThat(library.getName()).isEqualTo("New Library");
		Assertions.assertThat(library.getBookList()).hasSize(3);
	}

	@Test
	public void editEntities_Set() throws Exception {
		createLibrary(1L, "Library");
		createBook(1L, "Book1");
		createBook(2L, "Book2");
		createBook(3L, "Book3");
		refreshSession();

		Library library = getById(Library.T, 1L);
		Book book1 = getById(Book.T, 1L);
		Book book2 = getById(Book.T, 2L);
		Book book3 = getById(Book.T, 3L);

		library.getBookSet().addAll(asList(book1, book2, book3));
		refreshSession();

		library = getById(Library.T, 1L);
		Assertions.assertThat(library.getBookSet()).hasSize(3);
	}

	@Test
	public void editEntities_Map() throws Exception {
		createLibrary(1L, "Library");
		createBook(1L, "Book1");
		createBook(2L, "Book2");
		refreshSession();

		Library library = getById(Library.T, 1L);
		Book book1 = getById(Book.T, 1L);
		Book book2 = getById(Book.T, 2L);

		library.getBookByIsbn().putAll(asMap("b1", book1, "b2", book2));
		refreshSession();

		library = getById(Library.T, 1L);
		Assertions.assertThat(library.getBookByIsbn()).hasSize(2);
	}

	@Test
	public void deleteEntity() throws Exception {
		createLibrary(1L, "Library");
		refreshSession();

		Assertions.assertThat(listAllEntities()).isNotEmpty();

		Library library = getById(Library.T, 1L);
		session.deleteEntity(library);
		refreshSession();

		Assertions.assertThat(listAllEntities()).isEmpty();
	}

	private <T extends GenericEntity> T getById(EntityType<T> entityType, long id) {
		return (T) session.query().entity(entityType.getTypeSignature(), id).require();
	}

	private Library createLibrary(long id, String name) {
		Library library = session.create(Library.T);
		library.setName(name);
		library.setId(id);
		return library;
	}

	private Book createBook(long id, String title) {
		Book book = session.create(Book.T);
		book.setId(id);
		book.setTitle(title);
		return book;
	}

	private List<Object> listAllEntities() {
		return session.query().entities(EntityQueryBuilder.from(GenericEntity.T).done()).list();
	}

}
