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

import static org.mockito.Mockito.mock;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.processing.access.service.api.registry.AccessRegistrationInfo;
import com.braintribe.model.processing.access.service.impl.standard.OriginAwareAccessRegistrationInfo.Origin;

/**
 * Tests for {@link AccessUnwrappingFunction}
 * 
 * 
 */
public class AccessUnwrappingFunctionTest {

	private AccessUnwrappingFunction function;

	@Before
	public void setUp() throws Exception {
		function = new AccessUnwrappingFunction();
	}

	@Test
	public void testUnwrapping() throws Exception {
		IncrementalAccess access = mock(IncrementalAccess.class);
		AccessRegistrationInfo registrationInfo = new AccessRegistrationInfo();
		registrationInfo.setAccess(access);
		OriginAwareAccessRegistrationInfo wrapper = new OriginAwareAccessRegistrationInfo(registrationInfo, Origin.REGISTRATION);

		IncrementalAccess actual = function.apply(wrapper);

		Assertions.assertThat(actual).isEqualTo(access);
	}

	@Test
	public void testUnwrappingFromNull() throws Exception {
		IncrementalAccess actual = function.apply(null);

		Assertions.assertThat(actual).isNull();
	}
}
