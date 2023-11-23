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
package com.braintribe.model.queryplan.set;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.value.TupleComponent;

/**
 * 
 * @see IndexRange
 * @see IndexSubSet
 * 
 */
@Abstract
public interface IndexSet extends ReferenceableTupleSet {

	EntityType<IndexSet> T = EntityTypes.T(IndexSet.class);

	/**
	 * @return signature of entity which owns the index (one of it's properties is indexed). This information is needed when resolving type
	 *         information for given {@link TupleComponent}.
	 */
	String getTypeSignature();
	void setTypeSignature(String typeSignature);

	/**
	 * @return name of the indexed property; This information is not really needed, might be usable for debugging purposes.
	 */
	String getPropertyName();
	void setPropertyName(String propertyName);

}
