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
package tribefire.cortex.asset.resolving.ng.api;

import java.util.List;
import java.util.Set;

import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.processing.core.expert.api.DenotationMap;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

public interface AssetResolutionContextBuilder {
	AssetResolutionContextBuilder session(ManagedGmSession session);
	AssetResolutionContextBuilder natureParts(DenotationMap<PlatformAssetNature, List<String>> natureParts);
	AssetResolutionContextBuilder selectorFiltering(boolean selectorFiltering);
	AssetResolutionContextBuilder includeDocumentation(boolean includeDocument);
	AssetResolutionContextBuilder verboseOutput(boolean verboseOutput);
	AssetResolutionContextBuilder lenient(boolean lenient);
	AssetResolutionContextBuilder runtime(boolean runtime);
	AssetResolutionContextBuilder designtime(boolean designtime);
	AssetResolutionContextBuilder stage(String stage);
	AssetResolutionContextBuilder tags(Set<String> tags);

	AssetResolutionContext done();
}
