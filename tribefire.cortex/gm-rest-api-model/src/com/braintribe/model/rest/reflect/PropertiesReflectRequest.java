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
package com.braintribe.model.rest.reflect;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;


import com.braintribe.model.rest.HasCodec;
import com.braintribe.model.rest.HasEntityType;
import com.braintribe.model.rest.HasProjection;
import com.braintribe.model.rest.RestRequest;


public interface PropertiesReflectRequest extends RestRequest, HasProjection, HasEntityType, HasCodec {

	EntityType<PropertiesReflectRequest> T = EntityTypes.T(PropertiesReflectRequest.class);
	
	void setName(String name);
	String getName();
	
}
