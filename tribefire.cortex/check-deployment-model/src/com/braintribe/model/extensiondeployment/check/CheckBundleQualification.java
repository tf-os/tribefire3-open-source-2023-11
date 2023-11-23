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
package com.braintribe.model.extensiondeployment.check;

import java.util.Set;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.model.check.CheckWeight;

@Description("Bundles, qualifies and associates checks.")
public interface CheckBundleQualification extends HasName {
	
	EntityType<CheckBundleQualification> T = EntityTypes.T(CheckBundleQualification.class);

	String module = "module";
	String deployable = "deployable";
	String coverage = "coverage";
	String weight = "weight";
	String labels = "labels";
	String roles = "roles";
	String isPlatformRelevant = "isPlatformRelevant";
	
	Module getModule();
	void setModule(Module module);

	Deployable getDeployable();
	void setDeployable(Deployable deployable);
	
	CheckCoverage getCoverage();
	void setCoverage(CheckCoverage coverage);
	
	CheckWeight getWeight();
	void setWeight(CheckWeight weight);
	
	Set<String> getLabels();
	void setLabels(Set<String> labels);
	
	Set<String> getRoles();
	void setRoles(Set<String> roles);
	
	/**
	 * @deprecated This used to mark deployables which are not in custom cartridges. They don't exist anymore, so this is always set to true and thus
	 *             pointless.
	 */
	@Deprecated
	boolean getIsPlatformRelevant();
	void setIsPlatformRelevant(boolean isPlatformRelevant);
}
