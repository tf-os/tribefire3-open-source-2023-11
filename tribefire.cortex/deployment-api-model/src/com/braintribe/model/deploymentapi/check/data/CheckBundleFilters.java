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
package com.braintribe.model.deploymentapi.check.data;

import java.util.Set;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;

public interface CheckBundleFilters extends GenericEntity {

	EntityType<CheckBundleFilters> T = EntityTypes.T(CheckBundleFilters.class);
	
	Set<String> getModule();
	void setModule(Set<String> module);
	
	Set<String> getDeployable();
	void setDeployable(Set<String> deployable);
	
	Set<String> getLabel();
	void setLabel(Set<String> label);
	
	Set<String> getName();
	void setName(Set<String> name);
	
	Set<String> getNode();
	void setNode(Set<String> node);

	CheckWeight getWeight();
	void setWeight(CheckWeight weight);
	
	Set<CheckCoverage> getCoverage();
	void setCoverage(Set<CheckCoverage> coverage);
	
	Set<String> getRole();
	void setRole(Set<String> role);
	
	Boolean getIsPlatformRelevant();
	void setIsPlatformRelevant(Boolean isPlatformRelevant);
	
}
