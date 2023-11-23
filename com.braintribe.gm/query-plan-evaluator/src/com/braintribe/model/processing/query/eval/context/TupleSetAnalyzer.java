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
package com.braintribe.model.processing.query.eval.context;

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;

import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.queryplan.TupleComponentPosition;
import com.braintribe.model.queryplan.set.AggregatingProjection;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.CombinedSet;
import com.braintribe.model.queryplan.set.DistinctSet;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.IndexRange;
import com.braintribe.model.queryplan.set.IndexSubSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.OrderedSet;
import com.braintribe.model.queryplan.set.PaginatedSet;
import com.braintribe.model.queryplan.set.Projection;
import com.braintribe.model.queryplan.set.QuerySourceSet;
import com.braintribe.model.queryplan.set.SourceSet;
import com.braintribe.model.queryplan.set.StaticSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.TupleSetType;
import com.braintribe.model.queryplan.set.join.EntityJoin;
import com.braintribe.model.queryplan.set.join.IndexLookupJoin;
import com.braintribe.model.queryplan.set.join.IndexRangeJoin;
import com.braintribe.model.queryplan.set.join.Join;
import com.braintribe.model.queryplan.set.join.JoinedMapKey;
import com.braintribe.model.queryplan.set.join.ListJoin;
import com.braintribe.model.queryplan.set.join.MapJoin;
import com.braintribe.model.queryplan.set.join.SetJoin;

/**
 * 
 */
public class TupleSetAnalyzer implements TupleSetDescriptor {

	private final Map<Integer, TupleComponentPosition> componentPositionMapping = newMap();
	private final Map<JoinedMapKey, MapJoin> mapJoinMapping = newMap();

	// better name would be workTupleSize, which might be bigger/smaller than the final projection size;
	private int fullProductComponentsCount = 0;
	private int projectionComponentsCount = -1;

	public TupleSetAnalyzer(TupleSet tupleSet) {
		addAllTupleComponents(tupleSet);
		setProjectionComponentCountIfEligible(tupleSet);
	}

	// Might not be needed, see comment on TupleDescriptor
	@Override
	public Map<Integer, TupleComponentPosition> getComponentPositionMapping() {
		return componentPositionMapping;
	}

	@Override
	public Map<JoinedMapKey, MapJoin> getMapJoinMapping() {
		return mapJoinMapping;
	}

	@Override
	public int resultComponentsCount() {
		return projectionComponentsCount >= 0 ? projectionComponentsCount : fullProductComponentsCount();
	}

	@Override
	public int fullProductComponentsCount() {
		return fullProductComponentsCount;
	}

	protected void addAllTupleComponents(TupleSet tupleSet) {
		switch (tupleSet.tupleSetType()) {
			case aggregatingProjection:
				addAllTupleComponents((AggregatingProjection) tupleSet);
				return;
			case cartesianProduct:
				addAllTupleComponents((CartesianProduct) tupleSet);
				return;
			case concatenation:
				addAllTupleComponents((CombinedSet) tupleSet);
				return;
			case distinctSet:
				addAllTupleComponents((DistinctSet) tupleSet);
				return;
			case entityJoin:
				addJoinComponents((EntityJoin) tupleSet);
				return;
			case filteredSet:
				addAllTupleComponents((FilteredSet) tupleSet);
				return;
			case indexLookupJoin:
				addJoinComponents((IndexLookupJoin) tupleSet);
				return;
			case indexOrderedSet:
				addTupleComponentPosition((IndexOrderedSet) tupleSet);
				return;
			case indexRange:
				addTupleComponentPosition((IndexRange) tupleSet);
				return;
			case indexRangeJoin:
				addJoinComponents((IndexRangeJoin) tupleSet);
				return;
			case indexSubSet:
				addTupleComponentPosition((IndexSubSet) tupleSet);
				return;
			case intersection:
				addAllTupleComponents((CombinedSet) tupleSet);
				return;
			case listJoin:
				addAllTupleComponents((ListJoin) tupleSet);
				return;
			case mapJoin:
				addAllTupleComponents((MapJoin) tupleSet);
				return;
			case mergeLookupJoin:
				addAllTupleComponents((MergeLookupJoin) tupleSet);
				return;
			case mergeRangeJoin:
				addAllTupleComponents((MergeRangeJoin) tupleSet);
				return;
			case orderedSet:
			case orderedSetRefinement:
				addAllTupleComponents((OrderedSet) tupleSet);
				return;
			case pagination:
				addAllTupleComponents((PaginatedSet) tupleSet);
				return;
			case projection:
				addAllTupleComponents((Projection) tupleSet);
				return;
			case querySourceSet:
				addTupleComponentPosition((QuerySourceSet) tupleSet);
				return;
			case setJoin:
				addJoinComponents((SetJoin) tupleSet);
				return;
			case sourceSet:
				addTupleComponentPosition((SourceSet) tupleSet);
				return;
			case staticSet:
				addTupleComponentPosition((StaticSet) tupleSet);
				return;
			case union:
				addAllTupleComponents((CombinedSet) tupleSet);
				return;
			case extension:
				return;
		}

		throw new RuntimeQueryEvaluationException("Unsupported TupleSet: " + tupleSet + " of type: " + tupleSet.tupleSetType());
	}

	private void addAllTupleComponents(AggregatingProjection tupleSet) {
		ensureComponentsCountAtLeast(tupleSet.getValues().size());
		addAllTupleComponents(tupleSet.getOperand());
	}

	private void addAllTupleComponents(CartesianProduct tupleSet) {
		for (TupleSet operand : tupleSet.getOperands())
			addAllTupleComponents(operand);
	}

	private void addAllTupleComponents(CombinedSet tupleSet) {
		addAllTupleComponents(tupleSet.getFirstOperand());
	}

	private void addAllTupleComponents(DistinctSet tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
	}

	private void addAllTupleComponents(FilteredSet tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
	}

	private void addAllTupleComponents(ListJoin tupleSet) {
		addJoinComponents(tupleSet);
		addTupleComponentPosition(tupleSet.getListIndex());
	}

	private void addAllTupleComponents(MapJoin tupleSet) {
		addJoinComponents(tupleSet);
		addTupleComponentPosition(tupleSet.getMapKey());
	}

	private void addAllTupleComponents(MergeLookupJoin tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
		addAllTupleComponents(tupleSet.getOtherOperand());
	}

	private void addAllTupleComponents(MergeRangeJoin tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
		addAllTupleComponents(tupleSet.getIndex().getOperand());
	}

	private void addAllTupleComponents(OrderedSet tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
	}

	private void addAllTupleComponents(PaginatedSet tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
	}

	private void addAllTupleComponents(Projection tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
	}

	// ########

	private void addJoinComponents(Join tupleSet) {
		addAllTupleComponents(tupleSet.getOperand());
		addTupleComponentPosition(tupleSet);
	}

	private void addTupleComponentPosition(TupleComponentPosition tupleSet) {
		int index = tupleSet.getIndex();

		ensureComponentsCountAtLeast(index + 1);
		componentPositionMapping.put(index, tupleSet);
	}

	/* It is OK not to add info to componentPositionMapping, it won't be needed, as that is really only needed when resolving the right-side type of a
	 * right-join. See getComponentPositionMapping() of the interfaces (TupleSetDescriptor). */
	private void ensureComponentsCountAtLeast(int size) {
		fullProductComponentsCount = Math.max(size, fullProductComponentsCount);
	}

	// ########

	private void setProjectionComponentCountIfEligible(TupleSet topLevelTupleSet) {
		TupleSetType type = topLevelTupleSet.tupleSetType();

		if (type == TupleSetType.pagination) {
			topLevelTupleSet = ((PaginatedSet) topLevelTupleSet).getOperand();
			type = topLevelTupleSet.tupleSetType();
		}

		if (type == TupleSetType.distinctSet) {
			topLevelTupleSet = ((DistinctSet) topLevelTupleSet).getOperand();
			type = topLevelTupleSet.tupleSetType();
		}

		if (type == TupleSetType.orderedSet) {
			topLevelTupleSet = ((OrderedSet) topLevelTupleSet).getOperand();
			type = topLevelTupleSet.tupleSetType();
		}

		if (type == TupleSetType.filteredSet) {
			topLevelTupleSet = ((FilteredSet) topLevelTupleSet).getOperand();
			type = topLevelTupleSet.tupleSetType();
		}

		if (type == TupleSetType.projection || type == TupleSetType.aggregatingProjection) {
			projectionComponentsCount = ((Projection) topLevelTupleSet).getValues().size();
		}
	}
}
