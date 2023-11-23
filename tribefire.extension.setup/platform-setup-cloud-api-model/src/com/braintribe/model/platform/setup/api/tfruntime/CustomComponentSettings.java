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
package com.braintribe.model.platform.setup.api.tfruntime;

import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface CustomComponentSettings extends GenericEntity {
	EntityType<CustomComponentSettings> T = EntityTypes.T(CustomComponentSettings.class);

	String getName();
	void setName(String name);

	String getNameRegex();
	void setNameRegex(String nameRegex);

	LogLevel getLogLevel();
	void setLogLevel(LogLevel logLevel);

	Integer getReplicas();
	void setReplicas(Integer replicas);

	Boolean getEnableJpda();
	void setEnableJpda(Boolean enableJpda);

	Map<String, String> getEnv();
	void setEnv(Map<String, String> env);

	Resources getResources();
	void setResources(Resources resources);

	List<PersistentVolume> getPersistentVolumes();
	void setPersistentVolumes(List<PersistentVolume> persistentVolumes);

	String getCustomPath();
	void setCustomPath(String customPath);

	String getCustomHealthCheckPath();
	void setCustomHealthCheckPath(String customHealthCheckPath);
}
