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
package com.braintribe.model.platformreflection.host.tomcat;

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformreflection.HostInfo;


public interface TomcatHostInfo extends HostInfo {

	EntityType<TomcatHostInfo> T = EntityTypes.T(TomcatHostInfo.class);
	
	List<Connector> getConnectors();
	void setConnectors(List<Connector> connectors);
	
	String getHostBasePath();
	void setHostBasePath(String hostBasePath);
	
	Engine getEngine();
	void setEngine(Engine engine);

	ThreadPools getThreadPools();
	void setThreadPools(ThreadPools threadPools);
	
	List<Ssl> getSsl();
	void setSsl(List<Ssl> ssl);
	
}
