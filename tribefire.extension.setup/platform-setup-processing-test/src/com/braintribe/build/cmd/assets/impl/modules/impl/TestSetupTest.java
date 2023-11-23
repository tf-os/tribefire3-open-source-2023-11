// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl;

import static com.braintribe.build.cmd.assets.impl.modules.impl.base.ModuleTestsSolutionEnricher.API_JAR;
import static com.braintribe.build.cmd.assets.impl.modules.impl.base.ModuleTestsSolutionEnricher.LIB_JAR;
import static com.braintribe.build.cmd.assets.impl.modules.impl.base.ModuleTestsSolutionEnricher.MODEL_MANIFEST_JAR;
import static com.braintribe.build.cmd.assets.impl.modules.impl.base.ModuleTestsSolutionEnricher.MODEL_XML_JAR;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.braintribe.setup.tools.GmNature;

/**
 * Just a simple test which tests the test setup itself. Some inner parts of classpath handling are not fully mocked, but we have real jars in place
 * under res/ModuleSetup.
 * 
 * @author peter.gazdik
 */
public class TestSetupTest extends AbstractTfSetupResolverTest {

	@Test
	public void jarTest() throws Exception {
		test(LIB_JAR, GmNature.library);
		test(API_JAR, GmNature.api);
		test(MODEL_MANIFEST_JAR, GmNature.model);
		test(MODEL_XML_JAR, GmNature.model);
	}

	private void test(File file, GmNature expectedNature) {
		assertThat(GmNature.fromJarFileName(file.getAbsolutePath())).isEqualTo(expectedNature);
	}

}
