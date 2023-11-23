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
package tribefire.extension.metrics.templates.api;

import java.util.Set;
import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.extension.metrics.templates.api.connector.MetricsTemplateConnectorContext;
import tribefire.extension.metrics.templates.util.MetricsTemplateUtil;

public class MetricsTemplateContextImpl implements MetricsTemplateContext, MetricsTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(MetricsTemplateContextImpl.class);

	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<String, ? extends HasExternalId> lookupExternalIdFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	public MetricsTemplateContextImpl() {
	}

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	private String globalIdPrefix;

	private boolean addDemo;

	private String context;

	private Set<MetricsTemplateConnectorContext> connectorContexts;

	private Module metricsModule;

	// -----------------------------------------------------------------------
	// Metrics Connector
	// -----------------------------------------------------------------------

	private String serviceName;

	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	@Override
	public MetricsTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix) {
		this.globalIdPrefix = globalIdPrefix;
		return this;
	}

	@Override
	public String getGlobalIdPrefix() {
		return globalIdPrefix;
	}

	@Override
	public MetricsTemplateContextBuilder setAddDemo(boolean addDemo) {
		this.addDemo = addDemo;
		return this;
	}

	@Override
	public boolean getAddDemo() {
		return addDemo;
	}

	@Override
	public MetricsTemplateContextBuilder setContext(String context) {
		this.context = context;
		return this;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public MetricsTemplateContextBuilder setConnectorContexts(Set<MetricsTemplateConnectorContext> connectorContexts) {
		this.connectorContexts = connectorContexts;
		return this;
	}

	@Override
	public Set<MetricsTemplateConnectorContext> getConnectorContexts() {
		return connectorContexts;
	}

	@Override
	public MetricsTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public Module getMetricsModule() {
		return metricsModule;
	}

	@Override
	public MetricsTemplateContextBuilder setMetricsModule(Module module) {
		this.metricsModule = module;
		return this;
	}

	@Override
	public MetricsTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public MetricsTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction) {
		this.lookupExternalIdFunction = lookupExternalIdFunction;
		return this;
	}

	@Override
	public MetricsTemplateContext build() {
		return this;
	}

	@Override
	public String toString() {
		// TODO: change
		return null;
	}

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		String globalId = MetricsTemplateUtil.resolveContextBasedGlobalId(this, instanceConfiguration);

		InstanceQualification qualification = instanceConfiguration.qualification();

		entity.setGlobalId(globalId);

		if (entity instanceof HasExternalId) {
			HasExternalId eid = (HasExternalId) entity;

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
}
