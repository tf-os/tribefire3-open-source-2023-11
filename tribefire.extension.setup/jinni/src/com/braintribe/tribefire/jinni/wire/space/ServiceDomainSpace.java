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
package com.braintribe.tribefire.jinni.wire.space;

import static com.braintribe.tribefire.jinni.core.JinniTools.getClassPathModel;

import java.util.ArrayList;

import com.braintribe.devrock.templates._ArtifactTemplatesConfigModel_;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.service.api.PlatformRequest;
import com.braintribe.tribefire.jinni.core.JinniModelAccessoryFactory;
import com.braintribe.tribefire.jinni.wire.contract.ServiceDomainContract;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.util.Lists;

import tribefire.extension.setup._DevEnvGeneratorConfigModel_;
import tribefire.extension.setup._JinniConfigModel_;

@Managed
public class ServiceDomainSpace implements ServiceDomainContract {

	@Managed
	private ModelAccessoryFactory modelAccessoryFactory() {
		JinniModelAccessoryFactory bean = new JinniModelAccessoryFactory();

		bean.setPlatformDomainModels(platformDomainModels());

		return bean;
	}

	@Managed
	private ArrayList<GmMetaModel> platformDomainModels() {
		return Lists.list( //
				getClassPathModel("tribefire.extension.setup:jinni-api-model"), //
				getClassPathModel(_JinniConfigModel_.reflection), //
				getClassPathModel("tribefire.extension.setup:dev-env-generator-api-model"), //
				getClassPathModel("com.braintribe.devrock:dev-env-api-model"), //
				getClassPathModel(_DevEnvGeneratorConfigModel_.reflection), //
				getClassPathModel("tribefire.extension.setup:platform-setup-api-model"), //
				getClassPathModel("tribefire.extension.setup:platform-setup-cloud-api-model"), //
				getClassPathModel("com.braintribe.devrock.templates:artifact-template-service-model"), //
				getClassPathModel(_ArtifactTemplatesConfigModel_.reflection), //
				getClassPathModel("tribefire.cortex.assets.templates:platform-asset-template-service-model"), //
				getClassPathModel("tribefire.extension.schemed-xml:schemed-xml-xsd-api-model"), // TODO how did this ever work?!
				getClassPathModel("tribefire.extension.xmi:argo-exchange-api-model"), //
				getClassPathModel("tribefire.extension.js:js-setup-api-model"), //
				getClassPathModel("tribefire.extension.hydrux:hydrux-setup-api-model"), //
				getClassPathModel("tribefire.extension.hikari:hikari-deployment-model"), // Because HikariCpConnectionPool can be attached to requests
				getClassPathModel("tribefire.extension.artifact:artifact-management-api-model") //
		);
	}

	@Override
	public ModelAccessory modelAccessory() {
		return modelAccessoryFactory().getForServiceDomain(PlatformRequest.platformDomainId);
	}

}
