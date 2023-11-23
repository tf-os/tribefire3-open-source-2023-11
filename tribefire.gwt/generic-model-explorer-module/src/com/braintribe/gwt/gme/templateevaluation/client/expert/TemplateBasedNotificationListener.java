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
package com.braintribe.gwt.gme.templateevaluation.client.expert;

import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import com.braintribe.cfg.Required;
//import com.braintribe.gwt.gme.notification.client.CommandListener;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.notification.NotificationRegistry;
import com.braintribe.model.notification.NotificationRegistryEntry;
import com.braintribe.model.processing.notification.api.NotificationListener;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.processing.async.api.AsyncCallback;

public class TemplateBasedNotificationListener implements NotificationListener {

	private Supplier<? extends ManagedGmSession> gmManagedSessionSupplier;

	@Required
	public void setGmSession(Supplier<? extends ManagedGmSession> gmManagedSessionSupplier) {
		this.gmManagedSessionSupplier = gmManagedSessionSupplier;
	}

	@Override
	public void handleNotifications(List<Notification> notifications) {
		handleNotifications(notifications, null);
	}

	@Override
	public void handleNotifications(final List<Notification> notifications, final NotificationEventSource eventSource) {
		if (notifications == null)
			return;
		
		ManagedGmSession gmManagedSession = gmManagedSessionSupplier.get();
		gmManagedSession.query().entity(NotificationRegistry.T, NotificationRegistry.INSTANCE).require( //
				AsyncCallback.of(registry -> {
					NotificationRegistryEntry entry = gmManagedSession.create(NotificationRegistryEntry.T);
					entry.setId((long) entry.hashCode());
					entry.setReceivedAt(new Date());
					entry.setEventSource(eventSource);
					entry.setNotifications(notifications);
					registry.getEntries().add(entry);
				}, e -> ErrorDialog.show(e.getMessage(), e)));
	}

}
