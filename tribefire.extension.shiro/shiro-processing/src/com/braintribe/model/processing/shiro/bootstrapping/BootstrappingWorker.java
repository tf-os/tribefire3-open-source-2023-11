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
package com.braintribe.model.processing.shiro.bootstrapping;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.shiro.bootstrapping.ini.ShiroIniFactory;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.model.shiro.deployment.Login;
import com.braintribe.model.shiro.deployment.ShiroAuthenticationConfiguration;
import com.braintribe.utils.lcd.StringTools;

/**
 * Worker that is responsible for finalize the bootstrapping of the ShiroCartridge after everything has been setup and
 * tribefire services is reachable. The functionality will not be available before this worker has been started.
 */
public class BootstrappingWorker implements Worker {

	private static Logger logger = Logger.getLogger(BootstrappingWorker.class);

	private GenericEntity identification;
	private ShiroIniFactory shiroIniFactory;
	private ShiroProxyFilter proxyFilter;
	private ShiroAuthenticationConfiguration configuration;
	private Bootstrapping bootstrapping;
	private Supplier<PersistenceGmSession> cortexSessionProvider;
	private Login login;

	@Override
	public GenericEntity getWorkerIdentification() {
		return identification;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {

		logger.debug(() -> "Starting BootstrappingWorker");

		if (configuration == null || StringTools.isBlank(configuration.getCallbackUrl()) || StringTools.isBlank(configuration.getUnauthorizedUrl())) {
			String publicServicesUrl = TribefireRuntime.getPublicServicesUrl();
			if (!publicServicesUrl.endsWith("/")) {
				publicServicesUrl += "/";
			}

			PersistenceGmSession session = cortexSessionProvider.get();
			ShiroAuthenticationConfiguration sessionConfig = session.query().entities(EntityQueryBuilder.from(ShiroAuthenticationConfiguration.T)
					.where().property(ShiroAuthenticationConfiguration.id).eq(configuration.getId()).done()).first();

			if (StringTools.isBlank(configuration.getCallbackUrl())) {

				logger.debug(() -> "Computing the callback URL");

				String callbackUrl = publicServicesUrl + "component/" + login.getPathIdentifier() + "/auth/callback";
				sessionConfig.setCallbackUrl(callbackUrl);
				session.commit();

				logger.debug(() -> "Computed the callback URL: " + callbackUrl);

			}
			if (StringTools.isBlank(configuration.getUnauthorizedUrl())) {

				logger.debug(() -> "Computing the unauthorized URL");

				String unauthorizedUrl = publicServicesUrl + "component/" + login.getPathIdentifier();
				sessionConfig.setUnauthorizedUrl(unauthorizedUrl);
				session.commit();

				logger.debug(() -> "Computed the unauthorized URL: " + unauthorizedUrl);
			}

			configuration = sessionConfig;
		}

		logger.debug(() -> "Setting the configuration in the ShiroIni Factory");

		shiroIniFactory.setConfiguration(configuration);

		logger.debug(() -> "Setting the path identifier in the ShiroIni Factory: " + login.getPathIdentifier());

		shiroIniFactory.setLoginServletPath(login.getPathIdentifier());

		logger.debug(() -> "Initiating the bootstrapping of Shiro");

		bootstrapping.start();

		logger.debug(
				() -> "Done with finalizing the configuration. Activating the proxy filter (fallbackUrl: " + configuration.getFallbackUrl() + ").");

		proxyFilter.setFallbackUrl(configuration.getFallbackUrl());
		proxyFilter.activate();

		logger.debug(() -> "Done with executing the BootstrappingWorker");
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		// Nothing to do
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public void setIdentification(GenericEntity identification) {
		this.identification = identification;
	}
	public void setShiroIniFactory(ShiroIniFactory shiroIniFactory) {
		this.shiroIniFactory = shiroIniFactory;
	}
	public void setProxyFilter(ShiroProxyFilter proxyFilter) {
		this.proxyFilter = proxyFilter;
	}
	public void setConfiguration(ShiroAuthenticationConfiguration configuration) {
		this.configuration = configuration;
	}
	public void setBootstrapping(Bootstrapping bootstrapping) {
		this.bootstrapping = bootstrapping;
	}
	@Required
	@Configurable
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}
	public void setLogin(Login login) {
		this.login = login;
	}

}
