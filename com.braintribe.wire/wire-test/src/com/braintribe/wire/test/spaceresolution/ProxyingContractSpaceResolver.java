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
package com.braintribe.wire.test.spaceresolution;

import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.test.spaceresolution.wire.contract.ProxyContract;

public class ProxyingContractSpaceResolver implements ContractSpaceResolver {

	@Override
	public ContractResolution resolveContractSpace(WireContext<?> wireContext, Class<? extends WireSpace> contractSpaceClass) {
		return resolveContractSpace(contractSpaceClass);
	}
	
	@Override
	public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
		if (ProxyContract.class.isAssignableFrom(contractSpaceClass))
			return f -> TestProxying.create(contractSpaceClass);
		else
			return null;
	}

}
