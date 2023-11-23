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

import static tribefire.platform.wire.space.security.services.UserSessionServiceSpace.CLEANUP_USER_SESSIONS_PROCESSOR_ID;
import static tribefire.platform.wire.space.security.services.UserSessionServiceSpace.CLEANUP_USER_SESSIONS_PROCESSOR_NAME;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;

import tribefire.cortex.model.deployment.usersession.cleanup.CleanupUserSessionsProcessor;
import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.SimpleDenotationEnricher;

/**
 * @author peter.gazdik
 */
public class CleanupUserSessionsProcessorEnricher extends SimpleDenotationEnricher<CleanupUserSessionsProcessor> {

	public CleanupUserSessionsProcessorEnricher() {
		super(CleanupUserSessionsProcessor.T);
	}

	@Override
	public DenotationEnrichmentResult<CleanupUserSessionsProcessor> enrich(DenotationTransformationContext context,
			CleanupUserSessionsProcessor denotation) {

		if (!CLEANUP_USER_SESSIONS_PROCESSOR_ID.equals(context.denotationId()))
			return DenotationEnrichmentResult.error( //
					Reasons.build(InvalidArgument.T).text("Unexpected denotationId for AccessCleanupUserSessionsProcessor. Expected: ["
							+ CLEANUP_USER_SESSIONS_PROCESSOR_ID + "], actual: [" + context.denotationId() + "]. Instance: " + denotation).toReason());

		fill(denotation);

		return DenotationEnrichmentResult.allDone(denotation,
				"Configured externalId to [" + CLEANUP_USER_SESSIONS_PROCESSOR_ID + "] and name to [" + CLEANUP_USER_SESSIONS_PROCESSOR_NAME + "]");
	}

	public static void fill(CleanupUserSessionsProcessor denotation) {
		denotation.setGlobalId(CLEANUP_USER_SESSIONS_PROCESSOR_ID);
		denotation.setExternalId(CLEANUP_USER_SESSIONS_PROCESSOR_ID);
		denotation.setName(CLEANUP_USER_SESSIONS_PROCESSOR_NAME);
	}

}
