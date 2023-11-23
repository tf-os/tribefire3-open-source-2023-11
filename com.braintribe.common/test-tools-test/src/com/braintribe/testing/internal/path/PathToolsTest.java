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
package com.braintribe.testing.internal.path;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;

/**
 * Provides tests for {@link PathTools}.
 *
 * @author michael.lafite
 */
public class PathToolsTest extends AbstractTest {

	@Test
	public void testCanonicalPath() {
		assertThat(PathTools.canonicalPath("x/y/./.././y/z").toString()).endsWith(Paths.get("x/y/z").toString());
	}
}
