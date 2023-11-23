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

import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabPanel.VerticalTabListener;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.google.gwt.user.client.ui.Widget;

public class ShowNotificationsAction extends Action implements InitializableBean {

	private VerticalTabPanel explorerVTabPanel;
	private VerticalTabElement notificationsElement;
	private Widget notificationConstellation;

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (explorerVTabPanel != null && notificationsElement != null)
			explorerVTabPanel.setSelectedVerticalTabElement(notificationsElement);
	}

	@Required
	public void setNotificationsConstellation(Widget notificationsConstellation) {
		//RVE - commented out - notification Element directly add to ExplorerConstellation
		//notificationsElement = new VerticalTabElement("$notificationsConstellation", getName(), getTooltip(), () -> notificationsConstellation, getIcon(), null, true, null);
		//notificationsElement.setSystemConfigurable(true);
		notificationConstellation = notificationsConstellation;
	}

	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerVTabPanel = explorerConstellation.getVerticalTabPanel();
		this.notificationsElement = explorerConstellation.getNotificationElement();
	}

	@Override
	public void intializeBean() throws Exception {
		//if (explorerVTabPanel == null || notificationsElement == null)
		//	return;
		
		//explorerVTabPanel.insertVerticalTabElement(notificationsElement, -2, true);
		
		if (!(notificationConstellation instanceof NotificationConstellation))
			return;
		
		((NotificationConstellation) notificationConstellation).addNotificationConstellationListener(this::handleNotificationMarker);
		
		explorerVTabPanel.addVerticalTabListener(new VerticalTabListener() {
			@Override
			public void onVerticalTabElementSelected(VerticalTabElement previousVerticalTabElement, VerticalTabElement verticalTabElement) {
				if (verticalTabElement == notificationsElement)
					explorerVTabPanel.updateTabElementName("", notificationsElement);
			}
			
			@Override
			public void onVerticalTabElementAddedOrRemoved(int elements, boolean added, List<VerticalTabElement> verticalTabElements) {
				//NOP
			}
			
			@Override
			public void onHeightChanged(int newHeight) {
				//NOP
			}
		});
	}
	
	private void handleNotificationMarker() {
		if (explorerVTabPanel.getSelectedElement() != notificationsElement)
			explorerVTabPanel.updateTabElementName("*", notificationsElement);
	}

}
