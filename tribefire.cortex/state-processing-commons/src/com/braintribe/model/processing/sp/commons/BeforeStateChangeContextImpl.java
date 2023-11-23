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

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.EntityProperty;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.sp.api.BeforeStateChangeContext;
import com.braintribe.model.stateprocessing.api.StateChangeProcessorCapabilities;

public class BeforeStateChangeContextImpl<T extends GenericEntity> extends AbstractStateChangeContext<T> implements BeforeStateChangeContext<T> {


	public BeforeStateChangeContextImpl(PersistenceGmSession userSession, PersistenceGmSession systemSession, EntityReference entityReference, EntityProperty entityProperty, Manipulation manipulation) {
		super( userSession, systemSession, entityReference, entityProperty, manipulation);			
	}
	

}
