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
package com.braintribe.model.processing.notification.api;

import java.util.List;

import com.braintribe.model.command.Command;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.notification.InternalCommand;
import com.braintribe.model.notification.Level;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.session.api.notifying.GenericManipulationListenerRegistry;

public interface NotificationFactory {

	/** create a new instance of an event source */
	public <NES extends NotificationEventSource> NES createEventSource(EntityType<NES> entityType);

	/** create a new instance of a command */
	public <C extends Command> C createCommand(EntityType<C> entityType);

	/** create a new notification */
	public <N extends Notification> N createNotification(EntityType<N> entityType);

	/** create a messge notification */
	public <MN extends MessageNotification> MN createNotification(EntityType<MN> entityType, Level level, String message);

	/** create a message with a command */
	public <C extends Command> MessageWithCommand createNotification(EntityType<C> entityType, Level level, String message, String name);

	/** broadcast the notification with given event source */
	public void broadcast(List<Notification> notifications, NotificationEventSource eventSource);

	/** return the manipilation registry */
	public GenericManipulationListenerRegistry listeners();

	/** return the transient object from a command */
	public Object getTransientObject(InternalCommand command);

	/** return the transient command from an object */
	public InternalCommand createTransientCommand(String name, Object object);
}
