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
package tribefire.extension.messaging.templates.api;

import static java.lang.String.format;

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.UnsatisfiedMaybeTunneling;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.extension.messaging.model.deployment.event.ConsumerEventConfiguration;
import tribefire.extension.messaging.model.deployment.event.EventConfiguration;
import tribefire.extension.messaging.model.deployment.event.ProducerEventConfiguration;
import tribefire.extension.messaging.service.reason.validation.ArgumentNotSatisfied;
import tribefire.extension.messaging.templates.util.MessagingTemplateUtil;

public class MessagingTemplateContextImpl implements MessagingTemplateContext, MessagingTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(MessagingTemplateContextImpl.class);

	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<String, ? extends HasExternalId> lookupExternalIdFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	private EventConfiguration eventConfiguration;
	private GmMetaModel serviceModelDependency;

	private String globalIdPrefix;
	private String context;
	private Module messagingModule;

	@Override
	public MessagingTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix) {
		this.globalIdPrefix = globalIdPrefix;
		return this;
	}

	@Override
	public String getGlobalIdPrefix() {
		return globalIdPrefix;
	}

	@Override
	public MessagingTemplateContextBuilder setContext(String context) {
		this.context = context;
		return this;
	}

	@Override
	public MessagingTemplateContextBuilder setEventConfiguration(EventConfiguration eventConfiguration) {
		this.eventConfiguration = eventConfiguration;
		return this;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public EventConfiguration getEventConfiguration() {
		return this.eventConfiguration;
	}

	@Override
	public GmMetaModel getServiceModelDependency() {
		return serviceModelDependency;
	}

	@Override
	public MessagingTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public Module getMessagingModule() {
		return messagingModule;
	}

	@Override
	public MessagingTemplateContextBuilder setMessagingModule(Module module) {
		this.messagingModule = module;
		return this;
	}

	@Override
	public MessagingTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public MessagingTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction) {
		this.lookupExternalIdFunction = lookupExternalIdFunction;
		return this;
	}

	@Override
	public MessagingTemplateContextBuilder setServiceModelDependency(GmMetaModel serviceModelDependency) {
		this.serviceModelDependency = serviceModelDependency;
		return this;
	}

	@Override
	public MessagingTemplateContext build() {
		checkProperty(this::getContext, "context");
		checkProperty(this::getEventConfiguration, "eventConfiguration");
		// checkProperty(eventConfiguration::getEventRules, "eventRules"); //TODO fix me
		if (eventConfiguration instanceof ProducerEventConfiguration p) {
			// TODO more checks after @dmiex
		}
		if (eventConfiguration instanceof ConsumerEventConfiguration c) {
			c.getEventRules().forEach(r -> checkProperty(r::getPostProcessorType, "postProcessorType"));
		}
		return this;
	}

	private void checkProperty(Supplier<Object> supplier, String fieldName) {
		if (supplier.get() == null) {
			throw new UnsatisfiedMaybeTunneling(
					Reasons.build(ArgumentNotSatisfied.T).text(format("The MessagingTemplateContext property %s must be set!", fieldName)).toMaybe());
		}
	}

	@Override
	public String toString() {
		return "MessagingTemplateContextImpl [lookupFunction=" + lookupFunction + ", lookupExternalIdFunction=" + lookupExternalIdFunction
				+ ", entityFactory=" + entityFactory + ", context=" + context + ", eventConfiguration=" + eventConfiguration + ", deploymentType="
				+ eventConfiguration.getDeploymentType() + ", messagingModule=" + messagingModule + "]";
	}

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		String globalId = MessagingTemplateUtil.resolveContextBasedGlobalId(this, instanceConfiguration);

		InstanceQualification qualification = instanceConfiguration.qualification();

		entity.setGlobalId(globalId);

		if (entity instanceof HasExternalId eid) {
			String part1 = StringTools.camelCaseToDashSeparated(entityType.getShortName());
			String part2 = StringTools.camelCaseToDashSeparated(context);
			String part3 = StringTools.camelCaseToDashSeparated(qualification.space().getClass().getSimpleName());
			String part4 = StringTools.camelCaseToDashSeparated(qualification.name());
			String externalId = part1 + "." + part2 + "." + part3 + "." + part4;
			externalId = externalId.replace("/", "-");

			if (logger.isDebugEnabled()) {
				logger.debug("Prepared externalId: '" + externalId + "' for globalId: '" + globalId + "'");
			}

			eid.setExternalId(externalId);
		}

		return entity;
	}

	@Override
	public <T extends GenericEntity> T lookup(String globalId) {
		return (T) lookupFunction.apply(globalId);
	}

	@Override
	public <T extends HasExternalId> T lookupExternalId(String externalId) {
		return (T) lookupExternalIdFunction.apply(externalId);
	}

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	protected MessagingTemplateContextImpl() {
	}

}
