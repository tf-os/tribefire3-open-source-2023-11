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
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.websocket.server.client.ClientSocket;
import com.braintribe.model.service.api.PushRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.PushResponse;
import com.braintribe.model.service.api.result.PushResponseMessage;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;

/**
 * Class used for integration testing of process part of websocket server implemented in {@link GmWebSocketServer}
 * class.
 * 
 */
// ignored until IT CI is ready
@Ignore
public class GmWebSocketServerProcessTest extends AbstractGmWebSocketServerTest {

	private static Logger LOGGER = Logger.getLogger(GmWebSocketServerProcessTest.class);

	private static String cortexSessionId;
	private static String user1SessionId;
	private static String user2SessionId;

	private static ClientSocket cortexClientSocket;
	private static ClientSocket user1ClientSocket;
	private static ClientSocket user2ClientSocket;

	private static Notify notifyServiceRequest;

	private static JsonStreamMarshaller jsonMarshaller;

	private static CloseableHttpClient httpClient;

	@BeforeClass
	public static void beforeClass() throws Exception {

		setup();
		setupUsers();

		cortexSessionId = GmSessionFactories.remote(instanceUri).authentication("cortex", "cortex").done().newSession("cortex")
				.getSessionAuthorization().getSessionId();

		user1SessionId = GmSessionFactories.remote(instanceUri).authentication("user1", "user1").done().newSession("cortex").getSessionAuthorization()
				.getSessionId();

		user2SessionId = GmSessionFactories.remote(instanceUri).authentication("user2", "user2").done().newSession("cortex").getSessionAuthorization()
				.getSessionId();

		cortexClientSocket = connectToWsServer(wsEndpointUri(instanceHost, instancePort, cortexSessionId, "test_client_id_1", TYPE_APPLICATION_JSON),
				new JsonStreamMarshaller());
		awaitWsConnectionProcedureToFinish(cortexClientSocket, true);

		user1ClientSocket = connectToWsServer(wsEndpointUri(instanceHost, instancePort, user1SessionId, "test_client_id_2", TYPE_APPLICATION_XML),
				new StaxMarshaller());
		awaitWsConnectionProcedureToFinish(user1ClientSocket, true);

		user2ClientSocket = connectToWsServer(wsEndpointUri(instanceHost, instancePort, user2SessionId, "test_client_id_3", null),
				new JsonStreamMarshaller());
		awaitWsConnectionProcedureToFinish(user2ClientSocket, true);

		notifyServiceRequest = Notify.T.create();
		notifyServiceRequest.setGlobalId("notify_id");
		notifyServiceRequest.setNotifications(Arrays.asList(MessageNotification.T.create(), MessageWithCommand.T.create()));

		jsonMarshaller = new JsonStreamMarshaller();

		httpClient = HttpClients.createDefault();
	}

	/**
	 * Sets up users in tf instance.
	 */
	private static void setupUsers() throws Exception {

		PersistenceGmSessionFactory cortexSessionFactory = GmSessionFactories.remote(instanceUri).authentication("cortex", "cortex").done();
		PersistenceGmSession cortexAuthSession = cortexSessionFactory.newSession("auth");

		createUserEntity(cortexAuthSession, "user1", "user1", "tf-admin", "tf-user");
		createUserEntity(cortexAuthSession, "user2", "user2", "tf-admin", "tf-guest");

		cortexAuthSession.commit();
	}

	/**
	 * Creates user entity.
	 */
	private static User createUserEntity(PersistenceGmSession authSession, String username, String password, String... roleNames) {

		User user = authSession.create(User.T);
		user.setName(username);
		user.setPassword(password);
		Set<Role> roles = new HashSet<>();
		for (String roleName : roleNames) {
			Role role = authSession.create(Role.T);
			role.setName(roleName);
			roles.add(role);
		}
		user.setRoles(roles);

		return user;
	}

	@AfterClass
	public static void afterClass() throws Exception {

		httpClient.close();
	}

	@After
	public void after() throws Exception {

		cortexClientSocket.getReceivedServiceRequests().clear();
		user1ClientSocket.getReceivedServiceRequests().clear();
		user2ClientSocket.getReceivedServiceRequests().clear();
	}

	/**
	 * Used for testing processing of push requests in ws server when all clients are targeted. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetAllClients() throws Exception {

		LOGGER.info("Testing ws server push request processing - target all clients");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_3");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, true)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user1ClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user2ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target all clients -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by session id pattern. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsBySessionIdPattern1() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by session id pattern 1");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setSessionIdPattern(cortexSessionId + "+.?");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, false)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, false)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by session id pattern 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by session id pattern. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsBySessionIdPattern2() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by session id pattern 2");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setSessionIdPattern(".+");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_3");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user1ClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user2ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by session id pattern 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by client id pattern. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsByClientIdPattern1() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by client id pattern 1");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setClientIdPattern("test_client_id_(1|3){1}");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(2);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_3");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, false)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user2ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by client id pattern 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by client id pattern. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsByClientIdPattern2() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by client id pattern 2");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setClientIdPattern(".*");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_3");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, true)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user1ClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user2ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by client id pattern 2 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by role pattern. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsByRolePattern1() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by role pattern 1");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setRolePattern("tf-user");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, false)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, false)).doesNotThrowAnyException();

		testNotifyServiceRequest(user1ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by role pattern 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by role pattern. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsByRolePattern2() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by role pattern 2");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setRolePattern("tf-a.+");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(3);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_3");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, true)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user1ClientSocket.getReceivedServiceRequests().get(0));
		testNotifyServiceRequest(user2ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by role pattern 2 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by mixed patterns. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsByMixedPatterns1() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by mixed patterns 1");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setSessionIdPattern(".*");
		pushRequest.setClientIdPattern("test_client_id_[0-1]+");
		pushRequest.setRolePattern("tf-a.+");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_1");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, false)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, false)).doesNotThrowAnyException();

		testNotifyServiceRequest(cortexClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by mixed patterns 1 -> succeeded with no errors");
	}

	/**
	 * Used for testing processing of push requests in ws server when clients are targeted by mixed patterns. <br>
	 * Expected: Valid returned push response messages and valid service requests being received by targeted clients.
	 */
	@Test
	public void testProcessing_TargetClientsByMixedPatterns2() throws Exception {

		LOGGER.info("Testing ws server push request processing - target clients by mixed patterns 2");

		PushRequest pushRequest = PushRequest.T.create();
		pushRequest.setSessionId(cortexSessionId);
		pushRequest.setServiceRequest(notifyServiceRequest);
		pushRequest.setSessionIdPattern(user1SessionId);
		pushRequest.setClientIdPattern("test_client_id_[1-3]{1}");
		pushRequest.setRolePattern("tf-u.+");

		PushResponse pushResponse = sendPushRequest(wsServiceUri(instanceHost, instancePort, cortexSessionId), pushRequest);

		assertThat(pushResponse.getResponseMessages()).hasSize(1);

		PushResponseMessage pushResponseMessage = getResponseMessageByClientId(pushResponse.getResponseMessages(), "test_client_id_2");

		testPushResponseMessage(pushResponseMessage, "Pushed message to client", true, "master");

		assertThatCode(() -> awaitWsMessageProcedureToFinish(user1ClientSocket, true)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(cortexClientSocket, false)).doesNotThrowAnyException();
		assertThatCode(() -> awaitWsMessageProcedureToFinish(user2ClientSocket, false)).doesNotThrowAnyException();

		testNotifyServiceRequest(user1ClientSocket.getReceivedServiceRequests().get(0));

		LOGGER.info("Testing ws server push request processing - target clients by mixed patterns 2 -> succeeded with no errors");
	}

	/**
	 * Tests push response messages.
	 */
	private void testPushResponseMessage(PushResponseMessage pushResponseMessage, String message, boolean success, String instanceId) {

		assertThat(pushResponseMessage.getMessage()).isEqualTo(message);
		assertThat(pushResponseMessage.getSuccessful()).isEqualTo(success);
		assertThat(pushResponseMessage.getOriginId().getApplicationId()).isEqualTo(instanceId);
	}

	/**
	 * Helper method used for fetching correct push response message to test, because containing list is not ordered due to
	 * hash map being used as session registry.
	 */
	private PushResponseMessage getResponseMessageByClientId(List<PushResponseMessage> responseMessages, String clientId) {

		return responseMessages.stream().filter(rm -> rm.getClientIdentification().equals(clientId)).findFirst().get();
	}

	/**
	 * Tests whether sent notify service request is received in proper format by targeted clients.
	 */
	private void testNotifyServiceRequest(ServiceRequest serviceRequest) {

		assertThat(serviceRequest).isInstanceOf(Notify.class);
		Notify notify = (Notify) serviceRequest;
		assertThat(notify.getGlobalId()).isEqualTo("notify_id");
		assertThat(notify.getNotifications()).hasSize(2);
		assertThat(notify.getNotifications().get(0)).isInstanceOf(MessageNotification.class);
		assertThat(notify.getNotifications().get(1)).isInstanceOf(MessageWithCommand.class);
	}

	/**
	 * Creates http post requests with push request payload and sends it to websocket service.
	 */
	private PushResponse sendPushRequest(String wsServiceUri, PushRequest pushRequest) throws Exception {

		HttpPost httpRequest = new HttpPost(wsServiceUri);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		jsonMarshaller.marshall(os, pushRequest);
		String marshalledPayload = os.toString("UTF-8");
		httpRequest.setEntity(new StringEntity(marshalledPayload));
		httpRequest.setHeader("Content-type", TYPE_APPLICATION_JSON);
		httpRequest.setHeader("Accept", TYPE_APPLICATION_JSON);
		HttpResponse response = httpClient.execute(httpRequest);
		// System.out.println(EntityUtils.toString(response.getEntity()));
		return (PushResponse) jsonMarshaller.unmarshall(response.getEntity().getContent());
	}

}
