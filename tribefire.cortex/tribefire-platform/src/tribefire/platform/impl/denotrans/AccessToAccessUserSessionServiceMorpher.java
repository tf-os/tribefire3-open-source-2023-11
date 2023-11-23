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

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.model.accessdeployment.IncrementalAccess;

import tribefire.cortex.model.deployment.usersession.service.AccessUserSessionService;
import tribefire.module.api.DenotationTransformationContext;
import tribefire.module.api.SimpleDenotationMorpher;

/**
 * @author peter.gazdik
 */
public class AccessToAccessUserSessionServiceMorpher extends SimpleDenotationMorpher<IncrementalAccess, AccessUserSessionService> {

	public AccessToAccessUserSessionServiceMorpher() {
		super(IncrementalAccess.T, AccessUserSessionService.T);
	}

	@Override
	public Maybe<AccessUserSessionService> morph(DenotationTransformationContext context, IncrementalAccess denotation) {
		return Maybe.complete(context.create(AccessUserSessionService.T));
	}

}
