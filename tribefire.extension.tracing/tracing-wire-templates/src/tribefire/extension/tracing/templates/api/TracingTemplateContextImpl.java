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
package tribefire.extension.tracing.templates.api;

import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.extension.tracing.templates.api.connector.TracingTemplateConnectorContext;
import tribefire.extension.tracing.templates.util.TracingTemplateUtil;

public class TracingTemplateContextImpl implements TracingTemplateContext, TracingTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(TracingTemplateContextImpl.class);

	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<String, ? extends HasExternalId> lookupExternalIdFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	public TracingTemplateContextImpl() {
	}

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	private String globalIdPrefix;

	private boolean addDemo;

	private String context;

	private TracingTemplateConnectorContext connectorContext;

	private Module tracingModule;

	// -----------------------------------------------------------------------
	// Tracing Connector
	// -----------------------------------------------------------------------

	private String serviceName;

	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	@Override
	public TracingTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix) {
		this.globalIdPrefix = globalIdPrefix;
		return this;
	}

	@Override
	public String getGlobalIdPrefix() {
		return globalIdPrefix;
	}

	@Override
	public TracingTemplateContextBuilder setAddDemo(boolean addDemo) {
		this.addDemo = addDemo;
		return this;
	}

	@Override
	public boolean getAddDemo() {
		return addDemo;
	}

	@Override
	public TracingTemplateContextBuilder setContext(String context) {
		this.context = context;
		return this;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public TracingTemplateContextBuilder setConnectorContext(TracingTemplateConnectorContext connectorContext) {
		this.connectorContext = connectorContext;
		return this;
	}

	@Override
	public TracingTemplateConnectorContext getConnectorContext() {
		return connectorContext;
	}

	@Override
	public TracingTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public Module getTracingModule() {
		return tracingModule;
	}

	@Override
	public TracingTemplateContextBuilder setTracingModule(Module module) {
		this.tracingModule = module;
		return this;
	}

	@Override
	public TracingTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public TracingTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction) {
		this.lookupExternalIdFunction = lookupExternalIdFunction;
		return this;
	}

	@Override
	public TracingTemplateContext build() {
		return this;
	}

	@Override
	public String toString() {
		return "TracingTemplateContextImpl [lookupFunction=" + lookupFunction + ", lookupExternalIdFunction=" + lookupExternalIdFunction
				+ ", entityFactory=" + entityFactory + ", addDemo=" + addDemo + ", context=" + context + ", connectorContext=" + connectorContext
				+ ", tracingModule=" + tracingModule + ", serviceName=" + serviceName + "]";
	}

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		String globalId = TracingTemplateUtil.resolveContextBasedGlobalId(this, instanceConfiguration);

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
