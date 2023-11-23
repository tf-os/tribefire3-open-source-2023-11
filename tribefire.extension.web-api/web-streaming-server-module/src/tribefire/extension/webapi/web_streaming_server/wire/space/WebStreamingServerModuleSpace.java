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
package tribefire.extension.webapi.web_streaming_server.wire.space;

import com.braintribe.model.processing.resource.server.WebStreamingServer;
import com.braintribe.web.api.registry.WebRegistries;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.api.WebRegistryConfiguration;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class WebStreamingServerModuleSpace implements TribefireModuleContract {

	private static final String WEB_STREAMING_SERVLET_PATTERN = "/streaming";
	
	@Import
	private TribefireWebPlatformContract tfPlatform;

	//
	// Hardwired deployables
	//

	@Override
	public void bindHardwired() {
		WebRegistryConfiguration webRegistry = tfPlatform.hardwiredDeployables().webRegistry();
		
		webRegistry.addServlet( //
				 WebRegistries.servlet() //
				 .name("web-streaming-server") //
				 .instance(webStreamingServer()) //
				 .pattern(WEB_STREAMING_SERVLET_PATTERN) //
				 .multipart() //
		);
		
		webRegistry.strictAuthFilter().addPattern(WEB_STREAMING_SERVLET_PATTERN);
		webRegistry.threadRenamingFilter().addPattern(WEB_STREAMING_SERVLET_PATTERN);;
	}

	@Managed
	private WebStreamingServer webStreamingServer() {
		WebStreamingServer bean = new WebStreamingServer();
		bean.setSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setSystemSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setDefaultUploadResponseType("application/json");
		return bean;
	}
}
