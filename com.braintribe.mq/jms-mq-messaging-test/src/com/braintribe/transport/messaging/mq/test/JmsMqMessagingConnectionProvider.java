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
package com.braintribe.transport.messaging.mq.test;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.model.messaging.expert.Messaging;
import com.braintribe.model.messaging.jms.JmsMqConnection;
import com.braintribe.transport.messaging.api.MessagingConnection;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.transport.messaging.jms.JmsMqMessaging;
import com.braintribe.transport.messaging.mq.test.config.TestConfiguration;

public class JmsMqMessagingConnectionProvider {

	public static final JmsMqMessagingConnectionProvider instance = new JmsMqMessagingConnectionProvider();

	private MessagingConnectionProvider<? extends MessagingConnection> messagingConnectionProvider;

	private JmsMqMessagingConnectionProvider() {
		messagingConnectionProvider = getMessagingConnectionProvider();
	}

	public MessagingConnectionProvider<? extends MessagingConnection> get() {
		return messagingConnectionProvider;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected com.braintribe.transport.messaging.api.MessagingConnectionProvider<? extends MessagingConnection> getMessagingConnectionProvider() {

		com.braintribe.model.messaging.expert.Messaging denotationType = queryDenotationType("cortex");

		com.braintribe.transport.messaging.api.Messaging messaging = getExpertByDenotationType(denotationType);

		return messaging.createConnectionProvider(denotationType, getMessagingContext());

	}

	@SuppressWarnings("unused")
	protected com.braintribe.model.messaging.expert.Messaging queryDenotationType(String name) {

		try {
			TestConfiguration testConfiguration = ConfigurationHolder.configurator.getContext().getBean("testConfiguration", TestConfiguration.class);
			if (testConfiguration == null) {
				throw new RuntimeException("Could not find bean 'testConfiguration'");
			}

			JmsMqConnection provider = JmsMqConnection.T.create();
			provider.setHost(testConfiguration.getHost());
			provider.setUsername(testConfiguration.getUsername());
			provider.setPassword(testConfiguration.getPassword());
			provider.setChannel(testConfiguration.getChannel());
			provider.setQueueManager(testConfiguration.getQueueManager());

			return provider;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected com.braintribe.transport.messaging.api.Messaging<? extends Messaging> getExpertByDenotationType(
			com.braintribe.model.messaging.expert.Messaging denotationType) {

		if (denotationType instanceof JmsMqConnection) {
			JmsMqMessaging messaging = new JmsMqMessaging();
			return messaging;
		}

		return null;

	}

	protected MessagingContext getMessagingContext() {
		MessagingContext context = new MessagingContext();
		context.setMarshaller(getMessageMarshaller());
		return context;
	}

	protected Marshaller getMessageMarshaller() {
		return new Bin2Marshaller();
	}

}
