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
package com.braintribe.model.accessapi;


import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.service.api.ServiceRequest;


@Description("Returns the model that is currently deployed at an access.")
public interface GetModel extends PersistenceRequest {

	final EntityType<GetModel> T = EntityTypes.T(GetModel.class);

	
	@Override
	@Mandatory
	@Name("accessId")
	@Description("The id of the access that should be asked for its model environment.")
	String getServiceId();
	
	@Override
	EvalContext<? extends GmMetaModel> eval(Evaluator<ServiceRequest> evaluator);

	@Override
	default PersistenceRequestType persistenceRequestType() {
		return PersistenceRequestType.GetModel;
	}
}
