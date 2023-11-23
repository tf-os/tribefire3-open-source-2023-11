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
package com.braintribe.transport.messaging.rabbitmq;

import com.braintribe.model.messaging.Destination;
import com.braintribe.model.messaging.Queue;
import com.braintribe.model.messaging.Topic;

/**
 * <p>
 * Rabbit MQ representation of a {@link Destination}, composed by a pair of Strings (exchange name and a routing key)
 *
 * <p>
 * For {@link Topic}(s), the topic name is used as the exchange name and the routing key is set to the wild-card
 * character (#).
 * 
 * <p>
 * For {@link Queue}(s), the queue name is used as the routing key and the exchange name is left blank (default
 * exchange).
 * 
 */
public class RabbitMqDestination {
	
	private String id;
	private Destination destination;
	private String exchangeName;
	private String routingKey;
	
	public RabbitMqDestination(Destination destination) {
		if (destination instanceof Topic) {
			this.exchangeName = destination.getName();
			this.routingKey = "#";
			generateId("Topic");
		} else if (destination instanceof Queue) {
			this.exchangeName = "";
			this.routingKey = destination.getName();
			generateId("Queue");
		} else {
			throw new UnsupportedOperationException("Unsupported destination type: "+destination);
		}
	}
	
	public Destination getDestination() {
		return destination;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public String getRoutingKey() {
		return routingKey;
	}
	
	private void generateId(String destinationType) {
		this.id = destinationType+"[exchangeName=\""+exchangeName+"\",routingKey=\""+routingKey+"\"]";
	}
	
	@Override
	public String toString() {
		return id;
	}

}
