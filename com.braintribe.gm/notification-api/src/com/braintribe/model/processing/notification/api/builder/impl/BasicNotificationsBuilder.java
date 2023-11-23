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
package com.braintribe.model.processing.notification.api.builder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.Notify;
import com.braintribe.model.processing.notification.api.builder.NotificationBuilder;
import com.braintribe.model.processing.notification.api.builder.NotificationsBuilder;
import com.braintribe.model.processing.service.api.ServiceProcessorNotificationException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class BasicNotificationsBuilder implements NotificationsBuilder {

	private List<Notification> notifications = new ArrayList<Notification>();
	private Function<EntityType<? extends GenericEntity>, GenericEntity> entityFactory;
	private Set<String> context;

	public BasicNotificationsBuilder() {
		this.entityFactory = 
				new Function<EntityType<? extends GenericEntity>, GenericEntity>() {
					@Override
					public GenericEntity apply(EntityType<? extends GenericEntity> type) throws RuntimeException {
						return type.create();
					}
				};
	}
			
	public BasicNotificationsBuilder(PersistenceGmSession session) {
		this.entityFactory =
				new Function<EntityType<? extends GenericEntity>, GenericEntity>() {
					@Override
					public GenericEntity apply(EntityType<? extends GenericEntity> type) throws RuntimeException {
						try {
							return session.create(type);
						} catch (Exception e) {
							throw new RuntimeException("Could not create new instance of: "+type,e);
						}
					}
				};
	}
	
	@Override
	public NotificationBuilder add() {
		return new BasicNotificationBuilder(this, entityFactory, new Consumer<Notification>() {
			@Override
			public void accept(Notification object) throws RuntimeException {
				notifications.add(object);
				setContext(object);
			}
		});
	}	
	
	@Override
	public List<Notification> list() {
		return notifications;
	}

	@Override
	public Notify toServiceRequest() {
		Notify notify = Notify.T.create();
		notify.setNotifications(list());
		return notify;
	}

	@Override
	public ServiceProcessorNotificationException toException() {
		return new ServiceProcessorNotificationException(toServiceRequest());
	}

	@Override
	public <T extends Throwable> T enrichException(T throwable) {
		throwable.addSuppressed(toException());
		return throwable;
	}

	@Override
	public NotificationsBuilder context(Set<String> context) {
		this.context = context;
		return this;
	}

	@Override
	public NotificationsBuilder context(String... context) {
		Set<String> set = new HashSet<>();
		Collections.addAll(set, context);		
		return context(set);
	}

	private void setContext(Notification notification) {
		notification.setContext(context);
	}
	
}
