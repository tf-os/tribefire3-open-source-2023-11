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

import com.braintribe.common.lcd.annotations.NonNull;
import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;
import com.braintribe.model.processing.access.service.impl.standard.OriginAwareAccessRegistrationInfo.Origin;
import com.google.common.base.Function;

/**
 * {@link Function} implementation for wrapping an {@link AccessRegistrationInfo} in an
 * {@link OriginAwareAccessRegistrationInfo} . The origin of the wrapped elements has to be configured during
 * initialization.
 * 
 * 
 */
public class AccessRegistrationWrappingFunction implements
		Function<AccessRegistrationInfo, OriginAwareAccessRegistrationInfo> {

	private Origin originOfAccesses;

	public AccessRegistrationWrappingFunction(Origin originOfAccesses) {
		this.originOfAccesses = originOfAccesses;
	}

	/**
	 * Wraps an <code>accessRegistrationInfo</code> in a new {@link OriginAwareAccessRegistrationInfo}. Must not be
	 * <code>null</code>.
	 */
	@Override
	@NonNull
	public OriginAwareAccessRegistrationInfo apply(@NonNull AccessRegistrationInfo accessRegistrationInfo) {
		return new OriginAwareAccessRegistrationInfo(accessRegistrationInfo, originOfAccesses);
	}
}
