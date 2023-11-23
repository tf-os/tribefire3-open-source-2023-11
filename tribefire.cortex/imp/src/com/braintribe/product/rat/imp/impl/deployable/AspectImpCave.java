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
package com.braintribe.product.rat.imp.impl.deployable;

import com.braintribe.model.cortex.aspect.FulltextAspect;
import com.braintribe.model.cortex.aspect.StateProcessingAspect;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.StateChangeProcessorRule;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;
import com.braintribe.utils.CommonTools;

/**
 * An {@link AbstractImpCave} specialized in {@link AccessAspect}
 */
public class AspectImpCave extends AbstractImpCave<AccessAspect, AspectImp> {

	public AspectImpCave(PersistenceGmSession session) {
		super(session, "externalId", AccessAspect.T);
	}

	@Override
	protected AspectImp buildImp(AccessAspect instance) {
		return new AspectImp(session(), instance);
	}

	public BasicDeployableImp<StateProcessingAspect> createStateProcessingAspect(String name, String externalId,
			StateChangeProcessorRule... processors) {
		StateProcessingAspect accessAspect = createAA(StateProcessingAspect.T, name, externalId);
		accessAspect.setProcessors(CommonTools.getList(processors));
		return new BasicDeployableImp<StateProcessingAspect>(session(), accessAspect);
	}

	public BasicDeployableImp<FulltextAspect> createFulltextAspect(String name, String externalId) {
		FulltextAspect accessAspect = createAA(FulltextAspect.T, name, externalId);
		return new BasicDeployableImp<FulltextAspect>(session(), accessAspect);
	}

	private <T extends AccessAspect> T createAA(EntityType<T> accessType, String name, String externalId) {
		logger.info("Creating access aspect of type '" + accessType.getShortName() + "'");
		T accessAspect = session().create(accessType);
		accessAspect.setName(name);
		accessAspect.setExternalId(externalId);
		return accessAspect;
	}

	public <T extends AccessAspect> AspectImp createAccessAspect(EntityType<T> accessType, String name, String externalId) {
		return new AspectImp(session(), createAA(accessType, name, externalId));
	}

}
