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

import java.util.Comparator;

import com.braintribe.model.messaging.Message;

/**
 * <p>
 * A basic {@link Comparator} for {@link Message}(s).
 * 
 * <p>
 * Allows GM messaging implementations to enable {@link Message} prioritization.
 * 
 * <p>
 * The greater {@link Message#getPriority()} is, higher is the priority.
 * 
 */
public class MessagePriorityComparator implements Comparator<Message> {

	private static final Integer defaultPriority = Integer.valueOf(4);

	@Override
	public int compare(Message message1, Message message2) {
		return priority(message2).compareTo(priority(message1));
	}

	private static Integer priority(Message message) {
		return (message.getPriority() != null) ? message.getPriority() : defaultPriority;
	}

}
