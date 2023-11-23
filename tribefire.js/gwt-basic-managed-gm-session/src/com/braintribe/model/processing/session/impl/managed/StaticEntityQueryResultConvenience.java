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

import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.managed.EntityQueryResultConvenience;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;

@SuppressWarnings("unusable-by-js")
public class StaticEntityQueryResultConvenience extends AbstractQueryResultConvenience<EntityQuery, EntityQueryResult, EntityQueryResultConvenience> implements EntityQueryResultConvenience {
	private EntityQueryResult result;
	
	public StaticEntityQueryResultConvenience(EntityQuery entityQuery, EntityQueryResult result) {
		super(entityQuery);
		this.result = result;
	}
	
	@Override
	protected EntityQueryResult resultInternal(EntityQuery query) throws GmSessionException{
		return result;
	}

}
