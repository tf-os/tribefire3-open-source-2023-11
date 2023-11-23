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

import com.braintribe.model.openapi.v3_0.api.OpenapiPropertiesRequest;
import com.braintribe.model.openapi.v3_0.export.PropertyOpenapiProcessor;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.contract.EntitiesOpenapiProcessorTestContract;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class PropertiesOpenapiProcessorTestSpace extends AbstractOpenapiProcessorTestSpace implements EntitiesOpenapiProcessorTestContract {
	@Override
	protected void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.register(OpenapiPropertiesRequest.T, propertyOpenapiProcessor());
	}

	@Managed
	private PropertyOpenapiProcessor propertyOpenapiProcessor() {
		PropertyOpenapiProcessor bean = new PropertyOpenapiProcessor();

		bean.setCortexSessionFactory(cortexSessionSupplier());
		bean.setModelAccessoryFactory(modelAccessoryFactory());

		return bean;
	}
}
