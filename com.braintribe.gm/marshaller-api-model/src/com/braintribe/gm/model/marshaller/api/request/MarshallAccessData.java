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

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * NOTE: this request is currently not implemented
 * 
 * MarshallAccessData nmarshalls the persistent data from {@link #getData()} into a {@link Resource}.
 *  
 * @author Dirk Scheffler
 */
public interface MarshallAccessData extends MarshallRequest, AccessDataRequest {
	EntityType<MarshallAccessData> T = EntityTypes.T(MarshallAccessData.class);
	
	Object getData();
	void setData(Object value);
	
    @Override
	EvalContext<? extends Resource> eval(Evaluator<ServiceRequest> evaluator);
}
