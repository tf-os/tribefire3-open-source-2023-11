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
package tribefire.extension.appconfiguration.app_configuration_initializer.wire.contract;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.initializer.support.impl.lookup.GlobalId;
import tribefire.cortex.initializer.support.impl.lookup.InstanceLookup;
import tribefire.extension.appconfiguration.app_configuration_initializer.AppConfigurationConstants;
import tribefire.extension.js.model.deployment.UxModule;

@InstanceLookup(lookupOnly = true)
public interface ExistingInstancesContract extends WireSpace {

	@GlobalId(AppConfigurationConstants.APP_CONFIGURATION_DEPLOYMENT_MODEL_GLOBAL_ID)
	GmMetaModel appConfigurationDeploymentModel();

	@GlobalId(AppConfigurationConstants.APP_CONFIGURATION_API_MODEL_GLOBAL_ID)
	GmMetaModel appConfigurationApiModel();

	@GlobalId(AppConfigurationConstants.APP_CONFIGURATION_MODEL_GLOBAL_ID)
	GmMetaModel appConfigurationModel();

	@GlobalId(AppConfigurationConstants.WORKBENCH_ACCESS_GLOBAL_ID)
	IncrementalAccess workbenchAccess();

	@GlobalId(AppConfigurationConstants.APP_CONFIGURATION_UX_MODULE)
	UxModule uxModule();

	@GlobalId("selector:useCase/gme.gmeGlobalUseCase")
	UseCaseSelector gmeSelector();

	@GlobalId("selector:useCase/swagger")
	UseCaseSelector swaggerSelector();

	@GlobalId("useCase:openapi")
	UseCaseSelector openApiSelector();

}
