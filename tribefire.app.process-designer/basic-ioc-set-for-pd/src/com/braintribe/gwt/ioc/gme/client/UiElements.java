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

import com.braintribe.gwt.gme.actionmenubuilder.client.DefaultActionMenuBuilder;
import com.braintribe.provider.SingletonBeanProvider;

public class UiElements {
	
	protected static Supplier<DefaultActionMenuBuilder> defaultActionMenuBuilder = new SingletonBeanProvider<DefaultActionMenuBuilder>() {
		@Override
		public DefaultActionMenuBuilder create() throws Exception {
			DefaultActionMenuBuilder bean = new DefaultActionMenuBuilder();
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry.get());
			bean.setSeparatorActionProvider(Actions.separatorAction);
			return bean;
		}
	};
	
}
