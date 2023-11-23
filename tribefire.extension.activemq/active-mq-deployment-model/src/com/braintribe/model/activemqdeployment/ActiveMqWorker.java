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
package com.braintribe.model.activemqdeployment;

import java.util.List;

import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ActiveMqWorker extends Worker {

	final EntityType<ActiveMqWorker> T = EntityTypes.T(ActiveMqWorker.class);
	
	void setBindAddress(String bindAddress);
	@Initializer("'0.0.0.0'")
	String getBindAddress();
	
	void setPort(Integer port);
	@Initializer("61616")
	Integer getPort();
	
	void setDataDirectory(String dataDirectory);
	@Initializer("'WEB-INF/activemq-data'")
	String getDataDirectory();
	
	void setBrokerName(String brokerName);
	String getBrokerName();
	
	void setUseJmx(Boolean useJmx);
	@Initializer("false")
	Boolean getUseJmx();
	
	void setPersistenceDbDir(String persistenceDbDir);
	@Initializer("'WEB-INF/activemq-db'")
	String getPersistenceDbDir();
	
	void setClusterNodes(List<NetworkConnector> clusterNodes);
	List<NetworkConnector> getClusterNodes();
	
	void setHeapUsageInPercent(Integer heapUsageInPercent);
	Integer getHeapUsageInPercent();
	
	void setDiskUsageLimit(Long diskUsageLimit);
	Long getDiskUsageLimit();
	
	void setTempUsageLimit(Long tempUsageLimit);
	Long getTempUsageLimit();
	
	void setCreateVmConnector(Boolean createVmConnector);
	Boolean getCreateVmConnector();
	
	void setPersistent(Boolean persistent);
	@Initializer("false")
	Boolean getPersistent();

	void setDiscoveryMulticastUri(String discoveryMulticastUri);
	String getDiscoveryMulticastUri();

	void setDiscoveryMulticastGroup(String discoveryMulticastGroup);
	String getDiscoveryMulticastGroup();

	void setDiscoveryMulticastNetworkInterface(String discoveryMulticastNetworkInterface);
	String getDiscoveryMulticastNetworkInterface();

	void setDiscoveryMulticastAddress(String discoveryMulticastAddress);
	String getDiscoveryMulticastAddress();

	void setDiscoveryMulticastInterface(String discoveryMulticastInterface);
	String getDiscoveryMulticastInterface();

}
