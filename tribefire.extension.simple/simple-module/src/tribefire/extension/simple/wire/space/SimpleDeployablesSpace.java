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
package tribefire.extension.simple.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.provider.Holder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.simple.deployables.access.SimpleInMemoryAccess;
import tribefire.extension.simple.deployables.service.SimpleEchoServiceProcessor;
import tribefire.extension.simple.deployables.terminal.SimpleWebTerminal;
import tribefire.extension.simple.model.deployment.service.SimpleEchoService;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformResourcesContract;

/**
 * This space class hosts configuration of deployables based on their denotation types.
 */
@Managed
public class SimpleDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private WebPlatformResourcesContract resources;

	// == State Change Processors == //

	// == Accesses == //
	/**
	 * Creates and configures a new {@link SimpleInMemoryAccess}.
	 */
	@Managed
	public SimpleInMemoryAccess simpleInMemoryAccess(ExpertContext<tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess> context) {
		// get denotation type which holds configuration settings
		tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess denotationType = context.getDeployable();

		// create new access instance
		SimpleInMemoryAccess access = new SimpleInMemoryAccess();

		// configure access
		access.setInitializeWithExampleData(denotationType.getInitializeWithExampleData());
		access.setMetaModelProvider(new Holder<>(denotationType.getMetaModel()));

		return access;
	}

	// == Apps == //
	/**
	 * Creates and configures a new {@link SimpleWebTerminal}.
	 */
	@Managed
	public SimpleWebTerminal simpleWebTerminal(ExpertContext<tribefire.extension.simple.model.deployment.terminal.SimpleWebTerminal> context) {
		// get denotation type which holds configuration settings
		tribefire.extension.simple.model.deployment.terminal.SimpleWebTerminal denotationType = context.getDeployable();

		// create new web terminal instance
		SimpleWebTerminal terminal = new SimpleWebTerminal();

		// configure web terminal
		terminal.setPrintRequestHeaders(denotationType.getPrintHeaders());
		terminal.setPrintRequestParameters(denotationType.getPrintParameters());

		return terminal;
	}

	// == Service Processors == //
	/**
	 * Creates and configures a new {@link SimpleEchoServiceProcessor}.
	 */
	@Managed
	public SimpleEchoServiceProcessor simpleEchoServiceProcessor(ExpertContext<SimpleEchoService> context) {
		// get denotation type which holds configuration settings
		SimpleEchoService denotationType = context.getDeployable();

		// create new service processor instance
		SimpleEchoServiceProcessor processor = new SimpleEchoServiceProcessor();

		// configure service processor
		processor.setDelay(denotationType.getDelay());
		processor.setEchoCount(denotationType.getEchoCount());

		return processor;
	}

	// == Jobs == //

	// == Helpers == //

}
