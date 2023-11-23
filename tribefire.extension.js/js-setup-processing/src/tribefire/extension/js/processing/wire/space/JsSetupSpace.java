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
package tribefire.extension.js.processing.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.js.processing.JsSetupProcessor;
import tribefire.extension.js.processing.wire.contract.JsSetupConfigContract;
import tribefire.extension.js.processing.wire.contract.JsSetupContract;

@Managed
public class JsSetupSpace implements JsSetupContract {
	
	@Import
	private JsSetupConfigContract config;
	
	@Override
	@Managed
	public JsSetupProcessor jsSetupProcessor() {
		JsSetupProcessor bean = new JsSetupProcessor();
		
		bean.setVirtualEnvironment(config.virtualEnvironment());
		
		return bean;
	}

}
