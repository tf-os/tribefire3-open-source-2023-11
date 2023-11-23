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
package tribefire.extension.demo.demo_wb_initializer;

import static com.braintribe.wire.api.util.Lists.list;

import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.extension.demo.demo_wb_initializer.wire.DemoWbWireModule;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerContract;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerIconContract;
import tribefire.extension.demo.demo_wb_initializer.wire.contract.DemoWbInitializerMainContract;

/**
 * <p>
 * This initializer shows how to set up a workbench via code. <br>
 * It sets up upon the default workbench configuration which is being applied via asset dependency
 * <code>tribefire.cortex.assets:tribefire-default-workbench-initializer</code>. <br>

 * <p>
 * A good alternative to this approach is to use the GME as it provides a convenient way to create
 * the same configuration via UI support.
 *  
 */
public class DemoWbInitializer extends AbstractInitializer<DemoWbInitializerMainContract>{

	@Override
	public WireTerminalModule<DemoWbInitializerMainContract> getInitializerWireModule() {
		return DemoWbWireModule.INSTANCE;
	}
	
	@Override
	protected void initialize(PersistenceInitializationContext context,
			WiredInitializerContext<DemoWbInitializerMainContract> initializerContext,
			DemoWbInitializerMainContract initializerContract) {

		DefaultWbContract workbench = initializerContract.workbenchContract();
		DemoWbInitializerIconContract iconContract = initializerContract.demoWorkbenchInitializerIconContract();
		DemoWbInitializerContract demoWorkbench = initializerContract.demoWorkbenchInitializerContract();

		// update header bar
		workbench.tbLogoFolder().setIcon(iconContract.logoIcon());

		// add entry point
		workbench.defaultRootPerspective().getFolders().add(demoWorkbench.entryPointFolder());

		// add to home folder
		workbench.defaultHomeFolderPerspective().getFolders()
				.addAll(list(
						demoWorkbench.personFolder(), //
						demoWorkbench.companyFolder() //
						));

		// add actions
		workbench.defaultActionbarPerspective().getFolders().add(demoWorkbench.employeesByGenderFolder());
		workbench.defaultActionbarPerspective().getFolders().add(demoWorkbench.newEmployeeFolder());

	}
	
}