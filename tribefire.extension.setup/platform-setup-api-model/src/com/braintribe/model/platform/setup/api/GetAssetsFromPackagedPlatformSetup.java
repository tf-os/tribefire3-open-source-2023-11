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
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Gets assets by {@link #getNature() nature} from a given platform setup package, which has to be built separately
 * before (see {@link PackagePlatformSetup}).
 * 
 * @author michael.lafite
 */
@Description("Searches for assets of the specified nature in the given packaged platform setup and lists the fully qualified names (inluding versions).")
public interface GetAssetsFromPackagedPlatformSetup extends SetupRequest {
	EntityType<GetAssetsFromPackagedPlatformSetup> T = EntityTypes.T(GetAssetsFromPackagedPlatformSetup.class);

	@Override
	EvalContext<List<String>> eval(Evaluator<ServiceRequest> evaluator);

	@Description("Points to the file which describes the packaged platform setup.")
	@Initializer("'package/packaged-platform-setup.json'")
	String getPackagedPlatformSetupFilePath();
	void setPackagedPlatformSetupFilePath(String packagedPlatformSetupFilePath);

	@Description("Specifies the nature of the assets to get. If not set, all assets will be returned.")
	String getNature();
	void setNature(String nature);

	@Description("Whether or not to include revision in the qualified names that are returned.")
	boolean getIncludeRevision();
	void setIncludeRevision(boolean includeRevision);
}
