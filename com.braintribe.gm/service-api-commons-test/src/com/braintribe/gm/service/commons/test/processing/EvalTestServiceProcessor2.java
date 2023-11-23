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
package com.braintribe.gm.service.commons.test.processing;

import com.braintribe.gm.service.commons.test.model.EvalTestServiceRequest2;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessorException;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

public class EvalTestServiceProcessor2 extends AuthorizedServiceProcessorBase implements ServiceProcessor<EvalTestServiceRequest2, Long> {

	@Override
	public Long process(ServiceRequestContext context, EvalTestServiceRequest2 parameter) throws ServiceProcessorException {
		preProcess(context, parameter);
		return Long.MAX_VALUE;
	}

}