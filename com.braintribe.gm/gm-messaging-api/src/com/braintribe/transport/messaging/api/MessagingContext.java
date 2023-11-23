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
package com.braintribe.transport.messaging.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.messaging.Message;

/**
 * <p>
 * This context allows clients obtaining access to a Messaging implementation to provide API based configuration about
 * their environment, which can be useful for the components instances provided through a
 * {@link MessagingConnectionProvider}, such as {@link MessagingConnection}, {@link MessagingSession},
 * {@link MessageProducer} and {@link MessageConsumer}.
 * 
 */
public class MessagingContext {

	private Marshaller marshaller;
	private String applicationId;
	private String nodeId;
	private Function<Message, Message> inboundEnricher = this::enrichInboundDefault;
	private Function<Message, Message> outboundEnricher = this::enrichOutboundDefault;

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	/**
	 * <p>
	 * Gets the {@link MessageMarshaller} to be used by the {@link MessageProducer}(s) and {@link MessageConsumer}(s)
	 * created by a {@link Messaging} implementation.
	 * 
	 * <p>
	 * {@link MessageProducer}(s) may use this {@code MessageMarshaller} to marshall outgoing messages based on the mime
	 * type given by {@link MessageMarshaller#getOutgoingMessagesType()}, whereas {@link MessageConsumer}(s) may use
	 * this {@code MessageMarshaller} to unmarshall incoming messages, based on implementation specific message meta
	 * data.
	 * 
	 * @return The {@link MessageMarshaller} to be used by the {@link MessageProducer}(s) and {@link MessageConsumer}
	 *         (s) created by the {@link Messaging} implementation which received this context.
	 */
//	public MessageMarshaller getMessageMarshaller() {
//		return messageMarshaller;
//	}
	
	
	/**
	 * <p>
	 * Marshalls the given {@link Message}.
	 * 
	 * @param message
	 *            The {@link Message} to be marshalled.
	 * @return The {@link Message} marshalled bytes.
	 * @throws MessagingException
	 *             If any messaging specific configuration or state prevents the {@link Message} to be marshalled.
	 * @throws MarshallException
	 *             If the designated {@link Marshaller} fails to marshall the {@link Message}.
	 */
	public byte[] marshallMessage(Message message) throws MessagingException, MarshallException {
		return marshal(message);
	}
	
	public byte[] marshal(GenericEntity message) throws MessagingException, MarshallException {
		
		if (message == null) {
			throw new MessagingException("Unable to marshall null Message");
		}

		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			marshaller.marshall(baos, message);

			return baos.toByteArray();
			
		} catch (Exception e) { // As some marshaller impls throw java.lang.Error(s) when the input is unexpected.
			throw new MarshallException("Unable to marshall message: [ " + message + " ]: " + e.getMessage(), e);
		}
	}

	/**
	 * <p>
	 * Unmarshalls the given message bytes.
	 * 
	 * @return The unmarshalled {@link Message}.
	 * @throws MessagingException
	 *             If any messaging specific configuration or state prevents the unmarshalling.
	 * @throws MarshallException
	 *             If the designated {@link Marshaller} fails to unmarshall the given message bytes.
	 */
	public Message unmarshallMessage(byte[] messageBytes) throws MessagingException, MarshallException {

		Object message = this.unmarshal(messageBytes);

		if (!(message instanceof Message)) {
			throw new IllegalStateException("Unmarshalling message payload resulted in an entity of unexpected type "
					+ message.getClass().getName() + ", whereas an instance of: " + Message.class.getName() + " was expected");
		}

		return (Message) message;

	}
	
	public <T extends GenericEntity> T unmarshal(byte[] messageBytes) throws MessagingException, MarshallException {

		if (messageBytes == null || messageBytes.length < 1) {
			throw new MessagingException("Unable to unmarshall: empty message payload");
		}


		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(messageBytes);

			Object message = marshaller.unmarshall(bais);

			if (message == null) {
				throw new IllegalStateException("Unmarshalling message payload resulted in a null object");
			}

			return (T) message;

		} catch (Exception e) { // As some marshaller impls throw java.lang.Error(s) when the input is unexpected.
			throw new MarshallException("Failed to unmarshall message payload: " + e.getMessage(), e);
		}
		catch (Error e) {
			throw e;
		}
	}

	/**
	 * <p>
	 * Gets the application id. It is meant to assist messaging implementations to bypass the processing of messages not
	 * directly addressed to the current application.
	 * 
	 * @return The id of the application establishing messsaging connections.
	 */
	public String getApplicationId() {
		return applicationId;
	}

	/**
	 * <p>
	 * Sets the application node instance id. It is meant to assist messaging implementations to bypass the processing
	 * of messages not directly addressed to the current application or node.
	 * 
	 * @param applicationId
	 *            The id of the application establishing messsaging connections.
	 */
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	/**
	 * <p>
	 * Gets the node id. It is meant to assist messaging implementations to bypass the processing of messages not
	 * directly addressed to the current node of the application.
	 * 
	 * @return The id of the application node establishing messsaging connections.
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * <p>
	 * Sets the node id. It is meant to assist messaging implementations to bypass the processing of messages not
	 * directly addressed to the current node of the application.
	 * 
	 * @param nodeId
	 *            The id of the application node establishing messsaging connections.
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public void setInboundEnricher(Function<Message, Message> inboundEnricher) {
		if (inboundEnricher == null) {
			inboundEnricher = (m) -> m;
		}
		this.inboundEnricher = inboundEnricher;
	}

	public void setOutboundEnricher(Function<Message, Message> outboundEnricher) {
		if (outboundEnricher == null) {
			outboundEnricher = (m) -> m;
		}
		this.outboundEnricher = outboundEnricher;
	}

	public Message enrichInbound(Message message) {
		return inboundEnricher.apply(message);
	}

	public Message enrichOutbound(Message message) {
		return outboundEnricher.apply(message);
	}

	protected Message enrichInboundDefault(Message message) {

		Map<String, Object> properties = message.getProperties();

		if (applicationId != null) {
			properties.put(MessageProperties.consumerAppId.getName(), applicationId);
		} else {
			properties.remove(MessageProperties.consumerAppId.getName());
		}

		if (nodeId != null) {
			properties.put(MessageProperties.consumerNodeId.getName(), nodeId);
		} else {
			properties.remove(MessageProperties.consumerNodeId.getName());
		}

		return message;

	}

	protected Message enrichOutboundDefault(Message message) {

		Map<String, Object> properties = message.getProperties();

		if (applicationId != null) {
			properties.put(MessageProperties.producerAppId.getName(), applicationId);
		} else {
			properties.remove(MessageProperties.producerAppId.getName());
		}

		if (nodeId != null) {
			properties.put(MessageProperties.producerNodeId.getName(), nodeId);
		} else {
			properties.remove(MessageProperties.producerNodeId.getName());
		}

		return message;

	}

}
