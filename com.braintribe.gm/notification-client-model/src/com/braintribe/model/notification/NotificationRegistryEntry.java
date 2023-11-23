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
package com.braintribe.model.notification;

import java.util.Date;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Root model for the notification registry.
 * 
 */

public interface NotificationRegistryEntry extends GenericEntity {

	EntityType<NotificationRegistryEntry> T = EntityTypes.T(NotificationRegistryEntry.class);

	public static final String receivedAt = "receivedAt";
	public static final String wasReadAt = "wasReadAt";
	public static final String eventSource = "eventSource";
	public static final String notifications = "notifications";

	Date getReceivedAt();
	void setReceivedAt(Date receivedAt);

	Date getWasReadAt();
	void setWasReadAt(Date wasReadAt);

	NotificationEventSource getEventSource();
	void setEventSource(NotificationEventSource eventSource);

	List<Notification> getNotifications();
	void setNotifications(List<Notification> notifications);

}
