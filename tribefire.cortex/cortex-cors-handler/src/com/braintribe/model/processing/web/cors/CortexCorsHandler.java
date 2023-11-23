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
package com.braintribe.model.processing.web.cors;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.cortex.deployment.cors.CorsConfiguration;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import com.braintribe.web.cors.handler.BasicCorsHandler;
import com.braintribe.web.cors.handler.CorsHandler;

/**
 * {@link CorsHandler} implementation which retrieves the {@link CorsConfiguration} from the cortex database on
 * initialization, optionally refreshing it whenever an update is signaled through {@link #accept(Boolean)}.
 * 
 */
public class CortexCorsHandler extends BasicCorsHandler implements Consumer<Boolean>, InitializationAware {

	private Supplier<PersistenceGmSession> gmSessionProvider;

	private static final Logger log = Logger.getLogger(CortexCorsHandler.class);

	public CortexCorsHandler() {
		super();
	}

	@Required
	@Configurable
	public void setGmSessionProvider(Supplier<PersistenceGmSession> gmSessionProvider) {
		this.gmSessionProvider = gmSessionProvider;
	}

	@Override
	public void postConstruct() {
		refreshCorsConfiguration();
	}

	@Override
	public void accept(Boolean updated) {
		if (Boolean.TRUE.equals(updated)) {
			refreshCorsConfiguration();
		}
	}

	protected void refreshCorsConfiguration() {

		CorsConfiguration corsConfiguration = null;
		try {
			corsConfiguration = queryConfiguration();
		} catch (Exception e) {
			log.error("Unable to fetch CORS configuration: " + e.getMessage(), e);
		}

		super.setConfiguration(corsConfiguration);

		if (log.isDebugEnabled()) {
			log.debug("CORS configuration refreshed from cortex database: " + corsConfiguration);
		}

	}

	protected CorsConfiguration queryConfiguration() throws Exception {

		PersistenceGmSession gmSession = null;
		try {
			gmSession = gmSessionProvider.get();
			if (gmSession == null) {
				throw new Exception("null GM session was provided");
			}
		} catch (RuntimeException e) {
			throw new Exception("Failed to obtain a GM session: " + e.getMessage(), e);
		}

		try {
			CortexConfiguration cortexConfig = gmSession.query().entity(CortexConfiguration.T, "singleton").refresh();

			if (cortexConfig == null) {
				throw new Exception("No Cortex configuration was found.");
			}

			if (cortexConfig.getCorsConfiguration() == null) {
				throw new Exception("CORS configuration not set in Cortex configuration: " + cortexConfig);
			}

			return cortexConfig.getCorsConfiguration();

		} catch (GmSessionException e) {
			throw new Exception("Failed to query CORS configuration: " + e.getMessage(), e);
		}
	}

}
