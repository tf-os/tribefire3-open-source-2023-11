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
package com.braintribe.model.query.smart.processing.eval.context;

import java.util.Collections;
import java.util.Map;

import com.braintribe.model.processing.query.eval.context.TupleSetDescriptor;
import com.braintribe.model.processing.query.eval.set.join.AbstractEvalPropertyJoin;
import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.smartqueryplan.SmartQueryPlan;
import com.braintribe.model.smartqueryplan.set.DelegateQueryAsIs;
import com.braintribe.model.smartqueryplan.set.SmartTupleSet;

/**
 * 
 */
public class TupleSetDescriptorImpl implements TupleSetDescriptor {

	private final int fullProductComponentsCount;
	private final int resultComponentsCount;

	public TupleSetDescriptorImpl(SmartQueryPlan queryPlan) {
		this.fullProductComponentsCount = queryPlan.getTotalComponentCount();
		this.resultComponentsCount = computeResultComponentCount(queryPlan.getTupleSet());
	}

	private int computeResultComponentCount(TupleSet topLevelTupleSet) {
		TupleSetType type = topLevelTupleSet.tupleSetType();

		switch (type) {
			case pagination:
				return computeResultComponentCount(((PaginatedSet) topLevelTupleSet).getOperand());

			case distinctSet:
				return computeResultComponentCount(((DistinctSet) topLevelTupleSet).getOperand());

			case projection:
			case aggregatingProjection:
				return ((Projection) topLevelTupleSet).getValues().size();

			case concatenation:
			case intersection:
			case union:
				return computeResultComponentCount(((CombinedSet) topLevelTupleSet).getFirstOperand());

			case extension:
				return computeResultComponentCount((SmartTupleSet) topLevelTupleSet);

			default:
				return this.fullProductComponentsCount;
		}
	}

	private int computeResultComponentCount(SmartTupleSet ts) {
		switch (ts.smartType()) {
			case delegateQueryAsIs:
				return ((DelegateQueryAsIs) ts).getDelegateQuery().getSelections().size();
			default:
				return this.fullProductComponentsCount;
		}
	}
	
	
	/**
	 * Note this mapping is only used for evaluation of a right join (see {@link AbstractEvalPropertyJoin}), which is
	 * never part of the smart query plan directly (all joins are delegated of course). So returning an empty map is the
	 * right implementation.
	 */
	@Override
	public Map<Integer, TupleComponentPosition> getComponentPositionMapping() {
		return Collections.emptyMap();
	}

	/**
	 * TODO check what to do here.
	 */
	@Override
	public Map<JoinedMapKey, MapJoin> getMapJoinMapping() {
		return Collections.emptyMap();
	}

	@Override
	public int fullProductComponentsCount() {
		return fullProductComponentsCount;
	}

	@Override
	public int resultComponentsCount() {
		return resultComponentsCount;
	}

}
