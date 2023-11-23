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
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;
import com.braintribe.model.processing.access.service.impl.standard.OriginAwareAccessRegistrationInfo.Origin;
import static org.mockito.Mockito.*;
/**
 * Tests for {@link AccessRegistrationWrappingFunction}
 * 
 * 
 */
public class AccessRegistrationWrappingFunctionTest {

	private AccessRegistrationWrappingFunction function;
	private static final Origin ORIGIN = Origin.REGISTRATION;

	@Before
	public void setUp() throws Exception {
		function = new AccessRegistrationWrappingFunction(ORIGIN);
	}

	@Test
	public void testWrapping() throws Exception {
		IncrementalAccess access  = mock(IncrementalAccess.class);
		
		AccessRegistrationInfo regInfo = new AccessRegistrationInfo();
		regInfo.setAccess(access);

		OriginAwareAccessRegistrationInfo result = function.apply(regInfo);
		IncrementalAccess actualAccess = result.getAccess();
		Assertions.assertThat(actualAccess).isEqualTo(access);
		Origin actualOrigin = result.getOrigin();
		Assertions.assertThat(actualOrigin).isEqualTo(ORIGIN);
	}
}
