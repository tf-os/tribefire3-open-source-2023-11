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
package com.braintribe.transport.messaging.etcd;

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Message;
import com.braintribe.transport.messaging.api.MessagingContext;
import com.braintribe.utils.StringTools;

public abstract class EtcdAbstractMessageHandler {
	
	public final static String propertyPrefix = "tf_property_";
	
	private String applicationId;
	private String nodeId;
	private Destination destination;
	private MessagingContext messagingContext;
	private EtcdMessagingSession session;
	protected EtcdConnection connection;

	private static final String contentType = "application/octet-stream";
	
	public static String PROP_CONTENTTYPE   = propertyPrefix+"ContentType";
	public static String PROP_MESSAGEID     = propertyPrefix+"MessageId";
	public static String PROP_PRIORITY      = propertyPrefix+"Priority";
	public static String PROP_EXPIRATION    = propertyPrefix+"Expiration";
	public static String PROP_CORRELATIONID = propertyPrefix+"CorrelationId";
	public static String PROP_REPLYTO       = propertyPrefix+"ReplyTo";

	public EtcdMessagingSession getSession() {
		return session;
	}

	public void setSession(EtcdMessagingSession session) {
		this.session = session;
	}

	public MessagingContext getMessagingContext() {
		return messagingContext;
	}
	
	public void setMessagingContext(MessagingContext messagingContext) {
		this.messagingContext = messagingContext;
	}

	public Destination getDestination() {
		return destination;
	}
	
	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	
	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	protected Map<String,String> readMessageProperties(String etcdMessage) {
		Map<String,String> result = new HashMap<>();
		Map<String, String> map = StringTools.decodeStringMapFromString(etcdMessage);

		map.entrySet().forEach(e -> {
			String k = e.getKey();
			if (k.startsWith(propertyPrefix)) {
				String v = e.getValue();
				String key = k.substring(propertyPrefix.length());
				result.put(key, v);
				result.put(k, v);
			}
		}); 
		return result;
	}

	protected Map<String,String> createMessageProperties(Message message, String mimeType) {
		
		if (mimeType == null || mimeType.trim().isEmpty()) {
			mimeType = contentType;
		}
		
		Map<String,String> result = new HashMap<>();

		result.put(PROP_CONTENTTYPE, mimeType);
		if (message.getMessageId() != null) {
			result.put(PROP_MESSAGEID, message.getMessageId());
		}
		
		if (message.getPriority() != null) {
			result.put(PROP_PRIORITY, ""+message.getPriority());
		}
		
		if (message.getTimeToLive() != null && message.getTimeToLive() > 0L) {
			result.put(PROP_EXPIRATION, Long.toString(message.getTimeToLive()));
		}
		
		if (message.getCorrelationId() != null) {
			result.put(PROP_CORRELATIONID, message.getCorrelationId());
		}
		
		if (message.getReplyTo() != null) {
			result.put(PROP_REPLYTO, message.getReplyTo().getName());
		}

		Map<String, Object> headers = message.getHeaders();
		if (headers != null && !headers.isEmpty()) {
			headers.forEach((k,v) -> result.put(propertyPrefix+k, ""+v));
		}

		Map<String, Object> props = message.getProperties();
		if (props != null && !props.isEmpty()) {
			props.forEach((k,v) -> result.put(propertyPrefix+k, ""+v));
		}

		return result;
	}
	
	public void setConnection(EtcdConnection connection) {
		this.connection = connection;
	}


}
