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
package tribefire.extension.setup.dev_env_generator_api.model;

import com.braintribe.model.generic.annotation.meta.Alias;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.PositionalArguments;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.PlatformRequest;

/**
 * 
 *
 */
@Description("Generates a DevEnv by patching an existing template. Configure via jinni/conf/dev-env-generator-config.yaml")
@PositionalArguments("name")
public interface CreateDevEnv extends PlatformRequest {
	EntityType<CreateDevEnv> T = EntityTypes.T(CreateDevEnv.class);

	@Description("The name for the new dev-env. Cannot exists yet.")
	@Alias("n")
	@Mandatory
	String getName();
	void setName(String name);

}
