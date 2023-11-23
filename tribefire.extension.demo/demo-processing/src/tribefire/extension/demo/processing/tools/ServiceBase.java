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
package tribefire.extension.demo.processing.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.notification.HasNotifications;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;

public abstract class ServiceBase {

	private List<Notification> notifications = new ArrayList<>();

	protected GmMetaModel findModel(PersistenceGmSession session, String modelName, boolean required) {
		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.class).where().property("name").eq(modelName).done();
		GmMetaModel model = session.query().entities(query).unique();
		if (model == null && required) {
			throw new NotFoundException("Could not find required model: " + modelName);
		}
		return model;
	}

	protected IncrementalAccess findAccess(PersistenceGmSession session, String externalId) {
		EntityQuery query = EntityQueryBuilder.from(IncrementalAccess.class).where()
				.property(IncrementalAccess.externalId).eq(externalId).done();
		return session.query().entities(query).unique();
	}

	protected AccessAspect findAspect(PersistenceGmSession session, String externalId) {
		EntityQuery query = EntityQueryBuilder.from(AccessAspect.class).where().property(AccessAspect.externalId)
				.eq(externalId).done();
		return session.query().entities(query).unique();
	}

	protected void notifyInfo(String message) {
		notifyInfo(message, null);
	}

	protected void notifyInfo(String message, Logger logger) {
		notify(message, Level.INFO, logger);
	}

	protected void notifyWarning(String message) {
		notifyWarning(message, null);
	}

	protected void notifyWarning(String message, Logger logger) {
		notify(message, Level.WARNING, logger);
	}

	protected void notifyError(String message) {
		notifyError(message, null);
	}

	protected void notifyError(String message, Logger logger) {
		notify(message, Level.ERROR, logger);
	}

	protected void notify(String message, Level level, Logger logger) {
		if (logger != null)
			logger.debug(message);

		notifications
				.addAll(Notifications.build().add().message().level(level).message(message).close().close().list());
	}

	protected void addNotifications(List<Notification> notifications) {
		this.notifications.addAll(notifications);
	}

	protected List<Notification> getNotifications() {
		return notifications;
	}

	protected <T extends HasNotifications> T createConfirmationResponse(String message, Level level,
			EntityType<T> responseType) {
		return createResponse(message, level, responseType, true);
	}

	protected <T extends HasNotifications> T createResponse(String message, EntityType<T> responseType) {
		return createResponse(message, Level.INFO, responseType);
	}

	protected <T extends HasNotifications> T createResponse(String message, Level level, EntityType<T> responseType) {
		return createResponse(message, level, responseType, false);
	}

	protected <T extends HasNotifications> T createResponse(String message, Level level, EntityType<T> responseType,
			boolean confirmationRequired) {
		T response = responseType.create();

		if (confirmationRequired) {
			response.setNotifications(Notifications.build().add().message().confirmationRequired().message(message)
					.level(level).close().close().list());
		} else {
			notify(message, level, null);
		}

		Collections.reverse(notifications);
		response.getNotifications().addAll(notifications);
		return response;

	}
}
