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
package tribefire.extension.messaging.service.base;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;

public interface ResponseBuilder<T extends HasNotifications> {
	
	ResponseBuilder<T> ignoreCollectedNotifications();
	ResponseBuilder<T> notifications(Supplier<List<Notification>> notificationsSupplier);
	ResponseBuilder<T> notifications(Consumer<NotificationsBuilder> notificationsBuilder);
	ResponseBuilder<T> responseEnricher(Consumer<T> enricher);
	
	T build();
	
}
