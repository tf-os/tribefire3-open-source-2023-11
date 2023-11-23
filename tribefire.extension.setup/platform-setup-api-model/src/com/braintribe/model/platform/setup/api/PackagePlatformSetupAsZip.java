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

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@Description("Resolves the transitive asset dependencies and applies packaging expert logic that is associated for each asset nature."
		+ " Each expert will download nature specific asset parts from configured repositories and"
		+ " use the data to create or extend files in the setup package."
		+ " The directory structure will be archived in a zip and delivered as a streamable resource.")
public interface PackagePlatformSetupAsZip extends PackagePlatformSetup {
	EntityType<PackagePlatformSetupAsZip> T = EntityTypes.T(PackagePlatformSetupAsZip.class);
}
