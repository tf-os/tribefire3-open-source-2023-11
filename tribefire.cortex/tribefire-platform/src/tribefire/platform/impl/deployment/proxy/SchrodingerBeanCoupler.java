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
package tribefire.platform.impl.deployment.proxy;

import java.util.List;

import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.ConfigurableDcProxyDelegation;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * Initializer which sets a correct exernalId or default delegate on the configured {@link SchrodingerBean}s.
 * <p>
 * This must run as the very last initializer, once cortex is prepared. Then it can use SchrodingerBean's
 * {@link SchrodingerBean#deployable(CortexConfiguration, ManagedGmSession)} to access the deployable. If it finds one, it changes the extrnalId of
 * the bean's {@link SchrodingerBean#proxyDelegation() proxy delegation} to the deployable's id. Otherwise
 */
public class SchrodingerBeanCoupler extends SimplePersistenceInitializer {

	private static final Logger log = Logger.getLogger(SchrodingerBeanCoupler.class);

	private List<SchrodingerBean<?>> cortexBeans;

	@Required
	public void setBeans(List<SchrodingerBean<?>> cortexBeans) {
		this.cortexBeans = cortexBeans;
	}

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		ManagedGmSession cortexSession = context.getSession();
		CortexConfiguration cc = cortexSession.getEntityByGlobalId(CortexConfiguration.CORTEX_CONFIGURATION_GLOBAL_ID);

		for (SchrodingerBean<?> bean : cortexBeans) {
			Deployable cortexDeployable = bean.deployable(cc, cortexSession);
			if (cortexDeployable == null)
				throw new IllegalStateException("Cannot initialize SchrodingerBean: " + bean.name() + ", it has not provided a deployable.");

			String externalId = cortexDeployable.getExternalId();
			if (externalId == null)
				throw new IllegalStateException(
						"SchrodingerBean: " + bean.name() + " has a deployable with externalId null. Deployable:" + cortexDeployable);

			log.info("Coupling SchrodingerBean: " + bean.name() + " with externalId: '" + externalId + "'. Deployable: " + cortexDeployable);

			ConfigurableDcProxyDelegation proxyDelegation = bean.proxyDelegation();
			proxyDelegation.changeExternalId(externalId);
		}
	}
}
