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
package com.braintribe.model.smartqueryplan.functions;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

import com.braintribe.model.query.Source;
import com.braintribe.model.query.functions.QueryFunction;

/**
 * TODO update javadoc
 * 
 * This function represents the id of given source in the given delegate access. The given source represents a smart
 * entity, which is mapped to some delegate entity in given access. This function is used to retrieve the value of this
 * delegate entity's id, in it's original form without any conversion.
 */
public interface ResolveId extends QueryFunction {

	EntityType<ResolveId> T = EntityTypes.T(ResolveId.class);

	Source getSource();
	void setSource(Source source);

}
