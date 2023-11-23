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

import java.util.function.Supplier;

import com.braintribe.gwt.action.client.Action;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.customizationui.client.resources.UiResources;
import com.braintribe.gwt.gmview.client.GlobalState;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.security.client.SecurityService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.Dialog.PredefinedButton;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;

/**
 * This action is responsible for logging the user out.
 * @author michel.docouto
 *
 */
public class LogoutAction extends Action {
	
	static {
		UiResources.INSTANCE.css().ensureInjected();
	}
	
	private SecurityService securityService;
	private Supplier<String> logoutController;
	
	public LogoutAction() {
		this.setName(LocalizedText.INSTANCE.signOut());
		this.setStyleName(UiResources.INSTANCE.css().bannerLinkButton());
	}
	
	private void logout() {
		GlobalState.mask(LocalizedText.INSTANCE.signingOut());
    	
		AsyncCallback<Boolean> callback = AsyncCallbacks //
				.of(result -> GlobalState.unmask(), //
						e -> {
							GlobalState.unmask();
							ErrorDialog.show(LocalizedText.INSTANCE.errorSigningOut(), e);
							e.printStackTrace();
						});
		securityService.logout(callback);
	}
	
	/**
	 * Configures the required SecurityService used for performing the logout.
	 */
	@Configurable @Required
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	/**
	 * Configures a provider that can cancel the logout if it returns a message.
	 * That message is then shown to the user, and should be a question.
	 * Only by replying yes, the logout is performed, otherwise it is cancelled.
	 */
	@Configurable
	public void setLogoutController(Supplier<String> logoutController) {
		this.logoutController = logoutController;
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		if (logoutController == null) {
			logout();
			return;
		}
		
		String message = logoutController.get();
		if (message == null) {
			logout();
			return;
		}
		
		ConfirmMessageBox messageBox = new ConfirmMessageBox(LocalizedText.INSTANCE.signOut(), message);
		messageBox.addDialogHideHandler(event -> {
			if (PredefinedButton.YES.equals(event.getHideButton()))
				logout();
		});
	}

}
