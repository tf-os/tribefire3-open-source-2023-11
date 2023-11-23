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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.session.GmSession;

public abstract class AbstractIdentityCompetence implements IdentityCompetence {
	protected GmSession session;
	protected GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	
	public AbstractIdentityCompetence(GmSession session) {
		super();
		this.session = session;
	}

	@Override
	public GenericEntity createUnboundInstance(EntityType<?> entityType) throws IdentityCompetenceException {
		return entityType.create();
	}

	@Override
	public void bindInstance(GenericEntity entity) throws IdentityCompetenceException {
		session.attach(entity);
	}
	
}
