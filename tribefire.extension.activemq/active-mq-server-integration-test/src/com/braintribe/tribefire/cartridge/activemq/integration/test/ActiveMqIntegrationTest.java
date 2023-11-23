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
package com.braintribe.tribefire.cartridge.activemq.integration.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.logging.Logger;
import com.braintribe.model.activemqdeployment.ActiveMqWorker;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.user.User;
import com.braintribe.product.rat.imp.ImpApi;
import com.braintribe.testing.internal.tribefire.tests.AbstractTribefireQaTest;
import com.braintribe.transport.messaging.api.MessageConsumer;
import com.braintribe.transport.messaging.api.MessageProducer;
import com.braintribe.transport.messaging.api.MessagingSession;
import com.braintribe.transport.messaging.jms.JmsActiveMqMessagingSessionProvider;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;

/**
 * checks if all expected deployables are present and deployed, as well as expected demo entities are present
 *
 */
public class ActiveMqIntegrationTest extends AbstractTribefireQaTest {

	private static Logger log = Logger.getLogger(ActiveMqIntegrationTest.class);
	private static final String CARTRIDGE_ID = "tribefire.extension.activemq.active-mq-server-cartridge";

	public final static int ACTIVEMQ_PORT = 61616;
	private static JmsActiveMqMessagingSessionProvider messagingSessionProvider;
	private static MessagingSession messagingSession;
	private static Queue queue;
	private static ConcurrentLinkedDeque<Object> receivedMessages = new ConcurrentLinkedDeque<>();

	private ReentrantLock lock = new ReentrantLock();
	private Condition messageReceived = lock.newCondition();
	private static ImpApi imp;

	private static File dataDir;
	private static File dbDir;

	@BeforeClass
	public static void initialize() throws Exception {

		log.info("Making sure that all expected deployables are there and deployed...");

		imp = apiFactory().build();

		ActiveMqWorker worker = imp.deployable(ActiveMqWorker.T, "activemq.worker").get();

		dataDir = createTempDir("data");
		dbDir = createTempDir("dbpersistence");

		assertThat(worker).isNotNull();

		worker.setDataDirectory(dataDir.getAbsolutePath());
		worker.setPersistenceDbDir(dbDir.getAbsolutePath());
		imp.deployable(ActiveMqWorker.T, "activemq.worker").redeploy();

		messagingSessionProvider = MessagingSessionTools.messagingSessionProvider();
		messagingSessionProvider.postConstruct();

		messagingSession = messagingSessionProvider.get();
		queue = messagingSession.createQueue("queue-" + UUID.randomUUID().toString());

		log.info("Test preparation finished successfully!");
	}

	private static File createTempDir(String name) throws IOException {
		File resTemp = new File("res/temp");
		resTemp.mkdirs();
		File d = File.createTempFile(name, "folder", resTemp);
		d.delete();
		d.mkdirs();
		return d;
	}

	@AfterClass
	public static void shutdown() throws Exception {
		if (messagingSession != null) {
			messagingSession.close();
		}
		if (messagingSessionProvider != null) {
			messagingSessionProvider.preDestroy();
		}

		imp.deployable(ActiveMqWorker.T, "activemq.worker").undeploy();

		if (dataDir != null) {
			try {
				FileTools.deleteDirectoryRecursively(dataDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (dbDir != null) {
			try {
				FileTools.deleteDirectoryRecursively(dbDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Ignore
	private static void print(String text) {
		System.out.println(DateTools.encode(new Date(), DateTools.LEGACY_DATETIME_WITH_MS_FORMAT) + " [Master]: " + text);
	}

	@Ignore
	public void onMessage(Message message) {
		Object body = message.getBody();
		receivedMessages.add(body);
		lock.lock();
		try {
			messageReceived.signal();
		} finally {
			lock.unlock();
		}
	}

	@Test
	public void testMessaging() throws Exception {

		MessageConsumer messageConsumer = messagingSession.createMessageConsumer(queue);
		MessageProducer messageProducer = messagingSession.createMessageProducer(queue);
		try {
			messageConsumer.setMessageListener(this::onMessage);

			User user = User.T.create();
			user.setName("john.doe");
			user.setId(UUID.randomUUID().toString());

			Message message = messagingSession.createMessage();
			message.setBody(user);
			message.setTimeToLive(60_000L);
			messageProducer.sendMessage(message);

			lock.lock();
			try {
				boolean success = messageReceived.await(10_000L, TimeUnit.MILLISECONDS);
				assertThat(success).isTrue();
			} finally {
				lock.unlock();
			}

			User receivedUser = (User) receivedMessages.removeLast();
			assertThat(receivedUser.getName()).isEqualTo(user.getName());
			assertThat(receivedUser.getId().toString()).isEqualTo(user.getId().toString());

			print("Received " + receivedUser);

		} finally {
			messageConsumer.close();
			messageProducer.close();
		}

	}
}
