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
package com.braintribe.model.elasticsearchreflection.nodeinformation;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface JvmInfo extends StandardIdentifiable {

	final EntityType<JvmInfo> T = EntityTypes.T(JvmInfo.class);

	String getBootClassPath();
	void setBootClassPath(String bootClassPath);

	String getClassPath();
	void setClassPath(String classPath);

	List<String> getInputArguments();
	void setInputArguments(List<String> inputArguments);

	Mem getMem();
	void setMem(Mem mem);

	Long getPid();
	void setPid(Long pid);

	Date getStartTime();
	void setStartTime(Date startTime);

	Map<String, String> getSystemProperties();
	void setSystemProperties(Map<String, String> systemProperties);

	String getVersion();
	void setVersion(String version);

	String getVmName();
	void setVmName(String vmName);

	String getVmVendor();
	void setVmVendor(String vmVendor);

	String getVmVersion();
	void setVmVersion(String vmVersion);

}
