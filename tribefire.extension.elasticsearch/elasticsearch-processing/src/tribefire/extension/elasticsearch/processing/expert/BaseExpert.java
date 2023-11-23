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
package tribefire.extension.elasticsearch.processing.expert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import tribefire.extension.elasticsearch.model.api.ElasticsearchRequest;
import tribefire.extension.elasticsearch.model.api.ElasticsearchResponse;
import tribefire.extension.elasticsearch.processing.expert.base.ResponseBuilder;

public abstract class BaseExpert<S extends ElasticsearchRequest, T extends ElasticsearchResponse> implements Expert<T> {

	protected S request;

	protected ElasticsearchClient client;

	protected String indexName;

	// ***************************************************************************************************
	// Configuration
	// ***************************************************************************************************

	public void setRequest(S request) {
		this.request = request;
	}

	public void setClient(ElasticsearchClient client) {
		this.client = client;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	// ***************************************************************************************************
	// Generic factory method to create and configure concrete instances
	// ***************************************************************************************************

	protected static <E extends BaseExpert<? extends ElasticsearchRequest, ? extends ElasticsearchResponse>> E createExpert(Supplier<E> factory,
			Consumer<E> configurer) {
		E expert = factory.get();
		configurer.accept(expert);
		return expert;
	}

	// -----------------------------------------------------------------------
	// FOR NOTIFICATIONS
	// -----------------------------------------------------------------------

	protected <T extends HasNotifications> ResponseBuilder<T> responseBuilder(EntityType<T> responseType, ElasticsearchRequest request) {

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

}
