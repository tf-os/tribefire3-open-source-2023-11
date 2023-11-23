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

import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;

public class NameConventionContractSpaceResolver implements ContractSpaceResolver {

	private String contractPrefix;
	private String contractSuffix;
	
	private String implementationPrefix;
	private String implementationSuffix;
	
	private boolean lenient;
	
	public NameConventionContractSpaceResolver(String contractPrefix, String contractSuffix,
			String implementationPrefix, String implementationSuffix, boolean lenient) {
		this.contractPrefix = contractPrefix;
		this.contractSuffix = contractSuffix;
		this.implementationPrefix = implementationPrefix;
		this.implementationSuffix = implementationSuffix;
		this.lenient = lenient;
	}
	
	@Override
	public ContractResolution resolveContractSpace(WireContext<?> wireContext, Class<? extends WireSpace> contractSpaceClass) {
		return resolveContractSpace(contractSpaceClass);
	}

	@Override
	public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
		String contractName = contractSpaceClass.getName();
		if (!(contractName.startsWith(contractPrefix) && contractName.endsWith(contractSuffix))) {
			if (lenient) 
				return null;
			throw new IllegalStateException("Contract [" + contractSpaceClass.getName() + "] is not suitable for the naming pattern " + contractPrefix + "*" + contractSuffix);
		}
		
		String implementationName = implementationPrefix + 
				contractName.substring(contractPrefix.length(), contractName.length() - contractSuffix.length()) +
				implementationSuffix; 
		
		return new StandardContractResolution(implementationName);
	}
}
