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
import com.braintribe.gwt.logging.ui.gxt.client.GxtReasonErrorDialog;
import com.braintribe.gwt.security.client.SecurityService;
import com.braintribe.gwt.security.tfh.client.TfhSecurityService;
import com.braintribe.provider.SingletonBeanProvider;

public class Services {
	
	protected static Supplier<SecurityService> securityService = new SingletonBeanProvider<SecurityService>() {
		{
			GxtReasonErrorDialog.setGmContentViewSupplier(Constellations.errorMasterViewConstellationSupplier);
		}
		@Override
		public TfhSecurityService create() throws Exception {
			TfhSecurityService bean = (TfhSecurityService) publish(new TfhSecurityService());
			bean.setLocaleProvider(() -> UrlParameters.getInstance().getParameter("locale"));
			bean.setLocaleProvider(Startup.localeProvider.get());
			bean.setEvaluator(GmRpc.serviceRequestEvaluator.get());
			bean.setLogoutServletUrl(Runtime.logoutServletUrlProvider.get());
			return bean;
		}
	};

}
