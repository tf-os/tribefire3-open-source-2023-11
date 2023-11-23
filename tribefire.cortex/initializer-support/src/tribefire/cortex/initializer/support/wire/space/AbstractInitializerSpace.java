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
package tribefire.cortex.initializer.support.wire.space;

import java.util.function.Consumer;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.meta.configured.ConfigurationModelBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.SetGlobalIds;
import tribefire.cortex.initializer.support.impl.GlobalIdAssigner;
import tribefire.cortex.initializer.support.wire.contract.InitializerSupportContract;
import tribefire.module.model.resource.ModuleSource;
import tribefire.module.wire.contract.ModelApiContract;
import tribefire.module.wire.contract.ModuleReflectionContract;

/**
 * Base class for spaces in an initializer asset.
 * <p>
 * It offers various utility methods to create new or lookup existing entities, mostly by delegating to {@link InitializerSupportContract}
 * <p>
 * It also ensures globalIds are set automatically via {@link GlobalIdAssigner}, which is registered in the {@link MetaSpace}.
 * 
 */
@Managed
public class AbstractInitializerSpace implements WireSpace, SetGlobalIds {

	@Import
	protected InitializerSupportContract initializerSupport;

	@Import
	protected ModelApiContract modelApi;

	@Import
	protected ModuleReflectionContract modelReflection;

	// This import alone ensures GlobalIdAssigner is registered.
	@Import
	private MetaSpace meta;

	@Import
	private ResourcesSupportSpace moduleResourcesSupport;

	/** @see InitializerSupportContract#session() */
	public ManagedGmSession session() {
		return initializerSupport.session();
	}

	/** @see InitializerSupportContract#create(EntityType) */
	public <T extends GenericEntity> T create(EntityType<T> entityType) {
		return initializerSupport.create(entityType);
	}

	public <T extends GenericEntity> T create(EntityType<T> entityType, Consumer<? super T> initializer) {
		T entity = create(entityType);
		initializer.accept(entity);
		return entity;
	}

	public Resource createModuleResource(String path) {
		ModuleSource ms = create(ModuleSource.T);
		ms.setModuleName(currentModuleName());
		ms.setPath(path);

		Resource rWithMd = resolveResourceMetaData(path);

		Resource r = copySimpleProps(rWithMd);
		r.setResourceSource(ms);
		r.setSpecification(copySimpleProps(rWithMd.getSpecification()));

		return r;
	}

	private <E extends GenericEntity> E copySimpleProps(E source) {
		if (source == null)
			return null;

		EntityType<E> type = source.entityType();
		E e = create(type);

		for (Property p : type.getProperties())
			if (!p.isIdentifying() && p.getType().isSimple())
				p.set(e, p.get(source));

		return e;
	}

	private Resource resolveResourceMetaData(String path) {
		return moduleResourcesSupport.resourceMetaDataResolver().resolve(path);
	}

	/** @see InitializerSupportContract#lookup(String) */
	public <T extends GenericEntity> T lookup(String globalId) {
		return initializerSupport.lookup(globalId);
	}

	/** @see InitializerSupportContract#lookupExternalId(String) */
	public <T extends GenericEntity> T lookupExternalId(String externalId) {
		return initializerSupport.lookupExternalId(externalId);
	}

	/** @see InitializerSupportContract#currentModule() */
	public Module currentModule() {
		return initializerSupport.currentModule();
	}

	public String currentModuleName() {
		return modelReflection.moduleName();
	}

	/** @see InitializerSupportContract#importEntities(Object) */
	public <T> T importEntities(T entities) {
		return initializerSupport.importEntities(entities);
	}

	/**
	 * Returns a {@link ConfigurationModelBuilder} for a new configuration model which extends a model given by {@link ArtifactReflection} instance.
	 * <p>
	 * New model's <tt>groupId</tt> and <tt>version</tt> are taken directly from given artifact, and the new name is
	 * "configured-${artifactReflection.artifactId()}". <br>
	 * IMPORTANT: This means the method must not be called more than once for a given model as that would lead to the same configured model being
	 * created again.
	 */
	public ConfigurationModelBuilder buildConfiguredModelFor(ArtifactReflection ar) {
		return modelApi.newConfigurationModelFactory(session()) //
				.create(ar.groupId(), "configured-" + ar.artifactId(), ar.version()) //
				.addDependency(ar);
	}

}
