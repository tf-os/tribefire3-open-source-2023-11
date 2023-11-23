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
package com.braintribe.model.platformsetup.api.request;

import java.util.List;
import java.util.Set;

import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.selector.DependencySelector;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface AddAssetDependencies extends PlatformAssetRequest {

	EntityType<AddAssetDependencies> T = EntityTypes.T(AddAssetDependencies.class);

	PlatformAsset getDepender();
	void setDepender(PlatformAsset depender);
	
	List<PlatformAsset> getDependencies();
	void setDependencies(List<PlatformAsset> dependencies);
	
	boolean getRemoveRedundantDepsOfDepender();
	void setRemoveRedundantDepsOfDepender(boolean removeRedundantDepsOfDepender);
	
	boolean getAsGlobalSetupCandidate();
	void setAsGlobalSetupCandidate(boolean asGlobalSetupCandidate);
	
	boolean getAsDesigntimeOnly();
	void setAsDesigntimeOnly(boolean asDesigntimeOnly);
	
	boolean getAsRuntimeOnly();
	void setAsRuntimeOnly(boolean asRuntimeOnly);
	
	Set<String> getForStages();
	void setForStages(Set<String> forStages);
	
	DependencySelector getCustomSelector();
	void setCustomSelector(DependencySelector customSelector);
	
}
