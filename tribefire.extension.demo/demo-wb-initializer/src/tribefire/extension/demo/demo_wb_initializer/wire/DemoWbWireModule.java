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
package tribefire.extension.demo.demo_wb_initializer.wire;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.List;

import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.darktheme_wb_initializer.wire.DarkthemeWbWireModule;
import tribefire.cortex.assets.default_wb_initializer.wire.DefaultWbWireModule;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerMainContract;

/**
 * <p>
 * This is the WireModule of the demo-workbench-initializer. Within this module you can depend on other WireModules to
 * use their provided functionality. <br>
 * 
 * <p>
 * This WireModule provides the initializer's main contract. It is highly recommended that every initializer contains a
 * main contract to be extensible (once a contract is bound to its WireModule, an interface change would often
 * result in breaking changes).
 * 
 */
public enum DemoWbWireModule implements WireTerminalModule<DemoWbInitializerMainContract>{
	INSTANCE;

	@Override
	public Class<DemoWbInitializerMainContract> contract() {
		return DemoWbInitializerMainContract.class;
	}
	
	/**
	 * The demo workbench has the grayish-blue-style applied. Therefore we depend on the respective WireModule
	 * coming from asset <code>tribefire.cortex.assets:tribefire-grayish-blue-style-initializer</code>.
	 */
	@Override
	public List<WireModule> dependencies() {
		return list(DefaultWbWireModule.INSTANCE, DarkthemeWbWireModule.INSTANCE);
	}
}
