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
package tribefire.cortex.openapi_v3.wire.space;

import com.braintribe.model.openapi.servlets.OpenapiUiServlet;
import com.braintribe.model.openapi.v3_0.export.ApiV1OpenapiProcessor;
import com.braintribe.model.openapi.v3_0.export.EntityOpenapiProcessor;
import com.braintribe.model.openapi.v3_0.export.PropertyOpenapiProcessor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.module.wire.contract.SystemUserRelatedContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 * This space class hosts configuration of deployables based on their denotation types.
 */
@Managed
public class OpenapiV3DeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	@Import
	private RequestUserRelatedContract requestUserRelated;
	@Import
	private SystemUserRelatedContract systemUserRelated;

	@Import
	private WebPlatformResourcesContract resources;

	@Managed
	public OpenapiUiServlet swaggerUi() {
		OpenapiUiServlet bean = new OpenapiUiServlet();
		bean.setTemplateLocation("com/braintribe/model/openapi/servlets/openapi_ui.vm");
		bean.setCortexSessionFactory(systemUserRelated.cortexSessionSupplier());
		bean.setModelAccessoryFactory(requestUserRelated.modelAccessoryFactory());

		return bean;
	}

	@Managed
	public ApiV1OpenapiProcessor openapiServicesProcessor() {
		ApiV1OpenapiProcessor bean = new ApiV1OpenapiProcessor();

		bean.setCortexSessionFactory(systemUserRelated.cortexSessionSupplier());
		bean.setModelAccessoryFactory(requestUserRelated.modelAccessoryFactory());

		return bean;
	}

	@Managed
	public EntityOpenapiProcessor openapiEntitiesProcessor() {
		EntityOpenapiProcessor bean = new EntityOpenapiProcessor();

		bean.setCortexSessionFactory(systemUserRelated.cortexSessionSupplier());
		bean.setModelAccessoryFactory(requestUserRelated.modelAccessoryFactory());

		return bean;
	}

	@Managed
	public PropertyOpenapiProcessor openapiPropertiesProcessor() {
		PropertyOpenapiProcessor bean = new PropertyOpenapiProcessor();

		bean.setCortexSessionFactory(systemUserRelated.cortexSessionSupplier());
		bean.setModelAccessoryFactory(requestUserRelated.modelAccessoryFactory());

		return bean;
	}

}
