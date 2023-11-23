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

import java.util.Objects;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <p>
 * Base class for {@link ServiceRequest}-driven externalizing components.
 * 
 */
public abstract class AbstractExternalizingComponent {

	private Evaluator<ServiceRequest> remoteEvaluator;
	private String externalId;

	/**
	 * <p>
	 * Initializes the externalizer.
	 * 
	 * @param remoteEvaluator
	 *            The remote {@link Evaluator} used to evaluate the {@link ServiceRequest} instances.
	 * @param externalId
	 *            The external processor's id.
	 */
	public AbstractExternalizingComponent(Evaluator<ServiceRequest> remoteEvaluator, String externalId) {
		super();
		Objects.requireNonNull(remoteEvaluator, "remoteEvaluator must not be null");
		Objects.requireNonNull(externalId, "externalId must not be null");
		this.remoteEvaluator = remoteEvaluator;
		this.externalId = externalId;
	}

	protected Evaluator<ServiceRequest> evaluator() {
		return remoteEvaluator;
	}

	protected String externalId() {
		return externalId;
	}

}
