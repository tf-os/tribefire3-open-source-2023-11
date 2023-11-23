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
package tribefire.extension.js.core.wire.space;

import com.braintribe.build.artifacts.mc.wire.buildwalk.contract.GeneralConfigurationContract;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.js.core.wire.contract.JsResolverConfigurationContract;

/**
 * specific implementation of the {@link GeneralConfigurationContract} that takes the {@link VirtualEnvironment} from the {@link JsResolverConfigurationContract}
 * @author pit
 *
 */
@Managed
public class JsGeneralConfigurationSpace implements GeneralConfigurationContract {
	@Import
	JsResolverConfigurationContract resolverConfiguration;

	@Override
	public VirtualEnvironment virtualEnvironment() {
		return resolverConfiguration.virtualEnvironment();
	}
	
	

	@Override
	public boolean resolveParallel() {
		return true;
	}


	@Override
	public boolean respectExclusions() {	
		return true;
	}



	@Override
	public boolean lenient() {
		return false;
	}
	
	@Override
	public boolean walkParentStructure() {
		return false;
	}

}
