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
package com.braintribe.model.processing.query.eval.set;

import static com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools.moreHas;

import java.util.Iterator;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.RuntimeQueryEvaluationException;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.queryplan.set.PaginatedSet;

/**
 * 
 */
public class EvalPaginatedSet extends AbstractEvalTupleSet implements HasMoreAwareSet {

	protected final int limit;
	protected final int offset;
	protected final int tupleSize;
	protected final boolean operandMayApplyPagination;
	protected final EvalTupleSet evalOperand;

	protected Boolean hasMore;

	public EvalPaginatedSet(PaginatedSet paginatedSet, QueryEvaluationContext context) {
		super(context);

		this.limit = nonPositiveToInf(paginatedSet.getLimit());
		this.offset = paginatedSet.getOffset();
		this.tupleSize = paginatedSet.getTupleSize();
		this.operandMayApplyPagination = paginatedSet.getOperandMayApplyPagination();
		this.evalOperand = context.resolveTupleSet(paginatedSet.getOperand());
	}

	private static int nonPositiveToInf(int i) {
		return i > 0 ? i : Integer.MAX_VALUE;
	}

	@Override
	public boolean hasMore() {
		if (hasMore == null) {
			throw new RuntimeQueryEvaluationException("Cannot resolve hasMore value until the iterator is run through at least once.");
		}

		return hasMore.booleanValue();
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new PaginatedSetIterator();
	}

	protected class PaginatedSetIterator extends AbstractTupleIterator {

		protected Iterator<Tuple> delegateIterator;
		protected int remaining;

		@Override
		protected int totalComponentsCount() {
			return tupleSize == 0 ? context.resultComponentsCount() : tupleSize;
		}

		public PaginatedSetIterator() {
			delegateIterator = evalOperand.iterator();
			remaining = limit;
			prepareFirstValue();
		}

		protected void prepareFirstValue() {
			int counter = offset;

			while (delegateIterator.hasNext() && counter-- > 0) {
				delegateIterator.next();
			}

			if (!delegateIterator.hasNext()) {
				hasMore = false;
			}

			prepareNextValue();
		}

		@Override
		protected void prepareNextValue() {
			if (delegateIterator.hasNext() && remaining-- > 0) {
				next = delegateIterator.next();

			} else {
				hasMore = delegateIterator.hasNext() || (operandMayApplyPagination && moreHas(evalOperand));
				next = null;
			}
		}

	}
}
