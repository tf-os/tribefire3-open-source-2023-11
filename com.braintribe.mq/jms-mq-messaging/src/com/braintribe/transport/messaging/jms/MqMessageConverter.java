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
package com.braintribe.transport.messaging.jms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.apache.commons.codec.binary.Hex;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessagingException;
import com.ibm.mq.jms.MQDestination;

public class MqMessageConverter {

	public static String contentType = "application/octet-stream";
	public static String contentEncoding = "UTF-8";

	public final static String headerPrefix = "tf_header_";
	public final static String propertyPrefix = "tf_property_";
	public final static String messageIdKey = "tf_messageId";

	public final static String base64EncodedSerializedPrefix = "{SerializedBase64}";
	public final static String hexEncodedSerializedPrefix = "_Hex_";

	private static final Logger log = Logger.getLogger(MqMessageConverter.class);

	public static javax.jms.Message toJmsMessage(JmsMqSession session, Session jmsSession, MqMessageHandler messageHandler, Message message)
			throws MessagingException {

		try {
			Marshaller messageMarshaller = session.getConnection().getMessageMarshaller();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			messageMarshaller.marshall(baos, message);
			byte[] messageBytes = baos.toByteArray();
			BytesMessage jmsMessage = jmsSession.createBytesMessage();
			jmsMessage.writeBytes(messageBytes);

			assignProperties(session, messageHandler, jmsMessage, message, "application/octet-stream");

			return jmsMessage;
		} catch (Exception e) {
			throw new MessagingException("Failed to publish message: " + e.getMessage(), e);
		}

	}

	public static void assignProperties(@SuppressWarnings("unused") JmsMqSession session, MqMessageHandler messageHandler, BytesMessage jmsMessage,
			Message message, String mimeType) throws MessagingException {

		try {
			if (mimeType == null || mimeType.trim().isEmpty()) {
				mimeType = contentType;
			}
			jmsMessage.setStringProperty("contentType", mimeType);
			jmsMessage.setStringProperty("contentEncoding", contentEncoding);

			if (message.getPersistent()) {
				jmsMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
			} else {
				jmsMessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
			}

			if (message.getMessageId() != null) {
				jmsMessage.setStringProperty(messageIdKey, message.getMessageId());
			}

			if (message.getPriority() != null) {
				jmsMessage.setJMSPriority(message.getPriority());
			}

			if (message.getCorrelationId() != null) {
				jmsMessage.setJMSCorrelationID(message.getCorrelationId());
			}

			if (message.getHeaders() != null && !message.getHeaders().isEmpty()) {
				for (Map.Entry<String, Object> entry : message.getHeaders().entrySet()) {
					String key = entry.getKey();
					String encodedKey = encodePropertyName(key);
					Object value = entry.getValue();
					Object encodedValue = encodeParameterValue(value);
					jmsMessage.setObjectProperty(headerPrefix + encodedKey, encodedValue);
				}
			}

			Map<String, Object> properties = message.getProperties();
			if ((properties != null) && (!properties.isEmpty())) {
				for (Map.Entry<String, Object> entry : properties.entrySet()) {
					String key = entry.getKey();
					String encodedKey = encodePropertyName(key);
					Object value = entry.getValue();
					Object encodedValue = encodeParameterValue(value);
					jmsMessage.setObjectProperty(propertyPrefix + encodedKey, encodedValue);
				}
			}
		} catch (Exception e) {
			throw new MessagingException("Could not assign JMS properties.", e);
		}

		Destination replyTo = message.getReplyTo();
		try {
			if (replyTo != null) {
				javax.jms.Destination jmsReplyDestination = messageHandler.createJmsDestination(replyTo);
				jmsMessage.setJMSReplyTo(jmsReplyDestination);
			}
		} catch (Exception e) {
			throw new MessagingException("Could not set replyTo " + replyTo, e);
		}

	}

	protected static Object encodeParameterValue(Object value) throws MessagingException {
		if (value == null) {
			return null;
		}
		// boolean, byte, short, int, long, float, double, and String

		Class<?> c = value.getClass();
		if (c.equals(Integer.class) || c.equals(int.class)) {
			return value;
		} else if (c.equals(Boolean.class) || c.equals(boolean.class)) {
			return value;
		} else if (c.equals(Short.class) || c.equals(short.class)) {
			return value;
		} else if (c.equals(Long.class) || c.equals(long.class)) {
			return value;
		} else if (c.equals(Double.class) || c.equals(double.class)) {
			return value;
		} else if (c.equals(String.class)) {
			return value;
		} else {
			throw new MessagingException("Could not serialize " + value);
		}
	}

	protected static Object decodeParameterValue(Object value) throws MessagingException {
		if (value == null) {
			return null;
		}
		// boolean, byte, short, int, long, float, double, and String

		Class<?> c = value.getClass();
		if (c.equals(Integer.class) || c.equals(int.class)) {
			return value;
		} else if (c.equals(Boolean.class) || c.equals(boolean.class)) {
			return value;
		} else if (c.equals(Short.class) || c.equals(short.class)) {
			return value;
		} else if (c.equals(Long.class) || c.equals(long.class)) {
			return value;
		} else if (c.equals(Double.class) || c.equals(double.class)) {
			return value;
		} else if (c.equals(String.class)) {
			return value;
		} else {
			throw new MessagingException("Could not deserialize " + value);
		}
	}
	public static Message toMessage(JmsMqSession session, javax.jms.Message jmsMessage) throws MessagingException {
		if (!(jmsMessage instanceof BytesMessage)) {
			throw new MessagingException("Not accepting message " + jmsMessage + " because of wrong type.");
		}
		BytesMessage bytesMessage = (BytesMessage) jmsMessage;

		try {
			int length = (int) bytesMessage.getBodyLength();
			byte[] messageBody = new byte[length];
			bytesMessage.readBytes(messageBody, length);

			String mimeType = bytesMessage.getStringProperty("contentType");
			if (mimeType == null) {
				mimeType = contentType;
			}

			Marshaller messageMarshaller = session.getConnection().getMessageMarshaller();
			Message message = null;

			try {
				ByteArrayInputStream bain = new ByteArrayInputStream(messageBody);
				message = (Message) messageMarshaller.unmarshall(bain);
			} catch (MarshallException e) {
				log.error("Failed to extract " + Message.class.getName() + " from the given payload due to: " + e.getMessage(), e);
				return null;
			}

			message.setCorrelationId(jmsMessage.getJMSCorrelationID());

			MQDestination jmsDestination = (MQDestination) jmsMessage.getJMSDestination();
			Destination destination = null;
			if (jmsDestination != null) {
				String destinationName = JmsMqMessagingUtils.getJmsDestinationName(jmsDestination);
				if (jmsDestination instanceof javax.jms.Queue) {
					destination = Queue.T.create();
				} else if (jmsDestination instanceof javax.jms.Topic) {
					destination = Topic.T.create();
				} else {
					throw new MessagingException("Unsupported JMS destination type " + jmsDestination);
				}
				if (destination != null) {
					destination.setName(destinationName);
					message.setDestination(destination);
				}
			}

			message.setExpiration(jmsMessage.getJMSExpiration());

			String customMessageId = jmsMessage.getStringProperty(messageIdKey);
			if (customMessageId == null) {
				customMessageId = jmsMessage.getJMSMessageID();
			}
			message.setMessageId(customMessageId);

			int deliveryMode = jmsMessage.getJMSDeliveryMode();
			if (deliveryMode == DeliveryMode.PERSISTENT) {
				message.setPersistent(true);
			} else {
				message.setPersistent(false);
			}

			message.setPriority(jmsMessage.getJMSPriority());

			message.setExpiration(jmsMessage.getJMSExpiration());

			@SuppressWarnings("rawtypes")
			Enumeration e = jmsMessage.getPropertyNames();
			if (e != null) {
				Map<String, Object> propertiesMap = new HashMap<String, Object>();
				Map<String, Object> headersMap = new HashMap<String, Object>();

				for (; e.hasMoreElements();) {
					String name = (String) e.nextElement();
					if (name != null) {
						Object value = jmsMessage.getObjectProperty(name);
						if (value != null) {

							Object decodedValue = decodeParameterValue(value);

							if (name.startsWith(headerPrefix)) {
								String encodedName = name.substring(headerPrefix.length());
								name = decodePropertyName(encodedName);
								headersMap.put(name, decodedValue);
							} else if (name.startsWith(propertyPrefix)) {
								String encodedName = name.substring(propertyPrefix.length());
								name = decodePropertyName(encodedName);
								propertiesMap.put(name, decodedValue);
							} else {
								propertiesMap.put(name, decodedValue);
							}
						}
					}
				}
				message.setProperties(propertiesMap);
				message.setHeaders(headersMap);
			}

			return message;

		} catch (Exception e) {
			throw new MessagingException("Could not decode JMS message " + jmsMessage, e);
		}
	}

	public static String decodePropertyName(String name) throws MessagingException {
		if (name == null) {
			throw new MessagingException("The encoded name of an property must not be null.");
		}
		if (!name.startsWith(hexEncodedSerializedPrefix)) {
			return name;
		}
		String encodedName = name.substring(hexEncodedSerializedPrefix.length());
		String decodedName = null;
		try {
			decodedName = new String(Hex.decodeHex(encodedName.toCharArray()), "UTF-8");
		} catch (Exception e) {
			throw new MessagingException("Could not decode name " + name, e);
		}
		return decodedName;
	}

	public static String encodePropertyName(String name) throws MessagingException {
		if (name == null) {
			throw new MessagingException("The name of a property must not be null.");
		}
		if (isValidJavaIdentifier(name)) {
			return name;
		}
		String encodedName = null;
		try {
			encodedName = Hex.encodeHexString(name.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new MessagingException("Could not hex-encode property name " + name, e);
		}
		return hexEncodedSerializedPrefix + encodedName;
	}

	public static boolean isValidJavaIdentifier(String s) {
		// an empty or null string cannot be a valid identifier
		if (s == null || s.length() == 0) {
			return false;
		}

		char[] c = s.toCharArray();
		if (!Character.isJavaIdentifierStart(c[0])) {
			return false;
		}

		for (int i = 1; i < c.length; i++) {
			if (!Character.isJavaIdentifierPart(c[i])) {
				return false;
			}
		}

		return true;
	}
}
