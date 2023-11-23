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

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

/**
 * 
 * @deprecated Licensing in an Open Source framework has a different meaning. It cannot be the platform but rather some features to be licensed. Sinc
 *             jinni also runs for older TF installations, InstallLicense is anyway needed for the time being. It cannot be removed yet. 
 *
 */
@Deprecated
@Description("Installs a tribefire license given by a license file into the local maven repository as asset 'tribefire.cortex.assets:tribefire-license'")
@PositionalArguments({ "file", "version" })
public interface InstallLicense extends SetupRequest {
	EntityType<InstallLicense> T = EntityTypes.T(InstallLicense.class);

	@Alias("f")
	@Mandatory
	@Description("The license file issued for a tribefire platform")
	Resource getFile();
	void setFile(Resource resource);

	@Alias("v")
	@Mandatory
	@Description("The tribefire version for which the license was issued.")
	String getVersion();
	void setVersion(String version);

	@Override
	EvalContext<Neutral> eval(Evaluator<ServiceRequest> evaluator);
}
