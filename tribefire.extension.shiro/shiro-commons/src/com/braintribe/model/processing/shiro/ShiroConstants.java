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
package com.braintribe.model.processing.shiro;

public interface ShiroConstants {

	String CARTRIDGE_GROUPID = "tribefire.extension.shiro";

	String MODULE_NAME = "shiro-module";

	String CARTRIDGE_EXTERNALID = CARTRIDGE_GROUPID + ".shiro-cartridge";

	String CARTRIDGE_GLOBAL_ID = "cartridge:" + CARTRIDGE_EXTERNALID;

	String MODULE_GLOBAL_ID = "module://" + CARTRIDGE_GROUPID + ":" + MODULE_NAME;

	String DEPLOYMENT_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":shiro-deployment-model";
	String SERVICE_MODEL_QUALIFIEDNAME = CARTRIDGE_GROUPID + ":shiro-service-model";

	String SHIRO_LOGIN_EXTERNALID = "shiro.login.terminal";

	int MAJOR_VERSION = 3;

	String PATH_IDENTIFIER = "remote-login";

	String STATIC_IMAGES_RELATIVE_PATH = "/res/login-images/";
}
