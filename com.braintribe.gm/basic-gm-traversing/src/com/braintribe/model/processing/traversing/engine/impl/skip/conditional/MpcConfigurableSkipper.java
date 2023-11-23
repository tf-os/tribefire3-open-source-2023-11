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
package com.braintribe.model.processing.traversing.engine.impl.skip.conditional;

import com.braintribe.model.mpc.ModelPathCondition;
import com.braintribe.model.processing.mpc.MPC;
import com.braintribe.model.processing.mpc.evaluator.api.MpcEvaluatorRuntimeException;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.GmTraversingException;
import com.braintribe.model.processing.traversing.api.GmTraversingSkippingCriteria;
import com.braintribe.model.processing.traversing.api.SkipUseCase;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.impl.visitors.GmTraversingVisitorAdapter;

/**
 * A {link ConditionalSkipper} that requires {@link MPC} to evaluate if
 * condition.
 *
 */
public class MpcConfigurableSkipper extends GmTraversingVisitorAdapter implements ConditionalSkipper {

	private ModelPathCondition condition;
	private GmTraversingSkippingCriteria skippingCriteria;
	private SkipUseCase skipUseCase;

	public ModelPathCondition getCondition() {
		return condition;
	}

	public void setCondition(ModelPathCondition condition) {
		this.condition = condition;
	}

	@Override
	public void onElementEnter(GmTraversingContext context, TraversingModelPathElement pathElement) throws GmTraversingException {

		try {
			boolean match = MPC.matches(getCondition(), pathElement);

			if (match) {
				switch (getSkippingCriteria()) {
					case skipAll:
						context.skipAll(getSkipUseCase());
						break;
					case skipDescendants:
						context.skipDescendants(getSkipUseCase());
						break;
					case skipWalkFrame:
						context.skipWalkFrame(getSkipUseCase());
						break;
				}
			}

		} catch (MpcEvaluatorRuntimeException e) {
			throw new GmTraversingException("Evaluation of MPC at MpcConfigurableSkipper failed", e);
		}
	}

	@Override
	public SkipUseCase getSkipUseCase() {
		return skipUseCase;
	}

	@Override
	public void setSkipUseCase(SkipUseCase skipUseCase) {
		this.skipUseCase = skipUseCase;
	}

	@Override
	public GmTraversingSkippingCriteria getSkippingCriteria() {
		return skippingCriteria;
	}

	@Override
	public void setSkippingCriteria(GmTraversingSkippingCriteria skippingCriteria) {
		this.skippingCriteria = skippingCriteria;

	}
}
