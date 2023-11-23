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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.common.lcd.Pair;
import com.braintribe.gm.model.uiaction.RefreshEntitiesActionFolderContent;
import com.braintribe.gwt.gme.constellation.client.ServiceRequestActionHandler;
import com.braintribe.gwt.gme.constellation.client.SimpleInstantiationActionHandler;
import com.braintribe.gwt.gme.constellation.client.action.SeparatorAction;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateInstantationActionHandler;
import com.braintribe.gwt.gme.templateevaluation.client.expert.TemplateServiceRequestActionHandler;
import com.braintribe.gwt.gmview.action.client.ActionTypeAndName;
import com.braintribe.gwt.gmview.action.client.KnownActions;
import com.braintribe.gwt.gmview.action.client.RefreshEntitiesAction;
import com.braintribe.gwt.gmview.client.GmContentViewContext;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.workbenchaction.processing.client.WorkbenchActionHandlerRegistry;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.workbench.action.api.WorkbenchActionHandler;
import com.braintribe.model.workbench.ServiceRequestAction;
import com.braintribe.model.workbench.SimpleInstantiationAction;
import com.braintribe.model.workbench.TemplateInstantiationAction;
import com.braintribe.model.workbench.TemplateServiceRequestAction;
import com.braintribe.model.workbench.WorkbenchAction;
import com.braintribe.provider.PrototypeBeanProvider;

public class Actions {
	
	protected static Supplier<WorkbenchActionHandlerRegistry> workbenchActionHandlerRegistry = new SessionScopedBeanProvider<WorkbenchActionHandlerRegistry>() {
		@Override
		public WorkbenchActionHandlerRegistry create() throws Exception {
			WorkbenchActionHandlerRegistry bean = publish(new WorkbenchActionHandlerRegistry());
			bean.setWorkbenchActionHandlerRegistryMap(workbenchActionHandlerRegistryMap.get());
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			bean.setUserNameProvider(Providers.userNameProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>>> externalActionProviders = new SessionScopedBeanProvider<List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>>>() {
		@Override
		public List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>> create() throws Exception {
			List<Pair<ActionTypeAndName, Supplier<? extends ModelAction>>> bean = publish(new ArrayList<>());
			bean.add(new Pair<>(new ActionTypeAndName(RefreshEntitiesActionFolderContent.T, KnownActions.REFRESH_ENTITIES.getName()),
					refreshEntitiesAction));
			return bean;
		}
	};
	
	protected static Supplier<List<GmContentViewContext>> contentViewContexts = new PrototypeBeanProvider<List<GmContentViewContext>>() {
		@Override
		public List<GmContentViewContext> create() throws Exception {
			List<GmContentViewContext> bean = new ArrayList<>();
			return bean;
		}
	};
	
	protected static Supplier<RefreshEntitiesAction> refreshEntitiesAction = new PrototypeBeanProvider<RefreshEntitiesAction>() {
		@Override
		public RefreshEntitiesAction create() throws Exception {
			RefreshEntitiesAction bean = new RefreshEntitiesAction();
			bean.setSpecialEntityTraversingCriterion(Panels.specialEntityTraversingCriterionMap.get());
			bean.setCurrentContentViewProvider(Providers.currentContentViewProvider.get());
			return bean;
		}
	};
	
	protected static Supplier<SeparatorAction> separatorAction = new PrototypeBeanProvider<SeparatorAction>() {
		@Override
		public SeparatorAction create() throws Exception {
			SeparatorAction bean = new SeparatorAction();
			return bean;
		}
	};
	
	private static Supplier<Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>>> workbenchActionHandlerRegistryMap = new SessionScopedBeanProvider<Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>>>() {
		@Override
		public Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> create() throws Exception {
			Map<EntityType<? extends WorkbenchAction>, Supplier<? extends WorkbenchActionHandler<?>>> bean = publish(new HashMap<>());
			bean.put(SimpleInstantiationAction.T, simpleInstantiationActionHandler);
			bean.put(TemplateInstantiationAction.T, templateInstantiationActionHandler);
			bean.put(ServiceRequestAction.T, serviceRequestActionHandler);
			bean.put(TemplateServiceRequestAction.T, templateServiceRequestActionHandler);
			return bean;
		}
	};
	
	private static Supplier<SimpleInstantiationActionHandler> simpleInstantiationActionHandler = new SessionScopedBeanProvider<SimpleInstantiationActionHandler>() {
		@Override
		public SimpleInstantiationActionHandler create() throws Exception {
			SimpleInstantiationActionHandler bean = publish(new SimpleInstantiationActionHandler());
			bean.setNewInstanceProviderProvider(Providers.newInstanceProvider);
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	private static Supplier<TemplateInstantationActionHandler> templateInstantiationActionHandler = new SessionScopedBeanProvider<TemplateInstantationActionHandler>() {
		@Override
		public TemplateInstantationActionHandler create() throws Exception {
			TemplateInstantationActionHandler bean = publish(new TemplateInstantationActionHandler());
			bean.setUserNameProvider(Providers.userNameProvider.get());
			bean.setTransientGmSession(Session.transientManagedSession.get());
			return bean;
		}
	};
	
	private static Supplier<ServiceRequestActionHandler> serviceRequestActionHandler = new SessionScopedBeanProvider<ServiceRequestActionHandler>() {
		@Override
		public ServiceRequestActionHandler create() throws Exception {
			ServiceRequestActionHandler bean = publish(new ServiceRequestActionHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientSessionProvider(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			return bean;
		}
	};
	
	private static Supplier<TemplateServiceRequestActionHandler> templateServiceRequestActionHandler = new SessionScopedBeanProvider<TemplateServiceRequestActionHandler>() {
		@Override
		public TemplateServiceRequestActionHandler create() throws Exception {
			TemplateServiceRequestActionHandler bean = publish(new TemplateServiceRequestActionHandler());
			bean.setCurrentTransientSessionProvider(Constellations.explorerConstellationProvider.get());
			bean.setGmSession(Session.persistenceSession.get());
			bean.setTransientSessionProvider(Session.prototypeTransientManagedSession);
			bean.setNotificationFactory(Notifications.notificationFactory);
			bean.setUserNameProvider(Providers.userNameProvider.get());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			return bean;
		}
	};
	
}
