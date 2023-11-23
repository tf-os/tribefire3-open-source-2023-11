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
package tribefire.extension.demo.demo_wb_initializer.wire.contract;

import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;

/**
 * <p>
 * The main contract of an initializer serves to expose all relevant contracts to the "outside" world. <br>
 * It is highly recommended that every initializer contains a main contract to be extensible (once a contract is
 * bound to its WireModule, an interface change would often result in breaking changes).
 * 
 * <p>
 * In this example two internal contracts <code>demoWorkbenchInitializerIconContract</code> and
 * <code>demoWorkbenchInitializerContract</code>, as well as the depended <code>workbenchContract</code> is exposed.
 *
 */
public interface DemoWbInitializerMainContract extends WireSpace {

	/**
	 * Coming from DefaultWbWireModule.
	 */
	DefaultWbContract workbenchContract();

	/**
	 * Coming from this initializer.
	 */
	DemoWbInitializerIconContract demoWorkbenchInitializerIconContract();

	/**
	 * Coming from CoreInstancesWireModule.
	 */
	DemoWbInitializerContract demoWorkbenchInitializerContract();

}
