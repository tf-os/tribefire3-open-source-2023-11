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
package com.braintribe.devrock.api.storagelocker;

/**
 * simple interface to contain the keys used for the current {@link StorageLocker}
 * @author pit
 *
 */
public interface StorageLockerSlots {
	// virtual env
	String SLOT_VE_ACTIVATION = "ve-activation";
	String SLOT_VE_ENTRIES = "ve-entries";
	
	// copy & paste
	String SLOT_CLIP_COPY_MODE ="last-copy-mode";
	String SLOT_CLIP_PASTE_MODE ="last-paste-mode";
	
	// QI 
	String SLOT_SCAN_DIRECTORIES = "scan-directories";	
	String SLOT_ALTERNATIVE_QI = "qi-ui-alternative";
	String SLOT_QI_SHOW_ALL = "qi-ui-show-all";
	String SLOT_FILTER_WSET = "filter-workingset";
	
	// QD
	String SLOT_MAX_RESULT = "qd-ui-max-result";
	
	// dynamic commands
	String SLOT_DYNAMIC_COMMAND_MAX_DELAY = "dyn-cmd-delay-max";
	long DEFAULT_DYNAMIC_COMMAND_MAX_DELAY = 500;
	
	// AC
	String SLOT_AUTO_UPDATE_WS = "auto-update-workspace";
	String SLOT_SELECTIVE_WS_SYNCH = "selective-ws-synch";	
	String SLOT_AC_DEBUG_EVENT_LOGGING ="ac-debug-event-logging";
	String SLOT_DR_DEBUG_EVENT_LOGGING ="dr-debug-event-logging";
	String SLOT_AC_REQUIRE_HIGHER_VERSION = "strict-project-version-lower-boundary";
	String SLOT_AC_USE_STANDARD_CONFIGURATION = "ac-use-custom-configuration-for-analyis";
	String SLOT_AC_CUSTOM_CONFIGURATION = "ac-custom-configuration-for-analyis";
	
	// Resolution viewer
	String SLOT_ARTIFACT_VIEWER_TC_MAP_KEY = "artifact-viewer-context-map";
	String SLOT_ARTIFACT_VIEWER_SHORT_VERSION_NOTATION = "artifact-viewer-short-version-notation";
	String SLOT_ARTIFACT_VIEWER_SHOW_GROUP_IDS = "artifact-viewer-show-groups";
	String SLOT_ARTIFACT_VIEWER_ENABLED = "artifact-viewer-enabled";
	String SLOT_ARTIFACT_VIEWER_YAML_ENABLED = "artifact-viewer-yaml-enabled";
	String SLOT_ARTIFACT_VIEWER_STORE_VIEW_SETTINGS = "artiface-viewer-store-view-settings";
	String SLOT_ARTIFACT_VIEWER_PARENT = "artifact-viewer-show-parents";
	String SLOT_ARTIFACT_VIEWER_LAST_FILE = "artifact-viewer-last-file";
	
	String SLOT_ARTIFACT_VIEWER_INITIAL_TAG_TERMINAL = "artifact-viewer-initial-tab-terminal";
	String SLOT_ARTIFACT_VIEWER_UI_CLASSIC = "artifact-viewer-ui-classic";
	
	String SLOT_ARTIFACT_VIEWER_COLOR_AXIS_DEPENDENCY = "artifact-viewer-color-axis-dependency";
	String SLOT_ARTIFACT_VIEWER_COLOR_AXIS_PARENT = "artifact-viewer-color-axis-parent";
	String SLOT_ARTIFACT_VIEWER_COLOR_AXIS_IMPORT = "artifact-viewer-color-axis-import";
	
	String SLOT_ARTIFACT_VIEWER_COLOR_VERSION_STANDARD = "artifact-viewer-color-version-standard";
	String SLOT_ARTIFACT_VIEWER_COLOR_VERSION_PC = "artifact-viewer-color-version-pc";	
	
	String SLOT_ARTIFACT_VIEWER_PC_PURGE_NONDELETES = "artifact-viewer-pc-purge-nondeletes";
	
	// workspace importer
	String SLOT_WS_IMPORT_TOGGLE_INTRINSICS = "toggle-intrinsic-workingsets";
	String SLOT_WS_IMPORT_TOGGLE_DUPLICATES = "toggle-duplicates-in-workingsets";
	String SLOT_WS_IMPORT_WORKINGSETS = "toggle-workingsets";
	String SLOT_WS_IMPORT_LAST_FILE = "ws-import-last-file";
	String SLOT_WS_IMPORT_USE_SELECTIVE_EXPORT = "ws-import-selective-export";
	String SLOT_WS_IMPORT_USE_SELECTIVE_IMPORT = "ws-import-selective-import";
	String SLOT_WS_IMPORT_INCLUDE_STORAGE_LOCKER_DATA = "ws-import-include-storage-locker-data";
	

	// custom bling for decorators
	String SLOT_TF_NATURE_PROJECT_ICONS = "tf-nature-based-project-icons";
	String SLOT_TF_NATURE_PROJECT_BACKGROUND = "tf-nature-based-project-background";
	
	// ARB
	String SLOT_ARB_OUTPUT_DIR = "arb-output-directory";
	String DEFAULT_ARB_OUTPUT_DIRNAME = "class-gen";	
		
	// MB
	String SLOT_MB_SUCCESS_MESSAGES ="mb-success-messages-enabled";
	
	// TB runner
	String SLOT_TBR_TRANSITIVE = "tbr-transitive-run";	
	String SLOT_TBR_STANDARD_BUILDFILE = "tb-runner-use-standard-build";
	String SLOT_TBR_CUSTOM_BUILDFILE = "tb-runner-custom-build";
	String SLOT_TBR_CUSTOM_BUILDFILE_ASSOCIATION = "tb-runner-custom-associations";
	String SLOT_TBR_STANDARD_ASSOCIATION = "tb-runner-standard-associations";

	// zed
	String SLOT_ZED_VIEWER_LAST_FILE = "zed-viewer-last-selected-file";
	String SLOT_ZED_FP_CUSTOM_FILE = "zed-core-fp-custom-file";
	String SLOT_ZED_FP_OVERRIDE_RATINGS = "zed-core-fp-overrides";
	String SLOT_ZED_ALLOW_PURGE = "zed-allow-purge-of-excess-dependencies";
	String SLOT_ZED_MODEL_INITIAL_VIEWMODE ="zed-model-view-initial-mode";
	String SLOT_ZED_MODEL_INITIAL_SORTMODE ="zed-model-view-initial-sort-mode";
	String SLOT_ZED_COMPARISON_DUMP_LAST_FILE = "zed-comparison-last-selected-file";
	String SLOT_ZED_COMPARISON_NOTES_LAST_FILE = "zed-release-notes-last-selected-file";
	String SLOT_ZED_COMPARISON_NOTES_TEMPLATE = "zed-release-notes-template-file";
	
	// GF - currently not in use
	String SLOT_GF_TEMP_DIR = "gf-temp-directory";
	String SLOT_GF_SOURCE_REPOSITORIES = "source-repositories";
	String SLOT_GF_SKIP_OPTIONALS = "gf-skip-optionals";
	String SLOT_GF_SKIP_TEST_SCOPED = "gf-skip-test-scoped";
	String SLOT_GF_SKIP_EXISTING = "gf-skip-existing";
	String SLOT_GF_OVERWRITE_EXISTING = "gf-overwrite-existing";
	String SLOT_GF_USE_COMPILED_SCOPE = "gf-apply-compiled-scope";
	String SLOT_GF_VALIDATE_POMS = "gf-validate-poms";
	String SLOT_GF_ASYNC_SCAN = "gf-use-asynch-scan";
	String SLOT_GF_ENFORCE_LICENSE = "gf-enforce-license";
	String SLOT_GF_REPAIR_EXISTING = "gf-repair-existing";
	String SLOT_GF_PURGE_POMS = "gf-purge-poms";
	String SLOT_GF_FAKE_UPLOAD = "gf-faked-upload";
	String SLOT_GF_FAKE_UPLOAD_TARGET = "gf-faked-upload-target";
	String SLOT_GF_FAKE_UPLOAD_ERRORS = "gf-faked-upload-errors";
	String SLOT_GF_LAST_TARGET_REPO = "gf-last-target-repo";
	
}
