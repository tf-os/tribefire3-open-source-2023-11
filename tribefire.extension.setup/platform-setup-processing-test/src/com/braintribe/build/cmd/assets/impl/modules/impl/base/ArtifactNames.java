// ============================================================================
// Braintribe IT-Technologies GmbH - www.braintribe.com
// Copyright Braintribe IT-Technologies GmbH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules.impl.base;

/**
 * @author peter.gazdik
 */
public interface ArtifactNames {

	String TEST_PLATFORM = "test:test-platform#1.0";

	String MODULE_A = "test:A-module#1.0";
	String MODULE_B = "test:B-module#1.0";

	String JAR_A = "test:A-jar#1.0";
	String JAR_B = "test:B-jar#1.0";
	String JAR_C = "test:C-jar#1.0";
	String JAR_D = "test:D-jar#1.0";

	String JAR_A_V2 = "test:A-jar#2.0";

	String MODEL_A = "test:A-model#1.0";
	String MODEL_B = "test:B-model#1.0";

	String API_A = "test:A-api#1.0"; // // This is marked as API using jar Manifest
	String MVN_API_A = "test:A-mvnApi#1.0"; // This is marked as API, but using Maven properties, not jar Manifest

	String LIB_A = "test:A-lib#1.0";

	String PLATFORM_ONLY_A = "test:A-platformOnly#1.0";

}
