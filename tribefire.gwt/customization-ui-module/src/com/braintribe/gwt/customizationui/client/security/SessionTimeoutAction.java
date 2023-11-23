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
package com.braintribe.gwt.customizationui.client.security;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.google.gwt.user.client.Window.Location;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;

/**
 * This action is to be performed when there is a session timeout.
 * A message will be displayed, and then the application will be restarted (reloaded).
 * @author michel.docouto
 *
 */
public class SessionTimeoutAction extends Action {

	@Override
	public void perform(TriggerInfo triggerInfo) {
		AlertMessageBox box = new AlertMessageBox(LocalizedText.INSTANCE.sessionTimeout(), LocalizedText.INSTANCE.sessionTimeoutMessage());
		box.addDialogHideHandler(event -> {
			if (UrlParameters.getHashInstance().containsParameter("sessionId")) {
				String url = Location.getHref().replace("sessionId", "previousSessionId");
				Location.replace(url);
			} else
				Location.reload();
		});
		box.show();
	}

}
