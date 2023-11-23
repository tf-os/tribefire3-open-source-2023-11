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
package com.braintribe.gwt.gme.notification.client.resources;

import com.google.gwt.resources.client.CssResource;

public interface NotificationViewStyle extends CssResource {

	@ClassName("notificationView")
	String parent();

	@ClassName("notificationView-group")
	String group();

	@ClassName("notificationView-header")
	String head();

	@ClassName("notificationView-ruler")
	String ruler();

	@ClassName("notificationView-source")
	String source();

	@ClassName("notificationView-caption")
	String caption();

	@ClassName("notificationView-unread")
	String unread();

	@ClassName("notificationView-messages")
	String body();

	@ClassName("notificationView-item")
	String item();

	@ClassName("notificationView-command")
	String command();

	@ClassName("notificationView-item-text")
	String itemText();

	@ClassName("notificationView-item-sel")
	String itemSel();

	@ClassName("notificationView-collapsed")
	String collapsed();

	@ClassName("notificationView-expanded")
	String expanded();

	@ClassName("notificationView-info")
	String levelInfo();

	@ClassName("notificationView-warning")
	String levelWarning();

	@ClassName("notificationView-error")
	String levelError();
	
	@ClassName("notificationView-success")
	String levelSuccess();

}
