package com.braintribe.build.cmd.assets.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.platform.setup.api.GetAssetsFromPackagedPlatformSetup;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.testing.test.AbstractTest;

public class GetAssetsFromPackagedPlatformSetupTest extends AbstractTest {

	@Test
	public void test() {

		// *** create test setup ***
		PackagedPlatformSetup packagedPlatformSetup = CreateTribefireRuntimeManifestProcessorTest.createdTestPackagedPlatformSetup();

		GetAssetsFromPackagedPlatformSetup request = GetAssetsFromPackagedPlatformSetup.T.create();

		assertThat(GetAssetsFromPackagedPlatformSetupProcessor.process(request, packagedPlatformSetup)) //
				.contains("tribefire.cortex.controlcenter:tribefire-control-center#2.0", "tribefire.app.explorer:tribefire-explorer#2.0") //
				.doesNotContain("tribefire.cortex.controlcenter:tribefire-control-center#2.0.", "tribefire.app.explorer:tribefire-explorer#2.0.");

		request.setIncludeRevision(true);

		assertThat(GetAssetsFromPackagedPlatformSetupProcessor.process(request, packagedPlatformSetup)) //
				.contains("tribefire.cortex.controlcenter:tribefire-control-center#2.0.1", "tribefire.app.explorer:tribefire-explorer#2.0.1");
	}
}
