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

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.common.attribute.common.UserInfo;
import com.braintribe.common.attribute.common.UserInfoAttribute;
import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.browserfeatures.client.Console;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.security.client.SecurityService;
import com.braintribe.gwt.security.client.SessionListenerAdapter;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.PasswordField;

/**
 * This controller will be able to hide/show the login panel after the session is created/closed.
 * If SSO is enabled, the window will be closed.
 * @author michel.docouto
 *
 */
public class SessionController extends SessionListenerAdapter {
	
	private static Logger logger = new Logger(SessionController.class);
	
	private Supplier<? extends Widget> mainPanelProvider;
	private Supplier<Widget> loginPanelSupplier;
	private Widget mainPanel;
	private List<Supplier<?>> sessionCreatedProviders;
	private Timer inactivityTimer;
	private int inactivityInterval = -1;
	private SecurityService securityService;
	private MouseMoveHandler mouseMoveHandler;
	private Window inactivityWindow;
	private PasswordField passwordField;
	private boolean checkNoUIRole = true;
	private String noUIRole = "noUI";
	private boolean enableCloseWindow;
	private HandlerRegistration documentEventRegistration;
	private boolean handleInitializationUI = true;
	private Supplier<?> externalModuleInitializerSupplier;
	
	/**
	 * Configures the initializer to be used when {@link #setHandleInitializationUI(boolean)} is set to false.
	 */
	@Required
	public void setExternalModuleInitializerSupplier(Supplier<?> externalModuleInitializerSupplier) {
		this.externalModuleInitializerSupplier = externalModuleInitializerSupplier;
	}
	
	/**
	 * Configures the main panel to be optionally added to the Viewport.
	 */
	@Configurable
	public void setMainPanelProvider(Supplier<? extends Widget> mainPanelProvider) {
		this.mainPanelProvider = mainPanelProvider;
	}
	
	/**
	 * Configures whether we should handle the initialization UI (such as displaying the ViewPort for the main panel, masks and so on).
	 * Defaults to true.
	 */
	@Configurable
	public void setHandleInitializationUI(boolean handleInitializationUI) {
		this.handleInitializationUI = handleInitializationUI;
	}
	
	/**
	 * Configures the panel to be used as a login panel.
	 */
	@Configurable
	public void setLoginPanel(Supplier<Widget> loginPanelSupplier) {
		this.loginPanelSupplier = loginPanelSupplier;
	}
	
	/**
	 * Configures a list of providers to be provided when the session is created.
	 */
	@Configurable
	public void setSessionCreatedProviders(List<Supplier<?>> sessionCreatedProviders) {
		this.sessionCreatedProviders = sessionCreatedProviders;
	}
	
	/**
	 * Configures the interval (in millisecond) for performing automatic logout after inactivity.
	 * By default, no automatic logout is performed.
	 */
	@Configurable
	public void setIntervalForAutomaticLogout(int interval) {
		this.inactivityInterval = interval;
	}
	
	/**
	 * Configures the SecurityService used for automatic logout.
	 * This is required if {@link #setIntervalForAutomaticLogout(int)} is set to some value.
	 */
	@Configurable
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	/**
	 * Configures the name of the no UI role. Defaults to "noUI".
	 * @see #setCheckNoUIRole(boolean)
	 */
	@Configurable
	public void setNoUIRole(String noUIRole) {
		this.noUIRole = noUIRole;
	}
	
	/**
	 * If true (default), there is a check for the noUI role. If the loggedIn user has that role, then the access is denied.
	 * @see #setNoUIRole(String)
	 */
	@Configurable
	public void setCheckNoUIRole(boolean checkNoUIRole) {
		this.checkNoUIRole = checkNoUIRole;
	}
	
	/**
	 * If true, the window will be closed on end of a session.
	 * Notice that {@link #setHandleInitializationUI(boolean)} will overrule this.
	 */
	@Configurable
	public void setEnableCloseWindow(boolean enableCloseWindow) {
		this.enableCloseWindow = enableCloseWindow;
	}
	
	private Timer getInactivityTimer() {
		if (inactivityTimer == null) {
			inactivityTimer = new Timer() {
				@Override
				public void run() {
					getInactivityWindow().show();
				}
			};
		}
		
		return inactivityTimer;
	}
	
	@Override
	public void sessionCreated(SecurityService sender) {
		try {
			Set<String> roles = sender.getSession().getRoles();
			String username = sender.getSession().getUsername();
			if (checkNoUIRole && roles != null && roles.contains(noUIRole)) {
				sender.logout(true, null);
				ErrorDialog.showMessage(LocalizedText.INSTANCE.userHasNoUIAccess(username));
				return;
			}
			
			UserInfo info = UserInfo.of(username, roles);
			AttributeContexts.push(AttributeContexts.derivePeek().set(UserInfoAttribute.class, info).build());
			
			if (handleInitializationUI) {
				//Initializing the MainPanel
				logger.debug("Initializing the main panel.");
				for (int i = 0; i < RootPanel.get().getWidgetCount(); i++)
					RootPanel.get().remove(i);
				
				logger.debug("sessionID: " +sender.getSession().getId());
				if (mainPanelProvider != null) {
					mainPanel = mainPanelProvider.get();
					Viewport viewPort = new Viewport();
					viewPort.add(mainPanel);
					logger.debug("mainPanel provided successfully");
					RootPanel.get().add(viewPort);
					Console.timeEnd("bootstrap");
				}
				
				if (inactivityInterval > 0)
					prepareMouseListener();
			} else {
				//In case the UI is not initialized, we must load the initialization data in a different way
				externalModuleInitializerSupplier.get();
			}
			
			if (sessionCreatedProviders != null) {
				for (Supplier<?> provider : sessionCreatedProviders)
					provider.get();
			}
		} catch (Exception e) {
			ErrorDialog.show("Error While Starting", e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void sessionClosed(SecurityService sender) {
		if (!handleInitializationUI)
			return;
		
		for (int i = 0; i < RootPanel.get().getWidgetCount(); i++)
			RootPanel.get().remove(i);
		
		//For SSO, we will check if the sessionId is present in the UrlParameters.
		//If it is there and the feature is enabled, then we will close the pop up window
		if (UrlParameters.getHashInstance().containsParameter("sessionId") && enableCloseWindow) {
			String url = com.google.gwt.user.client.Window.Location.getHref().replace("sessionId", "previousSessionId");
			com.google.gwt.user.client.Window.Location.replace(url);
			closeWindow();
		} else if (loginPanelSupplier != null) {
			Viewport viewPort = new Viewport();
			viewPort.add(loginPanelSupplier.get());
			RootPanel.get().add(viewPort);
		}
	}
	
	private void prepareMouseListener() {
		getInactivityTimer().schedule(inactivityInterval);
		if (mouseMoveHandler == null)
			mouseMoveHandler = event -> getInactivityTimer().schedule(inactivityInterval);
		
		documentEventRegistration = RootPanel.get().addDomHandler(mouseMoveHandler, MouseMoveEvent.getType());
	}
	
	private Window getInactivityWindow() {
		if (inactivityWindow != null)
			return inactivityWindow;
		
		passwordField = new PasswordField();
		passwordField.setWidth(175);
		passwordField.addKeyDownHandler(event -> {
			int keyCode = event.getNativeKeyCode();
			if (keyCode == KeyCodes.KEY_ENTER)
				recheckUserPassword(passwordField.getValue());
		});
		
		inactivityWindow = new Window() {
			@Override
			public void show() {
				passwordField.clear();
				super.show();
				inactivityWindow.maximize();
				new Timer() {
					@Override
					public void run() {
						passwordField.focus();
					}
				}.schedule(100);
				documentEventRegistration.removeHandler();
			}
		};
		
		inactivityWindow.setModal(true);
		inactivityWindow.setClosable(false);
		inactivityWindow.setOnEsc(false);
		inactivityWindow.setResizable(false);
		inactivityWindow.setHeaderVisible(false);
		inactivityWindow.setBodyStyle("backgroundColor:white");
		inactivityWindow.setHeading(LocalizedText.INSTANCE.sessionLocked());
		
		FramedPanel framedPanel = new FramedPanel();
		VerticalLayoutContainer container = new VerticalLayoutContainer();
		framedPanel.setWidget(container);
		
		container.add(new InlineLabel(LocalizedText.INSTANCE.sessionLockedMessage()));
		container.setWidth(450);
		container.add(passwordField);
		final TextButton okButton = new TextButton(LocalizedText.INSTANCE.ok());
		final TextButton cancelButton = new TextButton(LocalizedText.INSTANCE.cancel());
		SelectHandler selectHandler = event -> {
			if (event.getSource() == okButton) {
				recheckUserPassword(passwordField.getValue());
				return;
			}
			
			inactivityWindow.mask(LocalizedText.INSTANCE.signingOut());
			securityService.logout(true, AsyncCallbacks.of(result -> handleResult(), e -> handleResult()));
		};
		okButton.addSelectHandler(selectHandler);
		cancelButton.addSelectHandler(selectHandler);
		container.add(okButton);
		container.add(cancelButton);
		
		CenterLayoutContainer centerPanel = new CenterLayoutContainer();
		centerPanel.setWidget(framedPanel);
		inactivityWindow.setWidget(centerPanel);
		
		return inactivityWindow;
	}
	
	private void handleResult() {
		if (!UrlParameters.getHashInstance().containsParameter("sessionId"))
			com.google.gwt.user.client.Window.Location.reload();
		else {
			String url = com.google.gwt.user.client.Window.Location.getHref().replace("sessionId", "previousSessionId");
			com.google.gwt.user.client.Window.Location.replace(url);
		}
	}
	
	private void recheckUserPassword(String password) {
		inactivityWindow.mask(LocalizedText.INSTANCE.signingIn());
		securityService.recheckUserPassword(password).andThen(this::handleRechedUserPassword).onError(e -> {
			e.printStackTrace();
			handleRechedUserPassword(false);
		});
	}
	
	private void handleRechedUserPassword(boolean result) {
		inactivityWindow.unmask();
		if (result) {
			inactivityWindow.hide();
			getInactivityTimer().schedule(inactivityInterval);
			documentEventRegistration = RootPanel.get().addDomHandler(mouseMoveHandler, MouseMoveEvent.getType());
		} else {
			AlertMessageBox box = new AlertMessageBox(LocalizedText.INSTANCE.signIn(), LocalizedText.INSTANCE.authenticationFailed());
			box.addDialogHideHandler(event -> Scheduler.get().scheduleDeferred(passwordField::focus));
			box.show();
		}
	}
	
	private static native void closeWindow() /*-{
		$wnd.close();
	}-*/;

}
