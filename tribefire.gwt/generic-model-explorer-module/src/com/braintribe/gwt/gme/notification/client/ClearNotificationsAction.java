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
package com.braintribe.gwt.gme.notification.client;

import java.util.Arrays;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.LocalizedText;
import com.braintribe.gwt.gme.constellation.client.resources.ConstellationResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;

/**
* This action provides clearing of Notification list
*
*/

public class ClearNotificationsAction extends ModelAction {	
	public ClearNotificationsAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.clearNotifications());
		setIcon(ConstellationResources.INSTANCE.clearBig());
		setHoverIcon(ConstellationResources.INSTANCE.clearBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar));
	}	

	@Override
	protected void updateVisibility() {
		Boolean useHidden = true;
		
		if 	(gmContentView instanceof NotificationView) {
			if (((NotificationView) gmContentView).getStore().size() > 0)
				useHidden = false;
		}
			
		setHidden(useHidden);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if 	(gmContentView instanceof NotificationView) {
		    ((NotificationView) gmContentView).clearNotifications();
		    updateVisibility();
		}		
	}

}
