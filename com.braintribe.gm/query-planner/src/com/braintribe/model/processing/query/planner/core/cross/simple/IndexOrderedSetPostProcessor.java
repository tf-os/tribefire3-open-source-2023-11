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
package com.braintribe.model.processing.query.planner.core.cross.simple;

import static com.braintribe.utils.SysPrint.spOut;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.model.processing.query.planner.builder.TupleSetBuilder;
import com.braintribe.model.processing.query.planner.context.OrderedSourceDescriptor;
import com.braintribe.model.processing.query.planner.context.QueryOrderingManager;
import com.braintribe.model.processing.query.planner.context.QueryPlannerContext;
import com.braintribe.model.query.From;
import com.braintribe.model.queryplan.index.GeneratedMetricIndex;
import com.braintribe.model.queryplan.set.CartesianProduct;
import com.braintribe.model.queryplan.set.FilteredSet;
import com.braintribe.model.queryplan.set.IndexOrderedSet;
import com.braintribe.model.queryplan.set.MergeLookupJoin;
import com.braintribe.model.queryplan.set.MergeRangeJoin;
import com.braintribe.model.queryplan.set.OperandSet;
import com.braintribe.model.queryplan.set.TupleSet;
import com.braintribe.model.queryplan.set.join.Join;
import com.braintribe.model.queryplan.set.join.PropertyJoin;

/**
 * This is actually a post-processor for the final {@link TupleSet} built based on the query's condition in {@link CrossJoinOrderResolver}. That
 * resolver eagerly uses {@link IndexOrderedSet} for every {@link From} possible. But in some cases it doesn't make sense, as other aspects of the
 * query imply the order would be re-shuffled, for example (relevant when multiple order-bys are in place).
 * 
 * Consider for example this query: <code>SELECT </code>
 * 
 * TODO finish example.
 * 
 * For this reason, we now go over the plan here and check which {@link IndexOrderedSet} are valid (and thus we ommit the ordering in later stages)
 * and which should be simple source-sets instead.
 * 
 * @author peter.gazdik
 */
@SuppressWarnings("unused")
public class IndexOrderedSetPostProcessor {

	public static TupleSet postProcess(QueryPlannerContext context, TupleSet tupleSet) {
		return new IndexOrderedSetPostProcessor(context).postProcess(tupleSet);
	}

	// ####################################################
	// ## . . . . . . . . Implementation . . . . . . . . ##
	// ####################################################

	private final QueryPlannerContext context;
	private final QueryOrderingManager om;

	private int nextOsdIndex = 0;
	private boolean osdStillOk = true;
	private boolean osdAllowed = true;

	private boolean iosChangedToSs = false;

	private IndexOrderedSetPostProcessor(QueryPlannerContext context) {
		this.context = context;
		this.om = context.orderingManager();
	}

	private TupleSet postProcess(TupleSet tupleSet) {
		switch (tupleSet.tupleSetType()) {
			case cartesianProduct:
				return postProcessCartesianProduct((CartesianProduct) tupleSet);

			case indexOrderedSet:
				return postProcessIndexOrderedSet((IndexOrderedSet) tupleSet);

			case mergeLookupJoin:
				return postProcessMergeLookupJoin((MergeLookupJoin) tupleSet);
			case mergeRangeJoin:
				return postMergeRangeJoin((MergeRangeJoin) tupleSet);

			// single operands
			case indexLookupJoin:
			case indexRangeJoin:
				return postProcessSingleOperandSet((Join) tupleSet);
			case filteredSet:
				return postProcessSingleOperandSet((FilteredSet) tupleSet);
			case entityJoin:
			case listJoin:
			case mapJoin:
			case setJoin:
				return postProcessSingleOperandSet((PropertyJoin) tupleSet);

			// no need to change
			case indexRange:
			case indexSubSet:
			case querySourceSet:
			case sourceSet:
			case staticSet:
				return tupleSet;

			// not expected as planner doesn't use them
			case concatenation:
			case intersection:
			case union:
				throw new IllegalStateException("This kind of TupleSet was not expected, last time I checked the planner didn't use them: "
						+ tupleSet.tupleSetType() + " . TupleSet: " + tupleSet);

				// Not expected at this stage
			case aggregatingProjection:
			case distinctSet:
			case extension:
			case orderedSet:
			case orderedSetRefinement:
			case pagination:
			case projection:
				throw new IllegalStateException("This kind of TupleSet was not expected at this stage when only examining the condition: "
						+ tupleSet.tupleSetType() + " . TupleSet: " + tupleSet);
		}

		throw new IllegalArgumentException("Unsupported TupleSet: " + tupleSet + " of type: " + tupleSet.tupleSetType());
	}

	private TupleSet postProcessCartesianProduct(CartesianProduct tupleSet) {
		List<TupleSet> _operands = newList();
		for (TupleSet operand : tupleSet.getOperands())
			_operands.add(postProcess(operand));

		tupleSet.setOperands(_operands);

		return tupleSet;
	}

	private TupleSet postProcessMergeLookupJoin(MergeLookupJoin tupleSet) {
		tupleSet.setOperand(postProcess(tupleSet.getOperand()));
		osdAllowed = false;
		tupleSet.setOtherOperand(postProcess(tupleSet.getOtherOperand()));
		osdAllowed = true;
		
		return tupleSet;
	}

	private TupleSet postMergeRangeJoin(MergeRangeJoin tupleSet) {
		tupleSet.setOperand(postProcess(tupleSet.getOperand()));
		osdAllowed = false;
		GeneratedMetricIndex index = tupleSet.getIndex();
		index.setOperand(postProcess(index.getOperand()));
		osdAllowed = true;
		
		return tupleSet;
	}

	private TupleSet postProcessIndexOrderedSet(IndexOrderedSet ios) {
		OrderedSourceDescriptor osd = om.findOsd(ios);

		if (osdStillOk && //
				(osdStillOk = (osdAllowed && osd.index == nextOsdIndex++))) {
			om.onIosApplied(osd);
			return ios;
		}

		iosChangedToSs = true;
		return TupleSetBuilder.sourceSet(osd.from, context);
	}

	private <T extends TupleSet & OperandSet> TupleSet postProcessSingleOperandSet(T tupleSet) {
		tupleSet.setOperand(postProcess(tupleSet.getOperand()));

		return tupleSet;
	}

}
