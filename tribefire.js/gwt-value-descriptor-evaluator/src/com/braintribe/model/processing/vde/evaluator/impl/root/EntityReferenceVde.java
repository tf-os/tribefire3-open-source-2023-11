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
package com.braintribe.model.processing.vde.evaluator.impl.root;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SessionAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;

/**
 * {@link ValueDescriptorEvaluator} for {@link EntityReference}
 * 
 */
public class EntityReferenceVde implements ValueDescriptorEvaluator<EntityReference> {

	private static EntityReferenceVde instance = null;

	protected EntityReferenceVde() {
		// empty
	}

	public static EntityReferenceVde getInstance() {
		if (instance == null) {
			instance = new EntityReferenceVde();
		}
		return instance;
	}
	
	@Override
	public VdeResult evaluate(VdeContext context, EntityReference valueDescriptor) throws VdeRuntimeException {
		// get the session
		PersistenceGmSession session = context.get(SessionAspect.class);

		if (session == null) {
			if (context.getEvaluationMode() == VdeEvaluationMode.Preliminary)
				return new VdeResultImpl("No session provided in context");
			else
				throw new IllegalStateException("No session provided in context");
		}

		try {
			// query the session to get the entity
			GenericEntity entity = session.query().entity(valueDescriptor).require();
			
			return new VdeResultImpl(entity,false);

		} catch (Exception e) {
			throw new VdeRuntimeException("EntityReference evaluation failed", e);
		}
	}

}
