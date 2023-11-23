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
package tribefire.extension.metrics.connector.prometheus;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;

import io.micrometer.prometheus.PrometheusMeterRegistry;

public class PrometheusMetricsScrapingEndpoint extends HttpServlet implements LifecycleAware {

	private static Logger logger = Logger.getLogger(PrometheusMetricsScrapingEndpoint.class);

	private static final long serialVersionUID = -651627373901583008L;

	private tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector deployable;

	private PrometheusMeterRegistry prometheusMeterRegistry;

	// -----------------------------------------------------------------------
	// LIFECYCLEAWARE
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		logger.info(() -> "Starting: '" + PrometheusMetricsScrapingEndpoint.class.getSimpleName() + "' of '" + deployable.getName() + "'...");
	}

	@Override
	public void preDestroy() {
		logger.info(() -> "Stopping: '" + PrometheusMetricsScrapingEndpoint.class.getSimpleName() + "' of '" + deployable.getName() + "'!");
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// TODO: improve code quality - only for demo here
		// TODO: set to debug
		logger.debug(() -> {
			String ipAddress = request.getHeader("X-FORWARDED-FOR");
			if (ipAddress == null) {
				ipAddress = request.getRemoteAddr();
			}

			return "Got request for scraping Prometheus request from: '" + ipAddress + "'";
		});

		if (prometheusMeterRegistry == null) {
			logger.debug(() -> "Prometheus Registry not initialized yet. Probably no metrics extension was added!");
			return;
		}

		String scrape = prometheusMeterRegistry.scrape();

		logger.debug(() -> "Scrape: '" + scrape + "'");

		response.setStatus(200);

		try (OutputStream os = response.getOutputStream()) {
			os.write(scrape.getBytes());
		}
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setPrometheusMeterRegistry(PrometheusMeterRegistry prometheusMeterRegistry) {
		this.prometheusMeterRegistry = prometheusMeterRegistry;
	}

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.metrics.model.deployment.connector.PrometheusMetricsConnector deployable) {
		this.deployable = deployable;
	}
}