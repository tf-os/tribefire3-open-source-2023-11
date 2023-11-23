// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2020 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl.views.lockversions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.lcd.CommonTools;

/**
 * Provides tests for {@link GetLockedVersionsProcessor}.
 */
public class GetLockedVersionsProcessorTest extends AbstractTest {

	@Test
	public void testGroupLockedVersionsByGroupId() {
		// @formatter:off
		List<String> inputLocks = CollectionTools.getList(
				"aaa:aaa-view#1.0.1",
				"ant:ant#1.6.5",
				"antlr:antlr#2.7.7",	
				"com.braintribe.common:codec-api#1.0.13",
				"com.braintribe.common:common-api#1.0.15",
				"com.braintribe.common:logging#1.0.19",
				"com.braintribe.common:logging#1.0.21",
				"com.braintribe.common:parent#1.0.11",
				"org.tallison.xmp:xmpcore-shaded#6.1.10",
				"org.tallison:isoparser#1.9.41.2",
				"org.tallison:jmatio#1.2",
				"org.tallison:jmatio#1.5",
				"org.tallison:metadata-extractor#2.13.0",
				"tribefire.extension.js:js-project-model#2.0",
				"tribefire.extension.js:js-project-model#2.1",
				"tribefire.extension.js:js-setup-api-model#2.0",
				"tribefire.extension.js:js-setup-api-model#2.1",
				"tribefire.extension.js:js-setup-core-processing#2.0",
				"tribefire.extension.js:js-setup-processing#2.0",
				"tribefire.extension.js:js-setup-processing#2.1",
				"zzz:zzz-view#1.0.1"
		);
		
		List<String> expectedLocks = CollectionTools.getList(
				"aaa 1.0",
				"ant 1.6",
				"antlr 2.7",	
				"com.braintribe.common 1.0",
				"org.tallison 1.2 1.5 1.9 2.13",
				"org.tallison.xmp 6.1",
				"tribefire.extension.js 2.0 2.1",
				"zzz 1.0"
		);

		assertThat(GetLockedVersionsProcessor.groupLockedVersionsByGroupId(inputLocks)).containsExactlyElementsOf(expectedLocks);
	}
	
	@Test
	public void testGetLocks() {
		assertGetLocksWithoutViews("repositoryview-empty.yaml", CollectionTools.getList());

		// @formatter:off
		List<String> expectedLocks = CollectionTools.getList(
				"aaa:aaa-view#1.0.1",
				"ant:ant#1.6.5",
				"antlr:antlr#2.7.7",
				"com.braintribe.common:codec-api#1.0.13",
				"com.braintribe.common:common-api#1.0.15",
				"com.braintribe.common:logging#1.0.19",
				"com.braintribe.common:logging#1.0.21",
				"com.braintribe.common:parent#1.0.11",
				"zzz:zzz-view#1.0.1"
		);
		// @formatter:on
		
		// here we don't include views. Therefore "aaa:aaa-view#1.0.1" and "zzz:zzz-view#1.0.1" are not expected.
		assertGetLocksWithoutViews("repositoryview-disjunction.yaml", expectedLocks.subList(1, expectedLocks.size() - 1));
		// here we include views
		assertGetLocksWithViews("repositoryview-disjunction.yaml", expectedLocks, CommonTools.getList("aaa:aaa-view#1.0.1", "zzz:zzz-view#1.0.1"));

		// @formatter:off
		expectedLocks = CollectionTools.getList(
				"com.braintribe.common:codec-api#1.0.123",
				"com.braintribe.common:common-api#1.0.12",
				"com.braintribe.common:logging#1.0.26"
		);
		// @formatter:on

		assertGetLocksWithoutViews("repositoryview-lockfilter.yaml", expectedLocks);
		assertGetLocksWithoutViews("repositoryview-conjunction-one-operand.yaml", expectedLocks);
		assertGetLocksWithoutViews("repositoryview-conjunction-no-locks.yaml", CollectionTools.getList());

		assertThatThrownBy(() -> assertGetLocksWithoutViews("repositoryview-conjunction-two-operands.yaml", null))
				.isInstanceOf(IllegalStateException.class).hasMessageMatching(
						"Found a ConjunctionArtifactFilter with a LockArtifactFilter and at least one more filter. This is not supported!");
	}

	private void assertGetLocksWithViews(String repositoryConfigurationFileName, List<String> expectedLocks, List<String> viewsSolutions) {
		assertGetLocks(repositoryConfigurationFileName, expectedLocks, viewsSolutions);
	}

	private void assertGetLocksWithoutViews(String repositoryConfigurationFileName, List<String> expectedLocks) {
		assertGetLocks(repositoryConfigurationFileName, expectedLocks, null);
	}

	private void assertGetLocks(String repositoryConfigurationFileName, List<String> expectedLocks, List<String> viewsSolutions) {
		String testFolderPath = testDir().toString();
		File repositoryConfigurationFile = new File(testFolderPath, repositoryConfigurationFileName);
		List<String> foundLocks = GetLockedVersionsProcessor.process(repositoryConfigurationFile, viewsSolutions);
		assertThat(foundLocks).isEqualTo(expectedLocks);
	}
}