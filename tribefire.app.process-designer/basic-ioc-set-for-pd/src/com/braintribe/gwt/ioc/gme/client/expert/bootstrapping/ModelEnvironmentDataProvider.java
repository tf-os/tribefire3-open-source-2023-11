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
package com.braintribe.gwt.ioc.gme.client.expert.bootstrapping;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.bapi.AvailableAccesses;

/**
 * Provider responsible for providing a {@link Future} for the {@link ModelEnvironment} for a given accessId.
 * @author michel.docouto
 *
 */
public class ModelEnvironmentDataProvider implements Function<String, Future<ModelEnvironment>> {
	
	private BootstrappingRequest bootstrappingRequest;
	
	/**
	 * Configures the required request responsible for loading the {@link AvailableAccesses}.
	 */
	@Required
	public void setBootstrappingRequest(BootstrappingRequest bootstrappingRequest) {
		this.bootstrappingRequest = bootstrappingRequest;
	}

	@Override
	public Future<ModelEnvironment> apply(String accessId) {
		if (accessId == null) { //This was needed so the bootstrapping load happens
			bootstrappingRequest.getModelEnvironmentEval(accessId);
			return null;
		}
		
		final Future<ModelEnvironment> future = new Future<>();
		bootstrappingRequest.addBootstrappingRequestListener(t -> future.onFailure(t));
		bootstrappingRequest.getModelEnvironmentEval(accessId).get(future);
		
		return future;
	}

}