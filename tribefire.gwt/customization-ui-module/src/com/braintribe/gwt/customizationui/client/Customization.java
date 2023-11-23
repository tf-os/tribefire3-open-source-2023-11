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
package com.braintribe.gwt.customizationui.client;

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.AsyncCallbacks;
import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.customizationui.client.security.LoginCredentials;
import com.braintribe.gwt.gxt.gxtresources.extendedcomponents.client.ExtendedBorderLayoutContainer;
import com.braintribe.gwt.gxt.gxtresources.text.LocalizedText;
import com.braintribe.gwt.gxt.gxtresources.whitemask.client.MaskController;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.ioc.client.InitializableBean;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.client.Logger;
import com.braintribe.gwt.security.client.SecurityService;
import com.braintribe.model.securityservice.credentials.TrustedCredentials;
import com.braintribe.model.securityservice.credentials.identification.UserNameIdentification;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.container.Viewport;

/**
 * This bean will serve for getting/setting the needed startup components.
 * It actually builds the whole login UI up.
 *
 */
public class Customization implements InitializableBean {
	protected static Logger logger = new Logger(Customization.class);
	private Supplier<MainWindowWatcher> mainWindowWatcherProvider;
	private Supplier<? extends Widget> loginPanelProvider;
	private Supplier<? extends SecurityService> securityServiceProvider;
	private LoginCredentials fixLogin;
	private static String loginMaskMessage = LocalizedText.INSTANCE.signingIn();
	private String sessionIdCookieName = "tfsessionId";
	private boolean redirect = true;
	private String loginServletUrl = "/tribefire-services/login";
	private static int PROGRESS_INITIAL_VALUE = 26;
	private static int PROGRESS_MAX_VALUE = 50;
	private boolean handleInitializationUI = true;
	
	/**
	 * Configures whether we should redirect the uses for the services login instead of using our local login dialog.
	 * Defaults to true.
	 */
	@Configurable
	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
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
	 * Configures the provider used for making sure the user is logged out when the application is closed.
	 */
	@Configurable
	public void setMainWindowWatcherProvider(Supplier<MainWindowWatcher> mainWindowWatcherProvider) {
		this.mainWindowWatcherProvider = mainWindowWatcherProvider;
	}
	
	/**
	 * Configures the LoginCredentials used for a fixed login.
	 */
	@Configurable
	public void setFixLogin(LoginCredentials fixLogin) {
		this.fixLogin = fixLogin;
	}

	/**
	 * Configures the required login panel provider.
	 */
	@Configurable @Required
	public void setLoginPanelProvider(Supplier<? extends Widget> loginPanelProvider) {
		this.loginPanelProvider = loginPanelProvider;
	}
	
	/**
	 * Configures the required {@link SecurityService} provider.
	 */
	@Configurable @Required
	public void setSecurityServiceProvider(Supplier<? extends SecurityService> securityServiceProvider) {
		this.securityServiceProvider = securityServiceProvider;
	}
	
	/**
	 * Configures the message that is displayed while signing in.
	 * Defaults to the signingIn {@link LocalizedText} entry value.
	 */
	@Configurable
	public static void setLoginMaskMessage(String maskMessage) {
		loginMaskMessage = maskMessage;
	}
	
	/**
	 * Configures the name of the cookie which stores the sessionId. Defaults to "tfsessionId".
	 */
	@Configurable
	public void setSessionIdCookieName(String sessionIdCookieName) {
		this.sessionIdCookieName = sessionIdCookieName;
	}
	
	/**
	 * Configures the URL of the login Servlet. Defaults to "/tribefire-services/login".
	 */
	@Configurable
	public void setLoginServletUrl(String loginServletUrl) {
		this.loginServletUrl = loginServletUrl;
	}
	
	@Override
	public void intializeBean() throws Exception {
		start();
	}
	
	protected void start() {
		try {
			Element loading = Document.get().getElementById("loading");
			if (loading != null)
				loading.getParentElement().removeChild(loading);
			
			if (handleInitializationUI)
				RootPanel.get().clear();

			// Fix login first
			String sessionIdFromCookie = Cookies.getCookie(sessionIdCookieName);
			if (fixLogin != null)
				performFixLogin();
			else if (UrlParameters.getHashInstance().containsParameter("sessionId") || UrlParameters.getHashInstance().containsParameter("si") || UrlParameters.getHashInstance().containsParameter("user")
					|| (sessionIdFromCookie != null && !sessionIdFromCookie.isEmpty())) {
				//For SSO, we will check if both the sessionId and user are present in the UrlParameters.
				//If they are there, then we login based on those informations. If they are not there, then
				//we show the LoginPanel
				performLoginWithIdOrUser(sessionIdFromCookie);
			} else if (handleInitializationUI)
				showLogin();
			
			if (mainWindowWatcherProvider != null && handleInitializationUI)
				mainWindowWatcherProvider.get();
		} catch (Exception e) {
			ErrorDialog.show("Error While Starting", e);
			e.printStackTrace();
		}
	}

	private void showLogin() {
		if (GWT.isProdMode() && redirect) {
			Window.Location.replace(loginServletUrl);
			return;
		}
		
		//Initializing the LoginPanel
		Viewport viewport = new Viewport();
		viewport.setBorders(false);
		try {
			viewport.add(loginPanelProvider.get());
			RootPanel.get().add(viewport);
		} catch (RuntimeException e) {
			logger.error("Error while preparing login UI");
			e.printStackTrace();
			Window.Location.replace(loginServletUrl);
		}
	}

	private void performLoginWithIdOrUser(String sessionIdFromCookie) throws RuntimeException {
		SecurityService securityService = securityServiceProvider.get();
		String sessionId = UrlParameters.getFailSafeParameter("sessionId", UrlParameters.getFailSafeParameter("si"));
		String userName = UrlParameters.getHashInstance().getParameter("user");
		
		ExtendedBorderLayoutContainer tempPanel;
		if (handleInitializationUI)
			tempPanel = prepareTempPanelForMask();
		else
			tempPanel = null;
		Future<Boolean> future;
		
		if (userName != null && sessionId == null) {
			TrustedCredentials trustedCredentials = TrustedCredentials.T.create();
			UserNameIdentification userNameIdentification = UserNameIdentification.T.create();
			userNameIdentification.setUserName(userName);
			trustedCredentials.setUserIdentification(userNameIdentification);
			future = securityService.login(trustedCredentials);
		} else
			future = securityService.loginWithExistingSession(userName, sessionId != null ? sessionId : sessionIdFromCookie);
		
		future //
				.andThen(result -> unMaskTempPanel(tempPanel)) //
				.onError(e -> {
					logger.error(LocalizedText.INSTANCE.errorSigningIn(), e);
					unMaskTempPanel(tempPanel);
					if (handleInitializationUI) {
						RootPanel.get().clear();
						showLogin();
					}
				});
	}
	
	private void unMaskTempPanel(ExtendedBorderLayoutContainer tempPanel) {
		if (tempPanel == null)
			return;
		
		tempPanel.unmask();
		if (MaskController.progressBarInitialValue >= PROGRESS_INITIAL_VALUE && MaskController.progressBarInitialValue <= PROGRESS_MAX_VALUE) 
			MaskController.progressBarInitialValue = null; //only disable if we are still handling the tempPanel
	}

	private void performFixLogin() throws RuntimeException {
		SecurityService securityService = securityServiceProvider.get();
		
		ExtendedBorderLayoutContainer tempPanel;
		if (handleInitializationUI)
			tempPanel = prepareTempPanelForMask();
		else
			tempPanel = null;
		
		securityService.login(fixLogin.getUser(), fixLogin.getPassword(), //
				AsyncCallbacks.of( //
						result -> unMaskTempPanel(tempPanel), //
						e -> {
							unMaskTempPanel(tempPanel);
							ErrorDialog.show(LocalizedText.INSTANCE.errorSigningIn(), e);
						}));
	}
	
	/**
	 * Creating a temporary panel for displaying the mask
	 */
	private static ExtendedBorderLayoutContainer prepareTempPanelForMask() {
		ExtendedBorderLayoutContainer tempPanel = new ExtendedBorderLayoutContainer() {
			@Override
			public void onResize() {
				//When we resize, masking is done once again
				MaskController.maskScreenOpaque = true;
				super.onResize();
				Scheduler.get().scheduleDeferred(() -> MaskController.maskScreenOpaque = false);
			}
			
			@Override
			protected void onResize(int width, int height) {
				//When we resize, masking is done once again
				MaskController.maskScreenOpaque = true;
				super.onResize(width, height);
				Scheduler.get().scheduleDeferred(() -> MaskController.maskScreenOpaque = false);
			}
		};
		
		ExtendedViewPort viewport = new ExtendedViewPort();
		viewport.setBorders(false);
		viewport.add(tempPanel);
		RootPanel.get().add(viewport);
		tempPanel.doLayout();
		viewport.doLayout();
		MaskController.setProgressMask(true, PROGRESS_INITIAL_VALUE, PROGRESS_MAX_VALUE);
		tempPanel.mask(loginMaskMessage);
		MaskController.maskScreenOpaque = false;
		
		new Timer() {
			@Override
			public void run() {
				tempPanel.setWidth(tempPanel.getOffsetWidth() - 1);
				tempPanel.doLayout();
			}
		}.schedule(50);
		
		return tempPanel;
	}
	
	private static class ExtendedViewPort extends Viewport {
		//Enabling doLayout to be public
		@Override
		public void doLayout() {
			super.doLayout();
		}
	}

}
