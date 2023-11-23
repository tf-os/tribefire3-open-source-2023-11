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
package com.braintribe.model.platformreflection;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformreflection.request.PlatformReflectionResponse;
import com.braintribe.model.platformreflection.streampipes.StreamPipesInfo;
import com.braintribe.model.platformreflection.tf.License;
import com.braintribe.model.platformreflection.tf.ModuleAssets;
import com.braintribe.model.platformreflection.tf.SetupAssets;
import com.braintribe.model.platformreflection.tf.TribefireServicesInfo;

public interface TribefireInfo extends PlatformReflectionResponse {

	EntityType<TribefireInfo> T = EntityTypes.T(TribefireInfo.class);

	void setServicesInfo(TribefireServicesInfo servicesInfo);
	TribefireServicesInfo getServicesInfo();

	void setTribefireRuntimeProperties(Map<String, String> tribefireRuntimeProperties);
	Map<String, String> getTribefireRuntimeProperties();

	License getLicense();
	void setLicense(License license);

	SetupAssets getSetupAssets();
	void setSetupAssets(SetupAssets setupAssets);

	ModuleAssets getModuleAssets();
	void setModuleAssets(ModuleAssets modulesAssets);

	StreamPipesInfo getStreamPipeInfo();
	void setStreamPipeInfo(StreamPipesInfo streamPipesInfo);

	FolderInfo getTempDirInfo();
	void setTempDirInfo(FolderInfo folderInfo);

	// TODO: Sessions, active users
	// TODO: https://github.com/brettwooldridge/HikariCP/wiki/MBean-(JMX)-Monitoring-and-Management
	// TODO: http://www.mchange.com/projects/c3p0/#jmx_configuration_and_management
	// https://amemon.wordpress.com/2007/07/15/monitoring-c3p0-using-jmxjconsole/
}
