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
package com.braintribe.model.processing.meta.configured;

import com.braintribe.common.artifact.ArtifactReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;

/**
 * The interface to generate new (configuration) {@link GmMetaModel model} given a name, version and dependency information.
 * 
 * Some of the functionality depends on the implementation, e.g. the globalId is only useful with aManagedGmSession.
 * 
 * Calls to the interface can be chained, and the final {@link #get()} is optional to return the assembled model.
 * 
 * @author Dirk Scheffler
 */
public interface ConfigurationModelBuilder {

	/**
	 * @param modelName
	 *            A fully qualified model name, i.e. "${groupId}:${artifactId}"``
	 */
	ConfigurationModelBuilder addDependencyByName(String modelName);

	/**
	 * @param modelArtifactReflection
	 *            {@link ArtifactReflection} for a model artifact.
	 */
	ConfigurationModelBuilder addDependency(ArtifactReflection modelArtifactReflection);
	
	ConfigurationModelBuilder addDependency(GmMetaModel gmModel);
	
	ConfigurationModelBuilder addDependency(Model model);

	/** Only available if the implementation is backed by a ManagedGmSession */
	ConfigurationModelBuilder addDependencyByGlobalId(String globalId); // only on session

	/** @return The assembled {@link GmMetaModel} with all the desired dependencies. */
	GmMetaModel get();
}
