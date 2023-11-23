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
package tribefire.cortex.initializer.support.impl;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * <p>
 * Clones an entity into a session. In case the entity already exists in the session, it is being skipped.
 *
 */
public class ImportAssemblyCloningContext extends StandardCloningContext {

	private ManagedGmSession session;
	
	public ImportAssemblyCloningContext(ManagedGmSession session) {
		this.session = session;
	}
	
	@Override
	public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
		return session.create(entityType);
	}
	
	@Override
	public <T> T getAssociated(GenericEntity entity) {
		T associated = super.getAssociated(entity);
		
		if (associated != null)
			return associated;
		
		if (entity.session() == session) {
			registerAsVisited(entity, entity);
			return (T)entity;
		}
		
		return null;
	}
}
