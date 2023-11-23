// ========================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2022 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission.
// To this file the Braintribe License Agreement applies.
// ========================================================================
package com.braintribe.build.cmd.assets.impl.check.artifact;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.check.api.Artifact;
import com.braintribe.build.cmd.assets.impl.check.process.ArtifactCheckContextImpl;
import com.braintribe.build.cmd.assets.impl.check.process.ResultStatus;
import com.braintribe.testing.test.AbstractTest;

/**
 * Provides tests for {@link StandardBuildScriptCheck}.
 * 
 * @author michael.lafite
 */
public class StandardBuildScriptCheckTest extends AbstractTest {

	@Test
	public void test() {
		CheckResult result;

		result = checkScript("standard.xml");
		assertThat(result.resultStatus).isEqualTo(ResultStatus.success);
		assertThat(result.context.currentResult.getDetailedInfo()).isNull();

		result = checkScript("standard-with-comment.xml");
		assertThat(result.resultStatus).isEqualTo(ResultStatus.success);
		assertThat(result.context.currentResult.getDetailedInfo()).isNull();

		result = checkScript("custom.xml");
		assertThat(result.resultStatus).isEqualTo(ResultStatus.error);
		assertThat(result.context.currentResult.getDetailedInfo()).matches("Unexpected element.+Only import.+");

		result = checkScript("file-does-not-exist.xml");
		assertThat(result.resultStatus).isEqualTo(ResultStatus.skipped);
		assertThat(result.context.currentResult.getDetailedInfo()).contains("Skipping check, because build script doesn't exist");
	}

	private CheckResult checkScript(String scriptFileName) {
		StandardBuildScriptCheck check = new StandardBuildScriptCheck();

		File file = testFile(scriptFileName);

		ArtifactCheckContextImpl context = new ArtifactCheckContextImpl();
		context.artifact = new Artifact();
		context.groupId = "not-used";
		context.artifactBuildXmlFile = file;
		context.newCheckResult();

		ResultStatus resultStatus = check.execute(context);

		CheckResult checkResult = new CheckResult();
		checkResult.resultStatus = resultStatus;
		checkResult.context = context;
		return checkResult;
	}

	private static class CheckResult {
		ResultStatus resultStatus;
		ArtifactCheckContextImpl context;
	}
}
