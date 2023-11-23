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
package com.braintribe.gwt.notification.client;

/**
 * This class transports notification within the client. It can have arbitrary data
 * as payload.
 * 
 * @author Dirk
 */
public class Notification<T> {
	private String targetKey;
	private String type;
	private T data;
	
	public Notification(String targetKey, String type, T data) {
		this.targetKey = targetKey;
		this.data = data;
		this.type = type;
	}
	
	/**
	 * Indicates the type of the data.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * The key that can used to route this notification to an endpoint with an application.
	 * @see NotificationDistributor 
	 */
	public String getTargetKey() {
		return targetKey;
	}
	
	/**
	 * The payload of the notification. That's the acutal message that is beeing transferred
	 * with this notification.
	 */
	public T getData() {
		return data;
	}
}
