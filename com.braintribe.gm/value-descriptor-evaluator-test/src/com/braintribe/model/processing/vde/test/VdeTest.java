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
package com.braintribe.model.processing.vde.test;

import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeContextAspect;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;

/**
 * @author peter.gazdik
 */
public abstract class VdeTest {

	protected Object evaluate(Object vdeOrValue) {
		return VDE.evaluate(vdeOrValue);
	}

	protected <T, A extends VdeContextAspect<? super T>> Object evaluateWith(Class<A> aspect, T value, Object object) throws VdeRuntimeException {
		return VDE.evaluateWith(aspect, value, object);
	}

	protected Object evaluateWithEvaluationMode(Object value, VdeEvaluationMode evalMode) {
		return VDE.evaluate().withEvaluationMode(evalMode).forValue(value);
	}

}
