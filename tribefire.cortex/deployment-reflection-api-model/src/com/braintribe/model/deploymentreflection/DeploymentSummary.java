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
package com.braintribe.model.deploymentreflection;

import java.util.List;
import java.util.Map;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.InstanceId;

public interface DeploymentSummary extends GenericEntity {

	EntityType<DeploymentSummary> T = EntityTypes.T(DeploymentSummary.class);

	QualifiedDeployedUnits getTotalUnits();
	void setTotalUnits(QualifiedDeployedUnits totalUnits);
	
	Map<String,QualifiedDeployedUnits> getUnitsByNode();
	void setUnitsByNode(Map<String,QualifiedDeployedUnits> unitsByNode);
	
	Map<String,QualifiedDeployedUnits> getUnitsByCartridge();
	void setUnitsByCartridge(Map<String,QualifiedDeployedUnits> unitsByCartridge);
	
	Map<Deployable,QualifiedDeployedUnits> getUnitsByDeployable();
	void setUnitsByDeployable(Map<Deployable,QualifiedDeployedUnits> unitsByDeployable);
	
	List<QualifiedDeployedUnits> getSourceUnits();
	void setSourceUnits(List<QualifiedDeployedUnits> sourceUnits);
	
	Map<InstanceId,QualifiedDeployedUnits> getUnitsByInstanceId();
	void setUnitsByInstanceId(Map<InstanceId,QualifiedDeployedUnits> unitsByInstanceId);
	
}
