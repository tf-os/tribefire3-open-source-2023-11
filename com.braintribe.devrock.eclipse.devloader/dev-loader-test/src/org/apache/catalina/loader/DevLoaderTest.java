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
package org.apache.catalina.loader;

import static org.apache.catalina.loader.Helpers.list;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;

/**
 * Provides tests for {@link DevLoader}.
 *
 * @author michael.lafite
 */
public class DevLoaderTest extends AbstractTest {

	static {
		DevLoader.dummyPathReplacement = testDir(DevLoaderTest.class).getParentFile().getParentFile().getAbsolutePath();
	}

	@Test
	public void test_ac_ac() {
		// @formatter:off
		List<String> expectedClassPath = list(
				"/absolute/path/to/res/DevLoaderTest/localRepository/com/braintribe/model/test-model/2.3/test-model-2.3.jar",
				"/absolute/path/to/res/DevLoaderTest/projects/ac_ac/simple-data-model/2.0/bin",
				"/absolute/path/to/res/DevLoaderTest/projects/ac_ac/simple-module/2.0/classes");
		// @formatter:on
		test(existingTestDir("projects/ac_ac/simple-module/2.0/context"), expectedClassPath);
	}

	@Test
	public void test_ac_ac_noContainerInTomcatPluginSettings() {
		// @formatter:off
		List<String> expectedClassPath = list(
				"/absolute/path/to/res/DevLoaderTest/localRepository/com/braintribe/model/test-model/2.3/test-model-2.3.jar",
				"/absolute/path/to/res/DevLoaderTest/projects/ac_ac_noContainerInTomcatPluginSettings/simple-data-model/2.0/bin",
				"/absolute/path/to/res/DevLoaderTest/projects/ac_ac_noContainerInTomcatPluginSettings/simple-module/2.0/classes");
		// @formatter:on
		test(existingTestDir("projects/ac_ac_noContainerInTomcatPluginSettings/simple-module/2.0/context"), expectedClassPath);
	}

	@Test
	public void test_gradle_etp() {
		// @formatter:off
		List<String> expectedClassPath = list(
			"/absolute/path/to/res/DevLoaderTest/localRepository/com/braintribe/model/test-model/2.3/test-model-2.3.jar",
			"/absolute/path/to/res/DevLoaderTest/projects/gradle_etp/simple-data-model/bin",
			"/absolute/path/to/res/DevLoaderTest/projects/gradle_etp/simple-module/bin");
		// @formatter:on
		test(existingTestDir("projects/gradle_etp/simple-module/src/main/webapp"), expectedClassPath);
	}

	@Test
	public void test_gradle_gradle() {
		// @formatter:off
		List<String> expectedClassPath = list(
			"/absolute/path/to/res/DevLoaderTest/localRepository/com/braintribe/model/test-model/2.3/test-model-2.3.jar",
			"/absolute/path/to/res/DevLoaderTest/projects/gradle_gradle/simple-data-model/bin",
			"/absolute/path/to/res/DevLoaderTest/projects/gradle_gradle/simple-module/bin");
		// @formatter:on
		test(existingTestDir("projects/gradle_gradle/simple-module/src/main/webapp"), expectedClassPath);
	}

	@Test
	public void test_maven_etp() {
		// @formatter:off
		List<String> expectedClassPath = list(
			"/absolute/path/to/res/DevLoaderTest/localRepository/com/braintribe/model/test-model/2.3/test-model-2.3.jar",
			"/absolute/path/to/res/DevLoaderTest/projects/maven_etp/simple-data-model/target/classes",
			"/absolute/path/to/res/DevLoaderTest/projects/maven_etp/simple-module/target/classes");
		// @formatter:on
		test(existingTestDir("projects/maven_etp/simple-module/src/main/webapp"), expectedClassPath);
	}

	@Test
	public void test_maven_maven() {
		// @formatter:off
		List<String> expectedClassPath = list(
			"/absolute/path/to/res/DevLoaderTest/localRepository/com/braintribe/model/test-model/2.3/test-model-2.3.jar",
			"/absolute/path/to/res/DevLoaderTest/projects/maven_maven/simple-data-model/target/classes",
			"/absolute/path/to/res/DevLoaderTest/projects/maven_maven/simple-module/target/classes");
		// @formatter:on
		test(existingTestDir("projects/maven_maven/simple-module/src/main/webapp"), expectedClassPath);
	}

	private void test(File webappDir, List<String> expectedClassPath) {
		List<String> actualClassPath = DevLoader.readWebClassPathEntries(webappDir);
		expectedClassPath = DevLoader.replaceDummyPaths(expectedClassPath);
		assertThat(actualClassPath).isEqualTo(expectedClassPath);
	}
}
