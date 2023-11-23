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

/**
 * <p>
 * The standard internal message property keys used by GenericModel-based messaging.
 * 
 */
public enum MessageProperties {

	addreseeAppId("gmMessagingAddreseeAppId"),
	addreseeNodeId("gmMessagingAddreseeNodeId"),
	producerAppId("gmMessagingProducerAppId"),
	producerNodeId("gmMessagingProducerNodeId"),
	consumerAppId("gmMessagingConsumerAppId"),
	consumerNodeId("gmMessagingConsumerNodeId");

	private String propertyName;
	
	private MessageProperties(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public String getName() {
		return propertyName;
	}

}
