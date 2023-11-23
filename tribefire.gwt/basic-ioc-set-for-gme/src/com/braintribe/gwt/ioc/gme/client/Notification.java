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

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.gme.notification.client.NotifyServiceProcessor;
import com.braintribe.gwt.ioc.gme.client.expert.LoadModelConfig;
import com.braintribe.gwt.ioc.gme.client.expert.LoadModelHandler;
import com.braintribe.gwt.ioc.gme.client.expert.LoadModelViaResourceConfig;
import com.braintribe.gwt.ioc.gme.client.expert.LoadModelViaResourceHandler;
import com.braintribe.gwt.ioc.gme.client.expert.ShowFolderConfig;
import com.braintribe.gwt.ioc.gme.client.expert.ShowFolderHandler;
import com.braintribe.gwt.ioc.gme.client.expert.UrlQueryConfig;
import com.braintribe.gwt.ioc.gme.client.expert.UrlQueryHandler;
import com.braintribe.gwt.notification.client.GmNotificationMapping;
import com.braintribe.gwt.notification.client.GmUrlNotificationPoll;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.utils.client.FastMap;
import com.braintribe.provider.SingletonBeanProvider;

public class Notification {
	
	public static boolean useLoadModel = false;
	
	protected static Supplier<GmUrlNotificationPoll> gmUrlNotificationPoll = new SessionScopedBeanProvider<GmUrlNotificationPoll>() {
		@Override
		public GmUrlNotificationPoll create() throws Exception {
			GmUrlNotificationPoll bean = publish(new GmUrlNotificationPoll());
			bean.setPollHashPart(true);
			bean.setNotificationMappings(notificationMappings.get());
			bean.setSession(Session.persistenceSession.get());
			return bean;
		}
	};

	private static Supplier<Map<String, Supplier<? extends GmNotificationMapping<?>>>> notificationMappings =
			new SessionScopedBeanProvider<Map<String, Supplier<? extends GmNotificationMapping<?>>>>() {
		@Override
		public Map<String, Supplier<? extends GmNotificationMapping<?>>> create() throws Exception {
			Map<String, Supplier<? extends GmNotificationMapping<?>>> bean = publish(new FastMap<>());
//			bean.put("loadAccess", loadAccessConfig);
//			bean.put("loadModelViaResource", loadModelViaResourceConfig);
			if (useLoadModel) {
				bean.put("loadModel", loadModelConfig);
				bean.put("showMapper", showMapperConfig);
				bean.put("modelResource", loadModelViaResourceConfig);
			} else {
				bean.put("showFolder", showFolderConfig);
				bean.put("query", urlQueryConfig);
//				bean.put("displayDocument", loadDocumentConfig);
			}
			return bean;
		}
	};
	
	private static Supplier<GmNotificationMapping<UrlQueryConfig>> urlQueryConfig = new SessionScopedBeanProvider<GmNotificationMapping<UrlQueryConfig>>() {
		@Override
		public GmNotificationMapping<UrlQueryConfig> create() throws Exception {
			GmNotificationMapping<UrlQueryConfig> bean = publish(new GmNotificationMapping<>());
			bean.setEntityClass(UrlQueryConfig.class);
			bean.setNotificationListener(urlQueryHandler.get());
			return bean;
		}
	};
	
	private static Supplier<UrlQueryHandler> urlQueryHandler = new SessionScopedBeanProvider<UrlQueryHandler>() {
		@Override
		public UrlQueryHandler create() throws Exception {
			UrlQueryHandler bean = publish(new UrlQueryHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			Constellations.customizationConstellationProvider.get().addModelEnvironmentSetListener(bean);
			return bean;
		}
	};
	
	private static Supplier<GmNotificationMapping<ShowFolderConfig>> showFolderConfig = new SessionScopedBeanProvider<GmNotificationMapping<ShowFolderConfig>>() {
		@Override
		public GmNotificationMapping<ShowFolderConfig> create() throws Exception{
			GmNotificationMapping<ShowFolderConfig> bean = publish(new GmNotificationMapping<>());
			bean.setEntityClass(ShowFolderConfig.class);
			bean.setNotificationListener(showFolderHandler.get());
			return bean;
		}
	};
	
	private static Supplier<ShowFolderHandler> showFolderHandler = new SessionScopedBeanProvider<ShowFolderHandler>() {
		@Override
		public ShowFolderHandler create() throws Exception {
			ShowFolderHandler bean = publish(new ShowFolderHandler());
			bean.setExplorerConstellation(Constellations.explorerConstellationProvider.get());
			bean.setWorkbenchActionHandlerRegistry(Actions.workbenchActionHandlerRegistry);
			bean.setGmSession(Session.workbenchPersistenceSession.get());
			Constellations.customizationConstellationProvider.get().addModelEnvironmentSetListener(bean);
			return bean;
		}
	};
	
	private static Supplier<GmNotificationMapping<LoadModelConfig>> loadModelConfig = new SessionScopedBeanProvider<GmNotificationMapping<LoadModelConfig>>() {
		@Override
		public GmNotificationMapping<LoadModelConfig> create() throws Exception {
			GmNotificationMapping<LoadModelConfig> bean = publish(new GmNotificationMapping<>());
			bean.setEntityClass(LoadModelConfig.class);
			bean.setNotificationListener(loadModelHandler.get());
			return bean;
		}
	};
	
	private static Supplier<LoadModelHandler> loadModelHandler = new SessionScopedBeanProvider<LoadModelHandler>() {
		@Override
		public LoadModelHandler create() throws Exception {
			LoadModelHandler bean = publish(new LoadModelHandler());			
			bean.setSession(Session.persistenceSession.get());
//			bean.setMasterDetailConstellation(Modeler.modellerMasterDetailConstellationProvider.get());
			bean.setModeler(ModelerNew.standAloneModeler);
			bean.setSessionReadyLoader(ModelerNew.sessionReadyLoader.get());
			return bean;
		}
	};
	
	private static Supplier<GmNotificationMapping<LoadModelConfig>> showMapperConfig = new SessionScopedBeanProvider<GmNotificationMapping<LoadModelConfig>>() {
		@Override
		public GmNotificationMapping<LoadModelConfig> create() throws Exception {
			GmNotificationMapping<LoadModelConfig> bean = publish(new GmNotificationMapping<>());
			bean.setEntityClass(LoadModelConfig.class);
			bean.setNotificationListener(showMapperHandler.get());
			return bean;
		}
	};
	
	private static Supplier<LoadModelHandler> showMapperHandler = new SessionScopedBeanProvider<LoadModelHandler>() {
		@Override
		public LoadModelHandler create() throws Exception {
			LoadModelHandler bean = publish(new LoadModelHandler());
			//bean.setComponent(LoadModelHandlerComponent.smartmapper);
			bean.setSession(Session.persistenceSession.get());
//			bean.setMasterDetailConstellation(Modeler.modellerMasterDetailConstellationProvider.get());
			bean.setModeler(ModelerNew.standAloneModeler);
			return bean;
		}
	};
	
	
	private static Supplier<GmNotificationMapping<LoadModelViaResourceConfig>> loadModelViaResourceConfig = new SessionScopedBeanProvider<GmNotificationMapping<LoadModelViaResourceConfig>>() {
		@Override
		public GmNotificationMapping<LoadModelViaResourceConfig> create() throws Exception {
			GmNotificationMapping<LoadModelViaResourceConfig> bean = publish(new GmNotificationMapping<>());
			bean.setEntityClass(LoadModelViaResourceConfig.class);
			bean.setNotificationListener(loadModelViaResourceHandler.get());
			return bean;
		}
	};
	
	public static Supplier<LoadModelViaResourceHandler> loadModelViaResourceHandler = new SessionScopedBeanProvider<LoadModelViaResourceHandler>() {
		@Override
		public LoadModelViaResourceHandler create() throws Exception {
			LoadModelViaResourceHandler bean = publish(new LoadModelViaResourceHandler());
			bean.setModeler(ModelerNew.standAloneModeler);
			bean.setSession(Session.persistenceSession.get());
//			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
			return bean;
		}
	};
	
	/*protected static Supplier<WebSocketNotificationHandler> webSocketNotificationHandler = new SingletonBeanProvider<WebSocketNotificationHandler>() {
		@Override
		public WebSocketNotificationHandler create() throws Exception {
			WebSocketNotificationHandler bean = new WebSocketNotificationHandler();
			bean.setNotificationFactory(Notifications.notificationFactory);
			return bean;
		}
	};*/
	
	protected static Supplier<NotifyServiceProcessor> notifyServiceProcessor = new SingletonBeanProvider<NotifyServiceProcessor>() {
		@Override
		public NotifyServiceProcessor create() throws Exception {
			NotifyServiceProcessor bean = new NotifyServiceProcessor();
			bean.setNotificationFactorySupplier(Notifications.notificationFactory);
			return bean;
		}
	};
	
	/*
	private static Supplier<GmNotificationMapping<WebReaderConfig>> loadDocumentConfig = new SessionScopedBeanProvider<GmNotificationMapping<WebReaderConfig>>() {
		@Override
		public GmNotificationMapping<WebReaderConfig> create() throws Exception {
			GmNotificationMapping<WebReaderConfig> bean = publish(new GmNotificationMapping<>());
			bean.setEntityClass(WebReaderConfig.class);
			bean.setNotificationListener(loadDocumentHandler.get());
			return bean;
		}
	};
	
	private static Supplier<LoadDocumentHandler> loadDocumentHandler = new SessionScopedBeanProvider<LoadDocumentHandler>() {
		@Override
		public LoadDocumentHandler create() throws Exception {
			LoadDocumentHandler bean = publish(new LoadDocumentHandler());
			bean.setWebReaderProvider(WebReaderSection.standaloneWebReader);
			bean.setAccessId(Runtime.accessId.get());
			bean.setEvaluator(GmRpc.serviceRequestEvaluator.get());
			bean.setSession(Session.persistenceSession.get());
			bean.setWorkbenchSession(Session.workbenchPersistenceSession.get());
//			Constellations.customizationConstellationProvider.get().addModelEnvironmentSetListener(bean);
			bean.setFavIcon(Providers.favIconLoader.get());
			bean.setTitle(Providers.titleLoader.get());
			bean.setApplicationId(Runtime.applicationId);
			return bean;
		}
	};
	*/
	
}
