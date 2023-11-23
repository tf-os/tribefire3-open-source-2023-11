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

import static org.assertj.core.api.Assertions.assertThat;

import javax.websocket.Session;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.websocket.server.stub.SessionStub;

import tribefire.extension.web_api.ws.WsRegistry.WsRegistrationEntry;

/**
 * Class used for testing connection part of websocket server implemented in {@link GmWebSocketServer} class.
 * 
 */
public class GmWebSocketServerConnectionTest extends AbstractGmWebSocketServerTest {

	private static Logger LOGGER = Logger.getLogger(GmWebSocketServerConnectionTest.class);
	
	@BeforeClass
	public static void beforeClass() {
		setupWsServer();
	}
	
	@After
	public void after() {
		wsSessionRegistry.remove(s -> true);
	}

	/**
	 * Used for testing {@link GmWebSocketServer#onOpen(Session, javax.websocket.EndpointConfig)} procedure when session
	 * id and/or client id are not included as query parameters when connecting. <br>
	 * Expected: session not added to session registry and closed.
	 */
	@Test
	public void testOnOpen_MissingSessionAndOrClientId() throws Exception {

		LOGGER.info("Testing ws server onOpen - missing session and/or client id");

		Session session = new SessionStub(null, null, null);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session.isOpen()).isFalse();

		session = new SessionStub("session_id_1", null, null);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session.isOpen()).isFalse();

		session = new SessionStub(null, "client_id", null);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session.isOpen()).isFalse();

		LOGGER.info("Testing ws server onOpen - missing session and/or client id -> succeeded with no errors");
	}

	/**
	 * Used for testing {@link GmWebSocketServer#onOpen(Session, javax.websocket.EndpointConfig)} procedure when invalid
	 * session id is included as query parameter when connecting. <br>
	 * Expected: session not added to session registry and closed.
	 */
	@Test
	public void testOnOpen_InvalidSessionId() throws Exception {

		LOGGER.info("Testing ws server onOpen - invalid session id");

		Session session = new SessionStub("invalid_session_id", "client_id", null);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session.isOpen()).isFalse();

		LOGGER.info("Testing ws server onOpen - invalid session id -> succeeded with no errors");
	}

	/**
	 * Used for testing {@link GmWebSocketServer#onOpen(Session, javax.websocket.EndpointConfig)} procedure when
	 * unsupported response format is requested via accept query parameter when connecting. <br>
	 * Expected: session not added to session registry and closed.
	 */
	@Test
	public void testOnOpen_UnsupportedResponseFormat() throws Exception {

		LOGGER.info("Testing ws server onOpen - unsupported response format");

		Session session = new SessionStub("session_id_1", "client_id", "unsupported_response_type");
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session.isOpen()).isFalse();

		LOGGER.info("Testing ws server onOpen - unsupported response format -> succeeded with no errors");
	}

	/**
	 * Used for testing {@link GmWebSocketServer#onOpen(Session, javax.websocket.EndpointConfig)} procedure when needed
	 * and valid info is included as query parameters when connecting. <br>
	 * Expected: session added to session registry and open.
	 */
	@Test
	public void testOnOpen_ValidInfo() throws Exception {

		LOGGER.info("Testing ws server onOpen - valid info");

		Session session = new SessionStub("session_id_1", "client_id_1", null);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).hasSize(1);
		assertThat(session.isOpen()).isTrue();

		WsRegistrationEntry sessionRegEntry = wsSessionRegistry.findEntry(session);

		assertThat(sessionRegEntry).isNotNull();
		assertThat(sessionRegEntry.getSession()).isEqualTo(session);
		assertThat(sessionRegEntry.getClientInfo().getSessionId()).isEqualTo("session_id_1");
		assertThat(sessionRegEntry.getClientInfo().getClientId()).isEqualTo("client_id_1");
		assertThat(sessionRegEntry.getClientInfo().getAccept()).isEqualTo(TYPE_GM_JSON);

		session = new SessionStub("session_id_1", "client_id_1", TYPE_GM_JSON);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).hasSize(2);
		assertThat(session.isOpen()).isTrue();

		sessionRegEntry = wsSessionRegistry.findEntry(session);

		assertThat(sessionRegEntry).isNotNull();
		assertThat(sessionRegEntry.getSession()).isEqualTo(session);
		assertThat(sessionRegEntry.getClientInfo().getSessionId()).isEqualTo("session_id_1");
		assertThat(sessionRegEntry.getClientInfo().getClientId()).isEqualTo("client_id_1");
		assertThat(sessionRegEntry.getClientInfo().getAccept()).isEqualTo(TYPE_GM_JSON);

		session = new SessionStub("session_id_3", "client_id_3", TYPE_APPLICATION_XML);
		wsServer.onOpen(session, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).hasSize(3);
		assertThat(session.isOpen()).isTrue();

		sessionRegEntry = wsSessionRegistry.findEntry(session);

		assertThat(sessionRegEntry).isNotNull();
		assertThat(sessionRegEntry.getSession()).isEqualTo(session);
		assertThat(sessionRegEntry.getClientInfo().getSessionId()).isEqualTo("session_id_3");
		assertThat(sessionRegEntry.getClientInfo().getClientId()).isEqualTo("client_id_3");
		assertThat(sessionRegEntry.getClientInfo().getAccept()).isEqualTo(TYPE_APPLICATION_XML);

		LOGGER.info("Testing ws server onOpen - valid info -> succeeded with no errors");
	}

	/**
	 * Used for testing {@link GmWebSocketServer#onClose(Session, javax.websocket.CloseReason)}. <br>
	 * Expected: session removed from session registry and closed.
	 */
	@Test
	public void testOnClose() throws Exception {

		LOGGER.info("Testing ws server onClose");

		Session session1 = new SessionStub("session_id_1", "client_id", null);
		wsServer.onOpen(session1, null);
		Session session2 = new SessionStub("session_id_2", "client_id", null);
		wsServer.onOpen(session2, null);

		wsServer.onClose(session1, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).hasSize(1);

		WsRegistrationEntry sessionRegEntry = wsSessionRegistry.findEntry(session2);

		assertThat(sessionRegEntry).isNotNull();
		assertThat(sessionRegEntry.getSession()).isEqualTo(session2);
		assertThat(session1.isOpen()).isFalse();
		assertThat(session2.isOpen()).isTrue();

		wsServer.onClose(session2, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session2.isOpen()).isFalse();

		LOGGER.info("Testing ws server onClose -> succeeded with no errors");
	}

	/**
	 * Used for testing {@link GmWebSocketServer#onError(Session, Throwable)}. <br>
	 * Expected: session removed from session registry and closed.
	 */
	@Test
	public void testOnError() throws Exception {

		LOGGER.info("Testing ws server onError");

		Session session1 = new SessionStub("session_id_1", "client_id", null);
		wsServer.onOpen(session1, null);
		Session session2 = new SessionStub("session_id_2", "client_id", null);
		wsServer.onOpen(session2, null);

		wsServer.onError(session1, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).hasSize(1);

		WsRegistrationEntry sessionRegEntry = wsSessionRegistry.findEntry(session2);

		assertThat(sessionRegEntry).isNotNull();
		assertThat(sessionRegEntry.getSession()).isEqualTo(session2);
		assertThat(session1.isOpen()).isFalse();
		assertThat(session2.isOpen()).isTrue();

		wsServer.onError(session2, null);

		assertThat(wsSessionRegistry.findEntries(p -> true)).isEmpty();
		assertThat(session2.isOpen()).isFalse();

		LOGGER.info("Testing ws server onError -> succeeded with no errors");

	}

}
