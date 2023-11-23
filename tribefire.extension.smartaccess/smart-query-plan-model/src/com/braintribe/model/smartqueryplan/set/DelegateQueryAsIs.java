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
package com.braintribe.model.smartqueryplan.set;

import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.queryplan.set.TupleSetType;

/**
 * A tuple set especially for the case when the entire query can be delegated.
 * 
 * This differs from {@link DelegateQuerySet} in that DQS also has mappings which tell how the result from the delegate
 * evaluation is mapped (i.e. how to transform the individual tuple values and where to place them). We avoid this noise
 * in the most simple use-case of delegating the entire query by creating a special instruction that implies that.
 */
public interface DelegateQueryAsIs extends SmartTupleSet {

	EntityType<DelegateQueryAsIs> T = EntityTypes.T(DelegateQueryAsIs.class);

	IncrementalAccess getDelegateAccess();
	void setDelegateAccess(IncrementalAccess delegateAccess);

	SelectQuery getDelegateQuery();
	void setDelegateQuery(SelectQuery delegateQuery);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.extension;
	}

	@Override
	default SmartTupleSetType smartType() {
		return SmartTupleSetType.delegateQueryAsIs;
	}

}
