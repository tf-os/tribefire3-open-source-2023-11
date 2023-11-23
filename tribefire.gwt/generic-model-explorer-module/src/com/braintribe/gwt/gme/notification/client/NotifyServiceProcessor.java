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
package com.braintribe.gwt.gme.notification.client;

import java.util.function.Supplier;

import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.notification.NotificationBarEventSource;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.notification.api.NotificationFactory;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;

/**
 * Local {@link ServiceProcessor} implementation which simply broadcasts the {@link Notify} notifications.
 * @author michel.docouto
 *
 */
@SuppressWarnings("unusable-by-js")
public class NotifyServiceProcessor implements ServiceProcessor<Notify, Object> {
	
	private Supplier<? extends NotificationFactory> notificationFactorySupplier;
	
	/**
	 * Configures the {@link NotificationFactory} used for broadcasting a notification.
	 */
	@Required
	public void setNotificationFactorySupplier(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}

	@Override
	@SuppressWarnings("unusable-by-js")
	public Object process(ServiceRequestContext requestContext, Notify notify) {
		NotificationFactory notificationFactory = notificationFactorySupplier.get();
		notificationFactory.broadcast(notify.getNotifications(), notificationFactory.createEventSource(NotificationBarEventSource.T));
		
		return null;
	}

}
