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
package com.braintribe.wire.impl.contract;

import com.braintribe.wire.api.annotation.ContractAggregation;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;

public class ContractAggregationSpaceResolver implements ContractSpaceResolver {

	@Override
	public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ContractResolution resolveContractSpace(WireContext<?> wireContext,
			Class<? extends WireSpace> contractSpaceClass) {
		if (!contractSpaceClass.isAnnotationPresent(ContractAggregation.class))
			return null;
		
		return className -> ContractAggregationInvocationHandler.create(wireContext, contractSpaceClass);
	}

}
