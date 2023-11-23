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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

public interface SetupRepositoryConfiguration extends SetupRequest {

	EntityType<SetupRepositoryConfiguration> T = EntityTypes.T(SetupRepositoryConfiguration.class);

	@Mandatory
	@Alias("views")
	List<String> getRepositoryViews();
	void setRepositoryViews(List<String> repositoryViews);

	@Initializer("'repository-configuration'")
	String getInstallationPath();
	void setInstallationPath(String installationPath);

	@Description("If enabled, wraps artifact filters with the standard development view artifact filter."
			+ " This restricts only the specified groups, but e.g. makes it possible to add new extensions or third party libraries.")
	boolean getEnableDevelopmentMode();
	void setEnableDevelopmentMode(boolean enableDevelopmentMode);

	@Override
	EvalContext<? extends Neutral> eval(Evaluator<ServiceRequest> evaluator);

}
