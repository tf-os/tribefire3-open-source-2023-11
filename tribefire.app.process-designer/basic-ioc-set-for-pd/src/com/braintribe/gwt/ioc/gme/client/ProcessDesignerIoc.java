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

import com.braintribe.gwt.ioc.gme.client.expert.ManipulationListenerAndForwarder;
import com.braintribe.gwt.processdesigner.client.ProcessDesigner;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.provider.PrototypeBeanProvider;

public class ProcessDesignerIoc {
	
	public static Supplier<ProcessDesigner> standAloneProcessDesigner = new SessionScopedBeanProvider<ProcessDesigner>() {
		@Override
		public ProcessDesigner create() throws Exception {
			ProcessDesigner bean = publish(new ProcessDesigner());
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.templateWorkbenchPersistenceSession.get());
			bean.setQuickAccessPanelProvider(Panels.spotlightPanelProvider);
			bean.setAccessIdChanger(Controllers.accessIdChanger.get());
			bean.setExternalSessionConsumer(manipulationListenerAndForwarder.get());
			return bean;
		}
	};
	
	public static Supplier<ProcessDesigner> processDesigner = new SessionScopedBeanProvider<ProcessDesigner>() {
		@Override
		public ProcessDesigner create() throws Exception {
			ProcessDesigner bean = publish(new ProcessDesigner());
			bean.configureGmSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.templateWorkbenchPersistenceSession.get());
			bean.setQuickAccessPanelProvider(Panels.spotlightPanelProvider);
			bean.setAccessIdChanger(Controllers.accessIdChanger.get());
			bean.setExternalSessionConsumer(manipulationListenerAndForwarder.get());
			return bean;
		}
	};
	
	private static Supplier<ManipulationListenerAndForwarder> manipulationListenerAndForwarder = new PrototypeBeanProvider<ManipulationListenerAndForwarder>() {
		@Override
		public ManipulationListenerAndForwarder create() throws Exception {
			ManipulationListenerAndForwarder bean = new ManipulationListenerAndForwarder();
			bean.setSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
}