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
import com.braintribe.model.notification.Level;

/**
 * Action which shows an dialog with a nice message for the AuthorizationException.
 * @author michel.docouto
 *
 */
public class ShowAuthorizationExceptionMessageAction extends Action {

	@Override
	public void perform(TriggerInfo triggerInfo) {
		ConfirmationDialog dialog = NotificationView.showConfirmationDialog(Level.ERROR, LocalizedText.INSTANCE.accessDenied());
		dialog.setCancelButtonVisible(false);
		dialog.setOkButtonVisible(false);
		dialog.setClosable(false);
		dialog.setOnEsc(false);
		dialog.show();
	}

}
