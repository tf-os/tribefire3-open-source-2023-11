// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets;

import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingConstants;

public interface PlatformAssetDistributionConstants extends PlatformAssetResolvingConstants {
	String ACCESS_ID_SETUP = "setup";
	String PROJECTION_NAME_MASTER = "tribefire-master";
	
	String WEBAPP_FOLDER_NAME = "webapps";
	String JS_LIBRARIES_FOLDER_NAME = "js-libraries";
	String WEB_CONTEXT_DESCRIPTOR_FILE_NAME = "context.xml";
	
	String MODULES_DIR_NAME = "modules";
	String UX_MODULES_YAML_NAME = "ux-modules.yaml";
	
	String FOLDER_ENVIRONMENT = "environment";
	String FILE_TRIBEFIRE_PROPERTIES = "tribefire.properties";
	String FILE_CONFIGURATION_SHARED_JSON = "configuration.shared.json";
	String FILE_CONFIGURATION_JSON = "configuration.json";
	String FILE_REPOSITORY_CONFIGURATION = "repository-configuration.yaml";
	String FILE_REPOSITORY_VIEW_RESOLUTION = "repository-view-resolution.yaml";
	
	String INSTALLATION_ROOT_DIR = "TRIBEFIRE_INSTALLATION_ROOT_DIR";

	String FILENAME_KEYSTORE = "keystore.p12";
	String FILENAME_REWRITECONFIG = "rewrite.config";
	String TRIBEFIRE_LOCAL_BASE_URL = "TRIBEFIRE_LOCAL_BASE_URL";
	String TRIBEFIRE_SERVICES = "tribefire-services";
	String TRIBEFIRE_LICENSE = "tribefire-license";
	String CARTRIDGES_URI_PRIMING_FOLDER = "cartridges-uri-priming";
	String DIRNAME_PROJECTION = "_projection";
	String FILENAME_UPDATE_DIR = "_update";
	String FILENAME_SETUP_INFO_DIR = "setup-info";
	String FILENAME_RUNTIME_UPDATE_INFO = "runtime-update-info.json";
	String FILENAME_SETUP_DESCRIPTOR = "setup-descriptor.yaml";
	String FILENAME_PACKAGED_PLATFORM_SETUP = "packaged-platform-setup.json";
	String FILENAME_TRIBEFIRE_PROPERTIES = "tribefire.properties";
	String FILENAME_DEFAULT_DCSA_SS = "default-dcsa-shared-storage.yml";
	String FILENAME_REPOSITORY_VIEW_RESOLUTION = "repository-view-resolution.yaml";
	
	String DEVROCK_REPOSITORY_CONFIGURATION = "DEVROCK_REPOSITORY_CONFIGURATION";
	
	String TOMCAT_ASSET_ID = "tribefire.cortex.assets:tomcat-runtime";
}
