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
package com.braintribe.model.query;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.conditions.Condition;

/**
 * Enriches the query by specified the <code>paging</code> {@link #setPaging(Paging)} and the <code>condition</code>
 * {@link #setCondition(Condition)}. That way one can limit the results of any search to a more desirable set.
 */
public interface Restriction extends GenericEntity {

	EntityType<Restriction> T = EntityTypes.T(Restriction.class);

	Condition getCondition();
	void setCondition(Condition condition);

	Paging getPaging();
	void setPaging(Paging paging);

}
