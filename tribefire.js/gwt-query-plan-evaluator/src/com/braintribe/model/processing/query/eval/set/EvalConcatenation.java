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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.braintribe.model.processing.query.eval.api.EvalTupleSet;
import com.braintribe.model.processing.query.eval.api.QueryEvaluationContext;
import com.braintribe.model.processing.query.eval.api.Tuple;
import com.braintribe.model.processing.query.eval.set.base.AbstractEvalTupleSet;
import com.braintribe.model.processing.query.eval.tools.QueryEvaluationTools;
import com.braintribe.model.queryplan.set.Concatenation;
import com.braintribe.model.queryplan.set.TupleSet;

/**
 * 
 */
public class EvalConcatenation extends AbstractEvalTupleSet {

	protected final List<EvalTupleSet> operands;
	protected final int tupleSize;

	public EvalConcatenation(Concatenation concatenation, QueryEvaluationContext context) {
		super(context);
		this.operands = QueryEvaluationTools.resolveTupleSets(listOfTupleSets(concatenation), context);
		this.tupleSize = concatenation.getTupleSize();
	}

	private List<TupleSet> listOfTupleSets(Concatenation c) {
		return Arrays.asList(c.getFirstOperand(), c.getSecondOperand());
	}

	@Override
	public Iterator<Tuple> iterator() {
		return new ConcatenationIterator();
	}

	protected class ConcatenationIterator extends AbstractTupleIterator {

		protected Iterator<? extends Iterable<Tuple>> operandsIterator;
		protected Iterator<Tuple> currentTupleIterator;

		public ConcatenationIterator() {
			operandsIterator = operands.iterator();
			currentTupleIterator = operandsIterator.next().iterator();

			prepareNextValue();
		}

		@Override
		protected int totalComponentsCount() {
			return tupleSize == 0 ? super.totalComponentsCount() : tupleSize;
		}

		@Override
		protected void prepareNextValue() {
			while (true) {
				if (currentTupleIterator.hasNext()) {
					next = currentTupleIterator.next();
					return;
				}

				if (operandsIterator.hasNext()) {
					currentTupleIterator = operandsIterator.next().iterator();

				} else {
					next = null;
					return;
				}
			}
		}

	}
}
