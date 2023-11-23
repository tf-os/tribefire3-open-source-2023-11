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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.model.processing.meta.cmd.context.SelectorContextAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.RoleAspect;

public class MetaData {
	
	protected static Supplier<Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>>> dynamicAspectValueProviders =
			new SessionScopedBeanProvider<Map<Class<? extends SelectorContextAspect<?>>,Supplier<?>>>() {
		@Override
		public Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> create() throws Exception {
			Map<Class<? extends SelectorContextAspect<?>>, Supplier<?>> bean = publish(new HashMap<>());
			bean.put(RoleAspect.class, Providers.rolesProvider.get());
			return bean;
		}
	};

}
