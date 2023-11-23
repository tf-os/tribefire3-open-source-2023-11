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
package tribefire.extension.modelling.management_wb.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.modelling.management_wb.wire.contract.ManagementWbInitializerContract;
import tribefire.extension.modelling.management_wb.wire.contract.ManagementWbInitializerMainContract;

/**
 * @see {@link ManagementWbInitializerMainContract}
 */
@Managed
public class ManagementWbInitializerMainSpace extends AbstractInitializerSpace implements ManagementWbInitializerMainContract {

	@Import
	private ManagementWbInitializerContract initializer;
	
	@Import
	private DefaultWbContract workbench;
	
	@Override
	public ManagementWbInitializerContract initializerContract() {
		return initializer;
	}

	@Override
	public DefaultWbContract workbenchContract() {
		return workbench;
	}
}
