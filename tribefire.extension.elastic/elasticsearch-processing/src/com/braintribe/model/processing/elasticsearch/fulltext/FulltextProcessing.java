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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.braintribe.cfg.Configurable;
import com.braintribe.codec.json.genericmodel.GenericModelJsonCodec;
import com.braintribe.codec.marshaller.api.GmCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.ModelAccessException;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.elastic.ElasticConstants;
import com.braintribe.model.processing.elasticsearch.ContextualizedElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.ContextualizedElasticsearchClientImpl;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.ElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.attachment.Attachment;
import com.braintribe.model.processing.elasticsearch.data.AttachmentContext;
import com.braintribe.model.processing.elasticsearch.indexing.FilteringCloningContext;
import com.braintribe.model.processing.elasticsearch.util.ElasticsearchUtils;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.utils.lcd.StringTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class FulltextProcessing {

	private final static Logger logger = Logger.getLogger(FulltextProcessing.class);

	protected static GmCodec<Object, JsonNode> jsonCodec = null;

	protected static JsonNodeFactory nodeFactory = new JsonNodeFactory(true);
	protected static EntityType<Attachment> attachmentEntityType = Attachment.T;

	protected Predicate<Resource> resourceAcceptance;

	private long maxFileSize = Numbers.MEGABYTE * 100;

	public static GmCodec<Object, JsonNode> getJsonCodec() {
		if (jsonCodec == null) {
			jsonCodec = new GenericModelJsonCodec<Object>();
		}

		return jsonCodec;
	}

	public void delete(IndexedElasticsearchConnector connector, PersistentEntityReference per) {

		logger.trace(() -> "Trying to delete fulltext index for " + per);

		ElasticsearchClient client = connector.getClient();
		String index = connector.getIndex();

		Object idObject = per.getId();
		if (idObject != null) {
			String id = idObject.toString();
			DeleteResponse response = client.elastic().prepareDelete(index, ElasticConstants.FULLTEXT_INDEX_TYPE, id)
					.get(TimeValue.timeValueSeconds(30L));
			logger.trace(() -> "Got response: " + response);
		} else {
			logger.trace(() -> "Cannot delete an entity without an ID: " + per);
		}

	}

	/**
	 * Indexes a {@link com.braintribe.model.generic.reflection.Property Property} from the given
	 * {@link com.braintribe.model.generic.value.EntityReference EntityReference} in elastic.
	 *
	 * <p>
	 * Note: If the entity already exists in elastic, it is updated, otherwise the whole entity is created.
	 * </p>
	 *
	 * @param entity
	 *            - The {@link GenericEntity} to index
	 * @param properties
	 *            - The list of {@link com.braintribe.model.generic.reflection.Property properties} to index
	 * @param session
	 *            - The {@link PersistenceGmSession session} to stream the resource content if the entity is of type
	 *            {@link com.braintribe.model.resource.Resource}
	 * @throws Exception
	 *             - if the indexing fails for any reason
	 */
	public void index(IndexedElasticsearchConnector connector, GenericEntity entity, List<Property> changedProperties, PersistenceGmSession session,
			Collection<Resource> resources) throws Exception {

		StopWatch stopWatch = new StopWatch();
		boolean debug = logger.isDebugEnabled();

		ContextualizedElasticsearchClient client = new ContextualizedElasticsearchClientImpl(connector.getClient(), session.getModelAccessory());
		String index = connector.getIndex();

		EntityType<GenericEntity> entityType = entity.entityType();
		List<Property> properties = entityType.getProperties();
		StandardCloningContext cloningContext = new FilteringCloningContext(properties);

		stopWatch.intermediate("Initialization");

		GenericEntity clonedEntity = (GenericEntity) entityType.clone(cloningContext, entity, StrategyOnCriterionMatch.skip);

		stopWatch.intermediate("Cloning");

		Resource originalResource = null;

		if (clonedEntity instanceof Resource) {
			originalResource = (Resource) entity;

		} else {

			List<Resource> allResources = new ArrayList<>();

			if (resources != null && !resources.isEmpty()) {
				allResources.addAll(resources);
			} else {

				for (Property p : properties) {
					if (logger.isTraceEnabled()) {
						logger.trace("Checking property " + p);
					}
					if (ElasticsearchUtils.isCascadingProperty(entityType, p, session)) {

						GenericModelType type = p.getType();

						if (type.isCollection()) {

							CollectionType collectionType = (CollectionType) type;
							GenericModelType collectionElementType = collectionType.getCollectionElementType();
							if (collectionElementType.getTypeSignature().equals(Resource.class.getName())) {
								Collection<Resource> collection = session.query().property(preparePropertyQuery(p, entity)).value();
								if (collection != null) {
									allResources.addAll(collection);
								}
							}

						} else if (type.getTypeSignature().equals(Resource.class.getName())) {

							if (debug) {
								logger.debug("Detected cascading property '" + p + "' for entity " + entity);
							}
							/**
							 * We can use the cloned entity here as through the cloning all the absence information is already there. Update:
							 * Experience showed that this did not work in 1.1. Some issues with an invalid session ID see:
							 * https://fisheye.braintribe.com/changelog/svn-master?cs=105189
							 */
							Resource resource = session.query().property(preparePropertyQuery(p, entity)).value(); // p.get(entity);
							// does
							// not
							// work
							// because
							// its
							// session
							// is
							// not
							// in
							// this
							// thread's
							// scope
							if (resource != null) {
								allResources.add(resource);
							}
						}

					} else {
						logger.trace(() -> "Property is not a cascading property.");
					}
				}

			}

			originalResource = getBestResourceForIndexing(allResources);

			if (logger.isDebugEnabled()) {
				logger.debug("Elected: " + originalResource + " to represent entity (of " + allResources + ")");
			}

		}

		stopWatch.intermediate("Resource Identification");

		AttachmentContext attachmentContext = null;
		if (originalResource != null) {
			boolean includeContent = true;

			if (resourceAcceptance != null) {
				includeContent = resourceAcceptance.test(originalResource);
			}

			attachmentContext = ElasticsearchUtils.createAttachmentContext(originalResource, session, includeContent);

		}
		stopWatch.intermediate("Attachment Extraction");

		insertRecord(client, index, clonedEntity, attachmentContext, false);

		stopWatch.intermediate("Insert Record");

		if (logger.isDebugEnabled()) {
			logger.debug("Inserted record for " + entity + ": " + stopWatch);
		}
	}

	private static void insertRecord(ContextualizedElasticsearchClient client, String index, GenericEntity ge, AttachmentContext attachmentContext,
			boolean isImmediateRefreshEnabled) throws ModelAccessException {
		Set<GenericEntity> alreadyInserted = new HashSet<>();
		insertRecord(client, index, ge, alreadyInserted, attachmentContext, isImmediateRefreshEnabled);
	}

	protected static void insertRecord(ContextualizedElasticsearchClient client, String index, GenericEntity ge, Set<GenericEntity> alreadyInserted,
			AttachmentContext attachmentContext, boolean isImmediateRefreshEnabled) throws ModelAccessException {

		if (alreadyInserted.contains(ge)) {
			// loop detected
			return;
		}
		alreadyInserted.add(ge);

		EntityType<?> entityType = ge.entityType();
		String type = entityType.getTypeSignature();
		ObjectMapper mapper = new ObjectMapper();

		long t0 = System.currentTimeMillis();
		String json = null;
		try {
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			JsonNode manualJsonValue = buildJsonObject(ge, entityType, attachmentContext);
			if (manualJsonValue instanceof ObjectNode) {
				cleanTypes((ObjectNode) manualJsonValue);
			}

			json = mapper.writeValueAsString(manualJsonValue);
		} catch (Exception e) {
			throw new ModelAccessException("Could not create a JSON object out of " + ge, e);
		}
		long t1 = System.currentTimeMillis();

		Object id = null;

		Object idObject = ge.getId();
		if (idObject != null) {
			id = idObject.toString();
		}
		if (id == null) {
			GenericModelType idType = client.getModelAccessory().getIdType(type);
			id = ElasticsearchUtils.generateId(idType);
			ge.setId(id);
		}

		long t2 = System.currentTimeMillis();
		//@formatter:off
		IndexRequestBuilder indexRequestBuilder = client.elastic().prepareIndex(index, ElasticConstants.FULLTEXT_INDEX_TYPE, id.toString())
				.setSource(json, XContentType.JSON);
		if (attachmentContext != null) {
			indexRequestBuilder = indexRequestBuilder.setPipeline("attachmentPipeline");
		}
		IndexResponse response = indexRequestBuilder
				/**
				 * Check {@link Check org.elasticsearch.action.support.WriteRequest.RefreshPolicy}. This settings is
				 * only recommended for testing because has performance impact. In 2.2.1 it is set like this - continue
				 * with this setting
				 */
				.setRefreshPolicy(isImmediateRefreshEnabled == true ? RefreshPolicy.IMMEDIATE : RefreshPolicy.NONE)
				.get(TimeValue.timeValueSeconds(60L));
		//@formatter:on

		long t3 = System.currentTimeMillis();

		if (logger.isTraceEnabled()) {
			logger.trace("Got response: " + response);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("JSON generation: %d, Inserting: %d", (t1 - t0), (t3 - t2)));
		}
	}

	protected static JsonNode buildJsonObject(GenericEntity ge, EntityType<?> entityType, AttachmentContext attachmentContext)
			throws ModelAccessException {

		List<Property> properties = entityType.getProperties();

		ObjectNode rootNode = nodeFactory.objectNode();

		TextNode textNodeTypeSignature = nodeFactory.textNode(entityType.getTypeSignature());
		rootNode.set("typeSignature", textNodeTypeSignature);
		String partition = ge.getPartition();
		if (partition != null) {
			TextNode textNodePartition = nodeFactory.textNode(partition);
			rootNode.set("partition", textNodePartition);
		}
		String globalId = ge.getGlobalId();
		if (globalId != null) {
			TextNode textNodeGlobalId = nodeFactory.textNode(globalId);
			rootNode.set("globalId", textNodeGlobalId);
		}

		if (properties != null) {
			try {
				StringBuilder textBuilder = new StringBuilder();

				for (Property property : properties) {

					Object propertyValue = property.get(ge);
					if (propertyValue != null) {
						if (textBuilder.length() > 0) {
							textBuilder.append(' ');
						}
						textBuilder.append(propertyValue.toString());
					}

				}

				if (textBuilder.length() > 0) {
					TextNode textNode = nodeFactory.textNode(textBuilder.toString());
					rootNode.set("propertyData", textNode);
				}

			} catch (Exception e) {
				throw new ModelAccessException("Could not create a JSON object out of " + ge, e);
			}
		}
		if (attachmentContext != null) {
			TextNode textNode = nodeFactory.textNode(attachmentContext.getBase64Content());
			rootNode.set("attachmentData", textNode);
		}
		return rootNode;
	}

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

	private static PropertyQuery preparePropertyQuery(Property property, GenericEntity entity) {
		PropertyQuery query = PropertyQuery.T.create();
		query.setPropertyName(property.getName());
		query.setEntityReference(entity.reference());
		return query;
	}

	private Resource getBestResourceForIndexing(Collection<Resource> collection) {

		Resource first = null;
		Resource firstAccepted = null;
		Resource pdf = null;
		Resource txt = null;

		if (collection != null && !collection.isEmpty()) {

			for (Resource r : collection) {

				if (maxFileSize > 0) {
					Long fileSize = r.getFileSize();
					if (fileSize == null || fileSize > maxFileSize) {
						continue;
					}
				}

				if (firstAccepted == null) {
					if (resourceAcceptance != null) {
						if (resourceAcceptance.test(r)) {
							firstAccepted = r;
						}
					}
				}

				if (first == null) {
					first = r;
				}
				String mimeType = r.getMimeType();
				if (mimeType != null) {
					if (mimeType.equals("application/pdf") && pdf == null) {
						pdf = r;
					} else if (mimeType.equals("text/plain") && txt == null) {
						txt = r;
					}
				}
			}
		}

		if (txt != null) {
			return txt;
		}
		if (pdf != null) {
			return pdf;
		}
		if (firstAccepted != null) {
			return firstAccepted;
		}
		return first;
	}

	public List<Object> executeFulltextSearch(IndexedElasticsearchConnector connector, EntityType<?> sourceEntityType, String text,
			int maxFulltextResultSize, PersistenceGmSession session) {

		if (StringTools.isBlank(text)) {
			return Collections.EMPTY_LIST;
		}

		ElasticsearchClient client = connector.getClient();
		String index = connector.getIndex();

		String[] typesList = getTypesList(sourceEntityType, session);

		SearchRequestBuilder srb = client.elastic().prepareSearch(index).setTypes(ElasticConstants.FULLTEXT_INDEX_TYPE);

		text = text.toLowerCase();
		text = text.replace('-', ' ');
		text = text.trim();

		QueryBuilder termQuery = fulltextQuery(text);

		BoolQueryBuilder typesQuery = QueryBuilders.boolQuery();
		for (String type : typesList) {
			QueryBuilder query = QueryBuilders.matchQuery("typeSignature", type);
			typesQuery.should(query);
		}

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.must(typesQuery);
		if (termQuery != null) {
			boolQuery.must(termQuery);
		}

		srb.setQuery(boolQuery);
		srb.setSize(maxFulltextResultSize);

		List<Object> ids = new ArrayList<>();

		SearchResponse response = srb.setExplain(false).execute().actionGet();
		if (response != null) {
			SearchHits hits = response.getHits();
			if (hits != null) {
				SearchHit[] hitArray = hits.getHits();
				if (hitArray != null && hitArray.length > 0) {

					GenericModelType idType = session.getModelAccessory().getIdType(sourceEntityType.getTypeSignature());
					boolean idIsLongType = idType.getTypeCode() == TypeCode.longType;

					for (SearchHit hit : hitArray) {
						String id = hit.getId();
						if (idIsLongType) {

							try {
								ids.add(Long.parseLong(id));

							} catch (NumberFormatException e) {
								logger.warn("Error while parsing id to long value, value: '" + id + "'");
							}

						} else {
							ids.add(id);

						}
					}
				}
			}
		}

		return ids;
	}

	private static QueryBuilder fulltextQuery(String text) {
		if (text == null || text.trim().length() == 0) {
			return null;
		}
		if (text.contains(" ")) {

			if (text.startsWith("\"") && text.endsWith("\"")) {
				text = StringTools.removeFirstAndLastCharacter(text);
				QueryBuilder query = QueryBuilders.matchPhrasePrefixQuery("_all", text);
				return query;
			} else {
				String[] strings = StringTools.splitString(text, " ");

				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				for (String t : strings) {
					if (text.startsWith("\"") && text.endsWith("\"")) {
						text = StringTools.removeFirstAndLastCharacter(text);
					}
					QueryBuilder query = QueryBuilders.wildcardQuery("_all", t);
					boolQuery.must(query);
				}
				return boolQuery;
			}
		} else {
			if (text.startsWith("\"") && text.endsWith("\"")) {
				text = StringTools.removeFirstAndLastCharacter(text);
				QueryBuilder query = QueryBuilders.wildcardQuery("_all", text);
				return query;
			} else {
				QueryBuilder query = QueryBuilders.wildcardQuery("_all", text);
				return query;
			}
		}
	}

	public static String[] getTypesList(EntityType<?> entityType, PersistenceGmSession session) {

		Set<EntityType<?>> subTypes = null;
		try {
			ModelAccessory modelAccessory = session.getModelAccessory();
			ModelOracle oracle = modelAccessory.getOracle();
			subTypes = oracle.getEntityTypeOracle(entityType).getSubTypes() //
					.transitive() //
					.includeSelf() //
					.onlyInstantiable() //
					.asTypes();

		} catch (Exception e) {
			logger.error("Could not access the type oracle.", e);
			subTypes = new HashSet<>();
		}

		List<String> typesList = new ArrayList<>();
		if (!entityType.isAbstract()) {
			typesList.add(entityType.getTypeSignature());
		}

		if (subTypes != null) {
			for (EntityType<?> subType : subTypes) {
				String signature = subType.getTypeSignature();
				if (!typesList.contains(signature)) {
					typesList.add(subType.getTypeSignature());
				}
			}
		}
		String[] typesArray = typesList.toArray(new String[typesList.size()]);
		return typesArray;
	}

	public SearchRequestBuilder createIdsQuery(Set<Object> ids, ElasticsearchConnector connector) {
		ElasticsearchClient client = connector.getClient();
		String index = connector.getIndex();
		SearchRequestBuilder srb = client.elastic().prepareSearch(index).setTypes(ElasticConstants.FULLTEXT_INDEX_TYPE);

		BoolQueryBuilder idQuery = QueryBuilders.boolQuery();
		for (Object id : ids) {
			QueryBuilder query = QueryBuilders.matchQuery("_id", "" + id);
			idQuery.should(query);
		}
		srb.setQuery(idQuery);
		srb.setSize(ids.size());
		return srb;
	}

	@Configurable
	public void setResourceAcceptance(Predicate<Resource> resourceAcceptance) {
		this.resourceAcceptance = resourceAcceptance;
	}
	@Configurable
	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
}
