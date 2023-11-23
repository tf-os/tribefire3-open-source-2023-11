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
package tribefire.extension.metrics.connector.api;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.deployment.Deployable;

import io.micrometer.core.instrument.MeterRegistry;

public abstract class AbstractMetricsConnector implements MetricsConnector, LifecycleAware {

	private final static Logger logger = Logger.getLogger(AbstractMetricsConnector.class);

	private MeterRegistry registry;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	// TODO: need to close registry - the problem is that the registry comes from wire, since it is managed it then
	// cannot recover....

	@Override
	public void postConstruct() {
		initialize();

		// TODO: config of compoundRegistry
		// TODO: dynamically add/remove meters??? Is this really necesary. I mean, when having it running you have the
		// intention to use the metrics - and not to check if can disable for example a bunch of meters - maybe it make
		// sense, let's see
		// TODO: use all/most of the about page metrics
		// TODO: define strategy with tags

		// ------------------
		// TODO: remove examples
		//@formatter:off
//		Counter counter = compoundRegistry.counter("", "");
//		Timer timer = compoundRegistry.timer("");
//		Integer gauge = compoundRegistry.gauge("", 1);
//		DistributionSummary summary = compoundRegistry.summary("");
		//@formatter:on
		// ------------------

	}

	@Override
	public void preDestroy() {
		registry.clear();
		registry.close();

		logger.info(() -> "Cleared registry of connector: '" + name() + "'");
	}

	// -----------------------------------------------------------------------
	// HEALTH
	// -----------------------------------------------------------------------

	@Override
	public CheckResultEntry health() {
		CheckResultEntry entry = actualHealth();
		return entry;
	}

	protected abstract CheckResultEntry actualHealth();

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public MeterRegistry registry() {
		return registry;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS FOR ADAPTING CONFIGURATION AT RUNTIME
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	protected String defaultName(Deployable deployable) {
		return deployable.getName() + "[" + deployable.getExternalId() + "]";
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Required
	@Configurable
	public void setRegistry(MeterRegistry registry) {
		this.registry = registry;
	}
}
