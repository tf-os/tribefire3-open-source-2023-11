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

import java.util.List;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * An {@link Ordering} that contains a list of orderings and can be used to set an order on more than one column. This works in a
 * simple hierarchy. The first property defined in the first instance is ordered first, the second is ordered within this order, and so on.
 */

public interface CascadedOrdering extends Ordering {

	EntityType<CascadedOrdering> T = EntityTypes.T(CascadedOrdering.class);

	void setOrderings(List<SimpleOrdering> orderings);
	List<SimpleOrdering> getOrderings();

}
