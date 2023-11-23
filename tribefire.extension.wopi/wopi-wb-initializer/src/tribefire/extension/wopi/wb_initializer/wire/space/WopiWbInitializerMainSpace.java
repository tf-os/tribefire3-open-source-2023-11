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
package tribefire.extension.wopi.wb_initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.assets.default_wb_initializer.wire.contract.DefaultWbContract;
import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.CommonWorkbenchContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.IconContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.ResourcesContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.WopiWbInitializerContract;
import tribefire.extension.wopi.wb_initializer.wire.contract.WopiWbInitializerMainContract;

@Managed
public class WopiWbInitializerMainSpace implements WopiWbInitializerMainContract {

	@Import
	private WopiWbInitializerContract initializer;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private DefaultWbContract workbench;

	@Import
	private IconContract icons;

	@Import
	ResourcesContract resource;

	@Import
	CommonWorkbenchSpace commonWorkbench;

	@Override
	public WopiWbInitializerContract initializer() {
		return initializer;
	}

	@Override
	public CoreInstancesContract coreInstances() {
		return coreInstances;
	}

	@Override
	public DefaultWbContract workbench() {
		return workbench;
	}

	@Override
	public IconContract icons() {
		return icons;
	}

	@Override
	public ResourcesContract resources() {
		return resource;
	}

	@Override
	public CommonWorkbenchContract commonWorkbench() {
		return commonWorkbench;
	}
}
