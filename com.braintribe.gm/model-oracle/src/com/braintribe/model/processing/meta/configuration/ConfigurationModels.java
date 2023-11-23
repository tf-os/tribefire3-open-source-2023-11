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

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.configured.ConfigurationModelBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * A few helper methods to create new {@link ConfigurationModelBuilder} experts.
 * 
 */
public interface ConfigurationModels {

	/**
	 * The new {@link GmMetaModel} will have the name `modelName` and globalId `model:modelName`.
	 */
	static ConfigurationModelBuilder create(ManagedGmSession session, String modelName) {
		return createVersioned(session, modelName, null);
	}

	/**
	 * The new {@link GmMetaModel} will have the name `groupId:artifactId` and globalId `model:groupId:artifactId`.
	 */
	static ConfigurationModelBuilder create(ManagedGmSession session, String groupId, String artifactId) {
		return createVersioned(session, groupId, artifactId, null);
	}

	static ConfigurationModelBuilder createVersioned(ManagedGmSession session, String groupId, String artifactId, String version) {
		return createVersioned(session, groupId + ":" + artifactId, version);
	}

	static ConfigurationModelBuilder createVersioned(ManagedGmSession session, String modelName, String version) {
		return new ConfigurationModelBuilderManagedImpl(session, modelName, version);
	}

	/**
	 * No new {@link GmMetaModel} will be generated, but provided `model` can be further extended.
	 */
	static ConfigurationModelBuilder extend(ManagedGmSession session, GmMetaModel model) {
		return new ConfigurationModelBuilderManagedImpl(session, model);
	}

	/**
	 * A new transient {@link GmMetaModel} will be generated with name `modelName`. This can be used without a session object.
	 */
	static ConfigurationModelBuilder create(String modelName) {
		return new ConfigurationModelBuilderGmfImpl(modelName);
	}

	/**
	 * A new transient {@link GmMetaModel} is generated with name `groupId:artifactId`. This can be done without a session object.
	 */
	static ConfigurationModelBuilder create(String groupId, String artifactId) {
		return create(groupId + ":" + artifactId);
	}

	/**
	 * The existing {@link GmMetaModel} model is further extended with transient dependencies.
	 */
	static ConfigurationModelBuilder extend(GmMetaModel model) {
		return new ConfigurationModelBuilderGmfImpl(model);
	}

}
