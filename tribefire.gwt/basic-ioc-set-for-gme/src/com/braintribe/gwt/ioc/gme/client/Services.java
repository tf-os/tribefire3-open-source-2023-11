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
package com.braintribe.gwt.ioc.gme.client;

import java.util.function.Supplier;

import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.logging.client.ErrorDialog;
import com.braintribe.gwt.logging.ui.gxt.client.GxtReasonErrorDialog;
import com.braintribe.gwt.logging.ui.gxt.client.experts.ServerNotRunningAction;
import com.braintribe.gwt.logging.ui.gxt.client.experts.ServerNotRunningExceptionFilter;
import com.braintribe.gwt.security.client.AuthorizationExceptionFilter;
import com.braintribe.gwt.security.client.SecurityService;
import com.braintribe.gwt.security.client.SessionNotFoundExceptionFilter;
import com.braintribe.gwt.security.client.SessionNotFoundReasonFilter;
import com.braintribe.gwt.security.tfh.client.TfhSecurityService;
import com.braintribe.provider.SingletonBeanProvider;

/**
 * This is the IoC configuration for the Services.
 *
 */
class Services {	
	
	protected static Supplier<SecurityService> securityService = new SingletonBeanProvider<SecurityService>() {
		{
			attach(Controllers.sessionController);
			//ErrorDialog.addExceptionFilterAction(new AuthorizationExceptionFilter(), new ShowAuthorizationExceptionMessageAction()::handleError);
			ErrorDialog.addExceptionFilterAction(new ServerNotRunningExceptionFilter(), new ServerNotRunningAction()::handleError);
			ErrorDialog.addExceptionFilterAction(new AuthorizationExceptionFilter(), Actions.sessionNotFoundExceptionMessageAction.get()::handleError);
			ErrorDialog.addExceptionFilterAction(new SessionNotFoundExceptionFilter(), Actions.sessionNotFoundExceptionMessageAction.get()::handleError);
			ErrorDialog.addExceptionFilterAction(new SessionNotFoundReasonFilter(), Actions.sessionNotFoundExceptionMessageAction.get()::handleError);
			GxtReasonErrorDialog.setGmContentViewSupplier(Constellations.errorMasterViewConstellationSupplier);
			GxtReasonErrorDialog.setIconProvider(Providers.typeIconProvider);
		}
		@Override
		public TfhSecurityService create() throws Exception {
			TfhSecurityService bean = (TfhSecurityService) publish(new TfhSecurityService());
			bean.setLocaleProvider(() -> UrlParameters.getInstance().getParameter("locale"));
			bean.setLocaleProvider(Startup.localeProvider.get());
			//bean.setPreparingLoaderProvider(preparingLoaderProvider);
			bean.setEvaluator(GmRpc.serviceRequestEvaluator.get());
			bean.setLogoutServletUrl(Runtime.logoutServletUrlProvider.get());
			return bean;
		}
	};
	
}
