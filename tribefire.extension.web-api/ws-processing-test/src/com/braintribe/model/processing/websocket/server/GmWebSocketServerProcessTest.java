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

import java.util.Arrays;
import java.util.List;

import javax.websocket.Session;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.websocket.server.stub.BasicRemoteStub;
import com.braintribe.model.processing.websocket.server.stub.SessionStub;
import com.braintribe.model.processing.websocket.server.stub.evaluator.MulticastRequestEvaluatorStub;
import com.braintribe.model.service.api.InternalPushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.service.api.result.PushResponseMessage;

/**
 * Class used for testing process part of websocket server implemented in {@link GmWebSocketServer} class.
 * 
 */
public class GmWebSocketServerProcessTest extends AbstractGmWebSocketServerTest {

	private static Logger LOGGER = Logger.getLogger(GmWebSocketServerProcessTest.class);

	private static Session session1;
	private static Session session2;
	private static Session session3;

	private static Notify notifyServiceRequest;

	@BeforeClass
	public static void beforeClass() {
		setupWsServer();

		session1 = new SessionStub("session_id_1", "client_id_1", TYPE_GM_JSON, new BasicRemoteStub(new JsonStreamMarshaller()));
		session2 = new SessionStub("session_id_2", "client_id_2", TYPE_APPLICATION_JSON, new BasicRemoteStub(new JsonStreamMarshaller()));
		session3 = new SessionStub("session_id_3", "client_id_3", TYPE_APPLICATION_XML, new BasicRemoteStub(new StaxMarshaller(), true));

		notifyServiceRequest = Notify.T.create();
		notifyServiceRequest.setGlobalId("notify_id");
		notifyServiceRequest.setNotifications(Arrays.asList(MessageNotification.T.create(), MessageWithCommand.T.create()));

		wsServer.onOpen(session1, null);
		wsServer.onOpen(session2, null);
		wsServer.onOpen(session3, null);
	}

	@After
	public void after() {
		((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests().clear();
		((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests().clear();
		((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests().clear();
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid returned push response messages and valid service requests being sent to all clients.
	 */
	@Test
	public void testPushInternal_TargetAllClients() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target all clients");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests().get(0));
		testSentNotifyServiceRequest(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_3");

		testPushResponseMessage(pushResponseMessage, "Unable to push message to client", false, "master");

		LOGGER.info("Testing ws server pushInternal - target all clients -> succeeded with no errors");
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by session
	 * id pattern.
	 */
	@Test
	public void testPushInternal_TargetClientsBySessionIdPattern1() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by session id pattern 1");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setSessionIdPattern("session_id_2*");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).isEmpty();
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		LOGGER.info("Testing ws server pushInternal - target clients by session id pattern 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by session
	 * id pattern.
	 */
	@Test
	public void testPushInternal_TargetClientsBySessionIdPattern2() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by session id pattern 2");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setSessionIdPattern("session_id_[1-9]{1}");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests().get(0));
		testSentNotifyServiceRequest(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_3");

		testPushResponseMessage(pushResponseMessage, "Unable to push message to client", false, "master");

		LOGGER.info("Testing ws server pushInternal - target cliens by session id pattern 2 -> succeeded with no errors");
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by client
	 * id pattern.
	 */
	@Test
	public void testPushInternal_TargetClientsByClientIdPattern1() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by client id pattern 1");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setClientIdPattern("client_id_1");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).isEmpty();
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		LOGGER.info("Testing ws server pushInternal - target clients by client id pattern 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by client
	 * id pattern.
	 */
	@Test
	public void testPushInternal_TargetClientsByClientIdPattern2() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by client id pattern 2");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setClientIdPattern("client_id_[2-9]+");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).isEmpty();
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(2);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_3");

		testPushResponseMessage(pushResponseMessage, "Unable to push message to client", false, "master");

		LOGGER.info("Testing ws server pushInternal - target clients by client id pattern 2 -> succeeded with no errors");
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by role
	 * pattern.
	 */
	@Test
	public void testPushInternal_TargetClientsByRolePattern1() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by role pattern 1");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setRolePattern("tb-guest");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();
		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).isEmpty();
		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).isEmpty();

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_3");

		testPushResponseMessage(pushResponseMessage, "Unable to push message to client", false, "master");

		LOGGER.info("Testing ws server pushInternal - target clients by role pattern 1 -> succeeded with no errors");

	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by role
	 * pattern.
	 */
	@Test
	public void testPushInternal_TargetClientsByRolePattern2() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by role pattern 2");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setRolePattern("tb.*");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests().get(0));
		testSentNotifyServiceRequest(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_3");

		testPushResponseMessage(pushResponseMessage, "Unable to push message to client", false, "master");

		LOGGER.info("Testing ws server pushInternal - target clients by role pattern 2 -> succeeded with no errors");
	}

	/**
	 * Used for testing
	 * {@link GmWebSocketServer#pushInternal(com.braintribe.model.processing.service.api.ServiceRequestContext, InternalPushRequest)}
	 * procedure. <br>
	 * Expected: Valid push response messages and service requests in valid format being sent to clients targeted by mixed
	 * patterns.
	 */
	@Test
	public void testPushInternal_TargetClientsByMixedPatterns() throws Exception {

		LOGGER.info("Testing ws server pushInternal - target clients by mixed patterns");

		MulticastRequestEvaluatorStub evaluator = new MulticastRequestEvaluatorStub();

		InternalPushRequest internalPushRequest = InternalPushRequest.T.create();
		internalPushRequest.setServiceRequest(notifyServiceRequest);
		internalPushRequest.setRolePattern("tb-ad.*");
		internalPushRequest.setClientIdPattern("client_id_[0-2]+");
		internalPushRequest.setSessionIdPattern("session.*");

		PushResponse pushResponse = wsServer.process(evaluator, internalPushRequest);

		assertThat(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests()).hasSize(1);
		assertThat(((BasicRemoteStub) session2.getBasicRemote()).getSentServiceRequests()).isEmpty();
		assertThat(((BasicRemoteStub) session3.getBasicRemote()).getSentServiceRequests()).isEmpty();

		testSentNotifyServiceRequest(((BasicRemoteStub) session1.getBasicRemote()).getSentServiceRequests().get(0));

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		LOGGER.info("Testing ws server pushInternal - target clients by mixed patterns -> succeeded with no errors");
	}

	/**
	 * Tests whether sent notify service request is received in proper format by targeted clients.
	 */
	private void testSentNotifyServiceRequest(ServiceRequest serviceRequest) {
		assertThat(serviceRequest).isInstanceOf(Notify.class);
		Notify notify = (Notify) serviceRequest;
		assertThat(notify.getGlobalId()).isEqualTo("notify_id");
		assertThat(notify.getNotifications()).hasSize(2);
		assertThat(notify.getNotifications().get(0)).isInstanceOf(MessageNotification.class);
		assertThat(notify.getNotifications().get(1)).isInstanceOf(MessageWithCommand.class);
	}

	/**
	 * Used for testing push response messages.
	 */
	private void testPushResponseMessage(PushResponseMessage pushResponseMessage, String message, boolean success, String instanceId) {
		assertThat(pushResponseMessage.getMessage()).isEqualTo(message);
		assertThat(pushResponseMessage.getSuccessful()).isEqualTo(success);
		assertThat(pushResponseMessage.getOriginId().getGlobalId()).isEqualTo(instanceId);
	}

	/**
	 * Used for testing push response messages.
	 */
	private void testPushResponseMessage(PushResponseMessage pushResponseMessage, String message, boolean success, String instanceId,
			String clientId) {
		assertThat(pushResponseMessage.getMessage()).isEqualTo(message);
		assertThat(pushResponseMessage.getSuccessful()).isEqualTo(success);
		assertThat(pushResponseMessage.getClientIdentification()).isEqualTo(clientId);
		assertThat(pushResponseMessage.getOriginId().getGlobalId()).isEqualTo(instanceId);
	}

	/**
	 * Helper method used for fetching correct push response message to test, because containing list is not ordered due to
	 * hash map being used as session registry.
	 */
	private PushResponseMessage getResponseMessageByClientId(List<PushResponseMessage> responseMessages, String clientId) {
		return responseMessages.stream().filter(rm -> rm.getClientIdentification().equals(clientId)).findFirst().get();
	}

}
