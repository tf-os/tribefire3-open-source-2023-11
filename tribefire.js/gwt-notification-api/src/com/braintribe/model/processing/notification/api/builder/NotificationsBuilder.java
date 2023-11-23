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
package com.braintribe.model.processing.notification.api.builder;

import java.util.List;
import java.util.Set;

import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.service.api.ServiceProcessorNotificationException;

public interface NotificationsBuilder {
	NotificationsBuilder context(Set<String> context);
	NotificationsBuilder context(String... context);
	NotificationBuilder add();
	List<Notification> list();
	Notify toServiceRequest();
	ServiceProcessorNotificationException toException();
	<T extends Throwable> T enrichException(T throwable);
}
