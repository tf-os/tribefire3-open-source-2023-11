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
package com.braintribe.model.processing.meta.configuration;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.configured.ConfigurationModelBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * A {@link ConfigurationModelBuilder} based on {@link ManagedGmSession} queries. The session object must be provided. The generated
 * {@link GmMetaModel} is created on the session.
 * 
 * @author Dirk Scheffler
 *
 */
public class ConfigurationModelBuilderManagedImpl implements ConfigurationModelBuilder {

	private final ManagedGmSession session;
	private final GmMetaModel model;

	/**
	 * @param session
	 *            The {@link ManagedGmSession} for queries and the new {@link GmMetaModel}.
	 * @param modelName
	 *            The name of the new model, its globalId will be `model:modelName`.
	 */
	public ConfigurationModelBuilderManagedImpl(ManagedGmSession session, String modelName, String version) {
		this.session = session;
		this.model = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
		this.model.setName(modelName);
		this.model.setVersion(version);
	}

	/**
	 * @param session
	 *            The {@link ManagedGmSession} for queries.
	 * @param model
	 *            An existing {@link GmMetaModel}, no new model will be created.
	 */
	public ConfigurationModelBuilderManagedImpl(ManagedGmSession session, GmMetaModel model) {
		this.session = session;
		this.model = model;
	}

	@Override
	public ConfigurationModelBuilder addDependency(ArtifactReflection standardArtifactReflection) {

		if (!standardArtifactReflection.archetypes().contains("model"))
			throw new IllegalArgumentException("Artifact " + standardArtifactReflection + " is not a model");

		return addDependencyByName(standardArtifactReflection.name());
	}

	@Override
	public ConfigurationModelBuilder addDependencyByName(String modelName) {
		return addDependencyByGlobalId(Model.modelGlobalId(modelName));
	}

	@Override
	public ConfigurationModelBuilder addDependency(Model model) {
		return addDependencyByGlobalId(model.globalId());
	}

	@Override
	public ConfigurationModelBuilder addDependencyByGlobalId(String globalId) {
		return addDependency((GmMetaModel) session.getEntityByGlobalId(globalId));
	}

	@Override
	public ConfigurationModelBuilder addDependency(GmMetaModel model) {
		this.model.getDependencies().add(model);
		return this;
	}

	@Override
	public GmMetaModel get() {
		return this.model;
	}

}
