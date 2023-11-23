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

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.gwt.async.client.RuntimeConfiguration;
import com.braintribe.gwt.browserfeatures.client.UrlParameters;
import com.braintribe.gwt.customizationui.client.startup.TribefireRuntime;
import com.braintribe.gwt.gme.websocket.client.WebSocketUrlProvider;
import com.braintribe.gwt.gmview.client.ViewSelectorExpert;
import com.braintribe.gwt.gmview.client.ViewSituationSelectorExpert;
import com.braintribe.gwt.ioc.client.Configurable;
import com.braintribe.gwt.logging.client.LogLevel;
import com.braintribe.gwt.security.client.SessionScopedBeanProvider;
import com.braintribe.gwt.utils.client.FastSet;
import com.braintribe.model.generic.manipulation.LocalEntityProperty;
import com.braintribe.model.generic.manipulation.accessor.LocalEntityPropertyOwnerAccessor;
import com.braintribe.model.generic.manipulation.accessor.OwnerAccessor;
import com.braintribe.model.generic.validation.expert.ElementMaxLenghtValidator;
import com.braintribe.model.generic.validation.expert.ElementMinLengthValidator;
import com.braintribe.model.generic.validation.expert.MandatoryValidator;
import com.braintribe.model.generic.validation.expert.MaxLenghtValidator;
import com.braintribe.model.generic.validation.expert.MaxValidator;
import com.braintribe.model.generic.validation.expert.MinLengthValidator;
import com.braintribe.model.generic.validation.expert.MinValidator;
import com.braintribe.model.generic.validation.expert.StringRegexpValidator;
import com.braintribe.model.generic.validation.expert.UniqueKeyValidator;
import com.braintribe.model.generic.validation.expert.Validator;
import com.braintribe.model.meta.data.constraint.ElementMaxLength;
import com.braintribe.model.meta.data.constraint.ElementMinLength;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.Max;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.meta.data.constraint.Min;
import com.braintribe.model.meta.data.constraint.MinLength;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.constraint.Unique;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertDefinition;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.provider.SingletonBeanProvider;
import com.braintribe.utils.lcd.CollectionTools;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.sencha.gxt.widget.core.client.container.Container;

public class Runtime {
	
	private static String defaultAccessId = "cortex";
	private static String tribefireServicesContextString = "tribefire-services";
	public static String applicationId = "tribefire-process-designer";
	public static String processDesignerName = "tribefire-process-designer";
	public static boolean handleInitializationUI = true;
	public static boolean useHardwiredAccesses = true;
	public static boolean useGlobalSearchPanel = false;
	private static String clientUrl = null;
	private static String webSocketUrl = "websocket";
	private static String logoutServletUrl = "logout?continue=" + getClientUrl();
	
	@Configurable
	public static void setDefaultAccessId(String defaultAccessId) {
		Runtime.defaultAccessId = defaultAccessId;
	}
	
	@Configurable
	public static void setTribefireServicesContext(String tribefireServicesContext) {
		tribefireServicesContextString = tribefireServicesContext;
	}
	
	@Configurable
	public static void setApplicationId(String applicationId) {
		Runtime.applicationId = applicationId;
	}
	
	/**
	 * Configures whether we should handle the initialization UI (such as displaying the ViewPort for the main panel, masks and so on).
	 * Defaults to true.
	 */
	@Configurable
	public static void setHandleInitializationUI(boolean handleInitializationUI) {
		Runtime.handleInitializationUI = handleInitializationUI;
	}
	
	@Configurable
	public static void setUseHardwiredAccesses(boolean useHardwiredAccesses) {
		Runtime.useHardwiredAccesses = useHardwiredAccesses;
	}
	
	public static Supplier<String> workbenchRootFolderName = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("workbenchRootFolderName", "root");
		}
	};
	
	public static Supplier<String> selectionUseCaseProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return KnownUseCase.selectionUseCase.getDefaultValue();
		}
	};
	
	public static Supplier<String> quickAccessPanelUseCaseProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return KnownUseCase.quickAccessPanelUseCase.getDefaultValue();
		}
	};
	
	public static Supplier<String> logoutServletUrlProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("services.context.logoutServletUrl", tribefireServicesUrl.get() + logoutServletUrl);
		}
	};
	
	public static Supplier<Set<String>> metadataResolverUseCases = new SingletonBeanProvider<Set<String>>() {
		@Override
		public Set<String> create() throws Exception {
			String useCase = metadataResolverUseCase.get();
			return useCase == null ? null : new FastSet(CollectionTools.decodeCollection(useCase, ",", true, false, true, false));
		}
	};
	
	public static Supplier<String> tribefireExplorerUrl = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			// Although the indirection is not needed now, it is kept for the future where, the property name would be added
			//return RuntimeConfiguration.getInstance().getProperty("Add.me", defaultTribefireExplorerUrl.provide());
			return  defaultTribefireExplorerUrl.get();
		}
	};
	
	public static Supplier<String> accessId = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return UrlParameters.getInstance().getParameter("accessId", UrlParameters.getInstance().getParameter("ai", defaultAccessId));
		}
	};
	
	protected static Supplier<String> uiThemeUrlRuntime = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {	
			String url = tribefireServicesAbsoluteUrl.get();
			url = url + "publicResource/dynamic/UiTheme";					 
			return url;
		}
	};
	
	protected static Supplier<String> gmeFavIconUrlRuntime = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {	
			String url = tribefireServicesAbsoluteUrl.get();
			url = url + "publicResource/dynamic/gme-favicon";					 
			return url;
		}
	};
	
	protected static Supplier<String> gmeTitleUrlRuntime = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {	
			String url = tribefireServicesAbsoluteUrl.get();
			url = url + "publicResource/dynamic/gme-title";					 
			return url;
		}
	};
	
	public static Supplier<Boolean> profilingEnablement = new SingletonBeanProvider<Boolean>() {
		@Override
		public Boolean create() throws Exception {
			return Boolean.valueOf(UrlParameters.getInstance().getParameter("profiling", runtimeProfilingEnablement.get()));
		}
	};
	
	public static Supplier<? extends Container> mainPanelProvider = Constellations.customizationConstellationProvider;
	
	public static Supplier<Boolean> platformSetupSupported = new SingletonBeanProvider<Boolean>() {
		@Override
		public Boolean create() throws Exception {
			String platformSetupSupported = TribefireRuntime.getProperty("TRIBEFIRE_PLATFORM_SETUP_SUPPORT", "false");
			return Boolean.TRUE.toString().equalsIgnoreCase(platformSetupSupported);
		}
	};
	
	public static Supplier<String> showHeader = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return UrlParameters.getHashInstance().getParameter(showHeaderUrlParameterNameProvider.get(),
					RuntimeConfiguration.getInstance().getProperty("showHeader", "true"));
		}
	};
	
	public static Supplier<String> assemblyPanelUseCaseProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return KnownUseCase.assemblyPanelUseCase.getDefaultValue();
		}
	};
	
	public static Supplier<String> useWorkbenchWithinTab = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return UrlParameters.getHashInstance().getParameter(workbenchUrlParameterNameProvider.get(),
					RuntimeConfiguration.getInstance().getProperty("workbenchAsTab", "false"));
		}
	};
	
	public static Supplier<String> gimaUseCaseProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return KnownUseCase.gimaUseCase.getDefaultValue();
		}
	};
	
	public static Supplier<String> tribefireServicesAbsoluteUrl = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			String url = tribefireServicesUrl.get();
			if (url.contains("http")) //absolute url
				return url;
			
			String new_url;
			if (Window.Location.getProtocol().contains("https:"))
				new_url = "https:";
			else
				new_url = "http:";
			
			new_url += "//" + Window.Location.getHost();
			new_url += url; //Window.Location.getPath();
			return new_url;
		}
	};
	
	protected static Supplier<WebSocketUrlProvider> webSocketUrlProvider = new SingletonBeanProvider<WebSocketUrlProvider>() {
		@Override
		public WebSocketUrlProvider create() throws Exception {
			WebSocketUrlProvider bean = publish(new WebSocketUrlProvider());
			bean.setWebSocketUrl(com.braintribe.gwt.customizationui.client.startup.TribefireRuntime.getProperty("TRIBEFIRE_WEBSOCKET_URL",
					tribefireServicesAbsoluteUrl.get().replaceFirst("http:", "ws:").replaceFirst("https:", "wss:")) + webSocketUrl);
			bean.setAccessIdProvider(accessId.get());
			bean.setPersistanceSession(Session.persistenceSession.get());
			bean.setSessionId(Providers.sessionIdProvider.get());
			bean.setApplicationId(applicationId);
			return bean;			
		}			
	};
	
	protected static Supplier<String> tribefireServicesUrl = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("services.url", defaultTribefireServicesUrl.get());
		}
	};
	
	public static Supplier<String> logLevel = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return UrlParameters.getInstance().getParameter("logLevel", runtimeLogLevel.get());
		}
	};
	
	private static Supplier<String> defaultTribefireServicesUrl = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			String url = TribefireRuntime.getProperty("TRIBEFIRE_PUBLIC_SERVICES_URL", "/" + tribefireServicesContext.get());
			if (!url.endsWith("/"))
				url = url + "/";

			return url;
		}
	};
	
	private static Supplier<String> tribefireServicesContext = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("services.context.name", tribefireServicesContextString);
		}
	};
	
	private static Supplier<String> showHeaderUrlParameterNameProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("showHeaderUrlParameterName", "showHeader");
		}
	};
	
	private static String getClientUrl() {
		if (clientUrl == null) {
			clientUrl = Location.getHref();
			if (clientUrl.endsWith("/"))
				clientUrl = clientUrl.substring(0, clientUrl.length() - 1);
		}
		
		if(clientUrl.contains("?"))
			clientUrl = clientUrl.replace("?", "%3F");
		
		if(clientUrl.contains("&"))
			clientUrl = clientUrl.replace("&", "%26");
		
		if(clientUrl.contains("#"))
			clientUrl = clientUrl.replace("#", "%23");	
		
		return clientUrl;
	}
	
	private static Supplier<String> workbenchUrlParameterNameProvider = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("workbenchUrlParameterName", "workbenchAsTab");
		}
	};
	
	//New expert registry
	public static Supplier<ConfigurableGmExpertRegistry> gmExpertRegistry = new SessionScopedBeanProvider<ConfigurableGmExpertRegistry>() {
		@Override
		public ConfigurableGmExpertRegistry create() throws Exception {
			ConfigurableGmExpertRegistry bean = publish(new ConfigurableGmExpertRegistry());
			bean.setExpertDefinitions(Arrays.asList(new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(LocalEntityProperty.class);
					setExpertType(OwnerAccessor.class);
					setExpert(localEntityPropertyOwnerAccessor.get());
				}
			}));
			
			bean.setExpertDefinitions(Arrays.asList(new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Unique.class);
					setExpertType(Validator.class);
					setExpert(new UniqueKeyValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Mandatory.class);
					setExpertType(Validator.class);
					setExpert(new MandatoryValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Min.class);
					setExpertType(Validator.class);
					setExpert(new MinValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Max.class);
					setExpertType(Validator.class);
					setExpert(new MaxValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(Pattern.class);
					setExpertType(Validator.class);
					setExpert(new StringRegexpValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(MaxLength.class);
					setExpertType(Validator.class);
					setExpert(new MaxLenghtValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(MinLength.class);
					setExpertType(Validator.class);
					setExpert(new MinLengthValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(ElementMaxLength.class);
					setExpertType(Validator.class);
					setExpert(new ElementMaxLenghtValidator());
				}
			}, new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(ElementMinLength.class);
					setExpertType(Validator.class);
					setExpert(new ElementMinLengthValidator());
				}
			}));
			
			bean.setExpertDefinitions(Arrays.asList(new ConfigurableGmExpertDefinition() {
				{
					setDenotationType(MetaDataSelector.class);
					setExpertType(ViewSelectorExpert.class);
					setExpert(viewSituationSelectorExpert.get());
				}
			}));
			
			return bean;
		}
	};	
	
	private static Supplier<LocalEntityPropertyOwnerAccessor> localEntityPropertyOwnerAccessor = new SingletonBeanProvider<LocalEntityPropertyOwnerAccessor>() {
		@Override
		public LocalEntityPropertyOwnerAccessor create() throws Exception {
			LocalEntityPropertyOwnerAccessor bean = publish(new LocalEntityPropertyOwnerAccessor());
			return bean;
		}
	};
	
	private static Supplier<ViewSituationSelectorExpert> viewSituationSelectorExpert = new SessionScopedBeanProvider<ViewSituationSelectorExpert>() {
		@Override
		public ViewSituationSelectorExpert create() throws Exception {
			ViewSituationSelectorExpert bean = publish(new ViewSituationSelectorExpert());
			bean.setGmSession(Session.persistenceSession.get());
			return bean;
		}
	};
	
	private static Supplier<String> metadataResolverUseCase = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return UrlParameters.getHashInstance().getParameter("useCase", null);
		}
	};
	
	private static Supplier<String> defaultTribefireExplorerUrl = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			String url = TribefireRuntime.getProperty("TRIBEFIRE_EXPLORER_URL", "/tribefire-explorer" );
			return url;
		}
	};
	
	private static Supplier<String> runtimeProfilingEnablement = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("logging.profiling", "false");
		}
	};
	
	private static Supplier<String> runtimeLogLevel = new SingletonBeanProvider<String>() {
		@Override
		public String create() throws Exception {
			return RuntimeConfiguration.getInstance().getProperty("logging.level",
					!GWT.isProdMode() ? LogLevel.PROFILINGDEBUG.toString() : LogLevel.INFO.toString());
		}
	};

}
