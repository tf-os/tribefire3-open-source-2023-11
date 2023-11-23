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
package com.braintribe.gm.model.marshaller.api.request;

import com.braintribe.gm.model.marshaller.api.data.MarshallQualification;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * MarshallRequest is the base type for all types that are intended to marshall (serialize) data into a {@link Resource} based on a marshaller that is selected by mimetype.
 * It inherits from {@link MarshallQualification} in order to let the caller parameterize the marshalling. 
 * @author Dirk Scheffler
 */
@Abstract
public interface MarshallRequest extends MarshallQualification, AbstractMarshallRequest {
	EntityType<MarshallRequest> T = EntityTypes.T(MarshallRequest.class);

    @Override
	EvalContext<? extends Resource> eval(Evaluator<ServiceRequest> evaluator);
}
