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
import static com.braintribe.utils.lcd.CollectionTools2.asMap;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.impls.PopulationProvidingPartitionRemovingAccess;
import com.braintribe.model.access.model.BasicAccesAdapterTestModelProvider;
import com.braintribe.model.access.model.Book;
import com.braintribe.model.access.model.Library;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.processing.query.fluent.PropertyQueryBuilder;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.record.ListRecord;

/**
 * This tests queries with {@link BasicAccessAdapter} which also clone the result. We had a {@link TraversingCriterion}
 * related bug - {@link ListRecord}'s properties were being set as absent.
 * 
 * @see PopulationProvidingPartitionRemovingAccess
 */
public class WithCloning_BasicAccessAdapterTests {

	private static Library la, lb;
	private static Book ba1, ba2, bb1, bb2;

	// ##########################################
	// ## . . . . Access Configuration . . . . ##
	// ##########################################

	private final BasicAccessAdapter access;
	
	public WithCloning_BasicAccessAdapterTests() {
		access = new PopulationProvidingPartitionRemovingAccess(configureSmood());
		access.setMetaModelProvider(BasicAccesAdapterTestModelProvider.INSTANCE);
	}	

	private static Smood configureSmood() {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(BasicAccesAdapterTestModelProvider.INSTANCE.get());

		la = newLibrary("la");
		lb = newLibrary("lb");

		ba1 = newBook("ba1", 100, la);
		ba2 = newBook("ba2", 200, la);
		bb1 = newBook("bb1", 300, lb);
		bb2 = newBook("bb2", 400, lb);

		la.setBookByIsbn(asMap("ISBN-ba1", ba1, "ISBN-ba2", ba2));
		lb.setBookByIsbn(asMap("ISBN-bb1", bb1, "ISBN-bb2", bb2));

		smood.initialize(Arrays.asList(la, lb, ba1, ba2, bb1, bb2));

		return smood;
	}

	private static Library newLibrary(String name) {
		Library result = newEntity(Library.class);
		result.setName(name);

		return result;
	}

	private static Book newBook(String title, int pages, Library library) {
		Book result = newEntity(Book.class);
		result.setTitle(title);
		result.setPages(pages);
		result.setLibrary(library);

		return result;
	}

	private static <T extends GenericEntity> T newEntity(Class<T> clazz) {
		return GMF.getTypeReflection().<T> getEntityType(clazz).create();
	}

	// ##########################################
	// ## . . . . . . Actual Tests . . . . . . ##
	// ##########################################

	@Test
	public void mapPropertyQuery() throws Exception {
		Map<String, Book> map = runQuery(PropertyQueryBuilder.forProperty(Library.T, la.getId(), "bookByIsbn").done());

		assertThat(map).hasSize(2);
	}

	// ##########################################
	// ## . . . . Assertions and Helpers. . . .##
	// ##########################################

	private <T> T runQuery(PropertyQuery query) {
		try {
			return (T) access.queryProperty(query).getPropertyValue();

		} catch (ModelAccessException e) {
			throw new RuntimeException("Query evaluation failed", e);
		}
	}

}
