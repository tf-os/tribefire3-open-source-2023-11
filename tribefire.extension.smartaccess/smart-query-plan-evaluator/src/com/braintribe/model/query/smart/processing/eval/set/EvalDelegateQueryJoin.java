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
package com.braintribe.model.query.smart.processing.eval.set;

import static com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools.emptyTupleIterator;
import static com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools.moreHas;
import static com.braintribe.model.query.smart.processing.eval.set.DelegateQuerySetAdapter.adaptQuerySet;
import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.HasMoreAwareSet;
import com.braintribe.model.processing.query.eval.set.base.TransientGeneratorEvalTupleSet;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.query.smart.processing.eval.tools.SmartQueryEvaluationTools;
import com.braintribe.model.queryplan.value.TupleComponent;
import com.braintribe.model.queryplan.value.Value;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.DelegateQueryJoin;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;
import com.braintribe.model.smartqueryplan.set.OperandRestriction;
import com.braintribe.model.smartqueryplan.value.ConvertedValue;

/**
 * 
 */
public class EvalDelegateQueryJoin extends TransientGeneratorEvalTupleSet implements HasMoreAwareSet {

	protected int bulkSize = 100;

	protected final SmartQueryEvaluationContext smartContext;
	protected final DelegateQueryJoin delegateQueryJoin;
	protected final EvalTupleSet materializedTupleSet;
	protected final boolean isLeftJoin;

	public EvalDelegateQueryJoin(DelegateQueryJoin delegateQueryJoin, SmartQueryEvaluationContext context) {
		super(context);

		this.smartContext = context;
		this.delegateQueryJoin = delegateQueryJoin;
		this.materializedTupleSet = smartContext.resolveTupleSet(delegateQueryJoin.getMaterializedSet());
		this.isLeftJoin = delegateQueryJoin.getIsLeftJoin();
	}

	public void setBulkSize(int bulkSize) {
		this.bulkSize = bulkSize;
	}

	@Override
	public boolean hasMore() {
		return moreHas(materializedTupleSet);
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new DelegateQueryJoinIterator();
	}

	protected class DelegateQueryJoinIterator extends AbstractTupleIterator {
		protected final Map<Tuple, List<Tuple>> correlationMap;

		private final Iterator<Tuple> materializedIterator;
		private Iterator<Tuple> bulked_M_TuplesIterator = emptyTupleIterator();

		private Tuple current_M_Tuple;
		private Iterator<Tuple> current_Q_Tuples = emptyTupleIterator();

		private boolean useNullForNextJoinedValue;

		public DelegateQueryJoinIterator() {
			correlationMap = SmartQueryEvaluationTools.tupleCorrelationMap(findCorrelationPositions());
			materializedIterator = materializedTupleSet.iterator();

			prepareNextValue();
		}

		private List<Integer> findCorrelationPositions() {
			List<Integer> result = newList();

			for (OperandRestriction restriction: delegateQueryJoin.getJoinRestrictions()) 
				result.add(extractPosition(restriction.getMaterializedCorrelationValue()));

			return result;
		}

		private Integer extractPosition(Value value) {
			if (value instanceof ConvertedValue)
				return extractPosition(((ConvertedValue) value).getOperand());

			else if (value instanceof TupleComponent)
				return ((TupleComponent) value).getTupleComponentIndex();

			throw new RuntimeQueryEvaluationException("Unsupported Value: " + value + " of type: " + value.valueType());
		}

		@Override
		protected void prepareNextValue() {
			if (useNullForNextJoinedValue) {
				next = createleftJoinTuple(current_M_Tuple);
				return;
			}

			if (current_Q_Tuples == null) {
				// this states indicates the iterator has no more values at all
				next = null;
				return;
			}

			if (current_Q_Tuples.hasNext()) {
				next = mergeTuples(current_M_Tuple, current_Q_Tuples.next());
				return;
			}

			ensureNextBulked_M_Tuple();
			prepareNextValue();
		}

		private Tuple createleftJoinTuple(Tuple mTuple) {
			useNullForNextJoinedValue = false;
			singletonTuple.acceptAllValuesFrom(mTuple);

			return singletonTuple;
		}

		private Tuple mergeTuples(Tuple mTuple, Tuple qTuple) {
			singletonTuple.acceptAllValuesFrom(mTuple);

			for (ScalarMapping sm: delegateQueryJoin.getQuerySet().getScalarMappings()) {
				int index = sm.getTupleComponentIndex();
				singletonTuple.setValueDirectly(index, qTuple.getValue(index));
			}

			return singletonTuple;
		}

		private void ensureNextBulked_M_Tuple() {
			while (!prepareNextBulked_M_Tuple() && materializedIterator.hasNext()) {
				loadNext_M_BulkAndEnsureCorrelatedTuples();
			}
		}

		private void loadNext_M_BulkAndEnsureCorrelatedTuples() {
			/* all materialized Tuples for current bulk */
			List<Tuple> allBulk_M_Tuples = newList();
			/* materialized Tuples for current bulk which do not have their correlation tuples determined yet */
			List<Tuple> newBulk_M_Tuples = newList();

			int currBulk = 0;
			while ((currBulk < bulkSize) && materializedIterator.hasNext()) {
				Tuple mTuple = materializedIterator.next().detachedCopy();

				allBulk_M_Tuples.add(mTuple);

				if (!correlationMap.containsKey(mTuple)) {
					newBulk_M_Tuples.add(mTuple);
					currBulk++;
				}
			}

			findCorrelatedTuplesFor(newBulk_M_Tuples);
			bulked_M_TuplesIterator = allBulk_M_Tuples.iterator();
		}

		private void findCorrelatedTuplesFor(Collection<Tuple> mTuples) {
			if (mTuples.isEmpty())
				return;

			DelegateQuerySet adaptedQuerySet = adaptQuerySet(delegateQueryJoin, mTuples, smartContext);
			if (adaptedQuerySet == null)
				// This can happen if none of the mTuples has a valid set of correlated values, i.e. there is always some null
				return;
			
			EvalTupleSet bulkedDelegateQuerySet = smartContext.resolveTupleSet(adaptedQuerySet);

			for (Tuple bulkedResult: bulkedDelegateQuerySet) {
				Tuple detached = bulkedResult.detachedCopy();
				acquireList(correlationMap, detached).add(detached);
			}
		}

		/**
		 * Prepares next M_tuple from the {@link #bulked_M_TuplesIterator}, for which there exists a corresponding right
		 * side (either a correlated one, or null, in case of a left join). If this is possible, it returns
		 * <tt>true</tt>, otherwise we know we have to prepare new M_bulk.
		 */
		private boolean prepareNextBulked_M_Tuple() {
			while (bulked_M_TuplesIterator.hasNext()) {
				current_M_Tuple = bulked_M_TuplesIterator.next();

				List<Tuple> correlatedTuples = correlationMap.get(current_M_Tuple);
				if (correlatedTuples != null) {
					current_Q_Tuples = correlatedTuples.iterator();
					return true;

				} else if (isLeftJoin) {
					current_Q_Tuples = emptyTupleIterator();
					return useNullForNextJoinedValue = true;
				}
			}

			current_Q_Tuples = null;
			return false;
		}

	}

}
