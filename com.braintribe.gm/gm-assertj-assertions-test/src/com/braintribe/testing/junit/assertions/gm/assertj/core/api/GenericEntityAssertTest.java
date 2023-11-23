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
package com.braintribe.testing.junit.assertions.gm.assertj.core.api;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThatExecuting;
import static com.braintribe.testing.junit.assertions.gm.assertj.core.api.GmAssertions.assertThat;

import org.junit.Test;

import com.braintribe.model.resource.Resource;

/**
 * Provides {@link GenericEntityAssert} related tests.
 *
 * @author michael.lafite
 */
public class GenericEntityAssertTest {

	@Test
	public void test() {
		Resource resource = Resource.T.create();
		resource.setCreator("joe");

		assertThat(resource).hasPresentProperty("fileSize");
		assertThatExecuting(() -> assertThat(resource).hasAbsentProperty("fileSize")).fails().with(AssertionError.class);

		assertThat(resource).hasNoSessionAttached();
		assertThatExecuting(() -> assertThat(resource).hasSessionAttached()).fails().with(AssertionError.class);
		
		Resource otherResource = Resource.T.create();
		resource.setCreator("john");
		
		assertThat(resource).hasIdMatchingIdOf(otherResource);
		resource.setId(123);
		otherResource.setId(resource.getId());
		assertThat(resource).hasIdMatchingIdOf(otherResource);
		otherResource.setId(456);
		assertThatExecuting(() -> assertThat(resource).hasIdMatchingIdOf(otherResource)).fails().with(AssertionError.class);
	}

}
