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

public interface ClusterIndexHealth extends AbstractHealth {

	final EntityType<ClusterIndexHealth> T = EntityTypes.T(ClusterIndexHealth.class);

	String getIndex();
	void setIndex(String index);

	Integer getNumberOfReplicas();
	void setNumberOfReplicas(Integer numberOfReplicas);

	Integer getNumberOfShards();
	void setNumberOfShards(Integer numberOfShards);

	Map<Integer, ClusterShardHealth> getShards();
	void setShards(Map<Integer, ClusterShardHealth> shards);

}
