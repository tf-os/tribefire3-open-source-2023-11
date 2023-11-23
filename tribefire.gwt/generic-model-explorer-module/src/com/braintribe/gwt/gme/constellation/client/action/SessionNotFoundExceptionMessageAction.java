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
package com.braintribe.gwt.gme.constellation.client.action;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.notification.client.ConfirmationDialog;
import com.braintribe.gwt.gme.notification.client.NotificationView;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.notification.Level;
import com.google.gwt.user.client.Window;

/**
 * Action which shows an dialog with a nice message for the SessionNotFoundException.
 * @author michel.docouto
 *
 */
public class SessionNotFoundExceptionMessageAction extends Action {
	
	private String loginServletUrl;
	
	/**
	 * Configures the URL of the login Servlet. Defaults to "/tribefire-services/login".
	 */
	@Required
	public void setLoginServletUrl(String loginServletUrl) {
		this.loginServletUrl = loginServletUrl;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		ConfirmationDialog dialog = NotificationView.showConfirmationDialog(Level.ERROR, LocalizedText.INSTANCE.sessionExpired());
		dialog.setCancelButtonVisible(false);
		dialog.setOkButtonVisible(true);
		
		dialog.getConfirmation().andThen(result -> Window.Location.replace(loginServletUrl));
	}

}
