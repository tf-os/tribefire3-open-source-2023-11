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

import java.util.List;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;

/**
 * Represents a {@link TupleSet} consisting of multiple tuples described by underlying {@link StaticTuple}s. The planner must guarantee that
 * none of these underlying tuples is empty.
 * 
 * @author peter.gazdik
 */
public interface StaticTuples extends SmartTupleSet {

	EntityType<StaticTuples> T = EntityTypes.T(StaticTuples.class);

	List<StaticTuple> getTuples();
	void setTuples(List<StaticTuple> value);

	@Override
	default TupleSetType tupleSetType() {
		return TupleSetType.extension;
	}

	@Override
	default SmartTupleSetType smartType() {
		return SmartTupleSetType.staticTuples;
	}

}
