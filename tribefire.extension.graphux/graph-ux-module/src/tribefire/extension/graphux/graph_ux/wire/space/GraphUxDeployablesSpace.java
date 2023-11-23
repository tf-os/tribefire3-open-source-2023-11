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
package tribefire.extension.graphux.graph_ux.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.extension.graphux.deployables.service.GraphUxServiceProcessor;
import tribefire.extension.graphux.model.deployment.GraphUxService;

@Managed
public class GraphUxDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;
	// == Service Processors == //
	/**
	 * Creates and configures a new {@link GraphUxServiceProcessor}.
	 */
	@Managed
	public GraphUxServiceProcessor graphUxServiceProcessor(ExpertContext<GraphUxService> context) {
		// get denotation type which holds configuration settings
		GraphUxService denotationType = context.getDeployable();

		// create new service processor instance
		GraphUxServiceProcessor processor = new GraphUxServiceProcessor();

		// configure service processor
//		processor.setDelay(denotationType.getDelay());
//		processor.setEchoCount(denotationType.getEchoCount());

		return processor;
	}
}
