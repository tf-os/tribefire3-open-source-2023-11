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
package com.braintribe.transport.messaging.dbm;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Topic;
import com.braintribe.transport.messaging.api.MessagingSession;

/**
 * <p>
 * Common message handler object referencing a {@link MessagingSession} and {@link Destination}.
 * 
 */
public class GmDmbMqMessageHandler {

	private GmDmbMqSession session;
	private Destination destination;
	private char destinationType;
	private String applicationId;
	private String nodeId;

	public GmDmbMqMessageHandler() {
	}

	public GmDmbMqSession getSession() {
		return session;
	}

	public void setSession(GmDmbMqSession session) {
		this.session = session;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destinationType = getDestinationType(destination);
		this.destination = destination;
	}

	public char getDestinationType() {
		return destinationType;
	}

	public char getDestinationType(Destination destinationInstance) {
		return (destinationInstance instanceof Topic) ? 't' : 'q';
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

}
