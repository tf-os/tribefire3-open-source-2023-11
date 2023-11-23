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
package com.braintribe.model.processing.access.service.impl.standard;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;

/**
 * Wrapper class for an {@link AccessRegistrationInfo} that is aware of the {@link Origin} of the registration.
 * 
 * @see AccessServiceImpl
 * 
 * 
 */
class OriginAwareAccessRegistrationInfo {

	/**
	 * Enumeration containing the possible origins of accesses in the {@link AccessServiceImpl}.
	 * 
	 * 
	 */
	enum Origin {
		/**
		 * Indicates that an access has been registered by configuration.
		 */
		CONFIGURATION,
		/**
		 * Indicates that an access has been registered during runtime.
		 */
		REGISTRATION;
	}

	private AccessRegistrationInfo accessRegistrationInfo;
	private Origin origin;

	protected OriginAwareAccessRegistrationInfo(AccessRegistrationInfo accessRegistrationInfo, Origin origin) {
		this.accessRegistrationInfo = accessRegistrationInfo;
		this.origin = origin;
	}
	
	public Origin getOrigin() {
		return origin;
	}

	public IncrementalAccess getAccess() {
		return accessRegistrationInfo.getAccess();
	}

	public String getAccessId() {
		return accessRegistrationInfo.getAccessId();
	}

	public String getAccessDenotationType() {
		return accessRegistrationInfo.getAccessDenotationType();
	}

	public String getMetaModelAccessId() {
		return accessRegistrationInfo.getModelAccessId();
	}

	public String getDataMetaModelName() {
		return accessRegistrationInfo.getModelName();
	}

	public String getWorkbenchMetaModelName() {
		return accessRegistrationInfo.getWorkbenchModelName();
	}
	
	public String getWorkbenchAccessId() {
		return accessRegistrationInfo.getWorkbenchAccessId();
	}
	
	public String getResourceAccessFactoryId() {
		return accessRegistrationInfo.getResourceAccessFactoryId();
	}

	public String getServiceModelName() {
		return accessRegistrationInfo.getServiceModelName();
	}
}
