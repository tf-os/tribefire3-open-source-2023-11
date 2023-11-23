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

public interface NotificationBarStyle extends CssResource {

	String up();

	String text();

	String down();

	String action();

	@ClassName("notificationBar-detailText")
	String detailText();
	
	@ClassName("notificationBar-boldText")
	String boldText();

	@ClassName("notificationBar-italicText")
	String italicText();

	@ClassName("notificationBar-underlineText")
	String underlineText();	

	@ClassName("notificationBar-strikeoutText")
	String strikeoutText();	
	
	public static interface Error extends NotificationBarStyle {

		@Override
		@ClassName("notificationBar-errorUp")
		String up();

		@Override
		@ClassName("notificationBar-errorText")
		String text();

		@Override
		@ClassName("notificationBar-errorDown")
		String down();

		@Override
		@ClassName("notificationBar-errorAction")
		String action();
	}

	public static interface Hint extends NotificationBarStyle {

		@Override
		@ClassName("notificationBar-hintUp")
		String up();

		@Override
		@ClassName("notificationBar-hintText")
		String text();

		@Override
		@ClassName("notificationBar-hintDown")
		String down();

		@Override
		@ClassName("notificationBar-hintAction")
		String action();
	}
	
	public static interface Warning extends NotificationBarStyle {

		@Override
		@ClassName("notificationBar-warningUp")
		String up();

		@Override
		@ClassName("notificationBar-warningText")
		String text();

		@Override
		@ClassName("notificationBar-warningDown")
		String down();

		@Override
		@ClassName("notificationBar-warningAction")
		String action();
		
	}

	public static interface Success extends NotificationBarStyle {

		@Override
		@ClassName("notificationBar-successUp")
		String up();

		@Override
		@ClassName("notificationBar-successText")
		String text();

		@Override
		@ClassName("notificationBar-successDown")
		String down();

		@Override
		@ClassName("notificationBar-successAction")
		String action();
		
	}

	public static interface ErrorBig extends Error {
		@Override
		@ClassName("notificationBar-errorTextBig")
		String text();
	}

	public static interface WarningBig extends Warning {
		@Override
		@ClassName("notificationBar-warningTextBig")
		String text();
	}

	public static interface HintBig extends Hint {
		@Override
		@ClassName("notificationBar-hintTextBig")
		String text();
	}
	
	public static interface MessageBig extends Hint {
		@Override
		@ClassName("notificationBar-hintTextBig")
		String text();
		
		@Override
		@ClassName("notificationBar-messageBig")
		String detailText();
	}

	public static interface SuccessBig extends Success {
		@Override
		@ClassName("notificationBar-successTextBig")
		String text();
	}

}
