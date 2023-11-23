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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.Base64;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.FileTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.json.spi.JsonProvider;
import tribefire.extension.elasticsearch.model.api.request.doc.IndexRequest;
import tribefire.extension.elasticsearch.model.api.request.doc.IndexResources;
import tribefire.extension.elasticsearch.model.api.response.IndexItemResponse;
import tribefire.extension.elasticsearch.model.api.response.IndexResponse;

public class IndexExpert extends BaseExpert<IndexRequest, IndexResponse> {

	private static final Logger logger = Logger.getLogger(IndexExpert.class);

	protected static JsonNodeFactory nodeFactory = new JsonNodeFactory(true);

	private List<String> indexNames;

	private List<Resource> resources;

	protected PersistenceGmSession session;

	protected String path;

	// ***************************************************************************************************
	// Configuration
	// ***************************************************************************************************

	private void setIndexNames(List<String> indexNames) {
		this.indexNames = indexNames;
	}

	private void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public IndexResponse process() {
		// Create the low-level client
		RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200)).build();

		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

		// And create the API client
		ElasticsearchClient client = new ElasticsearchClient(transport);

		if (!CommonTools.isEmpty(this.resources)) {
			BulkRequest.Builder br = new BulkRequest.Builder();

			this.resources.stream().forEach((resource) -> {
				try (InputStream in = session.resources().openStream(resource)) {
					Path filePath = Files.createTempFile(FileTools.getNameWithoutExtension(resource.getName()),
							FileTools.getExtension(resource.getName()));
					Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);

					String base64Content = Base64.encodeBytes(Files.readAllBytes(filePath), Base64.DONT_BREAK_LINES);

					ObjectNode rootNode = nodeFactory.objectNode();

					TextNode titleNode = nodeFactory.textNode(FileTools.getNameWithoutExtension(resource.getName()));
					rootNode.set("title", titleNode);
					TextNode textNode = nodeFactory.textNode(base64Content);
					rootNode.set("attachmentData", textNode);
					TextNode pathNode = nodeFactory.textNode(this.path);
					rootNode.set("$path", pathNode);

					String json = null;
					try {
						ObjectMapper mapper = new ObjectMapper();

						mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
						cleanTypes(rootNode);

						json = mapper.writeValueAsString(rootNode);
					} catch (Exception e) {
						throw new ModelAccessException("Could not create a JSON object out of " + rootNode, e);
					}

					try (Reader input = new StringReader(json)) {
						JsonpMapper jsonpMapper = client._transport().jsonpMapper();
						JsonProvider jsonProvider = jsonpMapper.jsonProvider();

						JsonData jsonData = JsonData.from(jsonProvider.createParser(input), jsonpMapper);
						this.indexNames.stream().forEach((indexName) -> {
							//@formatter:off
							br.operations(op -> op           
							        .index(idx -> idx            
							        		.index(indexName)
											.document(jsonData)
											.pipeline("attachmentPipeline")
							        )
							    );
							//@formatter:off
						});
					}
				} catch (IOException e) {
					logger.error("Error reading resources!", e);
					throw new IllegalArgumentException(e.getMessage());
				}
			});
			
			try {
				BulkResponse response = client.bulk(br.build());
			
				//@formatter:off
				return responseBuilder(IndexResponse.T, this.request)
					.responseEnricher(r -> {
						List<IndexItemResponse> items = response.items().stream().map(item -> {
							IndexItemResponse responseItem = IndexItemResponse.T.create();
							responseItem.setIndexName(item.index());
							responseItem.setIndexId(item.id());
							responseItem.setResult(item.result());
							if (item.error() != null) {
					            responseItem.setError(item.error().reason());
					        }
				            return responseItem;
						}).collect(Collectors.toList());
						r.setItems(items);
						r.setHasErrors(Boolean.valueOf(response.errors()));
					})
					.build();
				//@formatter:on
			} catch (IOException | ElasticsearchException e) {
				logger.error("Error indexing resources!", e);
				throw new IllegalArgumentException(e.getMessage());
			}
		} else {
			throw new IllegalArgumentException("At least one document must exist!");
		}

	}

	// ***************************************************************************************************
	// Initialization
	// ***************************************************************************************************

	public static IndexExpert forIndexResources(IndexResources request, PersistenceGmSession session) {
		return createExpert(IndexExpert::new, (expert) -> {
			expert.setRequest(request);
			expert.setSession(session);
			expert.setIndexNames(request.getIndexNames());
			expert.setPath(request.getPath());
			expert.setResources(request.getResources());
		});
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	protected static void cleanTypes(ObjectNode on) {
		on.remove("_type");
		on.remove("_id");
		Iterator<Map.Entry<String, JsonNode>> it = on.fields();
		while (it.hasNext()) {
			Map.Entry<String, JsonNode> entry = it.next();
			JsonNode jn = entry.getValue();
			if (jn instanceof ObjectNode) {
				cleanTypes((ObjectNode) jn);
			}
		}
	}

}
