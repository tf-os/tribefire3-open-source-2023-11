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
package tribefire.extension.shiro.wire.space;

import java.util.Set;

import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.shiro.ShiroConstants;
import com.braintribe.model.processing.shiro.bootstrapping.BootstrappingWorker;
import com.braintribe.model.processing.shiro.bootstrapping.FixedNewUserRolesProvider;
import com.braintribe.model.processing.shiro.bootstrapping.MappedNewUserRolesProvider;
import com.braintribe.model.processing.shiro.bootstrapping.MulticastSessionDao;
import com.braintribe.model.processing.shiro.bootstrapping.NodeSessionIdGenerator;
import com.braintribe.model.processing.shiro.login.LoginServlet;
import com.braintribe.model.processing.shiro.login.SessionValidatorServlet;
import com.braintribe.model.processing.shiro.service.HealthCheckProcessor;
import com.braintribe.model.processing.shiro.service.ShiroServiceProcessor;
import com.braintribe.model.processing.shiro.util.AuthenticationAccessIdSupplier;
import com.braintribe.model.processing.shiro.util.ExternalIconUrlHelper;
import com.braintribe.model.shiro.deployment.FixedNewUserRoleProvider;
import com.braintribe.model.shiro.deployment.Login;
import com.braintribe.model.shiro.deployment.MappedNewUserRoleProvider;
import com.braintribe.model.shiro.deployment.NewUserRoleProvider;
import com.braintribe.model.shiro.deployment.SessionValidator;
import com.braintribe.model.shiro.deployment.ShiroBootstrappingWorker;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.transport.ssl.SslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.EasySslSocketFactoryProvider;
import com.braintribe.transport.ssl.impl.StrictSslSocketFactoryProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.HttpContract;
import tribefire.module.wire.contract.MarshallingContract;
import tribefire.module.wire.contract.RequestProcessingContract;
import tribefire.module.wire.contract.RequestUserRelatedContract;
import tribefire.module.wire.contract.ServletsContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ShiroDeployablesSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(ShiroDeployablesSpace.class);

	@Import
	protected ShiroSpace shiro;

	@Import
	protected RequestUserRelatedContract user;

	@Import
	private ServletsContract servlets;

	@Import
	protected HttpContract http;

	@Import
	private MarshallingContract marshalling;

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private RequestProcessingContract requestProcessing;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		WireSpace.super.onLoaded(configuration);
		multicastSessionDao();
		nodeSessionIdGenerator();
	}

	@Managed
	public ShiroServiceProcessor serviceProcessor(ExpertContext<com.braintribe.model.shiro.deployment.ShiroServiceProcessor> context) {

		com.braintribe.model.shiro.deployment.ShiroServiceProcessor deployable = context.getDeployable();

		ShiroServiceProcessor bean = new ShiroServiceProcessor();
		bean.setConfiguration(deployable.getConfiguration());
		bean.setPathIdentifier(deployable.getPathIdentifier());
		bean.setStaticImagesRelativePath("/res/login-images/");
		bean.setMulticastSessionDao(multicastSessionDao());
		bean.setAuthAccessIdSupplier(authenticationAccessIdSupplier());
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		return bean;
	}

	@Managed
	public LoginServlet loginServlet(ExpertContext<Login> context) {

		Login deployable = context.getDeployable();

		LoginServlet bean = new LoginServlet();
		bean.setServicesUrl(TribefireRuntime.getPublicServicesUrl());
		bean.setCookieHandler(http.cookieHandler());
		bean.setRemoteAddressResolver(servlets.remoteAddressResolver());
		bean.setSessionFactory(tfPlatform.systemUserRelated().sessionFactory());
		bean.setCreateUsers(deployable.getCreateUsers());
		bean.setConfiguration(deployable.getConfiguration());
		bean.setExternalId(deployable.getPathIdentifier());
		bean.setHttpClientProvider(clientProvider());
		Set<String> userAcceptList = getPropertyWithDeprecationWarning("userAcceptList", deployable.getUserAcceptList(), "userWhiteList",
				deployable.getUserWhiteList());
		bean.setUserAcceptList(userAcceptList);
		Set<String> userBlockList = getPropertyWithDeprecationWarning("userBlockList", deployable.getUserBlockList(), "userBlackList",
				deployable.getUserBlackList());
		bean.setUserBlockList(userBlockList);

		com.braintribe.model.processing.shiro.bootstrapping.NewUserRoleProvider provider = context.resolve(deployable.getNewUserRoleProvider(),
				NewUserRoleProvider.T);
		bean.setNewUserRoleProvider(provider);

		bean.setShowStandardLoginForm(deployable.getShowStandardLoginForm());
		bean.setShowTextLinks(deployable.getShowTextLinks());
		bean.setExternalId(deployable.getExternalId());
		bean.setPathIdentifier(deployable.getPathIdentifier());

		bean.setAuthAccessIdSupplier(authenticationAccessIdSupplier());
		bean.setAddSessionParameter(deployable.getAddSessionParameterOnRedirect());
		bean.setStaticImagesRelativePath(ShiroConstants.STATIC_IMAGES_RELATIVE_PATH);
		bean.setEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setExternalIconUrlHelper(externalIconUrlHelper());
		bean.setObfuscateLogOutput(deployable.getObfuscateLogOutput());
		return bean;
	}

	@Managed
	private ExternalIconUrlHelper externalIconUrlHelper() {
		ExternalIconUrlHelper bean = new ExternalIconUrlHelper();
		bean.setHttpClientProvider(clientProvider());
		return bean;
	}

	private Set<String> getPropertyWithDeprecationWarning(String newName, Set<String> newValue, String deprecatedName, Set<String> deprecatedValue) {
		if (newValue != null) {
			return newValue;
		}
		if (deprecatedValue != null) {
			logger.warn(() -> "The deployment property " + deprecatedName + " is deprecated. Please use " + newName + " instead.");
			return deprecatedValue;
		}
		return null;
	}

	@Managed
	public SessionValidatorServlet sessionValidatorServlet(ExpertContext<SessionValidator> context) {

		@SuppressWarnings("unused")
		SessionValidator deployable = context.getDeployable();

		SessionValidatorServlet bean = new SessionValidatorServlet();
		bean.setRequestEvaluator(tfPlatform.requestProcessing().evaluator());
		bean.setMarshallerRegistry(marshalling.registry());
		return bean;
	}

	@Managed
	public BootstrappingWorker bootstrappingWorker(ExpertContext<ShiroBootstrappingWorker> context) {

		ShiroBootstrappingWorker deployable = context.getDeployable();

		BootstrappingWorker bean = new BootstrappingWorker();
		bean.setCortexSessionProvider(tfPlatform.requestUserRelated().cortexSessionSupplier());
		bean.setIdentification(deployable);
		bean.setConfiguration(deployable.getConfiguration());
		bean.setShiroIniFactory(shiro.iniFactory());
		bean.setProxyFilter(shiro.shiroProxyFilter());
		bean.setBootstrapping(shiro.bootstrapping());
		bean.setLogin(deployable.getLogin());
		return bean;
	}

	@Managed
	private MulticastSessionDao multicastSessionDao() {
		MulticastSessionDao bean = new MulticastSessionDao();
		bean.setRequestEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setInstanceIdAsString(tfPlatform.platformReflection().instanceId().stringify());
		MulticastSessionDao.INSTANCE = bean;
		return bean;
	}

	@Managed
	private NodeSessionIdGenerator nodeSessionIdGenerator() {
		NodeSessionIdGenerator bean = new NodeSessionIdGenerator();
		bean.setInstanceIdAsString(tfPlatform.platformReflection().instanceId().stringify());
		NodeSessionIdGenerator.INSTANCE = bean;
		return bean;
	}

	@Managed
	public FixedNewUserRolesProvider fixedNewUserRolesProvider(ExpertContext<FixedNewUserRoleProvider> context) {

		FixedNewUserRoleProvider deployable = context.getDeployable();

		FixedNewUserRolesProvider bean = new FixedNewUserRolesProvider();
		bean.setConfiguredRoles(deployable.getRoles());

		return bean;
	}

	@Managed
	public MappedNewUserRolesProvider mappedNewUserRolesProvider(ExpertContext<MappedNewUserRoleProvider> context) {

		MappedNewUserRoleProvider deployable = context.getDeployable();

		MappedNewUserRolesProvider bean = new MappedNewUserRolesProvider();
		bean.setConfiguredRoles(deployable.getMapping());
		bean.setFields(deployable.getFields());

		return bean;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor(ExpertContext<com.braintribe.model.shiro.deployment.HealthCheckProcessor> context) {

		com.braintribe.model.shiro.deployment.HealthCheckProcessor deployable = context.getDeployable();

		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setAuthAccessIdSupplier(authenticationAccessIdSupplier());
		return bean;

	}

	@Managed
	private AuthenticationAccessIdSupplier authenticationAccessIdSupplier() {
		AuthenticationAccessIdSupplier bean = new AuthenticationAccessIdSupplier();
		bean.setCortexSessionProvider(tfPlatform.systemUserRelated().cortexSessionSupplier());
		return bean;
	}

	@Managed
	private HttpClientProvider clientProvider() {
		DefaultHttpClientProvider bean = new DefaultHttpClientProvider();
		bean.setSslSocketFactoryProvider(sslSocketFactoryProvider());
		return bean;
	}

	@Managed
	private SslSocketFactoryProvider sslSocketFactoryProvider() {
		SslSocketFactoryProvider bean = TribefireRuntime.getAcceptSslCertificates() ? new EasySslSocketFactoryProvider()
				: new StrictSslSocketFactoryProvider();

		return bean;
	}

}
