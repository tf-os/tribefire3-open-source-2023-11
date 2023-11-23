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
package tribefire.cortex.openapi_initializer.wire.contract;

import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;

@InstanceLookup(lookupOnly = true)
public interface ExistingInstancesContract extends WireSpace {

	String TYPE_PREFIX = "type:";
	String MODEL_PREFIX = "model:";
	String OPENAPI_PACKAGE_PREFIX = "com.braintribe.model.openapi.v3_0.";
	String OPENAPI_API_TYPE_PREFIX = TYPE_PREFIX + OPENAPI_PACKAGE_PREFIX + "api.";

	@GlobalId("ddra:config")
	DdraConfiguration ddraConfiguration();

	@GlobalId(MODEL_PREFIX + "tribefire.extension.web-api:web-api-endpoints-model")
	GmMetaModel ddraEndpointsModel();

	@GlobalId(MODEL_PREFIX + "com.braintribe.gm:service-api-model")
	GmMetaModel serviceApiModel();

	@GlobalId(OPENAPI_API_TYPE_PREFIX + "OpenapiServicesRequest")
	GmEntityType openapiServicesRequestType();

	@GlobalId(OPENAPI_API_TYPE_PREFIX + "OpenapiEntitiesRequest")
	GmEntityType openapiEntitiesRequestType();

	@GlobalId(OPENAPI_API_TYPE_PREFIX + "OpenapiPropertiesRequest")
	GmEntityType openapiPropertiesRequestType();

}
