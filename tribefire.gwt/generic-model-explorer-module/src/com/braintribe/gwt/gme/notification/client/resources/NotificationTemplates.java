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

import java.util.List;

import com.braintribe.gwt.gme.notification.client.NotificationAction;
import com.braintribe.gwt.gme.notification.client.NotificationViewModel;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.notification.api.NotificationEventSourceExpert;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.XTemplates.FormatterFactories;
import com.sencha.gxt.core.client.XTemplates.FormatterFactory;
import com.sencha.gxt.core.client.XTemplates.FormatterFactoryMethod;

//@formatter:off
@FormatterFactories(@FormatterFactory(factory = NotificationFormatterFactory.class, methods = {
	@FormatterFactoryMethod(name = "expanded", method = "getExpanded"),
	@FormatterFactoryMethod(name = "source", method = "getSource"),
	@FormatterFactoryMethod(name = "unread", method = "getUnread"),
	@FormatterFactoryMethod(name = "level", method = "getLevel"),
	@FormatterFactoryMethod(name = "command", method = "getCommand"),
	@FormatterFactoryMethod(name = "message", method = "getMessage")
}))
//@formatter:on
public interface NotificationTemplates extends XTemplates {

	public static final NotificationTemplates INSTANCE = GWT.create(NotificationTemplates.class);

	@XTemplate(source = "NotificationBar.xhtml")
	SafeHtml renderBar(NotificationBarStyle style, String textStyleClass, SafeHtml message, List<NotificationAction> actions);
	
	@XTemplate(source = "ConfirmationBar.xhtml")
	SafeHtml renderConfirmationBar(NotificationBarStyle style, SafeStyles confirmationIconStyle, SafeHtml message, List<NotificationAction> actions);
	
	@XTemplate(source = "MessageBar.xhtml")
	SafeHtml renderMessageBar(NotificationBarStyle style, SafeStyles confirmationIconStyle, SafeHtml message, List<NotificationAction> actions);

	@XTemplate(source = "NotificationIcon.xhtml")
	SafeHtml renderIcon(NotificationIconStyle style, SafeStyles icon, String value);

	@XTemplate(source = "NotificationView.xhtml")
	SafeHtml renderView(NotificationViewStyle style, NotificationViewModel model, NotificationEventSourceExpert<NotificationEventSource> expert);

	@XTemplate(source = "NotificationEmpty.xhtml")
	SafeHtml renderEmpty(LocalizedText messages);

}
