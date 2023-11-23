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
package com.braintribe.gwt.gme.notification.client.expert;

import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.model.notification.InternalCommand;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.processing.notification.api.NotificationFactory;

public class InternalCommandExpert implements CommandExpert<InternalCommand> {

	private Supplier<? extends NotificationFactory> notificationFactorySupplier;

	@Required
	public void setNotificationFactory(Supplier<? extends NotificationFactory> notificationFactorySupplier) {
		this.notificationFactorySupplier = notificationFactorySupplier;
	}

	@Override
	public void handleCommand(InternalCommand command) {
		Object transientObject = notificationFactorySupplier.get().getTransientObject(command);
		if (transientObject instanceof Action)
			((Action) transientObject).perform(null);
	}

}
