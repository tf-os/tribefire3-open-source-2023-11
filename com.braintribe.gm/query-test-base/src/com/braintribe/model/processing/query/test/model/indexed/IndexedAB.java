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
package com.braintribe.model.processing.query.test.model.indexed;

import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
@ToStringInformation("${#type_short}[${ambig}]")
public interface IndexedAB extends IndexedA, IndexedB {

	EntityType<IndexedAB> T = EntityTypes.T(IndexedAB.class);

	// @formatter:off
	@Override default void putAmbig(String ambig) { IndexedA.super.putAmbig(ambig); }
	@Override default void putUnique(String unique) { IndexedA.super.putUnique(unique); }
	@Override default void putMetric(String metric) { IndexedA.super.putMetric(metric); }
	// @formatter:on

}
