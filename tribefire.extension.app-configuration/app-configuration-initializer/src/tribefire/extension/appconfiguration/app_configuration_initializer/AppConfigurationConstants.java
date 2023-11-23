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
package tribefire.extension.appconfiguration.app_configuration_initializer;

public final class AppConfigurationConstants {

	private static final String GROUP_ID = "tribefire.extension.app-configuration";

	private static final String MODEL_GLOBAL_ID_PREFIX = "model:";
	private static final String HARDWIRED_GLOBAL_ID_PREFIX = "hardwired:";

	private static final String APP_CONFIGURATION_MODEL_GLOBAL_ID_PREFIX = MODEL_GLOBAL_ID_PREFIX + GROUP_ID + ":";

	public static final String APP_CONFIGURATION_DEPLOYMENT_MODEL_GLOBAL_ID = APP_CONFIGURATION_MODEL_GLOBAL_ID_PREFIX
			+ "app-configuration-deployment-model";
	public static final String APP_CONFIGURATION_API_MODEL_GLOBAL_ID = APP_CONFIGURATION_MODEL_GLOBAL_ID_PREFIX + "app-configuration-api-model";
	public static final String APP_CONFIGURATION_MODEL_GLOBAL_ID = APP_CONFIGURATION_MODEL_GLOBAL_ID_PREFIX + "app-configuration-model";

	public static final String APP_CONFIGURATION_UX_MODULE = "js-ux-module://" + GROUP_ID + ":app-configuration-ux";

	public static final String WORKBENCH_ACCESS_GLOBAL_ID = HARDWIRED_GLOBAL_ID_PREFIX + "access/workbench";

	private AppConfigurationConstants() {
		// no instantiation required
	}

}
