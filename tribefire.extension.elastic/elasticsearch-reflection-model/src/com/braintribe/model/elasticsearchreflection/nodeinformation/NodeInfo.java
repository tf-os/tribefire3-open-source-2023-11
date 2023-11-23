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

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface NodeInfo extends StandardIdentifiable {

	final EntityType<NodeInfo> T = EntityTypes.T(NodeInfo.class);

	Set<String> getHeaders();
	void setHeaders(Set<String> header);

	String getHostName();
	void setHostName(String hostName);

	JvmInfo getJvmInfo();
	void setJvmInfo(JvmInfo jvmInfo);

	DiscoveryNode getNode();
	void setNode(DiscoveryNode node);

	OsInfo getOsInfo();
	void setOsInfo(OsInfo osInfo);

	PluginsAndModules getPluginsAndModules();
	void setPluginsAndModules(PluginsAndModules pluginsAndModules);

	ProcessInfo getProcessInfo();
	void setProcessInfo(ProcessInfo processInfo);

	Map<String, String> getServiceAttributes();
	void setServiceAttributes(Map<String, String> serviceAttributes);

	Map<String, String> getSettings();
	void setSettings(Map<String, String> settings);

	List<ThreadPoolInfo> getThreadPoolInfos();
	void setThreadPoolInfos(List<ThreadPoolInfo> threadPoolInfos);

	TransportInfo getTransport();
	void setTransport(TransportInfo transport);

	String getVersion();
	void setVersion(String version);

}
