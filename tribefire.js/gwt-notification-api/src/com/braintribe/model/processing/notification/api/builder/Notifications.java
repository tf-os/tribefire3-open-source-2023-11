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

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.processing.notification.api.NotificationAwareEvalContext;
import com.braintribe.model.processing.notification.api.builder.impl.BasicNotificationsBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public class Notifications {

	
	public static NotificationsBuilder build() {
		return new BasicNotificationsBuilder();
	}

	public static NotificationsBuilder buildWithSession(PersistenceGmSession session) {
		return new BasicNotificationsBuilder(session);
	}
	
	public static <T> NotificationAwareEvalContext<T> makeNotificationAware(EvalContext<T> other) {
		return new FailureNotificationAwareEvalContext<>(other);
	}
	
}
