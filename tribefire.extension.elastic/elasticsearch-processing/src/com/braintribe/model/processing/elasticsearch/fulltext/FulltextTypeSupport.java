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
package com.braintribe.model.processing.elasticsearch.fulltext;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.ingest.GetPipelineResponse;
import org.elasticsearch.action.ingest.WritePipelineResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentType;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.elastic.ElasticConstants;
import com.braintribe.model.processing.elasticsearch.ContextualizedElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.ContextualizedElasticsearchClientImpl;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.util.ElasticsearchUtils;

public class FulltextTypeSupport {

	private final static Logger logger = Logger.getLogger(FulltextTypeSupport.class);
	public static boolean ANALYZABLE_DEFAULT = false;
	private static boolean attachmentPipelineCreated = false;

	public static void updateMappings(ElasticsearchClient client, String index, Integer maxResultWindowSize) throws Exception {
		ContextualizedElasticsearchClientImpl cClient = new ContextualizedElasticsearchClientImpl(client, null);
		cClient.setIndex(index);
		cClient.setMaxResultWindow(maxResultWindowSize);
		updateMappings(cClient);
	}

	public static void updateMappings(ContextualizedElasticsearchClient client) throws Exception {

		String index = client.getIndex();

		IndicesAdminClient indicesAdminClient = client.getIndicesAdminClient();

		boolean indexNewlyCreated = ElasticsearchUtils.createIndexIfNonexistent(client);

		try {
			ensureFulltextType(client, indicesAdminClient);
			logger.info(() -> "Done with ensuring fulltext type: " + ElasticConstants.FULLTEXT_INDEX_TYPE + " in index " + index);

		} catch (Exception e) {
			if (indexNewlyCreated) {
				logger.info(() -> "Not trying to delete/create the index " + index + " again because it was already newly created.");
				throw e;
			} else {
				logger.info(() -> "Because of \"" + e.getMessage() + "\", we will try to delete/recreate index " + index);
				try {
					ElasticsearchUtils.deleteIndex(indicesAdminClient, index);
				} catch (Exception deletionException) {
					deletionException.addSuppressed(e);
					throw new Exception("Could not delete index " + index + " (which was tried because a type could not be ensured)",
							deletionException);
				}
				logger.info(() -> "Deleted index " + index);
				try {
					ElasticsearchUtils.createIndexIfNonexistent(client);
				} catch (Exception creationException) {
					creationException.addSuppressed(e);
					throw new Exception("Could not create index " + index + " after deleting it (because of a type incompatibility)",
							creationException);
				}
				logger.info(() -> "Recreated index " + index);
				// Try again
				ensureFulltextType(client, indicesAdminClient);
				logger.info(() -> "Done with ensuring fulltext type: " + ElasticConstants.FULLTEXT_INDEX_TYPE + " in recreated index " + index);
			}
		}
	}

	public static void ensureFulltextType(ContextualizedElasticsearchClient client, IndicesAdminClient indicesAdminClient) throws Exception {

		boolean typeExists = ElasticsearchUtils.typeExists(indicesAdminClient, client.getIndex(), ElasticConstants.FULLTEXT_INDEX_TYPE);
		logger.debug("Type " + ElasticConstants.FULLTEXT_INDEX_TYPE + " exists: " + typeExists);

		if (!typeExists) {
			createFulltextType(client, indicesAdminClient);
		}
	}

	public static void createFulltextType(ContextualizedElasticsearchClient client, IndicesAdminClient indicesAdminClient) throws Exception {

		StringBuilder mapping = new StringBuilder();
		mapping.append("{\n");
		mapping.append("   \"properties\" : {\n");

		boolean sourceEnabled = true;

		mapping.append("    \"partition\" : { \"type\": \"text\" }, \n");
		mapping.append("    \"globalId\" : { \"type\": \"text\" }, \n");
		mapping.append("    \"typeSignature\" : { \"type\": \"text\" }, \n");
		mapping.append("    \"propertyData\" : { \"type\": \"text\" }, \n");
		mapping.append("    \"attachmentData\" : { \"type\": \"text\" } \n");

		mapping.append("   },\n");
		mapping.append("  \"_source\": { \"enabled\":  " + sourceEnabled + " }\n");
		mapping.append("}\n");

		try {
			createType(client, indicesAdminClient, ElasticConstants.FULLTEXT_INDEX_TYPE, mapping.toString());
		} catch (Exception e) {
			throw new Exception("Could not create type " + ElasticConstants.FULLTEXT_INDEX_TYPE + " with mapping: " + mapping.toString(), e);
		}

		logger.info("Created: " + ElasticConstants.FULLTEXT_INDEX_TYPE + " on index: " + client.getIndex());
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append(" with mapping:\n");
			sb.append(mapping.toString());
			logger.debug(sb.toString());
		}
	}

	public static void createType(ContextualizedElasticsearchClient client, IndicesAdminClient indicesAdminClient, String type, String mapping)
			throws Exception {
		PutMappingResponse putMappingResponse = indicesAdminClient.preparePutMapping(client.getIndex()).setType(type)
				.setSource(mapping.toString(), XContentType.JSON).get();
		boolean acknowledged = putMappingResponse.isAcknowledged();
		if (!acknowledged) {
			throw new Exception("Could not create type " + type);
		}

		ClusterAdminClient clusterAdminClient = client.getClusterAdminClient();

		if (!attachmentPipelineCreated) {
			attachmentPipelineCreated = true;

			GetPipelineResponse getPipelineResponse = clusterAdminClient.prepareGetPipeline("attachmentPipeline").get();
			boolean found = getPipelineResponse.isFound();

			if (!found) {
				// WritePipelineResponse writePipelineResponse =
				// clusterAdminClient.prepareDeletePipeline("attachmentPipeline").get();
				// acknowledged = writePipelineResponse.isAcknowledged();

				BytesReference pipelineSource = jsonBuilder().startObject().field("description", "Attachment pipeline").startArray("processors")
						.startObject().startObject("attachment").field("field", "attachmentData").field("indexed_chars", -1).endObject().endObject()
						.endArray().endObject().bytes();
				WritePipelineResponse writePipelineResponse2 = clusterAdminClient
						.preparePutPipeline("attachmentPipeline", pipelineSource, XContentType.JSON).get();
				acknowledged = writePipelineResponse2.isAcknowledged();
				logger.debug("Created ingest pipeline.");
			} else {
				logger.debug("Found ingest pipeline.");
			}
		}
	}

}
