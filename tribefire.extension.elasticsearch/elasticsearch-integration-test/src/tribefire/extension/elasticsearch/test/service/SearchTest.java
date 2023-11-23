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
package tribefire.extension.elasticsearch.test.service;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.processing.session.api.resource.ResourceCreateBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.VerySlow;
import com.braintribe.utils.FileTools;

import tribefire.extension.elasticsearch.model.api.request.doc.Operator;
import tribefire.extension.elasticsearch.model.api.request.doc.SearchAsYouType;
import tribefire.extension.elasticsearch.model.api.request.doc.SearchRequest;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Conjunction;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Disjunction;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Negation;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.ValueComparison;
import tribefire.extension.elasticsearch.model.api.response.SearchResponse;
import tribefire.extension.elasticsearch.test.AbstractElasticsearchTest;

@Category(VerySlow.class)
public class SearchTest extends AbstractElasticsearchTest {

	private static ValueComparison sizeComparisonGreater;
	private static ValueComparison sizeComparisonLess;
	private static ValueComparison authorComparisonEqual;

	private String indexName;

	@BeforeClass
	public static void beforeClass() {
		sizeComparisonGreater = ValueComparison.T.create();
		sizeComparisonGreater.setOperator(Operator.greater);
		sizeComparisonGreater.setLeftOperand("attachment.content_length");
		sizeComparisonGreater.setRightOperand("10000");

		sizeComparisonLess = ValueComparison.T.create();
		sizeComparisonLess.setOperator(Operator.less);
		sizeComparisonLess.setLeftOperand("attachment.content_length");
		sizeComparisonLess.setRightOperand("10000");

		authorComparisonEqual = ValueComparison.T.create();
		authorComparisonEqual.setOperator(Operator.equal);
		authorComparisonEqual.setLeftOperand("attachment.author");
		authorComparisonEqual.setRightOperand("mfung4");
	}

	@Before
	public void setup() {
		indexName = UUID.randomUUID().toString();

		createIndex(request -> {
			request.setIndexName(indexName);
		});

		indexResources(request -> {
			request.setIndexNames(asList(indexName));

			ResourceCreateBuilder resourceBuilder = cortexSession.resources().create().name(pdf.getName());
			Resource resource = FileTools.read(pdf).fromInputStream(resourceBuilder::store);
			request.setResources(asList(resource));
		});
		indexResources(request -> {
			request.setIndexNames(asList(indexName));

			ResourceCreateBuilder resourceBuilder = cortexSession.resources().create().name(pdf_simple.getName());
			Resource resource = FileTools.read(pdf_simple).fromInputStream(resourceBuilder::store);
			request.setResources(asList(resource));
			request.setPath("/firstLevel");
		});
		indexResources(request -> {
			request.setIndexNames(asList(indexName));

			ResourceCreateBuilder resourceBuilder = cortexSession.resources().create().name(pdf.getName());
			Resource resource = FileTools.read(pdf).fromInputStream(resourceBuilder::store);
			request.setResources(asList(resource));
			request.setPath("/firstLevel/secondLevel");
		});

		await().timeout(10, SECONDS).pollDelay(5, SECONDS).untilAsserted(() -> assertTrue(true));
	}

	@After
	public void cleanup() {
		deleteIndex(request -> {
			request.setIndexName(indexName);
		});
	}

	// -----------------------------------------------------------------------
	// TESTS
	// -----------------------------------------------------------------------

	@Test
	public void testSearchInRootNoCondition() {
		SearchRequest request = SearchRequest.T.create();

		request.setIndexName(indexName);

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(1, result.getHits().size());
	}

	@Test
	public void testSearchInRootRecursivelyNoCondition() {
		SearchRequest request = SearchRequest.T.create();

		request.setIndexName(indexName);
		request.setRecursively(true);

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(3, result.getHits().size());
	}

	@Test
	public void testSearchInFolderNoCondition() {
		SearchRequest request = SearchRequest.T.create();

		request.setIndexName(indexName);
		request.setParentIds(asList("/firstLevel"));

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(1, result.getHits().size());
	}

	@Test
	public void testSearchInFolderRecursivelyNoCondition() {
		SearchRequest request = SearchRequest.T.create();

		request.setIndexName(indexName);
		request.setParentIds(asList("/firstLevel"));
		request.setRecursively(true);

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(2, result.getHits().size());
	}

	@Test
	public void testSearchInRootValueComparison() {
		SearchRequest requestGreater = SearchRequest.T.create();

		requestGreater.setIndexName(indexName);
		requestGreater.setCondition(sizeComparisonGreater);

		SearchResponse resultGreater = (SearchResponse) requestGreater.eval(cortexSession).get();
		assertNotNull(resultGreater.getHits());
		assertEquals(1, resultGreater.getHits().size());

		SearchRequest requestLess = SearchRequest.T.create();

		requestLess.setIndexName(indexName);
		requestLess.setCondition(sizeComparisonLess);

		SearchResponse resultLess = (SearchResponse) requestLess.eval(cortexSession).get();
		assertNotNull(resultLess.getHits());
		assertEquals(0, resultLess.getHits().size());

		SearchRequest requestEqual = SearchRequest.T.create();

		requestEqual.setIndexName(indexName);
		requestEqual.setCondition(authorComparisonEqual);

		SearchResponse resultEqual = (SearchResponse) requestEqual.eval(cortexSession).get();
		assertNotNull(resultEqual.getHits());
		assertEquals(1, resultEqual.getHits().size());
	}

	@Test
	public void testSearchInRootNegation() {
		SearchRequest request = SearchRequest.T.create();

		Negation negation = Negation.T.create();
		negation.setOperand(authorComparisonEqual);

		request.setIndexName(indexName);
		request.setCondition(negation);

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(0, result.getHits().size());
	}

	@Test
	public void testSearchInRootConjunction() {
		SearchRequest request = SearchRequest.T.create();

		Conjunction conjunction = Conjunction.T.create();
		conjunction.setOperands(asList(authorComparisonEqual, sizeComparisonLess));

		request.setIndexName(indexName);
		request.setCondition(conjunction);

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(0, result.getHits().size());
	}

	@Test
	public void testSearchInRootDisjunction() {
		SearchRequest request = SearchRequest.T.create();

		Disjunction disjunction = Disjunction.T.create();
		disjunction.setOperands(asList(authorComparisonEqual, sizeComparisonLess));

		request.setIndexName(indexName);
		request.setCondition(disjunction);

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(1, result.getHits().size());
	}

	@Test
	public void testSearchAsYouTypeInRootNoCondition() {
		SearchAsYouType request = SearchAsYouType.T.create();

		request.setIndexName(indexName);
		request.setTerm("pdf");

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(1, result.getHits().size());
	}

	@Test
	public void testSearchAsYouTypeInRootRecursivelyNoCondition() {
		SearchAsYouType request = SearchAsYouType.T.create();

		request.setIndexName(indexName);
		request.setRecursively(true);
		request.setTerm("pdf");

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(2, result.getHits().size());
	}

	@Test
	public void testSearchAsYouTypeInFolderNoCondition() {
		SearchAsYouType request = SearchAsYouType.T.create();

		request.setIndexName(indexName);
		request.setParentIds(asList("/firstLevel"));
		request.setTerm("pdf");

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(0, result.getHits().size());
	}

	@Test
	public void testSearchAsYouTypeInFolderRecursivelyNoCondition() {
		SearchAsYouType request = SearchAsYouType.T.create();

		request.setIndexName(indexName);
		request.setParentIds(asList("/firstLevel"));
		request.setRecursively(true);
		request.setTerm("pdf");

		SearchResponse result = (SearchResponse) request.eval(cortexSession).get();
		assertNotNull(result.getHits());
		assertEquals(1, result.getHits().size());
	}
}
