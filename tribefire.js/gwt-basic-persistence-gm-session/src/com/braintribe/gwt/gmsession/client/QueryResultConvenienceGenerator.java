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
package com.braintribe.gwt.gmsession.client;

import com.braintribe.model.processing.session.impl.managed.StaticEntityQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticPropertyQueryResultConvenience;
import com.braintribe.model.processing.session.impl.managed.StaticSelectQueryResultConvenience;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

public interface QueryResultConvenienceGenerator {
	
	public StaticSelectQueryResultConvenience generateSelectQueryResultConvenience(SelectQuery query, SelectQueryResult result);
	
	public StaticPropertyQueryResultConvenience generatePropertyQueryResultConvenience(PropertyQuery query, PropertyQueryResult result);
	
	public StaticEntityQueryResultConvenience generateEntityQueryResultConvenience(EntityQuery query, EntityQueryResult result);

}
