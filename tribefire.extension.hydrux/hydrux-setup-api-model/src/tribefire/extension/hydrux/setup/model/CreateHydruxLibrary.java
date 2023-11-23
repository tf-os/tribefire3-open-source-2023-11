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
package tribefire.extension.hydrux.setup.model;

import com.braintribe.devrock.templates.model.artifact.CreateTsLibrary;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.extension.hydrux._HydruxSetupApiModel_;

@Description("Creates a TypeScript-based js-library artifact.")
public interface CreateHydruxLibrary extends CreateTsLibrary {

	EntityType<CreateHydruxLibrary> T = EntityTypes.T(CreateHydruxLibrary.class);

	@Override
	default String template() {
		return "tribefire.extension.hydrux:hydrux-library-template#" + TmpHelper.removeRevision(_HydruxSetupApiModel_.reflection.version());
	}

}

/* Just a temporary solution to get the major.minor as ArtifactReflection doesn't provide that. Remove once ArtifactReflection is improved. */
class TmpHelper {

	public static String removeRevision(String version) {
		int f = version.indexOf('.');
		int l = version.lastIndexOf('.');
		if (f < 0 || l < 0 || f == l)
			throw new IllegalArgumentException("Unexpected version format. Version: " + version);
		return version.substring(0, l);
	}

}
