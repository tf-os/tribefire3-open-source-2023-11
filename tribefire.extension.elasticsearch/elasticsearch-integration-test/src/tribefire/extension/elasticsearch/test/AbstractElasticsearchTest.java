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
package tribefire.extension.elasticsearch.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;

import tribefire.extension.elasticsearch.model.api.request.admin.CreateIndexByName;
import tribefire.extension.elasticsearch.model.api.request.admin.DeleteIndexByName;
import tribefire.extension.elasticsearch.model.api.request.doc.IndexResources;
import tribefire.extension.elasticsearch.model.api.response.IndexResponse;
import tribefire.extension.elasticsearch.model.api.response.SuccessResult;

public abstract class AbstractElasticsearchTest extends AbstractTribefireQaTest {

	protected PersistenceGmSession cortexSession;
	private PersistenceGmSessionFactory sessionFactory;

	protected static final File pdf = new File("res/pdf/pdf-4-8.pdf");
	protected static final File pdf_simple = new File("res/pdf/Hello CAPRI 1.pdf");

	// -----------------------------------------------------------------------
	// CLASS - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@BeforeClass
	public static void beforeClass() throws Exception {
		// nothing so far
	}

	@AfterClass
	public static void afterClass() throws Exception {
		// nothing so far
	}

	// -----------------------------------------------------------------------
	// TEST - SETUP / TEARDOWN
	// -----------------------------------------------------------------------

	@Before
	public void before() throws Exception {
		sessionFactory = apiFactory().buildSessionFactory();
		cortexSession = sessionFactory.newSession("cortex");
	}

	@After
	public void after() throws Exception {
		// nothing so far
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	protected void createIndex(Consumer<CreateIndexByName> consumer) {
		CreateIndexByName request = CreateIndexByName.T.create();

		consumer.accept(request);

		SuccessResult result = (SuccessResult) request.eval(cortexSession).get();
		assertEquals(true, result.getSuccess());
	}

	protected void deleteIndex(Consumer<DeleteIndexByName> consumer) {
		DeleteIndexByName request = DeleteIndexByName.T.create();

		consumer.accept(request);

		SuccessResult result = (SuccessResult) request.eval(cortexSession).get();
		assertEquals(true, result.getSuccess());
	}

	protected IndexResponse indexResources(Consumer<IndexResources> consumer) {
		IndexResources request = IndexResources.T.create();

		consumer.accept(request);

		int expectedNumberOfIndexes = request.getIndexNames().size() * request.getResources().size();

		IndexResponse response = (IndexResponse) request.eval(cortexSession).get();
		assertIndexResponse(response, expectedNumberOfIndexes, request.getIndexNames());

		return response;
	}

	// -----------------------------------------------------------------------
	// ASSERTIONS
	// -----------------------------------------------------------------------

	protected void assertIndexResponse(IndexResponse response, int expectedNumberOfIndexes, List<String> indexNames) {
		assertThat(response).isNotNull();
		assertThat(response.getItems().size()).isEqualTo(expectedNumberOfIndexes);
		response.getItems().forEach(item -> {
			assertThat(item.getIndexName()).isIn(indexNames);
			assertThat(item.getIndexId()).isNotBlank();
			assertThat(item.getResult()).isNotBlank();
		});
	}
}
