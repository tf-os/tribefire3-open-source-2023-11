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
package tribefire.extension.templates.api;

import java.util.function.Function;

import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.scope.InstanceQualification;

public abstract class TemplateContextImpl<T extends TemplateContext> implements TemplateContext, TemplateContextBuilder<T> {

	private String idPrefix;
	private Function<String, ? extends GenericEntity> lookupFunction;
	private Function<String, ? extends HasExternalId> lookupExternalIdFunction;
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	private Module module;
	private String name;

	@Override
	public <S extends GenericEntity> S create(EntityType<S> entityType, InstanceConfiguration instanceConfiguration) {

		S entity = (S) entityFactory.apply(entityType);

		if (idPrefix == null) {
			throw new IllegalStateException("You have to specify a idPrefix.");
		}

		InstanceQualification qualification = instanceConfiguration.qualification();

		StringBuilder sb = new StringBuilder();
		if (idPrefix == null || !idPrefix.startsWith("wire://")) {
			sb.append("wire://");
		}
		if (idPrefix != null) {
			sb.append(idPrefix);
			sb.append("/");
		}
		sb.append(qualification.space().getClass().getSimpleName());
		sb.append("/");
		sb.append(qualification.name());

		String globalId = sb.toString();

		entity.setGlobalId(globalId);

		if (entity instanceof HasExternalId) {
			HasExternalId eid = (HasExternalId) entity;

			String extIdPrefix = idPrefix;
			if (extIdPrefix != null && extIdPrefix.startsWith("wire://")) {
				extIdPrefix = extIdPrefix.substring("wire://".length());
			}

			String externalId = StringTools.camelCaseToDashSeparated(entityType.getShortName()) + "."
					+ StringTools.camelCaseToDashSeparated(extIdPrefix) + "."
					+ StringTools.camelCaseToDashSeparated(qualification.space().getClass().getSimpleName()) + "."
					+ StringTools.camelCaseToDashSeparated(qualification.name());
			externalId = externalId.replace(':', '_');

			eid.setExternalId(externalId);
		}

		return entity;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TemplateContextImpl:\n");
		sb.append("idPrefix: " + idPrefix + "\n");
		sb.append("module: " + module + "\n");
		sb.append("name: " + name + "\n");
		return sb.toString();
	}

	@Override
	public TemplateContextBuilder<T> setIdPrefix(String idPrefix) {
		this.idPrefix = idPrefix;
		return this;
	}

	@Override
	public TemplateContextBuilder<T> setEntityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}

	@Override
	public TemplateContextBuilder<T> setModule(Module module) {
		this.module = module;
		return this;
	}

	@Override
	public TemplateContextBuilder<T> setLookupFunction(Function<String, ? extends GenericEntity> lookupFunction) {
		this.lookupFunction = lookupFunction;
		return this;
	}

	@Override
	public TemplateContextBuilder<T> setLookupExternalIdFunction(Function<String, ? extends HasExternalId> lookupExternalIdFunction) {
		this.lookupExternalIdFunction = lookupExternalIdFunction;
		return this;
	}

	@Override
	public TemplateContextBuilder<T> setName(String name) {
		this.name = name;
		return this;
	}

	@Override
	public T build() {
		return (T) this;
	}

	@Override
	public String getIdPrefix() {
		return idPrefix;
	}

	public Function<EntityType<?>, GenericEntity> getEntityFactory() {
		return entityFactory;
	}

	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public <S extends GenericEntity> S lookup(String globalId) {
		return (S) lookupFunction.apply(globalId);
	}

	@Override
	public <S extends HasExternalId> S lookupExternalId(String externalId) {
		return (S) lookupExternalIdFunction.apply(externalId);
	}

}
