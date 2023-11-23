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
package com.braintribe.model.processing.accessory.test.cortex;

import static com.braintribe.model.processing.accessory.test.cortex.MaTestConstants.CORTEX_MODEL_NAME;
import static com.braintribe.model.processing.accessory.test.cortex.MaTestConstants.CORTEX_SERVICE_MODEL_NAME;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.braintribe.model.access.collaboration.persistence.ModelsPersistenceInitializer;
import com.braintribe.model.accessapi.QueryRequest;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.meta.StreamWith;
import com.braintribe.model.generic.builder.meta.MetaModelBuilder;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * @author peter.gazdik
 */
public class MaInit_1A_CortexModel extends ModelsPersistenceInitializer {

	private final String[] cortexDataModelDeps = { //
			GmMetaModel.T.getModel().name(), // meta-model
			IncrementalAccess.T.getModel().name(), // access-deployment-model
			ServiceRequest.T.getModel().name(), // service-domain-model
			Resource.T.getModel().name(), // resource-model
			StreamWith.T.getModel().name(), // extension-deployment-model
			QueryRequest.T.getModel().name() // access-api-model
	};

	private final String[] cortexServiceModelDeps = { //
			ServiceRequest.T.getModel().name() // service-api-model
	};

	@Override
	protected Collection<GmMetaModel> getModels() {
		return asList(cortexModel(), cortexServiceModel());
	}

	private GmMetaModel cortexModel() {
		List<GmMetaModel> cortexDeps = toGmModels(Stream.of(cortexDataModelDeps));

		GmMetaModel cortexModel = MetaModelBuilder.metaModel(CORTEX_MODEL_NAME);
		cortexModel.setDependencies(cortexDeps);

		return cortexModel;
	}

	private GmMetaModel cortexServiceModel() {
		List<GmMetaModel> cortexDeps = toGmModels(Stream.of(cortexServiceModelDeps));

		GmMetaModel cortexModel = MetaModelBuilder.metaModel(CORTEX_SERVICE_MODEL_NAME);
		cortexModel.setDependencies(cortexDeps);

		return cortexModel;
	}

}
