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

import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Collections.emptyList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.HasMoreAwareSet;
import com.braintribe.model.processing.query.eval.set.base.TransientGeneratorEvalTupleSet;
import com.braintribe.model.processing.query.eval.tuple.OneDimensionalTuple;
import com.braintribe.model.processing.smartquery.eval.api.SmartQueryEvaluationContext;
import com.braintribe.model.query.Paging;
import com.braintribe.model.query.Restriction;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;
import com.braintribe.model.query.smart.processing.SmartQueryEvaluatorRuntimeException;
import com.braintribe.model.query.smart.processing.eval.wrapper.ListRecord2TupleAdapter;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.smartqueryplan.ScalarMapping;
import com.braintribe.model.smartqueryplan.set.DelegateQuerySet;

public class EvalDelegateQuerySet extends TransientGeneratorEvalTupleSet implements HasMoreAwareSet {

	protected final DelegateQuerySet delegateQuerySet;
	protected final List<Tuple> result;
	protected final SmartQueryEvaluationContext smartContext;

	protected boolean hasMore;

	public EvalDelegateQuerySet(DelegateQuerySet delegateQuerySet, SmartQueryEvaluationContext context) {
		super(context);

		this.delegateQuerySet = delegateQuerySet;
		this.smartContext = context;

		this.hasMore = false;
		this.result = getResultIfNotBulked();
	}

	@Override
	public boolean hasMore() {
		/* The only way for this to be true is that our delegate query returned 'hasMore'; In all other cases this set
		 * returns all the results there are, so even if there is pagination on top of this, it has to handle the
		 * hasMore by itself. */
		return hasMore;
	}

	private List<Tuple> getResultIfNotBulked() {
		if (isBulked())
			return emptyList();
		else
			return runQuery(delegateQuerySet.getDelegateQuery());
	}

	private List<Tuple> runQuery(SelectQuery query) {
		SelectQueryResult queryResult = smartContext.runQuery(delegateQuerySet.getDelegateAccess(), query);
		hasMore = queryResult.getHasMore();

		return asTupleList(queryResult.getResults());
	}

	private List<Tuple> asTupleList(List<Object> results) {
		List<Tuple> wrappedResult = newList();

		if ((results != null) && !results.isEmpty()) {
			if (results.get(0) instanceof ListRecord) {
				for (Object record : results)
					wrappedResult.add(new ListRecord2TupleAdapter((ListRecord) record));

			} else {
				for (Object record : results) {
					OneDimensionalTuple tupleRecord = new OneDimensionalTuple(0);
					tupleRecord.setValueDirectly(0, record);
					wrappedResult.add(tupleRecord);
				}
			}
		}

		return wrappedResult;
	}

	@Override
	public Iterator<Tuple> iterator() {
		return isBulked() ? new BulkedDelegateQueryIterator() : new DelegateQueryIterator();
	}

	protected abstract class AbstractDelegateQueryIterator extends AbstractTupleIterator {
		protected Iterator<Tuple> delegateIterator;

		/** This should only be called if delegateIterator has next(). */
		protected void prepareNextTuple() {
			Tuple tuple = delegateIterator.next();

			for (ScalarMapping mapping : delegateQuerySet.getScalarMappings()) {
				Object resolvedValue = context.resolveValue(tuple, mapping.getSourceValue());
				singletonTuple.setValueDirectly(mapping.getTupleComponentIndex(), resolvedValue);
			}

			next = singletonTuple;
		}
	}

	protected class DelegateQueryIterator extends AbstractDelegateQueryIterator {
		public DelegateQueryIterator() {
			delegateIterator = result.iterator();
			prepareNextValue();
		}

		@Override
		protected void prepareNextValue() {
			if (delegateIterator.hasNext())
				super.prepareNextTuple();
			else
				next = null;
		}
	}

	protected class BulkedDelegateQueryIterator extends AbstractDelegateQueryIterator {
		protected final SelectQuery query;
		protected int nextOffset = 0;
		protected boolean finished = false;
		protected boolean thisBulkIsDefinitelyLast = false;

		public BulkedDelegateQueryIterator() {
			this.query = copyAndEnsurePagination(delegateQuerySet.getDelegateQuery());
			this.delegateIterator = emptyTupleIterator();

			prepareNextValue();
		}

		private SelectQuery copyAndEnsurePagination(SelectQuery query) {
			SelectQuery result = copy(query);

			Restriction r = result.getRestriction();
			if (r == null) {
				result.setRestriction(r = Restriction.T.create());
			}

			if (r.getPaging() == null) {
				Paging p = Paging.T.create();
				p.setPageSize(batchSize());

				r.setPaging(p);

			} else {
				throw new SmartQueryEvaluatorRuntimeException(
						"Error. DelegateQurySet is bulked (bulk-size :" + batchSize() + ") but the pagination is already set for the query!");
			}

			return result;
		}

		@Override
		protected void prepareNextValue() {
			if (finished) {
				next = null;
				return;
			}

			if (delegateIterator.hasNext()) {
				super.prepareNextTuple();
				return;
			}

			if (thisBulkIsDefinitelyLast)
				finished = true;
			else
				loadNextBulk();

			prepareNextValue();
		}

		protected void loadNextBulk() {
			query.getRestriction().getPaging().setStartIndex(nextOffset++ * batchSize());

			List<Tuple> queryResult = runQuery(query);

			if (queryResult.isEmpty()) {
				finished = true;

			} else {
				delegateIterator = queryResult.iterator();
				thisBulkIsDefinitelyLast = queryResult.size() < batchSize();
			}
		}
	}

	private boolean isBulked() {
		return batchSize() != null;
	}

	private Integer batchSize() {
		return delegateQuerySet.getBatchSize();
	}

	private static Iterator<Tuple> emptyTupleIterator() {
		return Collections.<Tuple> emptySet().iterator();
	}

	private static SelectQuery copy(SelectQuery query) {
		EntityType<GenericEntity> et = query.entityType();
		return (SelectQuery) et.clone(new StandardCloningContext(), query, StrategyOnCriterionMatch.reference);
	}

}
