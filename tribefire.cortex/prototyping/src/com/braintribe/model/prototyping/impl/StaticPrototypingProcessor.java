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
package com.braintribe.model.prototyping.impl;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.prototyping.api.StaticPrototyping;

public class StaticPrototypingProcessor extends PrototypingProcessor<StaticPrototyping> {

	@Override
	public GenericEntity process(ServiceRequestContext requestContext, StaticPrototyping request) {
		if (!request.getClone())
			return request.getPrototype();
		
		ConfigurableCloningContext cloningContext = ConfigurableCloningContext.build()
			.skipGlobalId(true)
			.skipIndentifying(true)
			.done();
		
		GenericEntity clonedEntity = request.getPrototype().clone(cloningContext);
		return clonedEntity;
	}

}
