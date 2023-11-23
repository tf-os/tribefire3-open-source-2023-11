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

import org.junit.Test;

import com.braintribe.model.access.model.Book;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.utils.genericmodel.GMCoreTools;

/**
 * Tests a manipulation which contains an entity reference to an entity the access doesn't know yet (and thus has to
 * load it from the delegate).
 * 
 * @author michael.lafite
 */
public class BasicAccessAdapterUnknownEntityReferencedInManipulationTest {

	@Test
	public void test() throws Exception {

		IncrementalAccess delegateAccess = GmTestTools.newSmoodAccessMemoryOnly();
		PersistenceGmSession delegateSession = new BasicPersistenceGmSession(delegateAccess);
		TestAccess access = new TestAccess(delegateAccess);

		Book book = delegateSession.create(Book.T);
		book.setId(0l);
		delegateSession.commit();

		PersistentEntityReference bookReference = book.reference();

		EntityProperty bookPagesProperty = EntityProperty.T.create();
		bookPagesProperty.setPropertyName("pages");
		bookPagesProperty.setReference(bookReference);

		// LocalEntityProperty bookPagesProperty = LocalEntityProperty.T.create();
		// bookPagesProperty.setPropertyName("pages");
		// bookPagesProperty.setEntity(book);

		ChangeValueManipulation manipulation = ChangeValueManipulation.T.create();
		manipulation.setNewValue(2);
		manipulation.setOwner(bookPagesProperty);

		ManipulationRequest manipulationRequest = ManipulationRequest.T.create();
		manipulationRequest.setManipulation(manipulation);

		access.applyManipulation(manipulationRequest);
	}

	private static class TestAccess extends BasicAccessAdapter {

		private final IncrementalAccess delegate;

		public TestAccess(IncrementalAccess delegate) {
			this.delegate = delegate;
		}

		@Override
		public ManipulationResponse applyManipulation(ManipulationRequest manipulationRequest)
				throws ModelAccessException {
			println("Received manipulation request.");
			return super.applyManipulation(manipulationRequest);
		}

		@Override
		protected void save(AdapterManipulationReport manipulationReport) throws ModelAccessException {
			println("Received manipulation report. (Ignored by this test.)");
		}

		@Override
		public EntityQueryResult queryEntities(EntityQuery query) throws ModelAccessException {
			println("Received entity query:\n" + GMCoreTools.getDescription(query));
			return super.queryEntities(query);
		}

		@Override
		public PropertyQueryResult queryProperty(PropertyQuery query) throws ModelAccessException {
			throw new RuntimeException("Unexpectedly received property query:\n" + GMCoreTools.getDescription(query));
		}

		@Override
		public SelectQueryResult query(SelectQuery query) throws ModelAccessException {
			println("Received select query:\n" + GMCoreTools.getDescription(query));
			SelectQueryResult result = this.delegate.query(query);
			return result;
		}

		@SuppressWarnings("unused")
		private void println(String msg) {
			// System.out.println(msg);
		}
	}

}
