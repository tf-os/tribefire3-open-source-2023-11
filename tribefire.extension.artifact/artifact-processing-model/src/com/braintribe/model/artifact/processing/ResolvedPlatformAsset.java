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
package com.braintribe.model.artifact.processing;

import java.util.List;

import com.braintribe.model.artifact.info.HasRepositoryOrigins;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author pit
 *
 */
public interface ResolvedPlatformAsset extends ArtifactIdentification, HasParts, HasRepositoryOrigins {
	
	EntityType<ResolvedPlatformAsset> T = EntityTypes.T(ResolvedPlatformAsset.class);

	/**
	 * @return - the {@link PlatformAssetNature}
	 */
	PlatformAssetNature getNature();
	void setNature( PlatformAssetNature nature);
	
	/**
	 * @return - the DIRECT dependencies as {@link ResolvedPlatformAsset}
	 */
	List<ResolvedPlatformAsset> getDependencies();
	/**
	 * @param dependencies - the DIRECT dependencies as {@link ResolvedPlatformAsset}
	 */
	void setDependencies( List<ResolvedPlatformAsset> dependencies);
	
}
