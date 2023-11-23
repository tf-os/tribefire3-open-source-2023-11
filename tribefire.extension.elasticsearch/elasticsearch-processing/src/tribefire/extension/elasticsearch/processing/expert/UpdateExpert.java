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
import java.io.Reader;
import java.io.StringReader;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.json.spi.JsonProvider;
import tribefire.extension.elasticsearch.model.api.request.doc.UpdateById;
import tribefire.extension.elasticsearch.model.api.request.doc.UpdateRequest;
import tribefire.extension.elasticsearch.model.api.response.SuccessResult;

public class UpdateExpert extends BaseExpert<UpdateRequest, SuccessResult> {

	private static final Logger logger = Logger.getLogger(UpdateExpert.class);

	protected static JsonNodeFactory nodeFactory = new JsonNodeFactory(true);

	private String id;

	// ***************************************************************************************************
	// Configuration
	// ***************************************************************************************************

	private void setId(String id) {
		this.id = id;
	}

	@Override
	public SuccessResult process() {
		// Create the low-level client
		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();

		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

		// And create the API client
		ElasticsearchClient client = new ElasticsearchClient(transport);

		try {
			ObjectNode rootNode = nodeFactory.objectNode();

			TextNode textNode = nodeFactory.textNode("new/one");
			rootNode.set("path", textNode);

			String json = null;
			try {
				ObjectMapper mapper = new ObjectMapper();

				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

				json = mapper.writeValueAsString(rootNode);
			} catch (Exception e) {
				throw new ModelAccessException("Could not create a JSON object out of " + rootNode, e);
			}

			try (Reader input = new StringReader(json)) {
				JsonpMapper jsonpMapper = client._transport().jsonpMapper();
				JsonProvider jsonProvider = jsonpMapper.jsonProvider();

				JsonData jsonData = JsonData.from(jsonProvider.createParser(input), jsonpMapper);
			//@formatter:off
			UpdateResponse response = client.update(u -> u
					.index(this.indexName)
					.id(this.id).doc(jsonData),
					JsonData.class);
			
			return responseBuilder(SuccessResult.T, this.request)
				.responseEnricher(r -> {
						r.setSuccess(response.result() == Result.Updated);
				})
				.build();
			//@formatter:on
			}
		} catch (ElasticsearchException | IOException e) {
			logger.error("Error updating index!", e);
			throw new IllegalArgumentException(e.getMessage());
		}

	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	public static UpdateExpert forUpdateById(UpdateById request) {
		return createExpert(UpdateExpert::new, (expert) -> {
			expert.setRequest(request);
			expert.setIndexName(request.getIndexName());
			expert.setId(request.getIndexId());
		});
	}

}
