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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Tribefire platform is a virtual layer on top of the underlying runtime environment, which provides and normalized
 * interface for binding data and functionality from tribefire modules, and maps the communication over
 * environment-specific interfaces to standard tribefire APIs used by the modules.
 * <p>
 * For example, a runtime environment can be a web server (or a command line interface or anything else), the
 * environment-specific interface could be REST, and the platform is responsible for mapping the REST calls to the
 * DDSA/ServiceProcessors, IncrementalAccesses and other components bound from modules.
 * 
 * @see TribefireWebPlatform
 * @see TribefireModule
 * @see PlatformLibrary
 */
@Abstract
public interface TribefirePlatform extends PlatformAssetNature {

	EntityType<TribefirePlatform> T = EntityTypes.T(TribefirePlatform.class);

}
