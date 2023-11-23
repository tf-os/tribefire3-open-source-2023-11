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
package com.braintribe.web.servlet.home;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewSolution;
import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.artifact.essential.VersionedArtifactIdentification;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregatable;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregation;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregationKind;
import com.braintribe.model.deploymentapi.check.request.RunCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunDistributedCheckBundles;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.license.License;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.packaging.Packaging;
import com.braintribe.model.platformreflection.request.GetRepositoryViewResolution;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.license.LicenseManager;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.packaging.PackagingProvider;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.OrderingDirection;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;
import com.braintribe.web.servlet.home.model.Home;
import com.braintribe.web.servlet.home.model.Link;
import com.braintribe.web.servlet.home.model.LinkCollection;
import com.braintribe.web.servlet.home.model.LinkGroup;

import tribefire.cortex.model.check.CheckCoverage;

/**
 * Classic landing page of tribefire-services. This is the main page where the user can find links to the most common
 * needed resoures (e.g., Control Center, Explorer, About page, Logs,...). <br>
 * <br>
 * The page is aware of the user logged in. If no user is logged in, the page is nearly empty, save for the login button
 * so that the user can authenticate himself/herself with the server. <br>
 * <br>
 * If available (and deployed), Accesses and WebTerminals are also displayed. <br>
 * <br>
 * If a license is available in the system, it also shown on this page. A warning header will appear if the license
 * expiry date is near (i.e., < 30 days in advance).
 */
public class HomeServlet extends BasicTemplateBasedServlet {

	private static final long serialVersionUID = 4695919181704450507L;
	private static final String landingPageTemplateLocation = "com/braintribe/web/servlet/home/templates/tribefire-services.html.vm";

	private static final Logger logger = Logger.getLogger(HomeServlet.class);

	private static final String USECASE_GME_LOGON = "clientLogon";

	private static final String ACCESS_ID_CORTEX = "cortex";

	// *************************************************************
	// MEMBERS
	// *************************************************************

	private final String platformDomain = "serviceDomain:platform";
	private final String defaultDomain = "serviceDomain:default";

	private static class LinkConfigurerEntry {
		BiConsumer<GenericEntity, LinkCollection> configurer;
		EntityType<?> type;
		String groupIdPattern;

		public LinkConfigurerEntry(BiConsumer<GenericEntity, LinkCollection> configurer, EntityType<?> type, String groupIdPattern) {
			super();
			this.configurer = configurer;
			this.type = type;
			this.groupIdPattern = groupIdPattern;
		}
	}

	private List<LinkConfigurerEntry> configurers = new ArrayList<>();

	/* Packaging */
	private Supplier<Packaging> packagingProvider = new PackagingProvider();

	/* Required system components */
	private Supplier<PersistenceGmSession> cortexSessionFactory;

	private Supplier<PlatformSetup> platformSetupSupplier;

	/* Privileges */
	private Set<String> grantedRoles = Collections.singleton("tf-admin");

	/* Licensing */
	protected int licenseWarningThresholdInD = 30;
	protected String expirationWarning = "Your license expires %1$s.";
	protected String expirationError = "Your license has expired on %1$s.";
	private LicenseManager licenseManager = null;

	/* Application Links */
	private String tfJsUrl = TribefireRuntime.getTribefireJsUrl();
	private String explorerUrl = TribefireRuntime.getExplorerUrl();
	// private String controlCenterUrl = NullSafe.get(TribefireRuntime.getControlCenterUrl(), explorerUrl);
	private String controlCenterUrl = (TribefireRuntime.getControlCenterUrl() == null || TribefireRuntime.getControlCenterUrl().isEmpty())
			? explorerUrl
			: TribefireRuntime.getControlCenterUrl();
	private String modelerUrl = TribefireRuntime.getModelerUrl();

	/* Relative Paths */
	private String relativeLogPath = "logs";
	private String relativeAboutPath = "about";
	private String relativeDeploymentSummaryPath = "deployment-summary";
	private String relativeHealthPath = "healthz"; // TODO CORETS-553 remove as cartridges will soon no longer be
													// supported
	private final String relativePlatformBaseChecksPath = "api/v1/checkPlatform";
	private final String relativePlatformChecksPath = "api/v1/check";
	private String relativeModuleCheckPath = "api/v1/checkDistributed?aggregateBy=node&aggregateBy=bundle&module=";
	private String relativeVitalityCheckPath = "api/v1/checkDistributed?aggregateBy=node&aggregateBy=bundle&coverage=vitality&module=";
	private String relativeSignInPath = "login";

	private String relativeWebTerminalPath = "component";

	/* Static Links */
	private String onlineCompanyUrl = "https://www.braintribe.com";
	private String onlineCompanyImageUrl;
	private String onlineAcademyImageUrl;
	private String onlineAcademyUrl = "https://documentation.tribefire.com";
	/* Default AccessIds */
	private String defaultAuthAccessId = "auth";
	private String defaultUserSessionAccessId = "user-sessions";
	private String defaultUserStatisticsAccessId = "user-statistics";
	private String defaultUserSetupAccessId = "setup";
	private Evaluator<ServiceRequest> systemServiceRequestEvaluator;

	private ModelAccessoryFactory modelAccessoryFactory;

	// *************************************************************
	// SETTERS
	// *************************************************************

	@Configurable
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	/* Session factories */
	@Required
	@Configurable
	public void setCortexSessionFactory(Supplier<PersistenceGmSession> cortexSessionFactory) {
		this.cortexSessionFactory = cortexSessionFactory;
	}
	@Required
	@Configurable
	public void setPlatformSetupSupplier(Supplier<PlatformSetup> platformSetupSupplier) {
		this.platformSetupSupplier = platformSetupSupplier;
	}

	/* Privileges */
	@Configurable
	public void setGrantedRoles(Set<String> grantedRoles) {
		this.grantedRoles = grantedRoles;
	}

	/* Licensing */
	@Configurable
	@Required
	public void setLicenseManager(LicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}

	@Configurable
	public void setPackagingProvider(Supplier<Packaging> packagingProvider) {
		this.packagingProvider = packagingProvider;
	}

	@Configurable
	public void setLicenseWarningThresholdInD(int licenseWarningThresholdInD) {
		this.licenseWarningThresholdInD = licenseWarningThresholdInD;
	}

	@Configurable
	public void setExpirationWarning(String expirationWarning) {
		this.expirationWarning = expirationWarning;
	}

	@Configurable
	public void setExpirationError(String expirationError) {
		this.expirationError = expirationError;
	}

	/* Default AccessIds */
	public void setDefaultAuthAccessId(String defaultAuthAccessId) {
		this.defaultAuthAccessId = defaultAuthAccessId;
	}

	public void setDefaultUserSessionAccessId(String defaultUserSessionAccessId) {
		this.defaultUserSessionAccessId = defaultUserSessionAccessId;
	}

	public void setDefaultUserStatisticsAccessId(String defaultUserStatisticsAccessId) {
		this.defaultUserStatisticsAccessId = defaultUserStatisticsAccessId;
	}

	public void setDefaultUserSetupAccessId(String defaultUserSetupAccessId) {
		this.defaultUserSetupAccessId = defaultUserSetupAccessId;
	}

	/* Application Links */
	@Configurable
	public void setExplorerUrl(String explorerUrl) {
		this.explorerUrl = explorerUrl;
	}

	@Configurable
	public void setControlCenterUrl(String controlCenterUrl) {
		this.controlCenterUrl = controlCenterUrl;
	}

	@Configurable
	public void setTfJsUrl(String tfJsUrl) {
		this.tfJsUrl = tfJsUrl;
	}
	@Configurable
	public void setModelerUrl(String modelerUrl) {
		this.modelerUrl = modelerUrl;
	}

	/* Relative servlet paths */
	@Configurable
	public void setRelativeAboutPath(String aboutUrl) {
		this.relativeAboutPath = aboutUrl;
	}

	@Configurable
	public void setRelativeDeploymentSummaryPath(String deploymentSummaryUrl) {
		this.relativeDeploymentSummaryPath = deploymentSummaryUrl;
	}

	@Configurable
	public void setRelativeLogPath(String logUrl) {
		this.relativeLogPath = logUrl;
	}

	@Configurable
	public void setRelativeSignInPath(String relativeSignInPath) {
		this.relativeSignInPath = relativeSignInPath;
	}

	@Configurable
	public void setRelativeHealthPath(String relativeHealthPath) {
		this.relativeHealthPath = relativeHealthPath;
	}

	@Configurable
	public void setRelativeModuleCheckPath(String relativeModuleCheckPath) {
		this.relativeModuleCheckPath = relativeModuleCheckPath;
	}

	@Configurable
	public void setRelativeVitalityCheckPath(String relativeVitalityCheckPath) {
		this.relativeVitalityCheckPath = relativeVitalityCheckPath;
	}

	@Configurable
	public void setRelativeWebTerminalPath(String relativeWebTerminalPath) {
		this.relativeWebTerminalPath = relativeWebTerminalPath;
	}

	/* Static Links */
	@Configurable
	public void setOnlineAcademyUrl(String onlineAcademyUrl) {
		this.onlineAcademyUrl = onlineAcademyUrl;
	}

	@Configurable
	public void setOnlineCompanyImageUrl(String onlineCompanyImageUrl) {
		this.onlineCompanyImageUrl = onlineCompanyImageUrl;
	}

	@Configurable
	public void setOnlineAcademyImageUrl(String onlineAcademyImageUrl) {
		this.onlineAcademyImageUrl = onlineAcademyImageUrl;
	}

	@Configurable
	public void setCompanyUrl(String companyUrl) {
		this.onlineCompanyUrl = companyUrl;
	}

	@Configurable
	public void setOnlineCompanyUrl(String onlineCompanyUrl) {
		this.onlineCompanyUrl = onlineCompanyUrl;
	}

	@Configurable
	public void setCompanyImageUrl(String companyImageUrl) {
		this.onlineCompanyImageUrl = companyImageUrl;
	}

	@Configurable
	public void setSystemRequestEvaluator(Evaluator<ServiceRequest> serviceRequestEvaluator) {
		this.systemServiceRequestEvaluator = serviceRequestEvaluator;
	}

	@Override
	public void init() {
		setTemplateLocation(landingPageTemplateLocation);
	}

	@Override
	protected VelocityContext createContext(HttpServletRequest request, HttpServletResponse repsonse) {
		/* Check for current user-session */
		UserSession session = AttributeContexts.peek().findOrNull(UserSessionAspect.class);

		Home home = Home.T.create();
		if (isAccessGranted(session)) {

			// handle either tab or overview page.
			boolean wasTab = handleTab(request, home);
			if (!wasTab) {
				handleOverview(home, session);
			}

		}

		/* Prepare the template context. */
		VelocityContext context = new VelocityContext();

		/* Get packaging information */
		Packaging packaging = getPackaging();

		/* Get Release Information */
		Pair<List<VersionedArtifactIdentification>, Boolean> releaseArtifacts = getReleaseArtifacts();

		/* Get license information and fill context */
		License license = getLicensing(context);
		/* Provide additional text in case license is invalid */
		this.provideLicenseExpiryWarning(context, license);

		/* Build full Context */
		context.put("home", home);
		context.put("packaging", packaging);
		context.put("current_year", new GregorianCalendar().get(Calendar.YEAR));
		context.put("tribefireRuntime", TribefireRuntime.class);
		context.put("company_url", onlineCompanyUrl);
		context.put("company_image_url", onlineCompanyImageUrl);
		context.put("academy_url", onlineAcademyUrl);
		context.put("academy_image_url", onlineAcademyImageUrl);
		context.put("loginUrl", relativeSignInPath);

		if (releaseArtifacts != null) {
			context.put("releaseArtifacts", releaseArtifacts.first());
			context.put("hasMoreReleaseArtifacts", releaseArtifacts.second());
		}

		return context;
	}

	private Pair<List<VersionedArtifactIdentification>, Boolean> getReleaseArtifacts() {
		Pair<List<VersionedArtifactIdentification>, Boolean> versionedArtifacts = getReleaseArtifactsFromRepoViewResolution();

		if (versionedArtifacts != null)
			return versionedArtifacts;

		return getReleaseArtifactsFromSetupAsset();
	}

	private Pair<List<VersionedArtifactIdentification>, Boolean> getReleaseArtifactsFromSetupAsset() {
		PlatformSetup platformSetup = platformSetupSupplier.get();
		if (platformSetup == null) {
			logger.info(() -> "Could not find the PlatformSetup.");
			return null;
		}

		PlatformAsset setupAsset = platformSetup.getSetupAsset();
		return Pair.of(Collections.singletonList(VersionedArtifactIdentification.parse(setupAsset.qualifiedRevisionedAssetName())), false);
	}

	private Pair<List<VersionedArtifactIdentification>, Boolean> getReleaseArtifactsFromRepoViewResolution() {
		GetRepositoryViewResolution getRepoViewResolution = GetRepositoryViewResolution.T.create();
		RepositoryViewResolution repositoryViewResolution = getRepoViewResolution.eval(systemServiceRequestEvaluator).get();

		if (repositoryViewResolution == null)
			return null;

		Set<RepositoryViewSolution> visited = new HashSet<>();
		List<RepositoryViewSolution> disjunctReleaseSolutions = new ArrayList<>();

		scanForDisjunctReleases(repositoryViewResolution.getTerminals(), visited, disjunctReleaseSolutions);

		List<String> artifacts = disjunctReleaseSolutions.stream() //
				.map(RepositoryViewSolution::getArtifact) //
				.collect(Collectors.toList()); //

		if (artifacts.isEmpty())
			return null;

		Collections.reverse(artifacts);

		List<VersionedArtifactIdentification> versionedArtifacts = artifacts.stream() //
				.limit(3) //
				.map(VersionedArtifactIdentification::parse) //
				.collect(Collectors.toList());

		boolean hasMore = artifacts.size() > versionedArtifacts.size();

		return Pair.of(versionedArtifacts, hasMore);
	}

	private void scanForDisjunctReleases(List<RepositoryViewSolution> solutions, Set<RepositoryViewSolution> visited,
			List<RepositoryViewSolution> disjunctReleaseSolutions) {

		for (RepositoryViewSolution solution : solutions) {
			if (!visited.add(solution))
				continue; // avoid visits of already visited assets

			if (solution.getRepositoryView().getRelease() != null) {
				disjunctReleaseSolutions.add(solution);
				continue;
			}

			scanForDisjunctReleases(solution.getDependencies(), visited, disjunctReleaseSolutions);
		}
	}

	private License getLicensing(VelocityContext context) {
		License license = null;
		try {
			license = this.licenseManager.getLicense();
			context.put("license", license);
			Date expiry = license.getExpiryDate();
			if (expiry != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				context.put("licenseExpiry", sdf.format(expiry));
			}
		} catch (Exception e) {
			logger.error("Could not load license from license manager.", e);
		}
		return license;
	}

	private void handleGroup(UserSession userSession, Home home, boolean isAdminRequired, Supplier<LinkGroup> groupSupplier) {

		if (!isAdminRequired || isAdminAccessGranted(userSession)) {
			LinkGroup linkGroup = groupSupplier.get();
			home.getGroups().add(linkGroup);
		}
	}

	private class GroupContext {
		PersistenceGmSession cortexSession;
		UserSession userSession;
	}

	private void handleOverview(Home home, UserSession userSession) {
		GroupContext context = new GroupContext();
		context.cortexSession = cortexSessionFactory.get();
		context.userSession = userSession;

		handleGroup(userSession, home, true, () -> buildAdminGroup(context));
		handleGroup(userSession, home, true, () -> buildModelGroup(context));
		handleGroup(userSession, home, false, () -> buildDomainGroup(context));
		handleGroup(userSession, home, true, () -> buildFunctionalModuleGroup(context));
		handleGroup(userSession, home, true, () -> buildConfigurationModuleGroup(context));
		handleGroup(userSession, home, false, () -> buildWebTerminalGroup(context));

	}

	private LinkGroup buildWebTerminalGroup(GroupContext context) {
		LinkGroup webTerminalsGroup = LinkGroup.T.create();
		webTerminalsGroup.setName("Web Terminals");
		webTerminalsGroup.setIconRef("./webpages/images/cortex/webterminal.png");
		webTerminalsGroup.setOpenLink(createLink("Open", ensureTrailingSlash(controlCenterUrl) + "#do=showFolder&par.folderName=Apps",
				"tfControlCenter", null, "./webpages/images/open.png"));

		fillWebTerminalGroup(context, webTerminalsGroup);
		return webTerminalsGroup;
	}
	private LinkGroup buildFunctionalModuleGroup(GroupContext context) {
		LinkGroup modulesGroup = LinkGroup.T.create();
		modulesGroup.setName("Functional Modules");
		modulesGroup.setIconRef("./webpages/images/cortex/modules.png");
		modulesGroup.setOpenLink(createLink("Open", ensureTrailingSlash(controlCenterUrl) + "#do=showFolder&par.folderName=ShowFunctionalModules",
				"tfControlCenter", null, "./webpages/images/open.png"));

		fillFunctionalModulesGroup(context, modulesGroup);
		return modulesGroup;
	}
	private LinkGroup buildConfigurationModuleGroup(GroupContext context) {
		LinkGroup modulesGroup = LinkGroup.T.create();
		modulesGroup.setName("Configuration Modules");
		modulesGroup.setIconRef("./webpages/images/cortex/modules.png");
		modulesGroup.setOpenLink(createLink("Open", ensureTrailingSlash(controlCenterUrl) + "#do=showFolder&par.folderName=ShowConfigurationModules",
				"tfControlCenter", null, "./webpages/images/open.png"));

		fillConfigurationModulesGroup(context, modulesGroup);
		return modulesGroup;
	}

	private LinkGroup buildDomainGroup(GroupContext context) {
		LinkGroup serviceDomainsGroup = LinkGroup.T.create();
		serviceDomainsGroup.setName("Service Domains");
		serviceDomainsGroup.setIconRef("./webpages/images/cortex/domains.png");
		serviceDomainsGroup.setOpenLink(createLink("Open", ensureTrailingSlash(controlCenterUrl) + "#do=showFolder&par.folderName=CustomAccesses",
				"tfControlCenter", null, "./webpages/images/open.png"));

		fillDomainGroup(context, serviceDomainsGroup);
		return serviceDomainsGroup;
	}
	private LinkGroup buildModelGroup(GroupContext context) {
		LinkGroup modelGroup = LinkGroup.T.create();
		modelGroup.setName("Models");
		modelGroup.setIconRef("./webpages/images/cortex/models.png");
		modelGroup.setOpenLink(createLink("Open", ensureTrailingSlash(controlCenterUrl) + "#do=showFolder&par.folderName=CustomModels",
				"tfControlCenter", null, "./webpages/images/open.png"));

		fillModelGroup(context, modelGroup);
		return modelGroup;
	}

	private LinkGroup buildAdminGroup(GroupContext context) {
		LinkGroup adminGroup = LinkGroup.T.create();
		adminGroup.setName("Administration");
		adminGroup.setIconRef("./webpages/images/cortex/settings.png");

		fillAdminGroup(adminGroup, context);
		return adminGroup;
	}

	private void fillAdminGroup(LinkGroup administrationGroup, @SuppressWarnings("unused") GroupContext context) {

		LinkCollection cortexgroup = LinkCollection.T.create();
		cortexgroup.setDisplayName("Cortex");
		cortexgroup.setUrl("controlCenterUrl" + "#default");
		cortexgroup.setTarget("tfControlCenter-cortex");
		cortexgroup.setIconRef("./webpages/images/cortex/tf-cortex.png");

		if (controlCenterUrl != null) {

			cortexgroup.getNestedLinks()
					.add(createLink("Administration", ensureTrailingSlash(controlCenterUrl) + "#default", "tfControlCenter-cortex", null));

		}

		List<BiConsumer<GenericEntity, LinkCollection>> configurers = resolveConfigures(administrationGroup.getName(), IncrementalAccess.T);

		configureAccessLink(context, cortexgroup, configurers, "cortex");

		administrationGroup.getLinks().add(cortexgroup);

		LinkCollection usergroups = LinkCollection.T.create();
		usergroups.setDisplayName("User & Groups");
		usergroups.setUrl("controlCenterUrl" + "?accessId=" + defaultAuthAccessId + "#default");
		usergroups.setTarget("tfControlCenter-auth");
		usergroups.setIconRef("./webpages/images/cortex/groups.png");

		administrationGroup.getLinks().add(usergroups);

		LinkCollection runtimeStatus = LinkCollection.T.create();
		runtimeStatus.setDisplayName("Runtime");
		runtimeStatus.setIconRef("./webpages/images/cortex/runtime.png");

		String masterStatus = getPlatformVitalityStatus();

		runtimeStatus.getNestedLinks().add(createLink("About", "./home?selectedTab=ABOUT&selectedTabPath=" + relativeAboutPath, "_self", null));
		runtimeStatus.getNestedLinks().add(createLink("Health" + masterStatus,
				"./home?selectedTab=HEALTH&selectedTabPath=" + urlEncode(relativePlatformBaseChecksPath), "_self", null));
		runtimeStatus.getNestedLinks()
				.add(createLink("Checks", "./home?selectedTab=HEALTH&selectedTabPath=" + urlEncode(relativePlatformChecksPath), "_self", null));
		runtimeStatus.getNestedLinks().add(
				createLink("Log", "./home?selectedTab=LOGS&selectedTabPath=" + relativeLogPath, "_self", null, "./webpages/images/cortex/logs.png"));
		runtimeStatus.getNestedLinks()
				.add(createLink("Deployment Summary", "./home?selectedTab=DEPLOYMENT SUMMARY&selectedTabPath=" + relativeDeploymentSummaryPath,
						"_self", null, "./webpages/images/cortex/deploy.png"));
		administrationGroup.getLinks().add(runtimeStatus);
		// administrationGroup.getLinks().add(createLink("Logfiles",
		// "./home?selectedTab=LOGS&selectedTabPath="+relativeLogPath, "_self", null,
		// "./webpages/images/cortex/logs.png"));

		LinkCollection setupgroup = LinkCollection.T.create();
		setupgroup.setDisplayName("Platform Setup (Assets)");
		setupgroup.setIconRef("./webpages/images/cortex/asset.png");

		if (controlCenterUrl != null) {
			setupgroup.getNestedLinks()
					.add(createLink("Administration", ensureTrailingSlash(controlCenterUrl) + "?accessId=" + defaultUserSetupAccessId,
							"tfControlCenter-setup", null, "./webpages/images/cortex/asset.png"));

		}

		configureAccessLink(context, setupgroup, configurers, "setup");

		administrationGroup.getLinks().add(setupgroup);

		if (controlCenterUrl != null) {

			usergroups.getNestedLinks()
					.add(createLink("Administration",
							ensureTrailingSlash(controlCenterUrl) + "?accessId=" + urlEncode(defaultAuthAccessId) + "#default",
							"tfControlCenter-auth", null));

			usergroups.getNestedLinks()
					.add(createLink("User Sessions",
							ensureTrailingSlash(controlCenterUrl) + "?accessId=" + urlEncode(defaultUserSessionAccessId) + "#default",
							"tfControlCenter-sessions", null));

			usergroups.getNestedLinks()
					.add(createLink("User Statistics",
							ensureTrailingSlash(controlCenterUrl) + "?accessId=" + urlEncode(defaultUserStatisticsAccessId) + "#default",
							"tfControlCenter-statistics", null));

			/* administrationGroup.getLinks().add(createLink("Asset Setup", ensureTrailingSlash(controlCenterUrl) + "?accessId=" +
			 * defaultUserSetupAccessId, "tfControlCenter-setup", null, "./webpages/images/cortex/asset.png")); */
		}
		/* if (tfJsUrl != null) {
		 *
		 * LinkCollection tribefireJs = LinkCollection.T.create(); tribefireJs.setDisplayName("tribefire.js");
		 * tribefireJs.setIconRef("./webpages/images/cortex/code.png");
		 *
		 * tribefireJs.getNestedLinks().add(createLink("About", ensureTrailingSlash(tfJsUrl)+"#default", "tfJs", null));
		 * tribefireJs.getNestedLinks().add(createLink("runtime.js", ensureTrailingSlash(tfJsUrl)+"/tribefire.js", "_blank",
		 * null)); tribefireJs.getNestedLinks().add(createLink("debug.js", ensureTrailingSlash(tfJsUrl)+"/debug/tribefire.js",
		 * "_blank", null));
		 *
		 * administrationGroup.getLinks().add(tribefireJs); } */
	}

	private void configureAccessLink(GroupContext context, LinkCollection linkCollection, List<BiConsumer<GenericEntity, LinkCollection>> configurers,
			String accessId) {
		//@formatter:off
		SelectQuery queryCortexAccess = new SelectQueryBuilder().from(IncrementalAccess.T, "a")
			.where()
				.property("a", IncrementalAccess.externalId).eq(accessId)
			.select("a")
			.done();
		//@formatter:on

		queryAndConsume(context.cortexSession, queryCortexAccess, (IncrementalAccess a) -> {
			configurers.forEach(c -> c.accept(a, linkCollection));
		});
	}

	private void fillWebTerminalGroup(GroupContext context, LinkGroup webTerminalsGroup) {

		//@formatter:off
		EntityQuery q = 
			EntityQueryBuilder
				.from(HardwiredAccess.T)
				.where()
					.property(HardwiredAccess.externalId).eq(ACCESS_ID_CORTEX)
				.tc()
					.negation().joker()
				.done();
		
		HardwiredAccess cortexAccess = context
					.cortexSession
					.query()
					.entities(q)
					.unique();
		//@formatter:on

		ModelAccessory modelAccessory = modelAccessoryFactory.getForModel(cortexAccess.getMetaModel().getName());
		CmdResolver resolver = modelAccessory.getCmdResolver();

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(WebTerminal.T, "w")
			.select("w")
			.orderBy(OrderingDirection.ascending).property("w", WebTerminal.externalId)
			.done();
		//@formatter:on

		queryAndConsume(context.cortexSession, query, (c) -> {
			WebTerminal w = (WebTerminal) c;
			if (resolver.getMetaData().entity(w).is(Visible.T)) {
				webTerminalsGroup.getLinks().add(createLink(w.getName(), relativeWebTerminalPath + "/" + w.getPathIdentifier(),
						"tfTerminal-" + w.getExternalId(), null, "./webpages/images/cortex/webterminal.png"));
			}

		});
	}
	private void fillFunctionalModulesGroup(GroupContext context, LinkGroup modulesGroup) {

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(Module.T, "m")
			.where()
				.disjunction()
					.property("m", Module.bindsHardwired).eq(true)
					.property("m", Module.bindsDeployables).eq(true)
				.close()
			.select("m", Module.globalId)
			.select("m", Module.name)
			.orderBy(OrderingDirection.ascending).property("m", Module.globalId)
			.done();
		//@formatter:on

		Map<String, String> vitalityCheckPerModule = getDistributedVitalityStatusByModule();
		queryAndConsume(context.cortexSession, query, (m) -> {
			ListRecord lr = (ListRecord) m;
			List<Object> values = lr.getValues();
			String globalId = (String) values.get(0);
			String name = (String) values.get(1);

			LinkCollection links = LinkCollection.T.create();
			links.setIconRef("./webpages/images/cortex/modules.png");

			links.setDisplayName(name == null ? "Unknown Module" : name);

			// links.getNestedLinks().add(createLink("About",
			// "./home?selectedTab=CARTRIDGE&selectedTabPath="+urlEncode("cartridge/"+urlEncode(c.getExternalId())+"/aObout"),
			// "_self", null));
			String vitalityStatus = vitalityCheckPerModule.computeIfAbsent(globalId, s -> " &#x2714;");
			links.getNestedLinks().add(createLink("Health" + vitalityStatus,
					"./home?selectedTab=MODULE&selectedTabPath=" + urlEncode(relativeVitalityCheckPath + urlEncode(globalId)), "_self", null));

			links.getNestedLinks()
					.add(createLink("Checks",
							"./home?selectedTab=FUNCTIONAL MODULE&selectedTabPath=" + urlEncode(relativeModuleCheckPath + urlEncode(globalId)),
							"_self", null));

			modulesGroup.getLinks().add(links);

		});
	}
	private void fillConfigurationModulesGroup(GroupContext context, LinkGroup modulesGroup) {

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(Module.T, "m")
			.where()
				.property("m", Module.bindsInitializers).eq(true)
			.select("m", Module.name)
			.orderBy(OrderingDirection.ascending).property("m", Module.globalId)
			.done();
		//@formatter:on

		queryAndConsume(context.cortexSession, query, (m) -> {

			String name = (String) m;

			LinkCollection links = LinkCollection.T.create();
			links.setIconRef("./webpages/images/cortex/modules.png");

			links.setDisplayName(name == null ? "Unknown Module" : name);

			// links.getNestedLinks().add(createLink("About",
			// "./home?selectedTab=CARTRIDGE&selectedTabPath="+urlEncode("cartridge/"+urlEncode(c.getExternalId())+"/aObout"),
			// "_self", null));

			modulesGroup.getLinks().add(links);

		});
	}

	private String getPlatformVitalityStatus() {
		RunCheckBundles run = RunCheckBundles.T.create();
		run.setCoverage(Collections.singleton(CheckCoverage.vitality));
		run.setIsPlatformRelevant(true);

		CheckBundlesResponse response = run.eval(systemServiceRequestEvaluator).get();

		CheckStatus status = response.getStatus();
		switch (status) {
			case ok:
				return "&nbsp;&#x2714;";
			case warn:
				return "&nbsp;&#x26a0;";
			case fail:
			default:
				return "&nbsp;&#x2716;";
		}
	}

	private Map<String, String> getDistributedVitalityStatusByModule() {
		Map<String, String> res = new HashMap<>();

		RunDistributedCheckBundles run = RunDistributedCheckBundles.T.create();
		run.setCoverage(Collections.singleton(CheckCoverage.vitality));
		run.setIsPlatformRelevant(false);
		run.setAggregateBy(Collections.singletonList(CbrAggregationKind.module));

		CheckBundlesResponse response = run.eval(systemServiceRequestEvaluator).get();

		List<CbrAggregatable> elements = response.getElements();

		if (elements.isEmpty())
			return res;

		for (CbrAggregatable a : response.getElements()) {
			if (a instanceof CbrAggregation) {
				CbrAggregation aggregation = (CbrAggregation) a;
				Module module = (Module) aggregation.getDiscriminator();

				CheckStatus status = aggregation.getStatus();
				String statusRepresentation = getStatusRepresentationAsStr(status);

				res.put(module.getGlobalId(), statusRepresentation);
			}
		}

		return res;
	}

	private String getStatusRepresentationAsStr(CheckStatus status) {
		String statusRepresentation = null;
		switch (status) {
			case ok:
				statusRepresentation = " &#x2714;";
				break;
			case warn:
				statusRepresentation = " &#x26a0;";
				break;
			case fail:
			default:
				statusRepresentation = " &#x2716;";
		}
		return statusRepresentation;
	}

	private static String urlEncode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8");
		} catch (Exception e) {
			logger.warn("Could not URL encode text: " + text);
			return "Unknown";
		}
	}

	private void fillDomainGroup(GroupContext context, LinkGroup serviceDomainsGroup) {

		//@formatter:off
		SelectQuery queryPlatformDomain = new SelectQueryBuilder().from(ServiceDomain.T, "s")
			.where()
				.property("s", ServiceDomain.externalId).eq(platformDomain)
			.select("s")
			.done();
		//@formatter:on

		List<BiConsumer<GenericEntity, LinkCollection>> configurers = resolveConfigures(serviceDomainsGroup.getName(), ServiceDomain.T);

		queryAndConsume(context.cortexSession, queryPlatformDomain, (d) -> {

			ServiceDomain sd = (ServiceDomain) d;

			LinkCollection links = LinkCollection.T.create();
			links.setIconRef("./webpages/images/cortex/tf-cortex.png");

			setDisplayName(sd, links);

			configurers.forEach(c -> c.accept(sd, links));

			if (!links.getNestedLinks().isEmpty()) {
				serviceDomainsGroup.getLinks().add(links);
			}
		});

		//@formatter:off
		SelectQuery queryServiceDomain = new SelectQueryBuilder().from(ServiceDomain.T, "s")
			.select("s")
			.orderBy(OrderingDirection.ascending).property("s", ServiceDomain.externalId)
			.done();
		//@formatter:on

		List<String> wbIds = getListOfWorkbenchAccessIds(context.cortexSession);

		queryAndConsume(context.cortexSession, queryServiceDomain, (r) -> {

			ServiceDomain sd = (ServiceDomain) r;
			String externalId = sd.getExternalId();

			if (sd instanceof Deployable) {
				Deployable deployable = (Deployable) sd;
				if (deployable.getDeploymentStatus() != DeploymentStatus.deployed) {
					return;
				}
			}

			if (!(sd instanceof HardwiredDeployable) && !wbIds.contains(externalId)) {
				LinkCollection links = LinkCollection.T.create();
				if (platformDomain.equals(sd.getExternalId()) || defaultDomain.equals(externalId)) {
					return;
				}
				links.setIconRef("./webpages/images/cortex/domains.png");

				setDisplayName(sd, links);
				if ((sd instanceof IncrementalAccess)) {
					addAccessLinks(context, (IncrementalAccess) sd, links);
				}

				configurers.forEach(c -> c.accept(sd, links));

				if (links.getHasErrors()) {
					links.setDisplayName(links.getDisplayName() + " &#x2757;");
					serviceDomainsGroup.getLinks().add(links);
				} else if (!links.getNestedLinks().isEmpty()) {
					serviceDomainsGroup.getLinks().add(links);
				}
			}

		});
	}

	private boolean isModelVisible(GmMetaModel model, String... useCases) {
		if (model == null)
			return false;

		ModelAccessory modelAccessory = modelAccessoryFactory.getForModel(model.getName());
		CmdResolver resolver = modelAccessory.getCmdResolver();

		ModelMdResolver mdResolver = resolver.getMetaData();
		if (useCases != null && useCases.length > 0) {
			mdResolver.useCases(useCases);
		}
		return mdResolver.is(Visible.T);
	}

	private String tabLink(String title, String path) {
		return "./home?selectedTab=" + title + "&selectedTabPath=" + urlEncode(path);
	}

	private void addAccessLinks(GroupContext context, IncrementalAccess access, LinkCollection links) {

		GmMetaModel model = access.getMetaModel();
		if (model == null) {
			return;
		}

		try {
			if (isModelVisible(model, USECASE_GME_LOGON)) {
				links.getNestedLinks()
						.add(createLink("Explore", ensureTrailingSlash(explorerUrl) + "?accessId=" + urlEncode(access.getExternalId()) + "#default",
								"tfExplorer-" + access.getExternalId(), null));

				IncrementalAccess wbAccess = access.getWorkbenchAccess();
				if (wbAccess != null) {
					links.getNestedLinks()
							.add(createLink("Work&shy;bench",
									ensureTrailingSlash(explorerUrl) + "?accessId=" + urlEncode(wbAccess.getExternalId()) + "#default",
									"tfExplorer-" + wbAccess.getExternalId(), null));
				}
			}
		} catch (Exception e) {
			logger.warn(() -> "Error while trying to get the links for access " + access, e);
			links.setHasErrors(true);
		}
	}

	private void setDisplayName(ServiceDomain d, LinkCollection links) {
		String displayName = d.getName();
		if (displayName == null) {
			displayName = d.getExternalId();
		}
		links.setDisplayName(displayName);
	}

	private List<String> getListOfWorkbenchAccessIds(PersistenceGmSession cortexSession) {
		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(IncrementalAccess.T, "a")
				.join("a", IncrementalAccess.workbenchAccess, "w")
				.select("w", IncrementalAccess.externalId).distinct(true)
				.done();
		//@formatter:off
		List<String> list = cortexSession.query().select(query).list();
		return list;
	}
	
	private void fillModelGroup(GroupContext context, LinkGroup modelsGroup) {

		//@formatter:off
		SelectQuery query = new SelectQueryBuilder().from(GmMetaModel.T, "m")
			.leftJoin("m", GmMetaModel.types, "t")
			.where()
				.negation()
					.disjunction()
						.property("m", GmMetaModel.name).ilike("tribefire.cortex*")
						.property("m", GmMetaModel.name).ilike("com.braintribe*")
					.close()
			.select("m", GmMetaModel.name)
			.select("m", GmMetaModel.version)
			.select().count("t")
			.orderBy(OrderingDirection.ascending).property("m", GmMetaModel.name)
			.done();
		//@formatter:on

		queryAndConsume(context.cortexSession, query, (r) -> {

			ListRecord lr = (ListRecord) r;
			List<Object> values = lr.getValues();
			String modelName = (String) values.get(0);
			String version = (String) values.get(1);
			long typeCount = (Long) values.get(2);

			if (typeCount > 0) {
				LinkCollection links = LinkCollection.T.create();
				links.setIconRef("./webpages/images/cortex/models.png");
				if (StringTools.isEmpty(version)) {
					version = "unknownVersion";

				}

				String[] nameElements = modelName.split(":");
				String shortName = nameElements[nameElements.length - 1];

				String displayName = shortName; // + " (" + version + ")";

				links.setDisplayName(displayName);

				// TODO Remove the Standalone Modeler completely
				// if (modelerUrl != null) {
				// links.getNestedLinks()
				// .add(createLink("Browse",
				// ensureTrailingSlash(modelerUrl) + "#readOnly&do=loadModel&par.modelName=" +
				// urlEncode(m.getName()),
				// "tf-modeler", null));
				// }
				if (controlCenterUrl != null) {
					links.getNestedLinks()
							.add(createLink("Explore",
									ensureTrailingSlash(controlCenterUrl) + "#do=query&par.typeSignature="
											+ urlEncode(GmMetaModel.T.getTypeSignature()) + "&par.propertyName=name&par.propertyValue="
											+ urlEncode(modelName),
									"tfControlCenter", null));
				}
				/* links.getNestedLinks().add(createLink("Browse", ".", "tfModeler", null));
				 * links.getNestedLinks().add(createLink("Exchange Package", ".", "tfModeler", null));
				 * links.getNestedLinks().add(createLink("JSON", ".", "tfModeler", null)); */
				modelsGroup.getLinks().add(links);
			}

		});
	}
	private boolean handleTab(HttpServletRequest request, Home home) {
		String selectedTabParameter = request.getParameter("selectedTab");
		String selectedTabPathParameter = request.getParameter("selectedTabPath");
		if (selectedTabParameter != null && selectedTabPathParameter != null) {
			home.setSelectedTab(createLink(selectedTabParameter, selectedTabPathParameter, null, null));
			return true;
		}
		return false;
	}

	private <T> void queryAndConsume(PersistenceGmSession cortexSession, SelectQuery query, Consumer<T> consumer) {

		try {
			SelectQueryResult queryResult = cortexSession.query().select(query).result();
			List<T> results = (List<T>) queryResult.getResults();
			if (results != null) {
				results.stream().forEach(r -> consumer.accept(r));
			}
		} catch (Exception e) {
			logger.error("Could not execute query " + query, e);
		}

	}

	private void provideLicenseExpiryWarning(VelocityContext context, License license) {
		if (license == null) {
			context.put("licenseWarning", "No active license found.");
		} else {
			Date expiry = license.getExpiryDate();
			if (expiry != null) {
				long exp = expiry.getTime();
				long now = System.currentTimeMillis();
				long untilExpInMs = exp - now;
				long thresholdInMs = ((long) this.licenseWarningThresholdInD) * 24 * 60 * 60 * 1000;
				if (untilExpInMs < thresholdInMs) {
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
					String expiryString = sdf.format(expiry);
					String message = null;
					if (exp < now) {
						message = String.format(expirationError, expiryString);
					} else {
						message = String.format(expirationWarning, expiryString);
					}
					context.put("licenseWarning", message);
				}
			}
		}
	}

	private boolean isAdminAccessGranted(UserSession session) {
		return isAccessGranted(session) && CollectionTools.containsAny(this.grantedRoles, session.getEffectiveRoles());
	}

	private boolean isAccessGranted(UserSession session) {
		return session != null;
	}

	private static Link createLink(String displayName, String url, String target, String type) {
		return createLink(displayName, url, target, type, null);
	}

	private static Link createLink(String displayName, String url, String target, String type, String iconRef) {
		if (url == null || url.isEmpty())
			return null;

		Link repositoryLink = Link.T.create();
		repositoryLink.setDisplayName(displayName);
		repositoryLink.setUrl(url);
		repositoryLink.setTarget(target);
		repositoryLink.setType(type);
		repositoryLink.setIconRef(iconRef);
		return repositoryLink;
	}

	private Packaging getPackaging() {
		try {
			Packaging packaging = packagingProvider.get();
			return packaging;
		} catch (Exception e) {
			logger.error("Could not read packaging information.", e);
			return null;
		}

	}

	private String ensureTrailingSlash(String path) {
		if (path == null) {
			return "/";
		}
		if (path.endsWith("/")) {
			return path;
		}
		return path + "/";
	}

	public <T extends GenericEntity> void addLinkConfigurer(String groupPattern, EntityType<T> type, BiConsumer<T, LinkCollection> configurer) {
		configurers.add(new LinkConfigurerEntry((BiConsumer<GenericEntity, LinkCollection>) configurer, type, groupPattern));
	}

	private List<BiConsumer<GenericEntity, LinkCollection>> resolveConfigures(String groupId, EntityType<?> type) {
		List<BiConsumer<GenericEntity, LinkCollection>> matchingConfigurers = new ArrayList<>();
		for (LinkConfigurerEntry entry : configurers) {
			if (isConfigurerMatching(groupId, type, entry)) {
				matchingConfigurers.add(entry.configurer);
			}
		}
		return matchingConfigurers;
	}

	private boolean isConfigurerMatching(String groupId, EntityType<?> type, LinkConfigurerEntry entry) {
		return (entry.groupIdPattern == null || groupId.matches(entry.groupIdPattern)) && //
				(entry.type == null || entry.type.isAssignableFrom(type));
	}

}
