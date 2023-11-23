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

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.braintribe.logging.Logger;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.ingest.GetPipelineResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import tribefire.extension.elasticsearch.model.api.request.admin.CreateIndexByName;
import tribefire.extension.elasticsearch.model.api.request.admin.CreateIndexRequest;
import tribefire.extension.elasticsearch.model.api.response.SuccessResult;

public class CreateIndexExpert extends BaseExpert<CreateIndexRequest, SuccessResult> {

	private static final Logger logger = Logger.getLogger(CreateIndexExpert.class);

	@Override
	public SuccessResult process() {
		// Create the low-level client
		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();

		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

		// And create the API client
		ElasticsearchClient client = new ElasticsearchClient(transport);
		try {
			BooleanResponse existsResponse = client.indices().exists(b -> b.index(this.indexName));
			if (existsResponse.value()) {
				throw new IllegalArgumentException("Index with name " + this.indexName + " already exists!");
			}

			//@formatter:off
			CreateIndexResponse createResponse = client.indices()
					.create(b -> b
							.index(this.indexName)
							// path_hierarchy
							.settings(s -> s
									.analysis(a -> a
											.analyzer("path_tree", an -> an
													.custom(c -> c
															.tokenizer("path_tokenizer")))
											.tokenizer("path_tokenizer", t -> t
													.definition(d -> d
															.pathHierarchy(ph -> ph
																	.bufferSize(1024)
																	.delimiter("/")
																	.replacement("/")
																	.reverse(false)
																	.skip(0))))))
							.mappings(m -> m
									.properties("$path", p -> p
											.keyword(t -> t
													.fields("tree", f -> f
															.text(txt -> txt
																	.analyzer("path_tree")))))
									// autocomplete
									.properties("title", p -> p
											.searchAsYouType(s -> s
													.maxShingleSize(4))))
							);
			//@formatter:on

			GetPipelineResponse getPipelineResponse = client.ingest().getPipeline(p -> p.id("attachmentPipeline"));

			if (getPipelineResponse.result().isEmpty()) {
				//@formatter:off
				client.ingest().putPipeline(p -> p
						.id("attachmentPipeline")
						.processors(pr -> pr
								.attachment(a -> a
										.field("attachmentData")
										.indexedChars(-1L))
								)
						);
				//@formatter:on
			}

			//@formatter:off
			return responseBuilder(SuccessResult.T, this.request)
				.responseEnricher(r -> {
						r.setSuccess(createResponse.acknowledged());
				})
				.build();
			//@formatter:on
		} catch (ElasticsearchException | IOException e) {
			logger.error("Error creating index!", e);
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	public static CreateIndexExpert forCreateIndexByName(CreateIndexByName request) {
		return createExpert(CreateIndexExpert::new, (expert) -> {
			expert.setRequest(request);
			expert.setIndexName(request.getIndexName());
		});
	}

}
