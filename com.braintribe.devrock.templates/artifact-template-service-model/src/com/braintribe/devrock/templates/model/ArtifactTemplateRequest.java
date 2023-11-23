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
package com.braintribe.devrock.templates.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Abstract
@Description("The base request of all the artifact template requests.")
@PositionalArguments({ "installationPath", "directoryName" })
public interface ArtifactTemplateRequest extends ServiceRequest {
	
	EntityType<ArtifactTemplateRequest> T = EntityTypes.T(ArtifactTemplateRequest.class);
	
	@Description("The installation path of the artifact template projection.")
	@Alias("ip")
	@Initializer("'.'")
	String getInstallationPath();
	void setInstallationPath(String installationPath);
	
	@Description("The directory name of the artifact template projection. If specified, the directory name is appended to the installation path.")
	@Alias("dn")
	String getDirectoryName();
	void setDirectoryName(String directoryName);
	
	@Description(" Specifies whether the template projection should overwrite existing files in case they exist or report an error.")
	@Alias("o")
	boolean getOverwrite();
	void setOverwrite(boolean overwrite);

	/**
	 * Provides the fully-qualified name of the artifact template associated with the request.
	 */
	default String template() {
		return null;
	}

	@Override
	EvalContext<? extends ArtifactTemplateResponse> eval(Evaluator<ServiceRequest> evaluator);

}
