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

import java.util.List;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Represent a full <a href="https://en.wikipedia.org/wiki/Cartesian_product">Cartesian product</a> of the passed tuple sets.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select * from Person p, Document d </tt>
 * 
 * <code>
 * CartesianProduct {
 * 		operands: [
 * 			SourceSet ;, 
 * 			SourceSet ;
 * 		]
 * }
 * </code>
 */
public interface CartesianProduct extends TupleSet {

	EntityType<CartesianProduct> T = EntityTypes.T(CartesianProduct.class);

	List<TupleSet> getOperands();
	void setOperands(List<TupleSet> operands);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.cartesianProduct;
	}

}
