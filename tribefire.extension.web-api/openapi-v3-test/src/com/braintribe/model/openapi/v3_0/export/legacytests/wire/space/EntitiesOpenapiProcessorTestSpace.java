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
package com.braintribe.model.openapi.v3_0.export.legacytests.wire.space;

import com.braintribe.model.openapi.v3_0.api.OpenapiEntitiesRequest;
import com.braintribe.model.openapi.v3_0.export.EntityOpenapiProcessor;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.contract.EntitiesOpenapiProcessorTestContract;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class EntitiesOpenapiProcessorTestSpace extends AbstractOpenapiProcessorTestSpace implements EntitiesOpenapiProcessorTestContract {
	@Override
	protected void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.register(OpenapiEntitiesRequest.T, entityOpenapiProcessor());
	}

	@Managed
	private EntityOpenapiProcessor entityOpenapiProcessor() {
		EntityOpenapiProcessor bean = new EntityOpenapiProcessor();

		bean.setCortexSessionFactory(cortexSessionSupplier());
		bean.setModelAccessoryFactory(modelAccessoryFactory());

		return bean;
	}
}
