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

import com.braintribe.model.messaging.Destination;

public class JmsMessageHandler {

	private JmsSession session;
	private Destination destination;
	private javax.jms.Destination jmsDestination;
	
	public JmsSession getSession() {
		return session;
	}
	public void setSession(JmsSession session) {
		this.session = session;
	}
	public Destination getDestination() {
		return destination;
	}
	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	public javax.jms.Destination getJmsDestination() {
		return jmsDestination;
	}
	public void setJmsDestination(javax.jms.Destination jmsDestination) {
		this.jmsDestination = jmsDestination;
	}

}
