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

import com.braintribe.gwt.gme.constellation.client.ExplorerConstellation;
import com.braintribe.gwt.gme.verticaltabpanel.client.VerticalTabElement;
import com.braintribe.gwt.gmview.client.ReloadableGmView;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.processing.notification.api.CommandExpert;
import com.braintribe.model.uicommand.ReloadView;
import com.google.gwt.user.client.ui.Widget;

/**
 * Expert responsible for the implementation of the {@link ReloadView} command.
 * @author michel.docouto
 *
 */
public class ReloadViewExpert implements CommandExpert<ReloadView> {
	
	private ExplorerConstellation explorerConstellation;
	
	/**
	 * Configures the required {@link ExplorerConstellation} used for getting what is the currentView.
	 */
	@Required
	public void setExplorerConstellation(ExplorerConstellation explorerConstellation) {
		this.explorerConstellation = explorerConstellation;
		//test();
		//testReloadAll();
	}

	@Override
	public void handleCommand(ReloadView command) {
		if (command.getReloadAll())
			markViewsWithReloadPending();
		
		VerticalTabElement selectedElement = explorerConstellation.getVerticalTabPanel().getSelectedElement();
		if (selectedElement == null)
			return;
		
		Widget currentView = selectedElement.getWidget();
		if (currentView instanceof ReloadableGmView)
			((ReloadableGmView) currentView).reloadGmView();
	}
	
	private void markViewsWithReloadPending() {
		explorerConstellation.getVerticalTabPanel().getTabElements().stream().filter(el -> el.getWidgetIfSupplied() instanceof ReloadableGmView)
				.forEach(el -> ((ReloadableGmView) el.getWidgetIfSupplied()).setReloadPending(true));
	}
	
	/*private void test() {
		new Timer() {
			@Override
			public void run() {
				ReloadView rv = ReloadView.T.create();
				handleCommand(rv);
			}
		}.scheduleRepeating(10000);
	}*/
	
	/*private void testReloadAll() {
		new Timer() {
			@Override
			public void run() {
				ReloadView rv = ReloadView.T.create();
				rv.setReloadAll(true);
				handleCommand(rv);
			}
		}.schedule(100000);
	}*/

}
