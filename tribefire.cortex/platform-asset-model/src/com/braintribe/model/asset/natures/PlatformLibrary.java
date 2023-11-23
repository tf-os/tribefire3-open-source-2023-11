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
package com.braintribe.model.asset.natures;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * A library aggregating asset, which marks it's dependencies as direct dependencies of the {@link TribefirePlatform}.
 * <p>
 * Explanation: A platform setup consists of the main (platform) classpath, and one classpath per module. In case of
 * modules, only it's models and gm-api dependencies are guaranteed to be projected onto the main classpath, while other
 * libraries are usually put to the module's classpath(s). If you, however, want to force a certain library to the main classpath, you can do so
 * 
 * <p>
 * NOTE that this asset is only relevant if there is a {@link TribefirePlatform} present in your setup.
 */
public interface PlatformLibrary extends PlatformAssetNature, SupportsNonAssetDeps {

	final EntityType<PlatformLibrary> T = EntityTypes.T(PlatformLibrary.class);

}
