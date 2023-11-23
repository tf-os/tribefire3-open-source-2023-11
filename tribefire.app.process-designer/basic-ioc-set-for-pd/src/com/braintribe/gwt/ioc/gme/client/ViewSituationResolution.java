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

import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.ViewSituationResolver;
import com.braintribe.gwt.gmview.client.ViewSituationSelector;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.provider.PrototypeBeanProvider;

public class ViewSituationResolution {
	
	protected static Supplier<ViewSituationResolver<GmContentViewContext>> viewSituationResolver = new SessionScopedBeanProvider<ViewSituationResolver<GmContentViewContext>>() {
		@Override
		public ViewSituationResolver<GmContentViewContext> create() throws Exception {
			ViewSituationResolver<GmContentViewContext> bean = publish(new ViewSituationResolver<>());
			bean.setGmExpertRegistry(Runtime.gmExpertRegistry.get());
			bean.setViewSituationSelectorMap(standardViewSituationSelectorMap.get());
			bean.setPriorityReverse(false);
			return bean;
		}
	};
	
	private static Supplier<Map<ViewSituationSelector, GmContentViewContext>> standardViewSituationSelectorMap = new PrototypeBeanProvider<Map<ViewSituationSelector, GmContentViewContext>>() {
		@Override
		public Map<ViewSituationSelector, GmContentViewContext> create() throws Exception {
			Map<ViewSituationSelector, GmContentViewContext> bean = new HashMap<>();
			return bean;
		}
	};

}
