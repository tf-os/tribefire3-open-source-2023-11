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
package tribefire.extension.azure.templates.api;

import java.util.function.Function;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

public class AzureTemplateContextImpl implements AzureTemplateContext, AzureTemplateContextBuilder {

	private String idPrefix;
	private String storageConnectionString;
	private String containerName;
	private String name;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	private Function<String, ? extends GenericEntity> lookupFunction;
	private Module azureModule;
	private String pathPrefix;

	@Override
	public AzureTemplateContext build() {
		return this;
	}

	@Override
	public AzureTemplateContextBuilder setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
		return this;
	}

	@Override
	public AzureTemplateContextBuilder setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getIdPrefix() {
		return idPrefix;
	}

	@Override
	public int hashCode() {
		return idPrefix.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AzureTemplateContext) {
			return ((AzureTemplateContext) obj).getIdPrefix().equals(this.idPrefix);
		}
		return super.equals(obj);
	}

	@Override
	public AzureTemplateContextBuilder setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public <T extends GenericEntity> T create(EntityType<T> entityType, InstanceConfiguration instanceConfiguration) {

		T entity = (T) entityFactory.apply(entityType);

		if (idPrefix == null) {
			throw new IllegalStateException("You have to specify a idPrefix.");
		}

		InstanceQualification qualification = instanceConfiguration.qualification();

		String globalId = "wire://" + idPrefix + "/" + qualification.space().getClass().getSimpleName() + "/" + qualification.name();

		entity.setGlobalId(globalId);

		if (entity instanceof HasExternalId) {
			HasExternalId eid = (HasExternalId) entity;

			String externalId = StringTools.camelCaseToDashSeparated(entityType.getShortName()) + "." + StringTools.camelCaseToDashSeparated(idPrefix)
					+ "." + StringTools.camelCaseToDashSeparated(qualification.space().getClass().getSimpleName()) + "."
					+ StringTools.camelCaseToDashSeparated(qualification.name());

			eid.setExternalId(externalId);
		}

		return entity;
	}

	@Override
	public AzureTemplateContextBuilder setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public <T extends GenericEntity> T lookup(String globalId) {
		return (T) lookupFunction.apply(globalId);
	}

	@Override
	public AzureTemplateContextBuilder setStorageConnectionString(String storageConnectionString) {
		this.storageConnectionString = storageConnectionString;
		return this;
	}

	@Override
	public AzureTemplateContextBuilder setContainerName(String containerName) {
		this.containerName = containerName;
		return this;
	}

	@Override
	public AzureTemplateContextBuilder setAzureModule(Module azureModule) {
		this.azureModule = azureModule;
		return this;
	}

	@Override
	public String getStorageConnectionString() {
		return storageConnectionString;
	}

	@Override
	public String getContainerName() {
		return containerName;
	}

	@Override
	public Module getAzureModule() {
		return azureModule;
	}

	@Override
	public AzureTemplateContextBuilder setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
		return this;
	}

	@Override
	public String getPathPrefix() {
		return pathPrefix;
	}

}
