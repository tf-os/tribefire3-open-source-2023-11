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

import com.braintribe.cartridge.common.wire.contract.HttpContract;
import com.braintribe.cartridge.common.wire.contract.MarshallingContract;
import com.braintribe.cartridge.extension.wire.contract.ResourcesContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class DeployablesSpace implements WireSpace {

	@Import
	protected DeploymentSpace deployment;

	@Import
	protected CartridgeClientSpace cartridgeClient;
	
	@Import
	protected ResourcesContract resources;
	
	@Import
	protected ServletsSpace servlets;
	
	@Import
	protected RpcSpace rpc;
	
	@Import
	protected HttpContract http;

	@Import
	private MarshallingContract marshalling;


}
