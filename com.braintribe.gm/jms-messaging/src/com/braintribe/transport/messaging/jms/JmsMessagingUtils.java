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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

import com.braintribe.logging.Logger;
import com.braintribe.transport.messaging.api.MessagingException;

public class JmsMessagingUtils {

	public static boolean compareJmsDestination(Destination left, Destination right) throws MessagingException {
		String leftName = getJmsDestinationName(left);
		String rightName = getJmsDestinationName(right);
		return leftName.equalsIgnoreCase(rightName);
	}

	public static boolean compareDestination(com.braintribe.model.messaging.Destination left, com.braintribe.model.messaging.Destination right) {
		String leftName = left.getName();
		String rightName = right.getName();
		return leftName.equalsIgnoreCase(rightName);
	}
	public static String getJmsDestinationName(Destination dest) throws MessagingException {
		if (dest == null) {
			return null;
		}
		try {
			String destinationName = null;
			if (dest instanceof Queue) {
				destinationName = ((Queue) dest).getQueueName();
			} else if (dest instanceof Topic) {
				destinationName = ((Topic) dest).getTopicName();
			} else {
				destinationName = dest.toString();
			}
			return destinationName;
		} catch(Exception e) {
			throw new MessagingException("Could not get name of destination "+dest, e);
		}
	}
	
	public static Destination getJmsDestination(
			Map<com.braintribe.model.messaging.Destination,javax.jms.Destination> destinations,
			com.braintribe.model.messaging.Destination needle) {
		for (Map.Entry<com.braintribe.model.messaging.Destination,javax.jms.Destination> entry : destinations.entrySet()) {
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
