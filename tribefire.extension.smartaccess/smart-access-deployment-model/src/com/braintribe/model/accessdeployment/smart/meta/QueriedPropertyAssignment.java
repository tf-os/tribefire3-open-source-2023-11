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
package com.braintribe.model.accessdeployment.smart.meta;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.SelectQuery;

/**
 * @author peter.gazdik
 */
public interface QueriedPropertyAssignment extends VirtualPropertyAssignment {

	EntityType<QueriedPropertyAssignment> T = EntityTypes.T(QueriedPropertyAssignment.class);

	// @formatter:off
	/**
	 * This query is actually a template which (might) contain VDs (which serve as a contextual information, using a special VD for the
	 * enclosing entity of this property - com.braintribe.model.bvd.context.FocusedEntity).
	 */
	SelectQuery getQuery();
	void setQuery(SelectQuery value);
	// @formatter:on

}
