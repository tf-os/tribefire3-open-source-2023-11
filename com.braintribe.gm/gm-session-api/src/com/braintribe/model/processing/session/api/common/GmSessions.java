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
package com.braintribe.model.processing.session.api.common;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.empty.EmptyModelMdResolver;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

public interface GmSessions {
	
	static ModelMdResolver getMetaData(GenericEntity entity) {
		GmSession session = null;
		if (entity != null)
			session = entity.session();
		
		if (session instanceof ManagedGmSession) {
			ModelAccessory modelAccessory = ((ManagedGmSession) session).getModelAccessory();
			if (modelAccessory != null && modelAccessory.getModel() != null)
				return modelAccessory.getMetaData();
		}
		
		return EmptyModelMdResolver.INSTANCE;
	}
	
	/**
	 * Clones an entity with all its referenced entities into the target session.
	 * 
	 * @param source Entity to be cloned
	 * @param session target session
	 * @return cloned entity in target session
	 */
	static <T extends GenericEntity> T cloneIntoSession(T source, PersistenceGmSession session) {
		return cloneIntoSession(source, session, false);
	}

	/**
	 * Clones an entity with all its referenced entities into the target session.
	 * 
	 * @param source Entity to be cloned
	 * @param session target session
	 * @param transferIdProperties whether to transfer id, partition and globalId properties as well to the cloned entities
	 * @return cloned entity in target session
	 */
	static <T extends GenericEntity> T cloneIntoSession(T source, PersistenceGmSession session, boolean transferIdProperties) {
		return source.clone(new InSessionCloningContext(session, transferIdProperties));
	}
	
}
