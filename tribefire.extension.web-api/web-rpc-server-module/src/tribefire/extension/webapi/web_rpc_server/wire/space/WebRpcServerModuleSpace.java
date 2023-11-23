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
package tribefire.extension.webapi.web_rpc_server.wire.space;

import javax.servlet.http.HttpServlet;

import com.braintribe.model.processing.webrpc.server.GmWebRpcServer;
import com.braintribe.web.api.registry.WebRegistries;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.api.WebRegistryConfiguration;
import tribefire.module.wire.contract.PlatformResourcesContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class WebRpcServerModuleSpace implements TribefireModuleContract {

	private static final String RPC_SERVLET_PATTERN = "/rpc";
	private static final String RPC_FILTER_PATTERN = RPC_SERVLET_PATTERN + "/*";
	
	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private PlatformResourcesContract resources;
	
	//
	// Hardwired deployables
	//

	@Override
	public void bindHardwired() {
		WebRegistryConfiguration webRegistry = tfPlatform.hardwiredDeployables().webRegistry();
		
		webRegistry.addServlet( //
				 WebRegistries.servlet() //
				 .name("web-rpc-server") //
				 .instance(server()) //
				 .pattern(RPC_SERVLET_PATTERN) //
				 .multipart() //
		);
		
		webRegistry.threadRenamingFilter().addPattern(RPC_FILTER_PATTERN);
		webRegistry.compressionFilter().addPattern(RPC_FILTER_PATTERN);
	}

	@Managed
	private HttpServlet server() {
		GmWebRpcServer bean = new GmWebRpcServer();
		
		bean.setDefaultMarshallerMimeType("application/json");
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setStreamPipeFactory(tfPlatform.resourceProcessing().streamPipeFactory());
		
		return bean;
	}
}
