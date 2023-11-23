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
package com.braintribe.model.processing.elasticsearch.aspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gwt.utils.genericmodel.GMCoreTools;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchDeleteMetaData;
import com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingMetaData;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.DeleteManipulation;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessAspectRuntimeException;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.Advice;
import com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext;
import com.braintribe.model.processing.aop.api.context.AfterContext;
import com.braintribe.model.processing.aop.api.context.BeforeContext;
import com.braintribe.model.processing.aop.api.interceptor.AfterInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.BeforeInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;
import com.braintribe.model.processing.elasticsearch.ElasticsearchClient;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.fulltext.FulltextProcessing;
import com.braintribe.model.processing.elasticsearch.fulltext.FulltextTypeSupport;
import com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.processing.elasticsearch.indexing.IndexingPackage;
import com.braintribe.model.processing.query.tools.SourceTypeResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Operator;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.AbstractJunction;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.model.query.conditions.FulltextComparison;
import com.braintribe.model.query.conditions.Negation;
import com.braintribe.model.query.conditions.ValueComparison;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.HashMultiMap;
import com.braintribe.utils.lcd.StringTools;

/**
 * This Aspect implements the {@link BeforeInterceptor} as well as the {@link AfterInterceptor}.
 *
 * <p>
 * <b>BeforeInterception</b><br />
 * The aspect analyzes incoming queries and redirects {@link FulltextComparison} elements to <i>Elasticsearch</i>. The passed-back results are
 * transformed into a {@link ValueComparison} using the in-clause. These {@link ValueComparison} s substitute the originated
 * {@link FulltextComparison} in the {@link com.braintribe.model.query.Query Query}. Afterwards, the query is executed against the specific underlying
 * access.
 * </p>
 *
 * <p>
 * <b>AfterInterception</b><br />
 * The aspect analyzes the incoming {@link com.braintribe.model.generic.manipulation.Manipulation Manipulations} for for all {@link AtomicManipulation
 * AtomicManipulationTypes}. If the manipulations hold Entity Types which have the Elasticsearch-Metadata attached, the aspect redirects the
 * respective operations to <em>Elasticsearch</em>:
 *
 * <ul>
 * <li><b>DELETE</b><br/>
 * If the removed entity's removed type holds the Entity Meta Data {@link ElasticsearchDeleteMetaData}, the whole entity is deleted in
 * Elasticsearch.</li><br />
 * <li><b>ADD</b> If the added entity's type holds the Property Meta Data {@link ElasticsearchIndexingMetaData}, the respective properties are indexed
 * against Elasticsearch.</li> <br />
 * <li><b>CHANGE VALUE</b> If the changed entity's type holds the Property Meta Data {@link ElasticsearchIndexingMetaData}, the respective changed
 * properties are indexed against Elasticsearch.</li> <br />
 * </ul>
 * </p>
 *
 * @author christina.wilpernig
 */
public class ExtendedFulltextAspect
		implements AccessAspect, BeforeInterceptor<Query, QueryResult>, AfterInterceptor<ManipulationRequest, ManipulationResponse> {

	private final static Logger logger = Logger.getLogger(ExtendedFulltextAspect.class);
	private IndexedElasticsearchConnector elasticsearchConnector;
	private final Map<String, Boolean> typeSignaturesToElasticDeleteEnabled = new HashMap<>();
	private final Map<String, Boolean> entityPropertyToElasticIndexingEnabled = new HashMap<>();

	private ElasticsearchIndexingWorker worker;
	private Integer maxFulltextResultSize = 100;
	private Integer maxResultWindowSize;

	private boolean initialized = false;
	protected boolean cascadingAttachment = true;
	private boolean ignoreUnindexedEntities = false;

	private static ValueComparison trueComparison;
	private static ValueComparison falseComparison;

	private FulltextProcessing fulltextProcessing;

	static {
		trueComparison = ValueComparison.T.create();
		trueComparison.setLeftOperand(1);
		trueComparison.setOperator(Operator.equal);
		trueComparison.setRightOperand(1);

	}
	static {
		falseComparison = ValueComparison.T.create();
		falseComparison.setLeftOperand(1);
		falseComparison.setOperator(Operator.equal);
		falseComparison.setRightOperand(0);
	}

	@Override
	public void run(BeforeContext<Query, QueryResult> context) throws InterceptionException {

		boolean initializationSuccessful = false;
		try {
			initialize(elasticsearchConnector.getClient(), elasticsearchConnector.getIndex());
			initializationSuccessful = true;
		} catch (Exception e) {
			logger.info(() -> "Error while initializing fulltext aspect. Refer to debug log or more details.");
			logger.debug(() -> "Error while initializing context with connector " + elasticsearchConnector, e);
		}

		if (initializationSuccessful) {
			try {
				Query query = dispatchFulltextElements(context.getRequest(), context.getSession());
				context.overrideRequest(query);

			} catch (Exception e) {
				throw new InterceptionException("[before interception] Error while dispatching fulltext comparisons for:"
						+ GMCoreTools.getDescription(context.getRequest()) + " (connector: " + elasticsearchConnector + ")", e);
			}
		}
	}

	/**
	 * Analyzes the incoming {@link com.braintribe.model.query.Query Query} for {@link FulltextComparison} elements and replaces them with
	 * {@link ValueComparison}s.
	 *
	 * @param query
	 *            - The {@link com.braintribe.model.query.Query Query} to determine the {@link FulltextComparison} elements
	 * @param session
	 *            The current session
	 * @return - The adapted {@link com.braintribe.model.query.Query Query} without {@link FulltextComparison} elements
	 */
	private Query dispatchFulltextElements(Query query, PersistenceGmSession session) throws Exception {
		final MultiMap<FulltextComparison, Replacer> replacements = new HashMultiMap<>();

		Restriction restriction = query.getRestriction();
		if (restriction == null) {
			return query;
		}

		Condition condition = restriction.getCondition();
		if (condition == null) {
			return query;
		}

		if (logger.isTraceEnabled()) {
			logger.trace("[before interception] Analyzing query for fulltext comparison elements:" + GMCoreTools.getDescription(query));
		}

		addReplacer(replacements, restriction, condition);

		boolean debug = logger.isDebugEnabled();

		if (replacements.isEmpty()) {
			if (logger.isTraceEnabled()) {
				logger.trace("[before interception] No fulltext comparisons found. Nothing to replace.");
			}
			return query;
		}

		if (debug) {
			logger.debug("[before interception] Replacing " + replacements.size() + " fulltext comparison(s)...");
		}

		// Replacing fulltext comparisons by value comparisons
		for (FulltextComparison comparison : replacements.keySet()) {

			Condition c = substitute(query, comparison, session);
			for (Replacer replacer : replacements.getAll(comparison)) {
				replacer.replace(c);
			}
		}

		if (debug) {
			logger.debug("[before interception] Finished replacement(s) of " + replacements.size() + " fulltext comparion(s).");
		}

		if (logger.isTraceEnabled()) {
			logger.trace(
					"[before interception] Replaced " + replacements.size() + " fulltext-comparion(s), query: " + GMCoreTools.getDescription(query));
		}

		return query;
	}

	/**
	 * Substitutes the {@link com.braintribe.model.query.conditions.FulltextComparison FulltextComparison} with the results from elastic.
	 *
	 * @param query
	 *            - The query to determine the source, if required.
	 * @param comparison
	 *            - The {@link com.braintribe.model.query.conditions.FulltextComparison FulltextComparison} to be executed against elastic. - The
	 *            optional {@link com.braintribe.model.query.Ordering}
	 * @param session
	 *            The current session
	 * @return - The substituted fulltext comparison in form of a {@link ValueComparison} comparing the id of the
	 *         <code>sourceEntityType<code> to the <code>entityIds</code>.
	 */
	private Condition substitute(Query query, FulltextComparison comparison, PersistenceGmSession session) {
		String text = comparison.getText();

		if (StringTools.isBlank(text)) {
			return trueComparison;
		}

		Source source = comparison.getSource();
		EntityType<?> sourceEntityType;

		/**
		 * Check if the fulltext comparison is directly mapped to the entity query or the source of the comparison needs to be evaluated.
		 */
		if (source == null) {
			String ets = null;
			if (query instanceof EntityQuery) {
				ets = ((EntityQuery) query).getEntityTypeSignature();
			} else if (query instanceof SelectQuery) {
				SelectQuery sq = (SelectQuery) query;
				List<From> froms = sq.getFroms();
				if (froms.size() == 1) {
					From from = froms.get(0);
					ets = from.getEntityTypeSignature();
					source = from;

					if (logger.isDebugEnabled()) {
						logger.debug("Extracted type signature " + ets + " from single FROM of query.");
					}
				} else {
					logger.debug(() -> "There are 0 or more than 1 FROMs in the query. This is not yet supported.");
					return falseComparison;
				}
			} else {
				logger.debug(() -> "Unsupported query type: " + query);
				return falseComparison;
			}
			sourceEntityType = GMF.getTypeReflection().getEntityType(ets);
		} else {
			sourceEntityType = SourceTypeResolver.resolveType(source);
		}

		if (this.ignoreUnindexedEntities && !isIndexingEnabled(session, sourceEntityType)) {
			return comparison;
		}

		List<Object> entityIds = executeElasticFulltextSearch(sourceEntityType, text, session);

		ValueComparison valueComparison = ValueComparison.T.create();

		if (entityIds.isEmpty()) {
			return falseComparison;

		} else {
			PropertyOperand po = PropertyOperand.T.create();
			po.setPropertyName(GenericEntity.id);
			po.setSource(source);
			valueComparison.setLeftOperand(po);
			valueComparison.setOperator(Operator.in);
			valueComparison.setRightOperand(entityIds);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("[before interception] Substitued fulltext-comparison for text '" + text + "' with value comparison: "
					+ GMCoreTools.getDescription(valueComparison));
		}

		return valueComparison;
	}

	private List<Object> executeElasticFulltextSearch(EntityType<?> sourceEntityType, String text, PersistenceGmSession session) {

		boolean debug = logger.isDebugEnabled();
		List<Object> entityIds = fulltextProcessing.executeFulltextSearch(elasticsearchConnector, sourceEntityType, text, maxFulltextResultSize,
				session);

		if (entityIds.isEmpty()) {
			if (debug) {
				logger.debug("[before interception] No matches for '" + text + "' on'" + sourceEntityType.getTypeSignature()
						+ "' found in elasticsearch.");
			}
		}

		if (debug) {
			logger.debug("[before interception] Found " + entityIds.size() + " matche(s) in elasticsearch. Returning result.");
		}

		return entityIds;
	}

	/**
	 * Analyzes the incoming {@link com.braintribe.model.query.conditions.Condition Condition} and searches for {@link FulltextComparison} elements.
	 * If some were found, they are added to <code>replacements</code> containing the fulltext comparison and the related
	 * {@link com.braintribe.model.processing.elasticsearch.aspect.Replacer Replacer}.
	 *
	 * @param replacements
	 *            - A {@link com.braintribe.utils.collection.api.MultiMap MultiMap} to store the matches of fulltext comparisons
	 * @param restriction
	 *            - The {@link com.braintribe.model.query.Restriction Restriction} to substitute the condition on
	 * @param condition
	 *            - The {@link com.braintribe.model.query.conditions.Condition Condition} to be substituted
	 */
	private void addReplacer(final MultiMap<FulltextComparison, Replacer> replacements, Restriction restriction, Condition condition) {

		switch (condition.conditionType()) {
			case conjunction:
			case disjunction:
				AbstractJunction junction = (AbstractJunction) condition;
				List<Condition> junctionOperands = junction.getOperands();

				if (junctionOperands != null && !junctionOperands.isEmpty()) {
					for (int i = 0; i < junctionOperands.size(); i++) {
						Condition c = junctionOperands.get(i);
						if (c instanceof AbstractJunction) {
							addReplacer(replacements, restriction, c);

						} else if (c instanceof FulltextComparison) {

							if (logger.isDebugEnabled()) {
								logger.debug("[before interception] Replacing fulltext comparison: " + GMCoreTools.getDescription(c));
							}

							replacements.put((FulltextComparison) c, new JunctionReplacer(junction, i));
						}
					}
				}

				break;
			case fulltextComparison:

				if (logger.isDebugEnabled()) {
					logger.debug("[before interception] Replacing fulltext comparison: " + GMCoreTools.getDescription(condition));
				}

				replacements.put((FulltextComparison) condition, new RestrictionReplacer(restriction));

				break;
			case negation:
				Negation negation = (Negation) condition;
				Condition operand = negation.getOperand();

				if (operand instanceof FulltextComparison) {

					if (logger.isDebugEnabled()) {
						logger.debug("[before interception] Replacing fulltext comparison: " + GMCoreTools.getDescription(operand));
					}

					replacements.put((FulltextComparison) operand, new NegationReplacer(negation));
				}

				break;
			case valueComparison:
				break;
			default:
				break;
		}
	}

	@Override
	public void run(AfterContext<ManipulationRequest, ManipulationResponse> context) throws InterceptionException {

		if (worker == null) {
			// this.worker = getElasticsearchWorker();
			throw new InterceptionException("No worker has been set for this aspect.");
		}

		try {
			initialize(elasticsearchConnector.getClient(), elasticsearchConnector.getIndex());
		} catch (Exception e) {
			logger.info(() -> "Error while initializing fulltext aspect (after). Refer to debug log or more details.");
			logger.debug(() -> "Error while initializing context with connector " + elasticsearchConnector, e);
			return;
		}

		boolean debug = logger.isDebugEnabled();

		ManipulationRequest request = context.getRequest();
		ManipulationResponse response = context.getResponse();

		Manipulation requestManipulation = request.getManipulation();
		Manipulation inducedManipulation = response.getInducedManipulation();

		List<Manipulation> manipulations = new ArrayList<>();
		manipulations.add(requestManipulation);

		if (inducedManipulation != null) {
			manipulations.add(inducedManipulation);
		}

		// Deletion
		List<PersistentEntityReference> persistentEntityReferences = scanDeleteManipulations(context, manipulations);

		if (!CollectionTools.isEmpty(persistentEntityReferences)) {

			int size = persistentEntityReferences.size();

			for (PersistentEntityReference persistentEntityReference : persistentEntityReferences) {
				fulltextProcessing.delete(elasticsearchConnector, persistentEntityReference);
			}

			if (debug) {
				logger.debug("[after interception] Successfully deleted " + size + " instance(s).");
			}

		}

		// Indexing
		CompoundManipulation compoundManipulation = CompoundManipulation.T.create();
		compoundManipulation.setCompoundManipulationList(manipulations);
		ReferenceAdapter adapter = new ReferenceAdapter(compoundManipulation);

		Map<PersistentEntityReference, List<String>> entityReferencesToIndex = getEntitiesToIndex(context, manipulations, adapter);

		if (entityReferencesToIndex.isEmpty()) {
			if (debug) {
				logger.debug("[after interception] No indexable entities found. Done.");
			}
			return;
		}

		if (debug) {
			logger.debug("[after interception] " + entityReferencesToIndex.size() + " indexable candidates found, processing ...");
		}

		PersistenceGmSession session = context.getSession();
		String accessId = session.getAccessId();

		Map<String, IndexingPackage> indexingPackages = new HashMap<>();

		for (Map.Entry<PersistentEntityReference, List<String>> entry : entityReferencesToIndex.entrySet()) {
			PersistentEntityReference entityReference = entry.getKey();
			String typeSignature = entityReference.getTypeSignature();

			List<Property> properties = new ArrayList<>();
			GenericEntity entity = null;

			try {
				entity = session.query().entity(entityReference).withTraversingCriterion(TC.create().negation().joker().done()).refresh();

			} catch (Exception e) {
				logger.error("[after interception] Error while querying for " + typeSignature, e);
			}

			if (entity == null) {
				continue;
			}

			for (String propertyName : entry.getValue()) {
				Property property = entity.entityType().getProperty(propertyName);
				properties.add(property);

			}

			IndexingPackage indexingPackage = indexingPackages.get(typeSignature);
			if (indexingPackage == null) {
				indexingPackage = new IndexingPackage(accessId, typeSignature);
				indexingPackages.put(typeSignature, indexingPackage);
			}

			indexingPackage.addIndexableEntity(entity, properties);

			if (debug) {
				logger.debug("[after interception] Added indexable " + entity + " with properties [" + Arrays.toString(properties.toArray()) + "]");
			}
		}

		for (IndexingPackage p : indexingPackages.values()) {
			if (debug) {
				logger.debug("Enqueuing package to worker: " + p.toString());
			}

			boolean enqueued = worker.enqueue(p);

			if (!enqueued) {
				logger.info("Denied enqueuing of " + p);
			}
		}

	}

	private Map<PersistentEntityReference, List<String>> getEntitiesToIndex(AfterContext<ManipulationRequest, ManipulationResponse> context,
			List<Manipulation> manipulations, ReferenceAdapter adapter) {

		@SuppressWarnings({ "rawtypes" })
		Map<PersistentEntityReference, List<String>> entitiesToIndex = (Map) CodingMap.create(EntRefHashingComparator.INSTANCE);
		handleIndexingManipulations(context, manipulations, adapter, entitiesToIndex);

		return entitiesToIndex;
	}

	/**
	 * Iterates through {@link com.braintribe.model.generic.manipulation.Manipulation Manipulations} and checks for change value manipulations. If
	 * there are some and the properties manipulated have the meta data for indexing in elastic attached, they are collected and later on processed
	 * again elastic.
	 *
	 * @param context
	 *            - The context to determine if the meta data is attached to the respective property
	 * @param manipulations
	 *            - The manipulations to analyze for indexable property changes
	 * @param referenceAdapter
	 *            - The {@link ReferenceAdapter} to ensure {@link com.braintribe.model.generic.value.PersistentEntityReference
	 *            PersistentEntityReferences }
	 */
	private void handleIndexingManipulations(AfterContext<ManipulationRequest, ManipulationResponse> context, List<Manipulation> manipulations,
			ReferenceAdapter referenceAdapter, Map<PersistentEntityReference, List<String>> entitiesToIndex) {

		for (Manipulation manipulation : manipulations) {
			switch (manipulation.manipulationType()) {
				case COMPOUND:

					List<Manipulation> compoundManipulationList = ((CompoundManipulation) manipulation).getCompoundManipulationList();

					if (compoundManipulationList != null) {
						handleIndexingManipulations(context, compoundManipulationList, referenceAdapter, entitiesToIndex);
					}
					break;

				case CHANGE_VALUE:
				case ADD:
				case REMOVE:
				case CLEAR_COLLECTION:

					examineCandidateManipulation(context, referenceAdapter, (PropertyManipulation) manipulation, entitiesToIndex);

					break;

				default:
					break;
			}

		}
	}

	/**
	 * Analyzes the incoming property manipulation for an existing indexing meta data property.
	 *
	 * @param context
	 *            - The context to determine if the meta data is attached to the respective property
	 * @param referenceAdapter
	 *            - The {@link ReferenceAdapter} to ensure {@link com.braintribe.model.generic.value.PersistentEntityReference
	 *            PersistentEntityReferences }
	 * @param manipulation
	 *            - The manipulation to receive the {@link EntityProperty}
	 * @param entitiesToIndex
	 *            - A {@link java.util.List List} containing the entities and the names of the indexable properties
	 */
	private void examineCandidateManipulation(AfterContext<ManipulationRequest, ManipulationResponse> context, ReferenceAdapter referenceAdapter,
			PropertyManipulation manipulation, Map<PersistentEntityReference, List<String>> entitiesToIndex) {

		EntityProperty entityProperty = (EntityProperty) manipulation.getOwner();

		if (isIndexingEnabled(context, entityProperty)) {
			EntityProperty adaptedEntityProperty = referenceAdapter.adapt(entityProperty);
			PersistentEntityReference reference = (PersistentEntityReference) adaptedEntityProperty.getReference();

			mapPropertyToReference(entitiesToIndex, entityProperty, reference);
		}
	}

	private boolean isIndexingEnabled(PersistenceGmSession session, EntityType<?> type) {
		String signature = type.getTypeSignature();
		//@formatter:off
		return type.getProperties()
			.stream()
			.anyMatch(p -> isIndexingEnabled(session, signature, p.getName()));
		//@formatter:off
	}

	private boolean isIndexingEnabled(AfterContext<ManipulationRequest, ManipulationResponse> context,
			EntityProperty entityProperty) {

		PersistenceGmSession session = context.getSession();
		EntityReference entityReference = entityProperty.getReference();
		String signature = entityReference.getTypeSignature();
		String propertyName = entityProperty.getPropertyName();

		return isIndexingEnabled(session, signature, propertyName);
	}

	private Boolean isIndexingEnabled(PersistenceGmSession session, String signature, String propertyName) {
		String qualified = signature + ":" + propertyName;
		Boolean indexingEnabled = entityPropertyToElasticIndexingEnabled.get(qualified);

		if (indexingEnabled == null) {
			synchronized (entityPropertyToElasticIndexingEnabled) {

				ElasticsearchIndexingMetaData metaData = session.getModelAccessory()
						.getCmdResolver().getMetaData().entityTypeSignature(signature)
						.property(propertyName).meta(ElasticsearchIndexingMetaData.T).exclusive();

				indexingEnabled = (metaData != null);

				if (logger.isDebugEnabled()) {
					logger.debug("[after interception] Elasticserach indexing for '" + qualified + "' is set to: "
							+ indexingEnabled);
				}

				entityPropertyToElasticIndexingEnabled.put(qualified, indexingEnabled);

			}

		}
		return indexingEnabled;
	}

	private void mapPropertyToReference(Map<PersistentEntityReference, List<String>> entitiesToIndex,
			EntityProperty entityProperty, PersistentEntityReference reference) {

		List<String> properties = entitiesToIndex.get(reference);
		if (properties == null) {
			properties = new ArrayList<>();
			entitiesToIndex.put(reference, properties);

		}
		properties.add(entityProperty.getPropertyName());
	}

	/**
	 * Scans for delete manipulations and checks if the data to be deleted should be deleted in elastic as well (marked
	 * with a special meta data {@link ElasticsearchDeleteMetaData}).
	 *
	 * @param context
	 *            - The context for determining the required {@link ElasticsearchDeleteMetaData}
	 * @param manipulations
	 *            - The manipulations to analyze for {@link com.braintribe.model.generic.manipulation.DeleteManipulation
	 *            DeleteManipulations}
	 */
	private List<PersistentEntityReference> scanDeleteManipulations(
			AfterContext<ManipulationRequest, ManipulationResponse> context, List<Manipulation> manipulations) {

		List<PersistentEntityReference> persistentEntityReferences = new ArrayList<>();

		for (Manipulation manipulation : manipulations) {

			switch (manipulation.manipulationType()) {
			case COMPOUND:

				List<Manipulation> compoundManipulationList = ((CompoundManipulation) manipulation)
						.getCompoundManipulationList();

				if (compoundManipulationList != null) {
					persistentEntityReferences.addAll(scanDeleteManipulations(context, compoundManipulationList));
				}
				break;

			case DELETE:

				GenericEntity entity = ((DeleteManipulation) manipulation).getEntity();
				PersistentEntityReference persistentEntityReference = (PersistentEntityReference) entity;
				String typeSignature = persistentEntityReference.getTypeSignature();

				if (isDeleteEnabled(typeSignature, context)
						&& !persistentEntityReferences.contains(persistentEntityReference)) {

					if (logger.isTraceEnabled()) {
						logger.trace("[after interception] Adding entity (reference id:"
								+ persistentEntityReference.getRefId() + ") for deletion.");
					}

					persistentEntityReferences.add(persistentEntityReference);
				}

				break;

			default:
				break;
			}

		}
		return persistentEntityReferences;
	}

	/**
	 * Checks if the incoming {@link com.braintribe.model.generic.GenericEntity entity} holds the
	 * {@link ElasticsearchDeleteMetaData}.
	 *
	 * @param typeSignature
	 *            - The type signature of the {@link com.braintribe.model.generic.GenericEntity entity} to check the
	 *            meta data for
	 * @param context
	 *            - The {@link com.braintribe.model.processing.aop.api.context.AfterContext AfterContext} which holds
	 *            the session
	 * @return - <code>true</code> if the checked entity holds the meta data, <code>false</code> otherwise
	 */
	private boolean isDeleteEnabled(String typeSignature,
			AfterContext<ManipulationRequest, ManipulationResponse> context) {

		Boolean deleteEnabled = typeSignaturesToElasticDeleteEnabled.get(typeSignature);

		if (deleteEnabled == null) {
			synchronized (typeSignaturesToElasticDeleteEnabled) {

				ElasticsearchDeleteMetaData metaData = context.getSession().getModelAccessory()
						.getCmdResolver().getMetaData().entityTypeSignature(typeSignature)
						.meta(ElasticsearchDeleteMetaData.T).exclusive();

				deleteEnabled = (metaData != null);

				if (logger.isDebugEnabled()) {
					logger.debug("[after interception] Elasticserach deletion for '" + typeSignature + "' is set to: "
							+ deleteEnabled);
				}

				typeSignaturesToElasticDeleteEnabled.put(typeSignature, deleteEnabled);
			}
		}

		return deleteEnabled;
	}

	/**
	 * Initializes the aspect with all required components.
	 *
	 * @throws InterceptionException
	 *             - If the initialization fails for any reason
	 */
	private void initialize(ElasticsearchClient client, String index)
			throws InterceptionException {
		if (initialized) {
			return;
		}

		try {
			FulltextTypeSupport.updateMappings(client, index, maxResultWindowSize);

			initialized = true;

		} catch (Throwable e) {
			throw new InterceptionException("Error while initializing aspect.", e);
		}
	}
//
//	private ElasticsearchIndexingWorkerImpl getElasticsearchWorker() throws InterceptionException {
//
//		DeployedUnit unit = deployRegistry.resolve(workerId);
//
//		if (unit == null)
//			throw new InterceptionException("Elasticsearch Worker for '" + workerId + "' deployedUnit is null.");
//
//		ElasticsearchIndexingWorkerImpl impl = unit.getComponent(Worker.T);
//		return impl;
//	}

	/**
	 * class to replace entity property instances with preliminary references to their counterpart after the
	 * manipulations have been applied by the access maps {@link EntityReference} to {@link PersistentEntityReference}
	 *
	 * @author pit
	 *
	 */
	private class ReferenceAdapter {
		private final Map<EntityReference, PersistentEntityReference> translatedReferenceMap = CodingMap
				.create(EntRefHashingComparator.INSTANCE);

		public ReferenceAdapter(Manipulation manipulation) {
			super();
			scanIdManipulations(manipulation);
		}

		private void scanIdManipulations(Manipulation manipulation) {
			switch (manipulation.manipulationType()) {
			case COMPOUND:

				List<Manipulation> compoundManipulationList = ((CompoundManipulation) manipulation)
						.getCompoundManipulationList();

				if (compoundManipulationList != null) {

					for (Manipulation m : compoundManipulationList) {
						scanIdManipulations(m);
					}
				}
				break;

			case CHANGE_VALUE:

				ChangeValueManipulation m = (ChangeValueManipulation) manipulation;
				scanIdManipulationCandidate(m);

				break;

			default:
				break;
			}

		}

		private void scanIdManipulationCandidate(ChangeValueManipulation m) {
			EntityProperty entityProperty = (EntityProperty) m.getOwner();
			EntityReference reference = entityProperty.getReference();
			if (reference instanceof PreliminaryEntityReference) {

				if (GenericEntity.id.equalsIgnoreCase(entityProperty.getPropertyName())) {

					PersistentEntityReference persistentEntityReference = PersistentEntityReference.T.create();
					persistentEntityReference.setTypeSignature(reference.getTypeSignature());
					persistentEntityReference.setRefId(m.getNewValue());

					translatedReferenceMap.put(reference, persistentEntityReference);
				}
			}
		}

		/**
		 * translates an entity property with a {@link PreliminaryEntityReference} into one with a proper
		 * {@link PersistentEntityReference}
		 *
		 * @param entityProperty
		 *            - the {@link EntityProperty} involved
		 * @return - if needed a new {@link EntityProperty} or the one passed
		 */
		public EntityProperty adapt(EntityProperty entityProperty) {
			EntityReference reference = entityProperty.getReference();

			if (reference instanceof PreliminaryEntityReference) {

				if (logger.isDebugEnabled()) {
					logger.debug("[after interception] Adapting preliminary reference '" + reference.getRefId()
							+ "' to persistent one.");
				}

				PersistentEntityReference adaptedReference = translatedReferenceMap.get(reference);
				String propertyName = entityProperty.getPropertyName();
				entityProperty = EntityProperty.T.create();
				entityProperty.setReference(adaptedReference);
				entityProperty.setPropertyName(propertyName);
			}

			return entityProperty;
		}
	}

	/**
	 * @see com.braintribe.model.processing.aop.api.aspect.AccessAspect#configurePointCuts(com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext)
	 */
	@Override
	public void configurePointCuts(PointCutConfigurationContext context) throws AccessAspectRuntimeException {
		context.addPointCutBinding(AccessJoinPoint.query, Advice.before, this);
		context.addPointCutBinding(AccessJoinPoint.queryEntities, Advice.before, this);
		context.addPointCutBinding(AccessJoinPoint.queryProperties, Advice.before, this);

		// Deletion interception
		context.addPointCutBinding(AccessJoinPoint.applyManipulation, Advice.after, this);
	}

	@Required
	@Configurable
	public void setElasticsearchConnector(IndexedElasticsearchConnector elasticsearchConnector) {
		this.elasticsearchConnector = elasticsearchConnector;
	}

	@Required
	@Configurable
	public void setWorker(ElasticsearchIndexingWorker worker) {
		this.worker = worker;
	}

	@Configurable
	public void setMaxFulltextResultSize(Integer maxFulltextResultSize) {
		if (maxFulltextResultSize != null) {

			if (maxFulltextResultSize <= 0 || maxFulltextResultSize > 1000) {
				logger.warn("Maximum of fulltext search results must not be larger than 1000 (or negative), but was configured to "
						+ maxFulltextResultSize + ", result size set to '1000'.");

				this.maxFulltextResultSize = 1000;
			} else {
				this.maxFulltextResultSize = maxFulltextResultSize;

			}
		}
	}

	@Configurable
	public void setCascadingAttachment(boolean cascadingAttachment) {
		this.cascadingAttachment = cascadingAttachment;
	}

	public boolean isCascadingAttachment() {
		return cascadingAttachment;
	}

	@Configurable
	public void setMaxResultWindowSize(Integer maxResultWindowSize) {
		this.maxResultWindowSize = maxResultWindowSize;
	}

	@Configurable
	public void setIgnoreUnindexedEntities(boolean ignoreUnindexedEntities) {
		this.ignoreUnindexedEntities = ignoreUnindexedEntities;
	}

	@Configurable
	@Required
	public void setFulltextProcessing(FulltextProcessing fulltextProcessing) {
		this.fulltextProcessing = fulltextProcessing;
	}

}
