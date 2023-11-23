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
package com.braintribe.devrock.greyface.settings.preferences;

public class GreyfacePreferenceConstants {

	public static String PC_REPO_SETTINGS = "repositories";	
	public static String PC_TEMPORARY_DIRECTORY ="temporarydir";
	
	public static String PC_FAKE_UPLOAD = "fakeUpload";
	public static String PC_FAKE_UPLOAD_TARGET = "fakeUploadTarget";
	public static String PC_ASYNC_SCAN_MODE="asynchronousScan";
	public static String PC_SIMULATE_ERRORS = "simulateErrors";
	
	public static String PC_SETTING_EXCLUDE_OPTIONAL = "excludeOptional";
	public static String PC_SETTING_EXCLUDE_TEST = "excludeTest";
	
	public static String PC_SETTING_EXCLUDE_EXISTING = "excludeExisting";
	public static String PC_SETTING_OVERWRITE = "overwrite";
	public static String PC_SETTING_REPAIR = "repair";
	
	public static final String PC_SETTING_PURGE_POMS ="purgePom";
	public static final String PC_SETTING_ACCEPT_COMPILE ="acceptCompileScope";
	public static final String PC_VALIDATE_POMS = "validatePoms";
	
	public static final String REPAIR_PARTS_OF_EXISTING_ARTIFACT_IN_TARGET_REPOSITORY = "repair parts of existing artifact in target repository";
	public static final String OVERWRITE_EXISTING_ARTIFACT_IN_TARGET_REPOSITORY = "overwrite existing artifact in target repository";
	public static final String DO_NOT_SCAN_ARTIFACTS_THAT_EXIST_IN_TARGET_REPOSITORY = "do not scan artifacts that exist in target repository";
	public static final String SKIP_ARTIFACTS_WITH_SCOPE_TEST = "skip artifacts with scope \"test\"";
	public static final String SKIP_ARTIFACTS_MARKED_AS_OPTIONAL = "skip artifacts marked as optional";
	// debugs 
	public static final String FAKE_UPLOAD = "copy files instead of uploading";
	public static final String FAKE_UPLOAD_TARGET = "destination file system";
	public static final String SCAN_MODE = "activate asynchronuous scan";
	public static final String SIMULATE_ERRORS = "simulate upload errors";
	
	public static final String PC_SETTING_ENFORCE_LICENSES = "LicenseEnforcing";
	public static final String ENFORCE_LICENSES_PRIOR_TO_UPLOAD = "Enforce presence of license information as upload prerequiste";
	
	public static final String PURGE_POMS_OF_REPOSITORIES = "purge repository entries from poms";
	public static final String ACCEPT_COMPILE_SCOPE = "use compile magic-scope on terminal artifact";
	public static final String VALIDATE_POMS_DURING_SCAN = "validate pom during scan phase";
	
	public static final String PC_LAST_SELECTED_TARGET_REPO ="lastSelectedTargetRepo";

	
}
