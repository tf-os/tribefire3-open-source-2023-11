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

import java.util.Map;

import javax.jms.JMSException;

import com.braintribe.logging.Logger;
import com.braintribe.transport.messaging.api.MessagingException;
import com.ibm.mq.jms.MQDestination;
import com.ibm.mq.jms.MQQueue;
import com.ibm.mq.jms.MQTopic;

public class JmsMqMessagingUtils {

	public static boolean compareJmsDestination(MQDestination left, MQDestination right) throws MessagingException {
		String leftName = getJmsDestinationName(left);
		String rightName = getJmsDestinationName(right);
		return leftName.equalsIgnoreCase(rightName);
	}

	public static boolean compareDestination(com.braintribe.model.messaging.Destination left, com.braintribe.model.messaging.Destination right) {
		String leftName = left.getName();
		String rightName = right.getName();
		return leftName.equalsIgnoreCase(rightName);
	}
	public static String getJmsDestinationName(MQDestination dest) throws MessagingException {
		if (dest == null) {
			return null;
		}
		try {
			String destinationName = null;
			if (dest instanceof MQQueue) {
				destinationName = ((MQQueue) dest).getQueueName();
			} else if (dest instanceof MQTopic) {
				destinationName = ((MQTopic) dest).getTopicName();
			} else {
				destinationName = dest.toString();
			}
			return destinationName;
		} catch(Exception e) {
			throw new MessagingException("Could not get name of destination "+dest, e);
		}
	}
	
	public static MQDestination getJmsDestination(
			Map<com.braintribe.model.messaging.Destination,MQDestination> destinations,
			com.braintribe.model.messaging.Destination needle) {
		for (Map.Entry<com.braintribe.model.messaging.Destination,MQDestination> entry : destinations.entrySet()) {
			com.braintribe.model.messaging.Destination destination = entry.getKey();
			if (compareDestination(destination, needle)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public static void logError(Logger logger, Throwable ext, String text) {
		logger.error(text, ext);
		if ((ext != null) && (ext instanceof JMSException)) {
			logger.error("Linked Exception:", ((JMSException) ext).getLinkedException());
		}
	}
}
