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
package tribefire.extension.messaging.connector.api;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;

import tribefire.extension.messaging.model.Message;

public abstract class AbstractConsumerMessagingConnector implements ConsumerMessagingConnector {
	private static final Logger logger = Logger.getLogger(AbstractConsumerMessagingConnector.class);
	private static final Marshaller marshaller = new JsonStreamMarshaller();

	private static final GmDeserializationOptions DESERIALIZATION_OPTIONS = GmDeserializationOptions.deriveDefaults()
			.absentifyMissingProperties(false).build();

	// -----------------------------------------------------------------------
	// CONSUME
	// -----------------------------------------------------------------------

	@Override
	public List<Message> consumeMessages() {
		List<byte[]> consumedBytesList = consumerConsume();
		//@formatter:off
		List<Message> messages = consumedBytesList.stream()
				.map(this::unmarshallMessage).toList();
		//@formatter:on

		if (consumedBytesList.size() != messages.size()) {
			logger.error(() -> "Was only possible to unmarshal '" + messages.size() + "' out of '" + consumedBytesList.size()
					+ "'. Ignore and continue...");
		}

		return messages;
	}

	protected abstract List<byte[]> consumerConsume();

	@Override
	public void initConsume(Set<String> topicsToListen) {
		// nothing so far
	}

	@Override
	public void finalizeConsume() {
		// nothing so far
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	public CheckResultEntry health() {
		return actualHealth();
	}

	protected abstract CheckResultEntry actualHealth();

	// -----------------------------------------------------------------------
	// HELPERS - UNMARSHALLING
	// -----------------------------------------------------------------------

	protected Message unmarshallMessage(byte[] rawMessage) {
		try {
			return (Message) marshaller.unmarshall(new ByteArrayInputStream(rawMessage), DESERIALIZATION_OPTIONS);
		} catch (Exception e) {
			logger.error("Could not unmarshall message: '" + getMessageAsString(rawMessage) + "'");
			return null;
		}
	}

	private String getMessageAsString(byte[] rawMessage) {
		String messageAsString = "unknown";
		try {
			messageAsString = new String(rawMessage, StandardCharsets.UTF_8);
		} catch (Exception nothing) {
			// nothing
		}
		return messageAsString;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

}
