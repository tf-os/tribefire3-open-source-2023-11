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
package tribefire.platform.wire.space.system.servlets;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static com.braintribe.wire.api.util.Sets.set;

import javax.servlet.Servlet;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.tribefire.connector.RemoteTribefireConnection;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.logs.processor.LogsProcessor;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.GmSessionFactoryBuilderException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.servlet.ResourceServlet;
import com.braintribe.utils.StringTools;
import com.braintribe.web.servlet.about.AboutServlet;
import com.braintribe.web.servlet.about.expert.DiagnosticMultinode;
import com.braintribe.web.servlet.about.expert.Heapdump;
import com.braintribe.web.servlet.about.expert.HotThreadsExpert;
import com.braintribe.web.servlet.about.expert.Json;
import com.braintribe.web.servlet.about.expert.PackagingExpert;
import com.braintribe.web.servlet.about.expert.ProcessesExpert;
import com.braintribe.web.servlet.about.expert.SystemInformation;
import com.braintribe.web.servlet.about.expert.Threaddump;
import com.braintribe.web.servlet.about.expert.TribefireInformation;
import com.braintribe.web.servlet.deploymentreflection.DeploymentReflectionServlet;
import com.braintribe.web.servlet.home.HomeServlet;
import com.braintribe.web.servlet.logs.LogsServlet;
import com.braintribe.web.servlet.publicresource.PublicResourceServlet;
import com.braintribe.web.servlet.publicresource.streamer.FavIconStreamer;
import com.braintribe.web.servlet.publicresource.streamer.LocaleStreamer;
import com.braintribe.web.servlet.publicresource.streamer.LogoStreamer;
import com.braintribe.web.servlet.publicresource.streamer.TitleStreamer;
import com.braintribe.web.servlet.publicresource.streamer.UiThemeStreamer;
import com.braintribe.web.servlet.publicresource.streamer.WorkbenchConfigurationProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.system.HealthzServlet;
import tribefire.platform.impl.userimage.UserImageServlet;
import tribefire.platform.impl.web.CallerInfoFilter;
import tribefire.platform.impl.web.CaptureFilter;
import tribefire.platform.impl.web.ComponentDispatcherServlet;
import tribefire.platform.impl.web.ThreadRenamerFilter;
import tribefire.platform.wire.space.MasterResourcesSpace;
import tribefire.platform.wire.space.bindings.BindingsSpace;
import tribefire.platform.wire.space.common.CartridgeInformationSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.common.RuntimeSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.PlatformSetupAccessSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;
import tribefire.platform.wire.space.module.RequestUserRelatedSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.CurrentUserAuthContextSpace;
import tribefire.platform.wire.space.security.MasterUserAuthContextSpace;
import tribefire.platform.wire.space.system.LicenseSpace;
import tribefire.platform.wire.space.system.SystemInformationSpace;
import tribefire.platform.wire.space.system.TopologySpace;

@Managed
public class SystemServletsSpace implements WireSpace {

	private static Logger logger = Logger.getLogger(SystemServletsSpace.class);

	// @formatter:off
	@Import	private AccessServiceSpace accessService;
	@Import	private BindingsSpace bindings;
	@Import	private CartridgeInformationSpace cartridgeInformation;
	@Import	private CortexAccessSpace cortexAccess;
	@Import	private CurrentUserAuthContextSpace currentUserAuthContext;
	@Import	private EnvironmentSpace environment;
	@Import	private GmSessionsSpace gmSessions;
	@Import	private LicenseSpace license;
	@Import	private MasterUserAuthContextSpace masterUserAuthContext;
	@Import	private PlatformSetupAccessSpace platformSetupAccess;
	@Import	private RequestUserRelatedSpace requestUserRelated;
	@Import	private MasterResourcesSpace resources;
	@Import	private RpcSpace rpc;
	@Import	private RuntimeSpace runtime;
	@Import	private ServletsSpace servlets;
	@Import	private SystemInformationSpace systemInformation;
	@Import	private TopologySpace topology;
	@Import	private WorkerSpace worker;
	// @formatter:on

	@Managed
	public ComponentDispatcherServlet componentServlet() {
		return new ComponentDispatcherServlet();
	}

	@Managed
	public HomeServlet homeServlet() {
		HomeServlet bean = new HomeServlet();
		bean.setCortexSessionFactory(cortexAccess.sessionProvider());
		bean.setSystemRequestEvaluator(rpc.systemServiceRequestEvaluator());
		bean.setPackagingProvider(systemInformation.packagingProvider());
		bean.setLicenseManager(license.manager());
		bean.setGrantedRoles(set("tf-admin", "tf-locksmith"));
		bean.setPlatformSetupSupplier(platformSetupAccess.platformSetupSupplier());

		String relativeSignInPath = TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_TRIBEFIRE_WEB_LOGIN_RELATIVE_PATH);
		if (!StringTools.isBlank(relativeSignInPath)) {
			bean.setRelativeSignInPath(relativeSignInPath);
		}

		bean.setModelAccessoryFactory(requestUserRelated.modelAccessoryFactory());

		String tfs = TribefireRuntime.getPublicServicesUrl();
		if (!tfs.endsWith("/")) {
			tfs += "/";
		}

		bean.setOnlineAcademyImageUrl(tfs + "webpages/academy.svg");
		bean.setOnlineCompanyImageUrl(tfs + "webpages/bt.svg");

		return bean;
	}

	/**
	 * Don't confuse with the below method {@link #publicResourcesServlet()} which is something completely different.
	 */
	@Managed
	public PublicResourceServlet publicResourceServlet() {
		PublicResourceServlet bean = new PublicResourceServlet();
		//@formatter:off
		bean.setResourceProviderRegistry(map(
				entry("UiTheme", uiThemeStreamer()),
				entry("gme-css", uiThemeStreamer()),
				entry("gme-favicon", favIconStreamer()),
				entry("gme-title", titleStreamer()),
				entry("gme-locale",localeStreamer()),
				entry("gme-logo", logoStreamer())
		));
		//@formatter:on
		return bean;
	}

	/**
	 * Don't confuse with the above method {@link #publicResourceServlet()} which is something completely different.
	 */
	@Managed
	public ResourceServlet publicResourcesServlet() {
		ResourceServlet bean = new ResourceServlet();
		bean.setPublicResourcesDirectory(resources.publicResourcesPath());
		return bean;
	}

	@Managed
	public UiThemeStreamer uiThemeStreamer() {
		UiThemeStreamer bean = new UiThemeStreamer();
		bean.setConfigurationProvider(workbenchConfigurationProvider());
		bean.setUserSessionScoping(masterUserAuthContext.userSessionScoping());
		return bean;
	}

	@Managed
	public FavIconStreamer favIconStreamer() {
		FavIconStreamer bean = new FavIconStreamer();
		bean.setConfigurationProvider(workbenchConfigurationProvider());
		bean.setUserSessionScoping(masterUserAuthContext.userSessionScoping());
		bean.setDefaultFavicon(resources.webInf("/Resources/Icons/favicon.ico").asFile());
		return bean;
	}

	@Managed
	public TitleStreamer titleStreamer() {
		TitleStreamer bean = new TitleStreamer();
		bean.setConfigurationProvider(workbenchConfigurationProvider());
		bean.setDefaultTitle("tribefire");
		return bean;
	}

	@Managed
	public LogoStreamer logoStreamer() {
		LogoStreamer bean = new LogoStreamer();
		bean.setConfigurationProvider(workbenchConfigurationProvider());
		bean.setUserSessionScoping(masterUserAuthContext.userSessionScoping());
		return bean;
	}

	@Managed
	public LocaleStreamer localeStreamer() {
		LocaleStreamer bean = new LocaleStreamer();
		bean.setConfigurationProvider(workbenchConfigurationProvider());
		bean.setDefaultLocale("en");
		return bean;
	}

	@Managed
	private WorkbenchConfigurationProvider workbenchConfigurationProvider() {
		WorkbenchConfigurationProvider bean = new WorkbenchConfigurationProvider();
		bean.setAccessService(accessService.service());
		bean.setSessionFactory(gmSessions.systemSessionFactory());
		return bean;
	}

	@Managed
	public AboutServlet aboutServlet() {
		AboutServlet bean = new AboutServlet();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setLiveInstances(topology.liveInstances());
		bean.setLocalInstanceId(cartridgeInformation.instanceId());
		bean.setCurrentUserSessionIdProvider(currentUserAuthContext.userSessionIdProvider());
		bean.setExecutor(worker.threadPool());

		bean.setDiagnosticMultinode(aboutDiagnosticMultinode());
		bean.setThreaddump(aboutThreaddump());
		bean.setHeapdump(aboutHeapdump());
		bean.setJson(aboutJson());
		bean.setPackagingExpert(aboutPackagingExpert());
		bean.setHotThreadsExpert(aboutHotThreadsExpert());
		bean.setProcessesExpert(aboutProcessesExpert());
		bean.setSystemInformation(aboutSystemInformation());
		bean.setTribefireInformation(aboutTribefireInformation());
		return bean;
	}

	@Managed
	private DiagnosticMultinode aboutDiagnosticMultinode() {
		DiagnosticMultinode bean = new DiagnosticMultinode();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		return bean;
	}
	@Managed
	private Threaddump aboutThreaddump() {
		Threaddump bean = new Threaddump();
		return bean;
	}
	@Managed
	private Heapdump aboutHeapdump() {
		Heapdump bean = new Heapdump();
		return bean;
	}
	@Managed
	private Json aboutJson() {
		Json bean = new Json();
		return bean;
	}
	@Managed
	private PackagingExpert aboutPackagingExpert() {
		PackagingExpert bean = new PackagingExpert();
		return bean;
	}
	@Managed
	private HotThreadsExpert aboutHotThreadsExpert() {
		HotThreadsExpert bean = new HotThreadsExpert();
		return bean;
	}
	@Managed
	private ProcessesExpert aboutProcessesExpert() {
		ProcessesExpert bean = new ProcessesExpert();
		return bean;
	}
	@Managed
	private SystemInformation aboutSystemInformation() {
		SystemInformation bean = new SystemInformation();
		return bean;
	}
	@Managed
	private TribefireInformation aboutTribefireInformation() {
		TribefireInformation bean = new TribefireInformation();
		return bean;
	}

	@Managed
	public DeploymentReflectionServlet deploymentReflectionServlet() {
		DeploymentReflectionServlet bean = new DeploymentReflectionServlet();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setCortexSessionFactory(cortexAccess.sessionProvider());
		bean.setNodeFactory(topology.liveInstances());
		return bean;
	}

	@Managed
	public LogsServlet logsServlet() {
		LogsServlet bean = new LogsServlet();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setLiveInstances(topology.liveInstances());
		bean.setLocalInstanceId(cartridgeInformation.instanceId());
		return bean;
	}

	@Managed
	public LogsProcessor logsProcessor() {
		LogsProcessor bean = new LogsProcessor();

		bean.setUserNameProvider(currentUserAuthContext.userNameProvider());
		bean.setLocalInstanceId(cartridgeInformation.instanceId());
		return bean;
	}

	// Test streamer:
	// @Managed
	// public StaticResourceStreamer testUiThemeStreamer() throws Exception {
	// StaticResourceStreamer bean = new StaticResourceStreamer();
	// bean.setResource(resources.webInf("/Resources/UiThemes/DefaultTheme.css").asFile());
	// return bean;
	// }

	@Managed
	public ThreadRenamerFilter threadRenamerFilter() {
		ThreadRenamerFilter bean = new ThreadRenamerFilter();
		bean.setThreadRenamer(runtime.threadRenamer());
		return bean;
	}

	@Managed
	public HealthzServlet healthzServlet() {
		HealthzServlet bean = new HealthzServlet();
		return bean;
	}

	@Managed
	public Servlet userImageServlet() {
		UserImageServlet bean = new UserImageServlet();

		// TODO: This has to be replaced by a CortexConfiguration-based deployable that can be looked up

		GenericEntity denotation = environment.environmentDenotations().lookup(bindings.environmentSecurityConnection());
		if (denotation instanceof RemoteTribefireConnection) {
			RemoteTribefireConnection rtc = (RemoteTribefireConnection) denotation;

			String url = rtc.getServicesUrl();
			Credentials cred = rtc.getCredentials();

			logger.debug(() -> "Creating remote session factory to " + url);

			PersistenceGmSessionFactory sessionFactory;
			try {
				sessionFactory = GmSessionFactories.remote(url).authentication(cred).done();
			} catch (GmSessionFactoryBuilderException e) {
				throw Exceptions.unchecked(e, "Error while trying to create a remote session factory to " + url);
			}

			// PersistenceGmSessionFactory factory = (PersistenceGmSessionFactory) deployment.registry().resolve(rtc);
			bean.setSessionFactory(sessionFactory);
		} else {
			bean.setSessionFactory(gmSessions.systemSessionFactory());
		}

		return bean;
	}

	@Managed
	public CallerInfoFilter callerInfoFilter() {
		CallerInfoFilter bean = new CallerInfoFilter();
		bean.setRemoteAddressResolver(servlets.remoteAddressResolver());
		return bean;
	}
	
	@Managed
	public CaptureFilter captureFilter() {
		CaptureFilter bean = new CaptureFilter();
		bean.setCaptureDir(resources.tmp("servlet-response-captures").asFile());
		return bean;
	}


}
