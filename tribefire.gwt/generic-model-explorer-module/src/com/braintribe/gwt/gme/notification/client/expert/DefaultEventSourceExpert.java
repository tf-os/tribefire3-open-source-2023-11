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

import com.braintribe.cfg.Configurable;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.notification.api.NotificationEventSourceExpert;

public class DefaultEventSourceExpert implements NotificationEventSourceExpert<NotificationEventSource> {

	private String displayName;

	@Configurable
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String render(NotificationEventSource eventSource) {
		return displayName != null ? displayName : eventSource.getId();
	}

}
