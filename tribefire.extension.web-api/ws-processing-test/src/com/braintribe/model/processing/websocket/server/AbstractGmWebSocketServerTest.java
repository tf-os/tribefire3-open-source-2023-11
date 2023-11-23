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
package com.braintribe.model.processing.websocket.server;

import com.braintribe.model.processing.websocket.server.stub.evaluator.ValidateUserSessionEvaluatorStub;
import com.braintribe.model.processing.websocket.server.stub.marshaller.MarshallerRegistryStub;
import com.braintribe.model.service.api.InstanceId;

import tribefire.extension.web_api.ws.WsRegistry;
import tribefire.extension.web_api.ws.WsServer;

/**
 * Parent of test classes used for testing websocket server implemented in {@link GmWebSocketServer} class.
 * 
 */
public abstract class AbstractGmWebSocketServerTest {

	protected static WsServer wsServer;
	protected static WsRegistry wsSessionRegistry;
	
	protected static final String TYPE_GM_JSON = "gm/json";
	protected static final String TYPE_APPLICATION_JSON = "application/json";
	protected static final String TYPE_APPLICATION_XML = "application/xml";

	protected static void setupWsServer() {
		wsSessionRegistry = new WsRegistry();
		wsServer = createWsServer(wsSessionRegistry);
	}
	
	/**
	 * Creates a {@link GmWebSocketServer} instance populated with test doubles.
	 */
	private static WsServer createWsServer(WsRegistry wsSessionRegistry) {
		WsServer wsServer = new WsServer();
		wsServer.setMarshallerRegistry(new MarshallerRegistryStub());
		wsServer.setEvaluator(new ValidateUserSessionEvaluatorStub());
		wsServer.setProcessingInstanceId(InstanceId.T.create("master"));
		wsServer.setSessionRegistry(wsSessionRegistry);
		
		return wsServer;
	}
	
}
