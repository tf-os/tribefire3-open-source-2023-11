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
package com.braintribe.model.cortexapi.model;

import java.util.List;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;

@Abstract
public interface CreateModelRequest extends ModelRequest {
	
	EntityType<CreateModelRequest> T = EntityTypes.T(CreateModelRequest.class);

	@Mandatory
	@Description("The name of the model.")
	public String getName();
	public void setName(String name);
	
	@Initializer("'custom.model'")
	@Mandatory
	@Description("The group id of the model.")
	public String getGroupId();
	public void setGroupId(String groupId);
	
	@Initializer("'1.0'")
	@Mandatory
	@Description("The version of the model.")
	public String getVersion();
	public void setVersion(String version);
	
	@Description("An optional list of model dependencies. If not specified the root-model will be added as dependency to the new created model.")
	public List<GmMetaModel> getDependencies();
	public void setDependencies(List<GmMetaModel> dependencies);

}
