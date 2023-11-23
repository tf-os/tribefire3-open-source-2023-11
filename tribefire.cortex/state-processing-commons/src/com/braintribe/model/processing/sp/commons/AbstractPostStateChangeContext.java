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
package com.braintribe.model.processing.sp.commons;

import java.util.ArrayList;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.PostStateChangeContext;

public abstract class AbstractPostStateChangeContext<T extends GenericEntity> extends AbstractStateChangeContext<T> implements PostStateChangeContext<T> {
	
	public AbstractPostStateChangeContext(PersistenceGmSession userSession,
			PersistenceGmSession systemSession, EntityReference entityReference, EntityProperty entityProperty,
			Manipulation manipulation) {
		super(userSession, systemSession, entityReference, entityProperty, manipulation);		
	}

	private List<Manipulation> inducedManipulations;
	
	public void setInducedManipulations(List<Manipulation> inducedManipulations) {
		this.inducedManipulations = inducedManipulations;
	}	
	
	public List<Manipulation> getInducedManipulations() {
		if (inducedManipulations == null) {
			inducedManipulations = new ArrayList<Manipulation>();
		}
		return inducedManipulations;
	}

	@Override
	public void notifyInducedManipulation(Manipulation manipulation) {
		getInducedManipulations().add(manipulation);
		
	}
	
}
