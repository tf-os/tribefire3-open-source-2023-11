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
package com.braintribe.model.platform.setup.api;

import java.util.List;
import java.util.Map;

import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Abstract
public interface SetupPlatform extends FileSystemPlatformSetupConfig, SetupRequest {

	EntityType<SetupPlatform> T = EntityTypes.T(SetupPlatform.class);

	@Description("Delete the intermediate package containing prepared asset components after the setup has been finished.")
	@Initializer("true")
	boolean getDeletePackageBaseDir();
	void setDeletePackageBaseDir(boolean deletePackageBaseDir);

	@Description("Configures predefined components. If PredefinedComponent.DEFAULT_DB_CONNECTION is defined, all unconfigured components are derived from it.")
	Map<PredefinedComponent, GenericEntity> getPredefinedComponents();
	void setPredefinedComponents(Map<PredefinedComponent, GenericEntity> predefinedComponents);

	@Description("Custom components to be imported into the cortex database to make them available for further use.")
	List<GenericEntity> getCustomComponents();
	void setCustomComponents(List<GenericEntity> customComponents);

	@Description("Defines project specific parameters like the name of the project that is being setup.")
	ProjectDescriptor getProjectDescriptor();
	void setProjectDescriptor(ProjectDescriptor projectDescriptor);

	@Description("The SharedStorage implementation for system's (distributed) collaborative accesses. When empty, simple (non-distributed) CSA will be used instead.")
	DcsaSharedStorage getDcsaSharedStorage();
	void setDcsaSharedStorage(DcsaSharedStorage dcsaSharedStorage);

	@Override
	EvalContext<? extends SetupInfo> eval(Evaluator<ServiceRequest> evaluator);

}