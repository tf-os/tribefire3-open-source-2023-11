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
package com.braintribe.model.access.smart;

import com.braintribe.model.accessdeployment.smart.meta.PropertyAssignment;
import com.braintribe.model.accessdeployment.smart.meta.SmartUnmapped;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.session.impl.persistence.AbstractPersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.EagerLoader;
import com.braintribe.model.processing.smart.query.planner.structure.ModelExpert;

/**
 * LazyLoader used by {@link SmartAccess} when traversing query result according to given traversing criteria.
 * 
 * @author peter.gazdik
 */
public class SmartEagerLoader extends EagerLoader {

	private final ModelExpert modelExpert;

	public SmartEagerLoader(SmartAccess access, AbstractPersistenceGmSession session, ModelExpert modelExpert) {
		super(access, session);

		this.modelExpert = modelExpert;
	}

	@Override
	protected void eagerlyLoad(GenericEntity entity, Property property) {
		if (isPropertyMapped(entity, property))
			super.eagerlyLoad(entity, property);
		else
			property.setDirect(entity, property.getDefaultRawValue());
	}

	@Override
	protected boolean isCandidateForEagerLoader(GenericEntity owner, Property property) {
		return super.isCandidateForEagerLoader(owner, property) && isPropertyMapped(owner, property);
	}

	private boolean isPropertyMapped(GenericEntity owner, Property property) {
		PropertyAssignment md = getPropertyMappingMd(owner, property);
		return md != null && md.type() != SmartUnmapped.T;
	}

	private PropertyAssignment getPropertyMappingMd(GenericEntity owner, Property property) {
		String partition = owner.getPartition();

		// IncrementalAccess access = modelExpert.getAccess(partition);

		// modelExpert.resolveProp

		// TODO figuring out the use-case might be more complicated - in case the underlying access doesn't set
		// partition directly to accessId
		return modelExpert.cmdResolver.getMetaData() //
				.entity(owner) //
				.property(property) //
				.useCase(partition) //
				.meta(PropertyAssignment.T) //
				.exclusive();
	}

}
