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


import com.braintribe.gwt.gme.notification.client.MessageModel;
import com.braintribe.gwt.gme.notification.client.NotificationViewModel;
import com.braintribe.gwt.gmview.util.client.GMEUtil;
import com.braintribe.model.notification.NotificationEventSource;
import com.braintribe.model.processing.notification.api.NotificationEventSourceExpert;
import com.sencha.gxt.core.client.XTemplates.Formatter;

public final class NotificationFormatterFactory {

	/* ----- NotificationViewModel Formatter ----- */

	public static Formatter<NotificationViewModel> getExpanded(final NotificationViewStyle style) {
		return new Formatter<NotificationViewModel>() {
			@Override
			public String format(NotificationViewModel model) {
				return model.isExpanded() ? style.expanded() : style.collapsed();
			}
		};
	}

	public static Formatter<NotificationViewModel> getSource(final NotificationEventSourceExpert<NotificationEventSource> expert) {
		return new Formatter<NotificationViewModel>() {
			@Override
			public String format(NotificationViewModel model) {
				return expert.render(model.getEntry().getEventSource());
			}
		};
	}

	public static Formatter<NotificationViewModel> getUnread(final NotificationViewStyle style) {
		return new Formatter<NotificationViewModel>() {
			@Override
			public String format(NotificationViewModel model) {
				return model.getEntry().getWasReadAt() == null ? style.unread() : "";
			}
		};
	}

	/* ----- Notification Formatter ----- */

	public static Formatter<MessageModel> getLevel() {
		return new Formatter<MessageModel>() {
			@Override
			public String format(MessageModel data) {
				if (data.getLevel() != null)
					return data.getLevel().name();
				
				return null;
			}
		};
	}

	public static Formatter<MessageModel> getLevel(final NotificationViewStyle style) {
		return new Formatter<MessageModel>() {
			@Override
			public String format(MessageModel data) {
				if (data.getLevel() != null)
					switch (data.getLevel()) {
					case ERROR:
						return style.levelError();
					case INFO:
						return style.levelInfo();
					case WARNING:
						return style.levelWarning();
					case SUCCESS:
						return style.levelSuccess();
					}
				return null;
			}
		};
	}

	public static Formatter<MessageModel> getCommand() {
		return new Formatter<MessageModel>() {
			@Override
			public String format(MessageModel data) {
				if (data.getCommand() != null)
					return data.getCommand().getName();
				return null;
			}
		};
	}

	public static Formatter<MessageModel> getMessage() {
		return new Formatter<MessageModel>() {
			@Override
			public String format(MessageModel data) {
				return GMEUtil.htmlReEscape(data.getMessage());
			}
		};
	}

}
