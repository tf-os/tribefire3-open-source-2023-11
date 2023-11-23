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

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;

/**
 * Tests for {@link RegistrationWrapperIndexingFunction}.
 * 
 * 
 */
public class RegistrationWrapperIndexingFunctionTest {

	@Test
	public void testApply() throws Exception {
		AccessRegistrationInfo accessRegistrationInfo = new AccessRegistrationInfo();
		String accessId = "id";
		accessRegistrationInfo.setAccessId(accessId);
		OriginAwareAccessRegistrationInfo regInfo = new OriginAwareAccessRegistrationInfo(accessRegistrationInfo, null);

		String actual = new RegistrationWrapperIndexingFunction().apply(regInfo);
		Assertions.assertThat(actual).isEqualTo(accessId);
	}

}
