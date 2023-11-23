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
package tribefire.extension.opentracing.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;

import tribefire.extension.opentracing.model.service.OpentracingRequest;

/**
 * Base for all OPENTRACING service processors
 * 
 *
 */
public abstract class AbstractOpentracingServiceProcessor {

	protected <T extends HasNotifications> ResponseBuilder<T> responseBuilder(EntityType<T> responseType, OpentracingRequest request) {

		return new ResponseBuilder<T>() {
			private List<Notification> localNotifications = new ArrayList<>();
			private boolean ignoreCollectedNotifications = false;
			private Consumer<T> enricher;
			private NotificationsBuilder notificationsBuilder = null;
			private List<Notification> notifications = new ArrayList<>();

			@Override
			public ResponseBuilder<T> notifications(Supplier<List<Notification>> notificationsSupplier) {
				notifications = notificationsSupplier.get();
				return this;
			}
			@Override
			public ResponseBuilder<T> notifications(Consumer<NotificationsBuilder> consumer) {
				this.notificationsBuilder = Notifications.build();
				consumer.accept(notificationsBuilder);
				return this;
			}

			@Override
			public ResponseBuilder<T> ignoreCollectedNotifications() {
				this.ignoreCollectedNotifications = true;
				return this;
			}

			@Override
			public ResponseBuilder<T> responseEnricher(Consumer<T> enricher) {
				this.enricher = enricher;
				return this;
			}

			@Override
			public T build() {

				T response = responseType.create();
				if (enricher != null) {
					this.enricher.accept(response);
				}
				if (request.getSendNotifications()) {
					response.setNotifications(localNotifications);
					if (!ignoreCollectedNotifications) {

						if (notificationsBuilder != null) {
							notifications.addAll(notificationsBuilder.list());
						}

						Collections.reverse(notifications);
						response.getNotifications().addAll(notifications);
					}
				}
				return response;
			}
		};

	}

	protected <T extends HasNotifications> T prepareSimpleNotification(EntityType<T> responseEntityType, OpentracingRequest request, Level level,
			String msg) {

		//@formatter:off
		T result = responseBuilder(responseEntityType, request)
				.notifications(builder -> 
					builder	
					.add()
						.message()
							.level(level)
							.message(msg)
						.close()
					.close()
				).build();
		//@formatter:on
		return result;
	}

}
