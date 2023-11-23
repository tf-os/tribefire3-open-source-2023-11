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
package com.braintribe.model.processing.vde.evaluator.impl;

import com.braintribe.model.processing.vde.evaluator.api.VdeResult;

/**
 * Implementation of VdeResult
 * 
 * @see VdeResult
 * 
 */
public class VdeResultImpl implements VdeResult {

	private Object result;
	private boolean volatileValue;
	private final boolean noEvaluationPossible;
	private String noEvaluationReason;

	/**
	 * This constructor assumes that no evaluation was possible for the VD in
	 * question.
	 */
	public VdeResultImpl(String noEvalutionReason) {
		this.noEvaluationPossible = true;
		this.noEvaluationReason = noEvalutionReason;
	}

	/**
	 * Creates a wrapper for the Vde. This constructor assumes that evaluation
	 * was possible for the invoker VD.
	 */
	public VdeResultImpl(Object result, boolean volatileValue) {
		this.result = result;
		this.volatileValue = volatileValue;
		this.noEvaluationPossible = false;
	}

	@Override
	public Object getResult() {
		return result;
	}

	@Override
	public boolean isVolatileValue() {
		return volatileValue;
	}

	@Override
	public boolean isNoEvaluationPossible() {
		return noEvaluationPossible;
	}

	@Override
	public String getNoEvaluationReason() {
		return noEvaluationReason;
	}

}
