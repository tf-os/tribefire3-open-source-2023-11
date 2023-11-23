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

import java.util.Set;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.platformsetup.api.data.AssetNature;
import com.braintribe.model.platformsetup.api.response.AssetCollection;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * Returns a {@link AssetCollection} based on the provided filter criteria of
 * this request.
 * 
 * @author christina.wilpernig
 */
public interface GetAssets extends PlatformAssetRequest {

	EntityType<GetAssets> T = EntityTypes.T(GetAssets.class);
	
	@Override
	EvalContext<AssetCollection> eval(Evaluator<ServiceRequest> evaluator);

	@Override
	@Initializer("'setup'")
	String getDomainId();
	
	Set<AssetNature> getNature();
	void setNature(Set<AssetNature> nature);
	
	boolean getSetupAssets();
	void setSetupAssets(boolean setupAssets);
	
	@Initializer("true")
	Boolean getEffective();
	void setEffective(Boolean effective);
	
	Set<String> getRepoOrigin();
	void setRepoOrigin(Set<String> repoOrigin);
	
	Set<String> getGroupId();
	void setGroupId(Set<String> groupId);
	
	Set<String> getName();
	void setName(Set<String> name);

//	String getDependencyDepth();
//	void setDependencyDepth(String dependencyDepth);
	
}
