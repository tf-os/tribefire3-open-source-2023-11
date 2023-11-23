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
package tribefire.extension.antivirus.templates.api;

import java.util.List;
import java.util.function.Function;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

import tribefire.extension.antivirus.model.deployment.repository.configuration.ProviderSpecification;
import tribefire.extension.antivirus.templates.util.AntivirusTemplateUtil;

public class AntivirusTemplateContextImpl implements AntivirusTemplateContext, AntivirusTemplateContextBuilder {

	private static final Logger logger = Logger.getLogger(AntivirusTemplateContextImpl.class);

	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<String, ? extends HasExternalId> lookupExternalIdFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;

	// -----------------------------------------------------------------------
	// Constructor
	// -----------------------------------------------------------------------

	public AntivirusTemplateContextImpl() {
	}

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	private String globalIdPrefix;

	private boolean addDemo;

	private String context;

	private Module antivirusModule;

	private String serviceName;

	private List<ProviderSpecification> providerSpecifications;

	private String antivirusContext;

	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------
	// -----------------------------------------------------------------------

	@Override
	public AntivirusTemplateContextBuilder setProviderSpecifications(List<ProviderSpecification> providerSpecifications) {
		this.providerSpecifications = providerSpecifications;
		return this;
	}

	@Override
	public AntivirusTemplateContextBuilder setAntivirusContext(String antivirusContext) {
		this.antivirusContext = antivirusContext;
		return this;
	}

	// -----------------------------------------------------------------------
	// Common
	// -----------------------------------------------------------------------

	@Override
	public AntivirusTemplateContextBuilder setGlobalIdPrefix(String globalIdPrefix) {
		this.globalIdPrefix = globalIdPrefix;
		return this;
	}

	@Override
	public String getGlobalIdPrefix() {
		return globalIdPrefix;
	}

	@Override
	public AntivirusTemplateContextBuilder setContext(String context) {
		this.context = context;
		return this;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public AntivirusTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public Module getAntivirusModule() {
		return antivirusModule;
	}

	@Override
	public AntivirusTemplateContextBuilder setAntivirusModule(Module module) {
		this.antivirusModule = module;
		return this;
	}

	@Override
	public AntivirusTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public AntivirusTemplateContextBuilder setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction) {
		this.lookupExternalIdFunction = lookupExternalIdFunction;
		return this;
	}

	@Override
	public AntivirusTemplateContext build() {
		return this;
	}

	@Override
	public String toString() {
		return "AntivirusTemplateContextImpl [lookupFunction=" + lookupFunction + ", lookupExternalIdFunction=" + lookupExternalIdFunction
				+ ", entityFactory=" + entityFactory + ", addDemo=" + addDemo + ", context=" + context + ", antivirusModule=" + antivirusModule
				+ ", serviceName=" + serviceName + "]";
	}

	// -----------------------------------------------------------------------
	// CONTEXT METHODS
	// -----------------------------------------------------------------------

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		String globalId = AntivirusTemplateUtil.resolveContextBasedGlobalId(this, instanceConfiguration);

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
	@Override
	public List<ProviderSpecification> getProviderSpecifications() {
		return this.providerSpecifications;
	}

	@Override
	public String getAntivirusContext() {
		return antivirusContext;
	}

}
