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
package com.braintribe.cartridge.extension.wire.space;

import com.braintribe.cartridge.common.processing.deployment.DenotationTypeBindingsConfig;
import com.braintribe.cartridge.common.wire.contract.CommonComponentsContract;
import com.braintribe.cartridge.extension.api.customization.CartridgeCustomization;
import com.braintribe.cartridge.extension.processing.customization.ConfigurableCartridgeCustomization;
import com.braintribe.cartridge.extension.wire.contract.CustomCartridgeContract;
import com.braintribe.cartridge.extension.wire.contract.MasterComponentsContract;
import com.braintribe.cartridge.extension.wire.contract.WebRegistryContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class CustomCartridgeSpace implements CustomCartridgeContract {

	private final static String CARTRIDGE_EXTERNALID = "tribefire.extension.auth.auth-cartridge";

	@Import
	protected DeployablesSpace deployables;

	@Import
	protected MasterComponentsContract masterComponents;

	@Import
	private CommonComponentsContract commonComponents;

	@Import
	private DefaultDeployablesSpace defaultDeployables;
	
	@Import
	private WebRegistryContract webRegistry;
	
	
	@Managed
	@Override
	public CartridgeCustomization customization() {
		ConfigurableCartridgeCustomization bean = new ConfigurableCartridgeCustomization();
		bean.setExternalId(CARTRIDGE_EXTERNALID);
		bean.setExtensions(extensions());
		return bean;
	}
	

	@Managed
	public DenotationTypeBindingsConfig extensions() {
		DenotationTypeBindingsConfig bean = new DenotationTypeBindingsConfig();
		
		//@formatter:off
		
		
		//@formatter:on
		
		return bean;
	}
	
}
