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
package com.braintribe.model.elasticsearchreflection.clusterhealth;

import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ClusterHealth extends AbstractHealth {

	final EntityType<ClusterHealth> T = EntityTypes.T(ClusterHealth.class);

	Double getActiveShardsPercent();
	void setActiveShardsPercent(Double activeShardsPercent);

	String getClusterName();
	void setClusterName(String clusterName);

	Integer getDelayedUnassignedShards();
	void setDelayedUnassignedShards(Integer delayedUnassignedShards);

	Map<String, ClusterIndexHealth> getIndices();
	void setIndices(Map<String, ClusterIndexHealth> indices);

	Integer getNumberOfNodes();
	void setNumberOfNodes(Integer numberOfNodes);

	Integer getNumberOfDataNodes();
	void setNumberOfDataNodes(Integer numberOfDataNodes);

	Integer getNumberOfPendingTasks();
	void setNumberOfPendingTasks(Integer numberOfPendingTasks);

	String getTaskMaxWaitingTime();
	void setTaskMaxWaitingTime(String taskMaxWaitingTime);

}
