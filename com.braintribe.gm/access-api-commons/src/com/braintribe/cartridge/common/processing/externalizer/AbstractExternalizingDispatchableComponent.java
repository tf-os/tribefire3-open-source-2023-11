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
package com.braintribe.cartridge.common.processing.externalizer;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Base class for {@link DispatchableRequest}-driven externalizing components.
 * 
 */
public abstract class AbstractExternalizingDispatchableComponent<R extends DispatchableRequest> extends AbstractExternalizingComponent {

	private ExternalizingEvaluator<R> evaluator;

	/**
	 * <p>
	 * Initializes the externalizer.
	 * 
	 * @param remoteEvaluator
	 *            The remote {@link Evaluator} used to evaluate the {@link ServiceRequest} instances.
	 * @param externalId
	 *            The external processor's id.
	 */
	public AbstractExternalizingDispatchableComponent(Evaluator<ServiceRequest> remoteEvaluator, String externalId) {
		super(remoteEvaluator, externalId);
		this.evaluator = new ExternalizingEvaluator<R>(remoteEvaluator, externalId);
	}

	@Override
	protected Evaluator<ServiceRequest> evaluator() {
		Evaluator<?> dispatchingEvaluator = evaluator;
		return (Evaluator<ServiceRequest>) dispatchingEvaluator;
	}

	private static class ExternalizingEvaluator<D extends DispatchableRequest> implements Evaluator<D> {

		private Evaluator<ServiceRequest> remoteEvaluator;
		private String externalId;

		public ExternalizingEvaluator(Evaluator<ServiceRequest> remoteEvaluator, String externalId) {
			super();
			this.remoteEvaluator = remoteEvaluator;
			this.externalId = externalId;
		}

		@Override
		public <T> EvalContext<T> eval(D request) {
			String serviceId = request.getServiceId();
			if (serviceId == null) {
				request.setServiceId(externalId);
			}
			return remoteEvaluator.eval(request);
		}

	}

}
