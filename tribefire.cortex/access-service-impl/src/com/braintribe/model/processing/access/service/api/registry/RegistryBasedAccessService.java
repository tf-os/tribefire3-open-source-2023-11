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
package com.braintribe.model.processing.access.service.api.registry;

import com.braintribe.model.access.AccessService;
import com.braintribe.model.accessdeployment.IncrementalAccess;

/**
 * An extended interface for {@link AccessService}es based on a registry.
 * <p/>
 * Implementations of this interface are capable of registering and unregistering accesses during runtime.
 * 
 * 
 */
public interface RegistryBasedAccessService extends AccessService {

	void registerAccess(AccessRegistrationInfo registrationInfo);

	void registerAccess(IncrementalAccess deployable, com.braintribe.model.access.IncrementalAccess access);

	void unregisterAccess(String accessId);

	void unregisterAccess(IncrementalAccess deployable);

}
