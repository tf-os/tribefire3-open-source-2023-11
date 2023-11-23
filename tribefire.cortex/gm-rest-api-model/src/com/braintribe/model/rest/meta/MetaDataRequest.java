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
package com.braintribe.model.rest.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


import com.braintribe.model.rest.HasCodec;
import com.braintribe.model.rest.HasProjection;
import com.braintribe.model.rest.RestRequest;


public interface MetaDataRequest extends RestRequest, HasCodec, HasProjection{

	EntityType<MetaDataRequest> T = EntityTypes.T(MetaDataRequest.class);
	
	void setMetaData(String metaData);
	String getMetaData();
	void setUseCase(String useCase);
	String getUseCase();
	void setExclusive(Boolean exclusive);
	Boolean getExclusive();
	void setConstant(String constant);
	String getConstant();
	
}
