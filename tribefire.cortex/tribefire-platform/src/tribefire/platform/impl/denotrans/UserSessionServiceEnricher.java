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

import static tribefire.platform.wire.space.security.services.UserSessionServiceSpace.USER_SESSION_SERVICE_ID;
import static tribefire.platform.wire.space.security.services.UserSessionServiceSpace.USER_SESSION_SERVICE_NAME;

import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;

import tribefire.cortex.model.deployment.usersession.service.UserSessionService;
import tribefire.module.api.DenotationEnrichmentResult;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.SimpleDenotationEnricher;
import tribefire.platform.wire.space.security.services.UserSessionServiceSpace;

/**
 * @author peter.gazdik
 */
public class UserSessionServiceEnricher extends SimpleDenotationEnricher<UserSessionService> {

	public UserSessionServiceEnricher() {
		super(UserSessionService.T);
	}

	@Override
	public DenotationEnrichmentResult<UserSessionService> enrich(DenotationTransformationContext context, UserSessionService denotation) {
		if (!USER_SESSION_SERVICE_ID.equals(context.denotationId()))
			return DenotationEnrichmentResult.error( //
					Reasons.build(InvalidArgument.T).text("Unexpected denotationId for AccessUserSessionService. Expected: ["
							+ USER_SESSION_SERVICE_ID + "], actual: [" + context.denotationId() + "]. Instance: " + denotation).toReason());

		fill(denotation);

		return DenotationEnrichmentResult.allDone(denotation,
				"Configured externalId to [" + UserSessionServiceSpace.USER_SESSION_SERVICE_ID + "] and name to [" + USER_SESSION_SERVICE_NAME + "]");
	}

	public static void fill(UserSessionService denotation) {
		denotation.setGlobalId(USER_SESSION_SERVICE_ID);
		denotation.setExternalId(USER_SESSION_SERVICE_ID);
		denotation.setName(USER_SESSION_SERVICE_NAME);
	}

}
