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

import com.braintribe.gwt.customizationui.client.Customization;
import com.braintribe.gwt.customizationui.client.GwtLocaleProvider;
import com.braintribe.gwt.customizationui.client.security.LoginCredentials;
import com.braintribe.gwt.gmview.util.client.LocaleUtil;
import com.braintribe.provider.SingletonBeanProvider;
import com.braintribe.utils.i18n.I18nTools;
import com.google.gwt.core.client.GWT;

public class Startup {
	
	protected static Supplier<GwtLocaleProvider> localeProvider = new SingletonBeanProvider<GwtLocaleProvider>() {
		@Override
		public GwtLocaleProvider create() throws Exception {
			GwtLocaleProvider bean = publish(new GwtLocaleProvider());
			bean.setGmSession(Session.persistenceSession);
			
			LocaleUtil.setLocaleProvider(bean);
			I18nTools.localeProvider = bean;
			return bean;
		}
	};
	
	public static Supplier<Customization> customization = new SingletonBeanProvider<Customization>() {
		{
			attach(Services.securityService);
		}
		@Override
		public Customization create() throws Exception {
			Customization bean = publish(new Customization());
			bean.setSecurityServiceProvider(Services.securityService);
			bean.setHandleInitializationUI(Runtime.handleInitializationUI);
			
			if (!GWT.isProdMode())
				bean.setFixLogin(new LoginCredentials("cortex", "cortex"));
			
			return bean;
		}
	};
	
}
