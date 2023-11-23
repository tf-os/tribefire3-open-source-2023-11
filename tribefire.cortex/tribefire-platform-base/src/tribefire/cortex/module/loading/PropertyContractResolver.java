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
package tribefire.cortex.module.loading;

import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.wire.api.space.ContractResolution;
import com.braintribe.wire.api.space.ContractSpaceResolver;
import com.braintribe.wire.api.space.WireSpace;
import com.braintribe.wire.impl.properties.PropertyLookups;

import tribefire.module.wire.contract.PropertyLookupContract;

public class PropertyContractResolver implements ContractSpaceResolver {

	private boolean suppressDecryption;

	public void setSuppressDecryption(boolean suppressDecryption) {
		this.suppressDecryption = suppressDecryption;
	}
	
	@Override
	public ContractResolution resolveContractSpace(Class<? extends WireSpace> contractSpaceClass) {
		if (PropertyLookupContract.class.isAssignableFrom(contractSpaceClass))
			return f -> PropertyLookups.create(contractSpaceClass, TribefireRuntime::getProperty, suppressDecryption);
		else
			return null;
	}
}
