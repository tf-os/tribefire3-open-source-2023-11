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

import com.braintribe.gwt.async.client.DeferredExecutor;
import com.braintribe.gwt.async.testing.Futures;
import com.braintribe.model.processing.vde.clone.async.DeferredExecutorAspect;
import com.braintribe.model.processing.vde.evaluator.VDE;
import com.braintribe.model.processing.vde.evaluator.api.VdeContextAspect;
import com.braintribe.model.processing.vde.evaluator.api.VdeEvaluationMode;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.builder.VdeContextBuilder;
import com.braintribe.model.processing.vde.impl.VDGenerator;
import com.braintribe.processing.async.impl.HubPromise;

/**
 * @author peter.gazdik
 */
public abstract class VdeTest {

	public static VDGenerator $ = new VDGenerator();

	protected DeferredExecutor deferredExecutor = Futures.singleThreadedDeferredExecutor;

	public VdeTest() {

	}

	protected Object evaluate(Object vdeOrValue) {
		HubPromise<Object> promise = new HubPromise<>();
		evaluate().forValue(vdeOrValue, promise);
		return promise.get();
	}

	protected <T, A extends VdeContextAspect<? super T>> Object evaluateWith(Class<A> aspect, T aspectValue, Object value)
			throws VdeRuntimeException {
		HubPromise<Object> promise = new HubPromise<>();
		evaluate().with(aspect, aspectValue).forValue(value, promise);
		return promise.get();
	}

	protected Object evaluateWithEvaluationMode(Object value, VdeEvaluationMode evalMode) {
		HubPromise<Object> promise = new HubPromise<>();
		evaluate().withEvaluationMode(evalMode).forValue(value, promise);
		return promise.get();
	}

	protected VdeContextBuilder evaluate() {
		return VDE.evaluate().with(DeferredExecutorAspect.class, deferredExecutor);
	}

}
