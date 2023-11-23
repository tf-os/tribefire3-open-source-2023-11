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
package com.braintribe.testing.test;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;

import java.io.File;

import com.braintribe.utils.FileTools;

/**
 * Super class for tests that test method {@link #testDir()}.
 *
 * @author michael.lafite
 */
public class AbstractPathBuilderTestDirTest extends AbstractTest {

	protected void checkTestDir(String expectedPartOfQualifiedNameTestDirIsBasedOn, boolean testDirIsSubFolderBased) {
		File testDir = testDir();
		String testDirAsString = FileTools.normalizePath(testDir.toString());

		String expectedTestDirAsString = expectedPartOfQualifiedNameTestDirIsBasedOn;
		if (testDirIsSubFolderBased) {
			expectedTestDirAsString = expectedPartOfQualifiedNameTestDirIsBasedOn.replace(".", "/");
		}

		assertThat(testDirAsString).endsWith(expectedTestDirAsString);
	}

}
