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
package com.braintribe.model.queryplan.set.join;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.model.queryplan.value.Value;

/**
 * Implicit join for a case where the filtered property has an index. One can view it as a special type of filtered {@link CartesianProduct}
 * with only two operands, and a special type of filter which takes advantage of a property index.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select * from Person p, Company c where p.companyName = c.name</tt>
 * 
 * <code>
 * 	IndexLookupJoin {
 * 		operand: SourceSet ;
 * 		lookupIndex: RepositoryIndex { 
 * 			indexId: "Company_name" *
 * 		}
 * 		lookupValue: ValueProperty {
 * 			value: TupleComponent {
 * 				tupleComponentPosition: SourceSet ;
 * 			}
 * 			propertyPath: "companyName"
 * 		}
 * 	} 
 * * - an index for Company 
 * </code>
 */

public interface IndexLookupJoin extends Join {

	EntityType<IndexLookupJoin> T = EntityTypes.T(IndexLookupJoin.class);

	Index getLookupIndex();
	void setLookupIndex(Index lookupIndex);

	Value getLookupValue();
	void setLookupValue(Value lookupValue);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.indexLookupJoin;
	}

}
