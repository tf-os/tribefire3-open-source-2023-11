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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.index.Index;
import com.braintribe.model.queryplan.value.ConstantValue;

/**
 * 
 * Represents part of the population retrieved from an index using values for indexed properties.
 * 
 * <h4>Example:</h4>
 * 
 * <tt>select * from Person p where p.father = :fatherInstance</tt>
 * 
 * <code>
 * IndexSubSet* {
 * 		typeSignature: "Person"
 * 		propertyName: father
 * 		lookupIndex: RepositoryIndex {indexId: "person#father"}
 * 		keys: StaticValue {
 * 			values: [:fatherInstance]
 * 		}
 * }
 * * - we assume there exists an index on property Person.father 
 * </code>
 * 
 * <h4>Example2:</h4>
 * 
 * <tt>select * from Person p where p.age in (10, 20, 30, 40)</tt>
 * 
 * <code>
 * IndexSubSet* {
 * 		typeSignature: "Person"
 * 		propertyName: age
 * 		lookupIndex: RepositoryIndex {indexId: "person#age"}
 * 		values = [10, 20, 30, 40]
 * }
 * * - we assume there exists an index on property Person.age 
 * </code>
 * 
 */

public interface IndexSubSet extends IndexSet {

	EntityType<IndexSubSet> T = EntityTypes.T(IndexSubSet.class);

	ConstantValue getKeys();
	void setKeys(ConstantValue keys);

	Index getLookupIndex();
	void setLookupIndex(Index lookupIndex);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.indexSubSet;
	}

}
