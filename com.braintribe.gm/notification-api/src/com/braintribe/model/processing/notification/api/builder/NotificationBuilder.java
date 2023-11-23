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

import java.util.Set;

import com.braintribe.model.command.Command;
import com.braintribe.model.notification.CommandNotification;
import com.braintribe.model.notification.MessageNotification;
import com.braintribe.model.notification.MessageWithCommand;
import com.braintribe.model.notification.Notification;

/**
 * Builder API to create a {@link Notification}. 
 */
public interface NotificationBuilder {
	
	/**
	 * Returns a {@link CommandBuilder} that can be used to create a {@link Command} for this {@link Notification}. <br /> 
	 * If called the resulting {@link Notification} will be of type {@link CommandNotification}. 
	 */
	CommandBuilder command();
	/**
	 * Returns a {@link MessageBuilder} that can be used to create a message for this {@link Notification}. <br />
	 * If called the resulting {@link Notification} will be of type {@link MessageNotification}.
	 */
	MessageBuilder message();
	/**
	 * Sets the context of the {@link Notification}. Must be called before close()
	 */
	NotificationBuilder context(Set<String> context);
	NotificationBuilder context(String... context);
	/**
	 * Based on the settings before either a {@link CommandNotification}, a {@link MessageNotification} or a {@link MessageWithCommand} is created.
	 */
	NotificationsBuilder  close();
	
	
}
