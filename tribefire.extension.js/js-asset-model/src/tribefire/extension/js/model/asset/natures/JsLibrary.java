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
package tribefire.extension.js.model.asset.natures;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.SupportsNonAssetDeps;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * This is a little tricky - not every js library artifact needs to be an asset, so let's explain what it is and when it should be an asset.
 * <p>
 * Js library artifact is an artifact which contains a <code>js.zip</code> part.
 * <p>
 * From this perspective, we can package any 3rd party JavaScript library as our js library artifact, as well as every model is also such an artifact.
 * <p>
 * When doing a setup, for every js library artifact, the content of it's <code>zip</code> is extracted into a folder called
 * <code>${groupId}.${artifactId}-${version}~</code> inside a target folder (e.g. <code>tribefire-services/context/js-libraries</code>).
 * <p>
 * Now, what is a JsLibrary asset? Since the setup process is driven by {@link PlatformAsset} assets, i.e. it resolves all the assets starting from a
 * given terminal artifact, and then processes each asset according to it's {@link PlatformAssetNature nature}, we need an asset that marks a js
 * library artifact. Thus a setup process knows that artifact and all it's js dependencies need to have their <code>js.zip</code> parts processed.
 */
public interface JsLibrary extends PlatformAssetNature, SupportsNonAssetDeps {

	EntityType<JsLibrary> T = EntityTypes.T(JsLibrary.class);

}
