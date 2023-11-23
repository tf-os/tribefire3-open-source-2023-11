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
package tribefire.extension.demo.demo_wb_initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerContract;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerIconContract;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerMainContract;

/**
 * @see DemoWbInitializerMainContract
 */
@Managed
public class DemoWbInitializerMainSpace extends AbstractInitializerSpace implements DemoWbInitializerMainContract {

	@Import
	DefaultWbContract workbench;
	
	@Import
	DemoWbInitializerIconContract icons;
	
	@Import
	DemoWbInitializerContract demoWorkbench;
	
	/**
	 * Note: No @Managed annotation as we are just passing-through existing instances.
	 * Only annotate a managed instance if entities are created in there.
	 */
	@Override
	public DefaultWbContract workbenchContract() {
		return workbench;
	}
	
	
	/**
	 * Note: No @Managed annotation as we are just passing-through existing instances.
	 */
	@Override
	public DemoWbInitializerIconContract demoWorkbenchInitializerIconContract() {
		return icons;
	}

	
	/**
	 * Note: No @Managed annotation as we are just passing-through existing instances.
	 */
	@Override
	public DemoWbInitializerContract demoWorkbenchInitializerContract() {
		return demoWorkbench;
	}
}
