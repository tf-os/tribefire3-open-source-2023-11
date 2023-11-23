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
package tribefire.extension.messaging.service;

import static tribefire.extension.messaging.model.deployment.event.DiffLoader.QUERY;
import static tribefire.extension.messaging.model.deployment.event.DiffLoader.SERVICE;
import static tribefire.extension.messaging.model.meta.RelatedObjectType.REQUEST;
import static tribefire.extension.messaging.model.meta.RelatedObjectType.RESPONSE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.attribute.AttributeContext;
import com.braintribe.gm.model.reason.ReasonException;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.CmdResolverImpl;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ProceedContext;
import com.braintribe.model.processing.service.api.ServiceAroundProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.collection.impl.AttributeContexts;

import tribefire.extension.messaging.model.Message;
import tribefire.extension.messaging.model.comparison.ComparisonResult;
import tribefire.extension.messaging.model.deployment.event.DiffLoader;
import tribefire.extension.messaging.model.deployment.event.rule.EventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerDiffEventRule;
import tribefire.extension.messaging.model.deployment.event.rule.ProducerEventRule;
import tribefire.extension.messaging.model.deployment.service.MessagingAspect;
import tribefire.extension.messaging.model.deployment.service.MessagingProcessor;
import tribefire.extension.messaging.model.meta.MessagingProperty;
import tribefire.extension.messaging.model.meta.MessagingTypeSignature;
import tribefire.extension.messaging.model.service.admin.FlushCacheMessagingResult;
import tribefire.extension.messaging.model.service.admin.FlushProducerConfigurationCache;
import tribefire.extension.messaging.model.service.produce.ProduceMessage;
import tribefire.extension.messaging.service.cache.CortexCache;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;
import tribefire.extension.messaging.service.reason.validation.MandatoryNotSatisfied;
import tribefire.extension.messaging.service.utils.MessageComposer;

/**
 * This class should be thread safe!!!
 */
public class MessagingAspectImpl implements ServiceAroundProcessor<ServiceRequest, Object>,
		ServiceProcessor<FlushProducerConfigurationCache, GenericEntity> {
	private static final Logger logger = Logger.getLogger(MessagingAspectImpl.class);
	private static final BiPredicate<ProducerEventRule, DiffLoader> RULE_LOADER_PREDICATE = ((rule,
			type) -> rule instanceof ProducerDiffEventRule r && type == r.getDiffLoader());
	private static final BiPredicate<List<ProducerEventRule>, DiffLoader> LIST_RULES_LOADER_PREDICATE = (matchingRules,
			type) -> matchingRules.stream().anyMatch(rule -> RULE_LOADER_PREDICATE.test(rule, type));
	private static final TraversingCriterion LOAD_ALL_TC = TC.create().negation().joker().done();
	private MessagingProcessor messagingProcessor;
	private MessagingAspect deployable;
	private String externalId;
	private PersistenceGmSessionFactory factory;
	private String contextName;
	private CortexCache<String, ProducerEventRule> producerRuleCache;
	private CortexCache<String, MessagingTypeSignature> tsMdCache;
	private CortexCache<Property, MessagingProperty> propMdCache;

	@Override
	public Object process(ServiceRequestContext requestContext, ServiceRequest request, ProceedContext proceedContext) {
		long start = System.currentTimeMillis();
		logger.info(() -> externalId
				+ "PreProcessor engaged! Try applying conditions to see if we need to send the message");
		List<ProducerEventRule> rules = updateRules();
		List<ProducerEventRule> matchingRules = rules.stream().filter(r -> r.appliesTo(request)).toList();
		Object response = matchingRules.isEmpty() ? processNoRules(request, proceedContext)
				: processWithRules(requestContext, request, proceedContext, matchingRules);
		logger.info(">>>>>>>>>>>>>>Time used total messaging: " + (System.currentTimeMillis() - start)
				+ " requestType: " + request.entityType().getShortName());
		return response;
	}

	private Object processNoRules(ServiceRequest request, ProceedContext proceedContext) {
		logger.info(externalId + "no message has to be sent. " + "REQ_TYPE = " + request.entityType().getShortName());
		return proceedContext.proceed(request);
	}

	private Object processWithRules(ServiceRequestContext requestContext, ServiceRequest request,
			ProceedContext proceedContext, List<ProducerEventRule> matchingRules) {
		Object expertResult = null;
		List<GenericEntity> beforeStateQuery = new ArrayList<>();
		List<GenericEntity> afterStateQuery = new ArrayList<>();
		List<GenericEntity> beforeStateService = new ArrayList<>();
		List<GenericEntity> afterStateService = new ArrayList<>();

		if (matchingRules.stream().anyMatch(ProducerEventRule::requiresDiff)) {
			expertResult = processForDiff(requestContext, request, proceedContext, matchingRules, beforeStateQuery,
					afterStateQuery, beforeStateService, afterStateService);
		} else if (matchingRules.stream().anyMatch(ProducerEventRule::requiresResponse)) {
			expertResult = processRequestWithCorrespondingExpert(proceedContext, request);
		}

		emitMessagePerRule(requestContext, request, matchingRules, expertResult, beforeStateQuery, afterStateQuery,
				beforeStateService, afterStateService);

		if (expertResult == null) {
			expertResult = processRequestWithCorrespondingExpert(proceedContext, request);
		}

		if (expertResult instanceof UnsatisfiedMaybeTunneling e) {
			throw e;
		} else {
			return expertResult;
		}
	}

	private Object processForDiff(ServiceRequestContext requestContext, ServiceRequest request,
			ProceedContext proceedContext, List<ProducerEventRule> matchingRules, List<GenericEntity> beforeStateQuery,
			List<GenericEntity> afterStateQuery, List<GenericEntity> beforeStateService,
			List<GenericEntity> afterStateService) {
		CmdResolver resolver = getResolver(request);
		Object expertResult = null;
		MessagingTypeSignature typeSignatureMd = getTypeSignatureMd(request, resolver);
		GenericEntity idContainer;
		if (typeSignatureMd.getIdObjectType() == RESPONSE) {
			expertResult = processRequestWithCorrespondingExpert(proceedContext, request);
			idContainer = (GenericEntity) expertResult;
		} else {
			idContainer = request;
		}

		Set<Object> idSet = new HashSet<>();
		MessagingProperty md = getPropertyMdAndExtractIds(idSet, idContainer, resolver);

		if (idSet.isEmpty()) {
			throw new UnsatisfiedMaybeTunneling(Reasons.build(MandatoryNotSatisfied.T)
					.text("There are no Ids extracted from request/response!").toMaybe());
		}

		// here we are have to consider possibility of multiple matching rules with
		// different loading settings could be applied,
		// so situation when we would need both Service/Query loaded instances is quite
		// possible
		boolean loadWithService = LIST_RULES_LOADER_PREDICATE.test(matchingRules, SERVICE);
		boolean loadWithQuery = LIST_RULES_LOADER_PREDICATE.test(matchingRules, QUERY);
		PersistenceGmSession session = session(requestContext);
		if (typeSignatureMd.getIdObjectType() == REQUEST) {
			loadAndSet(loadWithQuery, beforeStateQuery::addAll, () -> loadObjectStateWithQuery(session, idSet, md));
			loadAndSet(loadWithService, beforeStateService::addAll,
					() -> loadUsingService(md, idSet, requestContext, resolver));
			expertResult = processRequestWithCorrespondingExpert(proceedContext, request);
		}

		loadAndSet(loadWithQuery, afterStateQuery::addAll, () -> loadObjectStateWithQuery(session, idSet, md));
		loadAndSet(loadWithService, afterStateService::addAll,
				() -> loadUsingService(md, idSet, requestContext, resolver));
		return expertResult;
	}

	private void loadAndSet(boolean predicate, Consumer<Collection<GenericEntity>> consumer,
			Supplier<List<GenericEntity>> entrySupplier) {
		if (predicate) {
			consumer.accept(entrySupplier.get());
		}
	}

	private Object processRequestWithCorrespondingExpert(ProceedContext proceedContext, ServiceRequest request) {
		try {
			long start = System.currentTimeMillis();
			Object result = proceedContext.proceed(request);
			logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>Time used processing: " + (System.currentTimeMillis() - start));
			return result;
		} catch (UnsatisfiedMaybeTunneling e) {
			return e;
		}
	}

	private void emitMessagePerRule(ServiceRequestContext requestContext, ServiceRequest request,
			List<ProducerEventRule> matchingRules, Object result, List<GenericEntity> beforeState,
			List<GenericEntity> afterState, List<GenericEntity> beforeStateService,
			List<GenericEntity> afterStateService) {
		for (ProducerEventRule r : matchingRules) {
			if (r.getEndpointConfiguration().isEmpty()) {
				logger.error(String.format(
						"Message can't be sent using rule %s, as there are no destinations configured for it!",
						r.getName()));
			} else {
				boolean useExpertLoaderResults = RULE_LOADER_PREDICATE.test(r, SERVICE);
				Message message = MessageComposer.createMessage(r, request, result,
						useExpertLoaderResults ? beforeStateService : beforeState,
						useExpertLoaderResults ? afterStateService : afterState
								, requestContext
								);
				ProduceMessage produceMessage = ProduceMessage.create(message);
				produceMessage.setServiceId(messagingProcessor.getExternalId());

				// THIS IS TEST LOGGER TO TRACE MESSAGE IN CONSOLE --TODO should be removed
				// after fully tested and confirmed
				produceMessage.getEnvelop().getMessages().get(0).getValues().entrySet().stream().map(e -> {
					if (e.getValue() instanceof ComparisonResult c) {
						return ">>>>>>>>>>>> message contents: " + e.getKey() + " " + c.expectedDiffsAsStringMessage();
					}
					return ">>>>>>>>>>>> message contents: " + e.getKey() + " " + e.getValue();
				}).forEach(logger::info);
				logger.info(">>>>>>>>>>>> END SENDING >>>>>>>>>>>>>>>>>>");
				// END OF TEST LOGGER

				AttributeContext attributeContext = AttributeContexts.peek();
				AttributeContext derivedContext = attributeContext.derive()
						.set(MessagingDestination.class, r.getEndpointConfiguration()).build();
				AttributeContexts.push(derivedContext);
				try {
					produceMessage.eval(requestContext).get();
				} finally {
					AttributeContexts.pop();
				}
			}
		}
	}

	// ------------------------ MetaData Related Methods ------------------------ //
	private MessagingTypeSignature getTypeSignatureMd(ServiceRequest request, CmdResolver resolver) {
		String typeSignature = request.entityType().getTypeSignature();
		MessagingTypeSignature md = loadTypeSignatureMdListFromCacheOrCortex(request, resolver, typeSignature);

		if (md != null) {
			tsMdCache.put(typeSignature, md);
			return md;
		}
		throw new UnsatisfiedMaybeTunneling(Reasons.build(ArgumentNotSatisfied.T)
				.text("TypeSignatureMetaData for " + typeSignature + ", not found! Consider registering one!")
				.toMaybe());
	}

	private MessagingTypeSignature loadTypeSignatureMdListFromCacheOrCortex(ServiceRequest request,
			CmdResolver resolver, String typeSignature) {
		synchronized (this) {
			MessagingTypeSignature md = tsMdCache.get(typeSignature);
			if (md == null) {
				//@formatter:off
				md = resolver.getMetaData()
					         .lenient(true)
					         .entity(request)
						     .meta(MessagingTypeSignature.T)
						     .exclusive();
				//@formatter:on
			}
			return md;
		}
	}

	private MessagingProperty getPropertyMdAndExtractIds(Set<Object> ids, GenericEntity idContainer,
			CmdResolver resolver) {
		EntityType<?> entityType = idContainer.entityType();
		synchronized (this) {
			for (Property p : idContainer.entityType().getProperties()) {
				MessagingProperty md = getPropertyMd(resolver, entityType, p);
				if (md != null) {
					Object valueObject = p.get(idContainer);
					ids.addAll(extractIds(valueObject));

					if (!ids.isEmpty()) {
						return md;
					}
				}
			}
		}
		throw new UnsatisfiedMaybeTunneling(
				Reasons.build(ArgumentNotSatisfied.T)
						.text("PropertyMetaData for " + entityType.getTypeSignature()
								+ ", are not found neither in cache nor in cortex! Consider registering one!")
						.toMaybe());
	}

	private MessagingProperty getPropertyMd(CmdResolver resolver, EntityType<?> entityType, Property property) {
		MessagingProperty propertyMd = propMdCache.get(property);
		if (propertyMd == null) {
			//@formatter:off
			propertyMd = resolver.getMetaData()
					             .lenient(true)
					             .entityType(entityType)
					             .property(property)
					             .meta(MessagingProperty.T)
					             .exclusive();
			//@formatter:on
			if (propertyMd != null) {
				propMdCache.put(property, propertyMd);
			}
		}
		return propertyMd;
	}

	private Set<String> extractIds(Object valueObject) {
		if (valueObject instanceof GenericEntity e) {
			return Set.of((String) e.getId());
		} else if (valueObject.getClass().isPrimitive() || valueObject instanceof String) {
			return Set.of((String) valueObject);
		} else if (valueObject instanceof Collection<?> c) {
			return c.stream().map(v -> {
				if (v instanceof GenericEntity e) {
					return (String) e.getId();
				} else if (v.getClass().isPrimitive() || v instanceof String) {
					return (String) v;
				} else {
					logger.error("Unknown object type! Could not extract ids out of it!" + v.getClass().getTypeName());
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toSet());
		} else {
			logger.error("Unknown object type! Could not extract ids out of it! " + valueObject.getClass().getName());
			return Collections.emptySet();
		}
	}

	// ------------------------ Load Objects for Diff Related Methods
	// ------------------------ //
	private List<GenericEntity> loadObjectStateWithQuery(PersistenceGmSession session, Set<Object> ids,
			MessagingProperty propertyMd) {
		//@formatter:off
		EntityQuery query =
				EntityQueryBuilder.from(propertyMd.getLoadedObjectType())
						.where()
						.property(GenericEntity.id)
						.in(ids)
						.tc(LOAD_ALL_TC)
						.done();
		//@formatter:on
		return session.queryDetached().entities(query).list();
	}

	private List<GenericEntity> loadUsingService(MessagingProperty propertyMd, Set<Object> ids,
			ServiceRequestContext requestContext, CmdResolver resolver) {
		EntityType<ServiceRequest> expertRequestType = (EntityType<ServiceRequest>) EntityTypes
				.get(propertyMd.getGetterEntityType());
		for (Property property : expertRequestType.getProperties()) {
			MessagingProperty requestPropertyMd = getPropertyMd(resolver, expertRequestType, property);
			if (requestPropertyMd != null) {
				if (property.getType().isSimple()) {
					return ids.stream().map(id -> (GenericEntity) loadUsingServiceRequest(expertRequestType, property,
							requestContext.getDomainId(), id)).toList();
				}

				if (property.getType().isCollection()) {
					Object result = loadUsingServiceRequest(expertRequestType, property, requestContext.getDomainId(),
							ids);
					if (result instanceof GenericEntity entity) {
						return List.of(entity);
					} else {
						return new ArrayList<>((Collection<GenericEntity>) result);
					}
				}
				throw new IllegalArgumentException("Found PropertyMd but it is not usable for loading with expert! "
						+ expertRequestType.getTypeSignature());
			}
		}
		throw new IllegalArgumentException("Could not load PropertyMd to load Objects using Expert! " + propertyMd);
	}

	private Object loadUsingServiceRequest(EntityType<ServiceRequest> type, Property property, String domainId,
			Object id) {
		ServiceRequest request = type.create();
		property.set(request, id);
		PersistenceGmSession newSession = factory.newSession(domainId);
		try {
			return request.eval(newSession).get();
		} catch (ReasonException exception) {
			logger.warn(exception.getReason().asString());
			return null;
		}
	}
	// ------------------------ EventRule update Related ------------------------ //

	private List<ProducerEventRule> updateRules() {
		ArrayList<ProducerEventRule> cachedRules = producerRuleCache.getAll(ArrayList::new);
		return cachedRules.isEmpty() ? queryCortexForRules() : cachedRules;
	}

	private List<ProducerEventRule> queryCortexForRules() {
		String searchPattern = StringUtils.isEmpty(contextName) ? "*" : ("*" + contextName + "*");
		//@formatter:off
        EntityQuery query = EntityQueryBuilder.from(ProducerEventRule.T)
                .where()
                	.conjunction()
	                	.property(EventRule.ruleEnabled).eq(true)
	                	.property(ProducerEventRule.globalId).like(searchPattern)
	                .close()
                .done();
        //@formatter:on
		List<ProducerEventRule> rules = factory.newSession("cortex").query().entities(query)
				.setTraversingCriterion(LOAD_ALL_TC).list();
		rules.forEach(r -> producerRuleCache.put(r));
		return rules;
	}

	// ------------------------ Miscellaneous private methods
	// ------------------------ //
	private CmdResolver getResolver(ServiceRequest request) {
		GmMetaModel model = request.entityType().getModel().getMetaModel();
		String globalId = model.getGlobalId();
		ModelOracle serviceModelOracle = createOracleFor(globalId);
		return new CmdResolverImpl(serviceModelOracle);
	}

	private ModelOracle createOracleFor(String globalId) {
		PersistenceGmSession cortexSession = factory.newSession("cortex");
		GmMetaModel model = cortexSession.findEntityByGlobalId(globalId);
		//@formatter:off
		model = cortexSession.query()
				        .entity(model)
				        .withTraversingCriterion(LOAD_ALL_TC)
				        .refresh();
		//formatter:on
		return new BasicModelOracle(model);
	}

	private PersistenceGmSession session(ServiceRequestContext requestContext) {
		String domainId = requestContext.getDomainId();
		return factory.newSession(domainId);
	}

	// ------------------------ Setters ------------------------ //

	@Required
	@Configurable
	public void setMessagingProcessor(MessagingProcessor messagingProcessor) {
		this.messagingProcessor = messagingProcessor;
	}

	@Required
	@Configurable
	public void setDeployable(MessagingAspect deployable) {
		this.deployable = deployable;
		this.externalId = "[" + deployable.getExternalId() + "] ";
	}

	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory factory) {
		this.factory = factory;
	}

	@Required
	@Configurable
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	@Required
	@Configurable
	public void setProducerRuleCache(CortexCache<String, ProducerEventRule> producerRuleCache) {
		this.producerRuleCache = producerRuleCache;
	}

	@Required
	@Configurable
	public void setTypeSignatureMdCache(CortexCache<String, MessagingTypeSignature> typeSignatureMdCache) {
		this.tsMdCache = typeSignatureMdCache;
	}

	@Required
	@Configurable
	public void setPropertyMdCache(CortexCache<Property, MessagingProperty> propertyMdCache) {
		this.propMdCache = propertyMdCache;
	}

	// ------------------------ ProcessWith override ------------------------ //

	@Override
	public GenericEntity process(ServiceRequestContext requestContext, FlushProducerConfigurationCache request) {
		flushCaches();
		return FlushCacheMessagingResult.T.create();
	}

	// ------------------------ Flush producerEventRule cache handle ------------------------ //
	public void flushCaches() {
		producerRuleCache.invalidateCache();
		propMdCache.invalidateCache();
		tsMdCache.invalidateCache();
		logger.info(() -> "Caches has being wiped!!!");
	}
}
