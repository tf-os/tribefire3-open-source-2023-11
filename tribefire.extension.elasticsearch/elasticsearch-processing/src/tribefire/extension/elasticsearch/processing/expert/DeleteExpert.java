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
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import tribefire.extension.elasticsearch.model.api.request.doc.DeleteById;
import tribefire.extension.elasticsearch.model.api.request.doc.DeleteRequest;
import tribefire.extension.elasticsearch.model.api.response.SuccessResult;

public class DeleteExpert extends BaseExpert<DeleteRequest, SuccessResult> {

	private static final Logger logger = Logger.getLogger(DeleteExpert.class);

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
			//@formatter:off
			DeleteResponse response = client.delete(i -> i
					.index(this.indexName)
					.id(this.id));
			
			return responseBuilder(SuccessResult.T, this.request)
				.responseEnricher(r -> {
						r.setSuccess(response.result() == Result.Deleted);
				})
				.build();
			//@formatter:on
		} catch (ElasticsearchException | IOException e) {
			logger.error("Error deleting index!", e);
			throw new IllegalArgumentException(e.getMessage());
		}

	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	public static DeleteExpert forDeleteById(DeleteById request) {
		return createExpert(DeleteExpert::new, (expert) -> {
			expert.setRequest(request);
			expert.setIndexName(request.getIndexName());
			expert.setId(request.getIndexId());
		});
	}

}
