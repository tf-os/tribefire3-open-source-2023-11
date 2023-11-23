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
import com.google.common.base.Function;

/**
 * {@link Function} implementation for indexing an {@link OriginAwareAccessRegistrationInfo} based on its
 * <code>accessId</code>.
 * 
 * 
 */
public class RegistrationWrapperIndexingFunction implements Function<OriginAwareAccessRegistrationInfo, String> {

	/**
	 * Returns {@link OriginAwareAccessRegistrationInfo#getAccessId()} of the input.
	 */
	@Override
	public String apply(@NonNull OriginAwareAccessRegistrationInfo input) {
		return input.getAccessId();
	}
}
