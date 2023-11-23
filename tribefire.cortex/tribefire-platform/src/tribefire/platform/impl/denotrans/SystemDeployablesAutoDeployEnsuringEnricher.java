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
package tribefire.platform.impl.denotrans;

import static tribefire.module.api.DenotationEnrichmentResult.allDone;
import static tribefire.module.api.PlatformBindIds.isPlatformBindId;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;

import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.EnvironmentDenotations;
import tribefire.module.api.SimpleDenotationEnricher;
import tribefire.platform.impl.initializer.Edr2ccPostInitializer;

/**
 * Ensures known platform-relevant {@link Deployable}s from {@link EnvironmentDenotations} are auto-deployed.
 * 
 * @see Edr2ccPostInitializer
 * 
 * @author peter.gazdik
 */
public class SystemDeployablesAutoDeployEnsuringEnricher extends SimpleDenotationEnricher<Deployable> {

	private static final Logger log = Logger.getLogger(SystemDeployablesAutoDeployEnsuringEnricher.class);

	public SystemDeployablesAutoDeployEnsuringEnricher() {
		super(Deployable.T);
	}

	@Override
	public DenotationEnrichmentResult<Deployable> enrich(DenotationTransformationContext context, Deployable deployable) {
		if (deployable.getAutoDeploy() || !isPlatformBindId(context.denotationId()))
			return DenotationEnrichmentResult.nothingNowOrEver();

		log.info("Edr2cc: Setting auto-deploy to true for: " + deployable);
		deployable.setAutoDeploy(true);

		return allDone(deployable, "Set autoDeploy=true for " + deployable.entityType().getShortName() + "[" + deployable.getExternalId() + "]");
	}

}
