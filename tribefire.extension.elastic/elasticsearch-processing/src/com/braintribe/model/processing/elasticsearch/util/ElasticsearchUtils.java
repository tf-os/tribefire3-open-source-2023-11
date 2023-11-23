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
package com.braintribe.model.processing.elasticsearch.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexNotFoundException;

import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.processing.elasticsearch.ContextualizedElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.data.AttachmentContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.Base64;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.LongIdGenerator;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.genericmodel.GuidGenerator;

/**
 * This class contains utility methods which are used for CRUD operations against elastic.
 */
public class ElasticsearchUtils {

	private final static Logger logger = Logger.getLogger(ElasticsearchUtils.class);

	public static GuidGenerator guidGenerator = new GuidGenerator();
	private static Map<String, Boolean> cascadingProperties = new HashMap<>();

	public static Object generateId(GenericModelType idType) throws ModelAccessException {
		// PGA TODO ID
		switch (idType.getTypeCode()) {
			case longType:
				return LongIdGenerator.provideLongId();
			case stringType:
				return guidGenerator.get();
			default:
				throw new ModelAccessException("Unsupported id type " + idType);
		}
	}

	public static String getBase64ContentFromResource(Resource resource, PersistenceGmSession session) throws Exception {

		String base64Content = null;
		if (resource.getResourceSource() != null) {

			InputStream resourceStream = null;
			try {
				if (resource.isTransient()) {
					resourceStream = resource.openStream();
				} else {
					Resource sessionBoundResource = session.query().entity(Resource.T, resource.getId()).require();
					resourceStream = session.resources().retrieve(sessionBoundResource).stream();
				}

				// base64Content = Base64.encodeBytes(IOTools.slurpBytes(resourceStream, true),
				// Base64.DONT_BREAK_LINES);
				StringWriter writer = new StringWriter();
				OutputStream os = new Base64.OutputStream(writer, (Base64.DONT_BREAK_LINES | Base64.ENCODE));
				IOTools.pump(resourceStream, os, 1 << 16); // 64k
				os.flush();
				base64Content = writer.toString();

			} catch (Exception e) {
				logger.warn("Could not open stream on resource '" + resource.getId() + "'", e);

			} finally {
				if (resourceStream != null) {
					resourceStream.close();
				}
			}
		}
		return base64Content;
	}

	/**
	 *
	 * @param client
	 *            The client to use
	 * @return True, if the index had to be created, false if it already existed
	 * @throws Exception
	 *             Thrown when the index could not be created
	 */
	public static boolean createIndexIfNonexistent(ContextualizedElasticsearchClient client) throws Exception {

		IndicesAdminClient indicesAdminClient = client.getIndicesAdminClient();
		String index = client.getIndex();

		try {
			indicesAdminClient.prepareGetIndex().addIndices(index).execute().actionGet();
			return false;
		} catch (IndexNotFoundException infe) {
			logger.debug("Index " + index + " does not yet exist.");
			CreateIndexResponse createIndexResponse = indicesAdminClient.prepareCreate(index).get();
			boolean acknowledged = createIndexResponse.isAcknowledged();
			if (!acknowledged) {
				throw new Exception("Could not create index " + index);
			}

			Integer maxResultWindow = client.getMaxResultWindow();
			if (maxResultWindow != null) {
				UpdateSettingsResponse updateSettingsResponse = indicesAdminClient.prepareUpdateSettings(index).setPreserveExisting(true)
						.setSettings(Settings.builder().put("index.max_result_window", maxResultWindow.intValue())).get();
				logger.debug("Setting max_result_window: " + updateSettingsResponse.isAcknowledged());
			}
			return true;
		}

	}

	public static void openIndex(ElasticsearchClient client, String... indices)
			throws IndexNotFoundException, InterruptedException, IllegalStateException {

		IndicesAdminClient indicesAdminClient = client.getIndicesAdminClient();

		OpenIndexResponse openIndexResponse;
		try {
			openIndexResponse = indicesAdminClient.prepareOpen(indices).execute().get();
		} catch (InterruptedException e) {
			logger.debug(() -> "Got interrupted while waiting for a response on opening indices " + StringTools.createStringFromArray(indices));
			throw e;
		} catch (ExecutionException e) {
			throw new IllegalStateException("Got an error while trying to open indices " + StringTools.createStringFromArray(indices), e);
		}
		boolean ack = openIndexResponse.isAcknowledged();
		if (ack) {
			logger.trace(() -> "Got a positive acknowledgement from Elastic for opening indices " + StringTools.createStringFromArray(indices));
		} else {
			logger.debug(
					() -> "Did not get a positive acknowledgement from Elastic for opening indices " + StringTools.createStringFromArray(indices));
		}

	}

	public static boolean typeExists(IndicesAdminClient indicesAdminClient, String index, String type) throws Exception {
		boolean typeExists = false;
		try {
			TypesExistsResponse typeExistsResponse = indicesAdminClient.typesExists(new TypesExistsRequest(new String[] { index }, type)).actionGet();
			typeExists = typeExistsResponse.isExists();
		} catch (IndexNotFoundException infe) {
			typeExists = false;
		}
		return typeExists;
	}

	public static boolean deleteIndex(IndicesAdminClient indicesAdminClient, String index) throws Exception {
		try {
			DeleteIndexResponse response = indicesAdminClient.delete(new DeleteIndexRequest(index)).actionGet();
			return response.isAcknowledged();
		} catch (IndexNotFoundException infe) {
			logger.debug(() -> "Could not find index " + index);
			return true;
		}
	}

	public static boolean isScalarType(GenericModelType type) {
		switch (type.getTypeCode()) {
			case enumType:
			case booleanType:
			case dateType:
			case decimalType:
			case doubleType:
			case floatType:
			case integerType:
			case longType:
			case stringType:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks if the given {@link com.braintribe.model.generic.reflection.Property property} is a cascading index
	 * property, which means that the entity it behaves on is indexed on the property's value as well.
	 * <p>
	 * Example: The entity Document contains the property sourceRepresentation, which is of type Resource. If the
	 * property sourceRepresentation is marked as indexable (by the respective meta data) and the cascade option on this
	 * meta data is set to true, then the entity Document will be indexed with the value of the sourceRepresentation as
	 * well.
	 * </p>
	 *
	 * @param entityType
	 *            - The entity type to check its property for cascading behaviour
	 *
	 * @param property
	 *            - The {@link com.braintribe.model.generic.reflection.Property property} to check
	 * @param session
	 *            - The {@link com.braintribe.model.processing.session.api.persistence.PersistenceGmSession session} to
	 *            query the meta data
	 * @return - <code>true</code> if the property is a cascading indexing property, <code>false</code> otherwise
	 */
	public static boolean isCascadingProperty(EntityType<GenericEntity> entityType, Property property, PersistenceGmSession session) {

		String typeSignature = entityType.getTypeSignature();
		String propertyName = property.getName();

		return isCascadingProperty(typeSignature, propertyName, session);
	}

	/**
	 * @see ElasticsearchUtils#isCascadingProperty(EntityType, Property, PersistenceGmSession)
	 */
	public static boolean isCascadingProperty(GmEntityType entityType, GmProperty property, PersistenceGmSession session) {

		String typeSignature = entityType.getTypeSignature();
		String propertyName = property.getName();

		return isCascadingProperty(typeSignature, propertyName, session);

	}

	private static boolean isCascadingProperty(String typeSignature, String propertyName, PersistenceGmSession session) {

		boolean debug = logger.isDebugEnabled();
		String qualified = typeSignature + ":" + propertyName;

		Boolean isCascadingProperty = cascadingProperties.get(qualified);

		if (isCascadingProperty == null) {
			if (debug) {
				logger.debug("Could not find cached value for " + qualified);
			}
			ElasticsearchIndexingMetaData metaData = session.getModelAccessory().getCmdResolver().getMetaData().entityTypeSignature(typeSignature)
					.property(propertyName).meta(ElasticsearchIndexingMetaData.T).exclusive();

			isCascadingProperty = (metaData != null) ? metaData.getCascade() : false;

			synchronized (cascadingProperties) {
				cascadingProperties.put(qualified, isCascadingProperty);
			}

		}

		return isCascadingProperty;
	}

	/**
	 * Extracts the base64-content out of the given
	 * <code>originalResource<code> and creates a {@link com.braintribe.model.processing.elasticsearch.data.AttachmentContext}
	 * for that.
	 * &#64;param resource
	 *            - The {@link com.braintribe.model.resource.Resource } to
	 *            extract the base64-content for
	 * &#64;param session
	 *            - The session to stream the resource on
	 * &#64;return - a context object
	 *         {@link com.braintribe.model.processing.elasticsearch.data.AttachmentContext }
	 *         filled with the base64-content, the mime type and the name, or
	 *         <code>null</code> if the base64-content could not be extracted.
	 */
	public static AttachmentContext createAttachmentContext(Resource resource, PersistenceGmSession session, boolean includeContent) {

		if (resource == null) {
			return null;
		}

		String base64Content = null;
		AttachmentContext attachmentContext = null;

		if (includeContent) {
			try {
				base64Content = ElasticsearchUtils.getBase64ContentFromResource(resource, session);

			} catch (Exception e) {
				String msg = "Could not read content of resource " + resource + ".";
				logger.info(msg);
				if (logger.isDebugEnabled()) {
					logger.debug(msg, e);
				}
			}
		} else {
			base64Content = ""; // We want to include the name of the Resource at least
		}

		if (base64Content != null) {

			attachmentContext = new AttachmentContext();
			attachmentContext.setBase64Content(base64Content);
			attachmentContext.setMimeType(resource.getMimeType());
			attachmentContext.setName(resource.getName());

		}
		return attachmentContext;

	}

}
