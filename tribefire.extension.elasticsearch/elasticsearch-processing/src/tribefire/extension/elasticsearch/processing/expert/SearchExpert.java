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
package tribefire.extension.elasticsearch.processing.expert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.braintribe.logging.Logger;
import com.braintribe.utils.ArrayTools;
import com.braintribe.utils.StringTools;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import tribefire.extension.elasticsearch.model.api.request.doc.SearchAsYouType;
import tribefire.extension.elasticsearch.model.api.request.doc.SearchRequest;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.AbstractJunction;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Condition;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Conjunction;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Disjunction;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.Negation;
import tribefire.extension.elasticsearch.model.api.request.doc.conditions.ValueComparison;
import tribefire.extension.elasticsearch.model.api.response.SearchResponse;
import tribefire.extension.elasticsearch.model.api.response.SearchResponseHit;

public class SearchExpert extends BaseExpert<SearchRequest, SearchResponse> {

	private static final Logger logger = Logger.getLogger(SearchExpert.class);

	private String term;

	private Condition condition;

	private List<String> parentIds;
	private Boolean recursively;

	private Integer pageOffset;
	private Integer pageLimit;

	// ***************************************************************************************************
	// Configuration
	// ***************************************************************************************************

	protected void setTerm(String term) {
		this.term = term;
	}

	protected void setCondition(Condition condition) {
		this.condition = condition;
	}

	private void setParentIds(List<String> parentIds) {
		this.parentIds = parentIds;
	}

	protected void setRecursively(Boolean recursively) {
		this.recursively = recursively;
	}

	protected void setPageOffset(Integer pageOffset) {
		this.pageOffset = pageOffset;
	}

	protected void setPageLimit(Integer pageLimit) {
		this.pageLimit = pageLimit;
	}

	@Override
	public SearchResponse process() {
		// Create the low-level client
		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();

		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

		// And create the API client
		ElasticsearchClient client = new ElasticsearchClient(transport);

		try {
			BoolQuery.Builder builder = new BoolQuery.Builder();

			AtomicReference<BoolQuery.Builder> builderRef = new AtomicReference<>(builder);

			if (!StringTools.isEmpty(this.term)) {
				builderRef.set(
						builder.must(m -> m.multiMatch(mma -> mma.fields(ArrayTools.toList("title", "title._2gram", "title._3gram", "title._4gram"))
								.type(TextQueryType.BoolPrefix).query(this.term))));
			}

			builderRef.set(builder.must(parseCondition(this.condition)));

			BoolQuery.Builder pathBuilder = new BoolQuery.Builder();

			AtomicReference<BoolQuery.Builder> pathBuilderRef = new AtomicReference<>(pathBuilder);

			boolean isRootQuery = this.parentIds.isEmpty();

			if (Boolean.TRUE.equals(!this.recursively) && isRootQuery) {
				setParentIds(Arrays.asList("/"));
			}

			if (!this.parentIds.isEmpty()) {
				this.parentIds.stream().forEach(id -> {
					if (Boolean.TRUE.equals(this.recursively)) {
						pathBuilderRef.set(pathBuilder.should(s -> s.term(t -> t.field("$path.tree").value(id))));
					} else {
						pathBuilderRef.set(pathBuilder.should(s -> s.matchPhrase(m -> m.field("$path").query(id))));
					}
				});

				BoolQuery pathBoolQuery = pathBuilder.build();
				if (Boolean.TRUE.equals(this.recursively)) {
					builder.filter(f -> f.bool(pathBoolQuery));
				} else {
					builder.must(pathBoolQuery._toQuery());
				}
			}

			BoolQuery boolQuery = builder.build();
			//@formatter:off
			co.elastic.clients.elasticsearch.core.SearchRequest searchRequest = 
				co.elastic.clients.elasticsearch.core.SearchRequest.of(s -> s
					.index(this.indexName)
					.query(q -> q      
						.bool(boolQuery)
						)
					.from(this.pageOffset)
					.size(this.pageLimit > 0 ? this.pageLimit : 10000)
					);
			//@formatter:on

			co.elastic.clients.elasticsearch.core.SearchResponse<Void> response = client.search(searchRequest, Void.class);

			TotalHits total = response.hits().total();

			List<SearchResponseHit> responseHits = new ArrayList<>();

			List<Hit<Void>> hits = response.hits().hits();
			for (Hit<Void> hit : hits) {
				SearchResponseHit responseHit = SearchResponseHit.T.create();
				responseHit.setIndexId(hit.id());
				responseHit.setScore(hit.score());
				responseHits.add(responseHit);
			}
			//@formatter:off
			return responseBuilder(SearchResponse.T, this.request)
					.responseEnricher(r -> {
						r.setTotalHits(total.value());
						r.setHits(responseHits);
					})
					.build();
			//@formatter:on
		} catch (ElasticsearchException | IOException e) {
			logger.error("Error searching!", e);
			throw new IllegalArgumentException(e.getMessage());
		}

	}

	// ***************************************************************************************************
	// Helpers
	// ***************************************************************************************************

	Query parseCondition(Condition condition) {
		BoolQuery.Builder builder = new BoolQuery.Builder();

		if (condition instanceof Conjunction conjunction) {
			return parseJunction(conjunction);
		} else if (condition instanceof Disjunction disjunction) {
			return parseJunction(disjunction);
		} else if (condition instanceof Negation negation) {
			return parseNegation(negation);
		} else if (condition instanceof ValueComparison comparison) {
			return parseValueComparison(comparison);
		}
		return builder.build()._toQuery();
	}

	Query parseJunction(AbstractJunction junction) {
		BoolQuery.Builder builder = QueryBuilders.bool();
		if (junction instanceof Conjunction conjunction) {
			conjunction.getOperands().stream().forEach(o -> builder.must(parseCondition(o)));
		} else if (junction instanceof Disjunction disjunction) {
			disjunction.getOperands().stream().forEach(o -> builder.should(parseCondition(o)));
		}
		return builder.build()._toQuery();
	}

	Query parseNegation(Negation negation) {
		return QueryBuilders.bool().mustNot(parseCondition(negation.getOperand())).build()._toQuery();
	}

	Query parseValueComparison(ValueComparison comparison) {
		String leftOperand = (String) comparison.getLeftOperand();
		String rightOperand = (String) comparison.getRightOperand();
		Query query = null;
		switch (comparison.getOperator()) {
			case equal:
				query = !comparison.getMatchPhrase() ? QueryBuilders.match().field(leftOperand).query(rightOperand).build()._toQuery()
						: QueryBuilders.matchPhrase().field(leftOperand).query(rightOperand).build()._toQuery();
				break;
			case notEqual:
				query = QueryBuilders.bool().mustNot(QueryBuilders.match().field(leftOperand).query(rightOperand).build()._toQuery()).build()
						._toQuery();
				break;
			case less:
				query = QueryBuilders.range().field(leftOperand).lt(JsonData.of(rightOperand)).build()._toQuery();
				break;
			case lessOrEqual:
				query = QueryBuilders.range().field(leftOperand).lte(JsonData.of(rightOperand)).build()._toQuery();
				break;
			case greater:
				query = QueryBuilders.range().field(leftOperand).gt(JsonData.of(rightOperand)).build()._toQuery();
				break;
			case greaterOrEqual:
				query = QueryBuilders.range().field(leftOperand).gte(JsonData.of(rightOperand)).build()._toQuery();
				break;
			default:

		}
		if (!comparison.getFilter()) {
			return QueryBuilders.bool().must(query).build()._toQuery();
		} else {
			return QueryBuilders.bool().filter(query).build()._toQuery();
		}
	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	public static SearchExpert forSearch(SearchRequest request) {
		return createExpert(SearchExpert::new, expert -> {
			expert.setRequest(request);
			expert.setIndexName(request.getIndexName());
			expert.setCondition(request.getCondition());
			expert.setParentIds(request.getParentIds());
			expert.setRecursively(request.getRecursively());
			expert.setPageOffset(request.getPageOffset());
			expert.setPageLimit(request.getPageLimit());
		});
	}

	public static SearchExpert forSearchAsYouType(SearchAsYouType request) {
		return createExpert(SearchExpert::new, expert -> {
			expert.setRequest(request);
			expert.setIndexName(request.getIndexName());
			expert.setTerm(request.getTerm());
			expert.setCondition(request.getCondition());
			expert.setParentIds(request.getParentIds());
			expert.setRecursively(request.getRecursively());
			expert.setPageOffset(request.getPageOffset());
			expert.setPageLimit(request.getPageLimit());
		});
	}

}
