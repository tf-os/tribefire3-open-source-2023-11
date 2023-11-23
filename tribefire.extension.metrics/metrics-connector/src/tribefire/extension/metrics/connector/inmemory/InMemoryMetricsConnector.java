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
package tribefire.extension.metrics.connector.inmemory;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckResultEntry;

import tribefire.extension.metrics.connector.api.AbstractMetricsConnector;

/**
 *
 */
public class InMemoryMetricsConnector extends AbstractMetricsConnector {

	private final static Logger logger = Logger.getLogger(InMemoryMetricsConnector.class);

	private tribefire.extension.metrics.model.deployment.connector.InMemoryMetricsConnector deployable;

	// -----------------------------------------------------------------------
	// LifecycleAware
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	@Override
	public void initialize() {

		// ---------
		// TODO: remove
		//@formatter:off
//		Counter counter = registry.counter("test", "region", "test");

//	    Counter featureCounter = registry().counter("feature", "region", "test");
//	    featureCounter.increment();
//	    
//	    String type = "";
//	    
//        Counter counter = registry().counter("feature.2", "type", type);
//        counter.increment();
//        counter.increment(2.0);
//        
//        Search find = registry().find("feature");
//        Collection<Counter> counters = find.counters();
//        
//        
//        
//        
//        System.out.println("--->>>> " + counter.count());
//        System.out.println("--->>>> " + featureCounter.count());
		//@formatter:on

	}

	@Override
	protected CheckResultEntry actualHealth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String name() {
		return defaultName(deployable);
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.metrics.model.deployment.connector.InMemoryMetricsConnector deployable) {
		this.deployable = deployable;
	}
}
