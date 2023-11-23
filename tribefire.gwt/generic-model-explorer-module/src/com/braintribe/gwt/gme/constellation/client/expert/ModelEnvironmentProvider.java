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
package com.braintribe.gwt.gme.constellation.client.expert;

import java.util.function.Function;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.accessapi.ModelEnvironment;


/**
 * Provider which loads the {@link ModelEnvironment} for the given accessId.
 * @author michel.docouto
 *
 */
public class ModelEnvironmentProvider implements Function<String, Future<ModelEnvironment>> {
	
	private String currentAccessId;
	private Future<ModelEnvironment> future;
	private Function<String, Future<ModelEnvironment>> modelEnvironmentFutureProvider;
	
	/**
	 * Configures the required provider used for providing the {@link ModelEnvironment}.
	 */
	@Required
	public void setModelEnvironmentFutureProvider(Function<String, Future<ModelEnvironment>> modelEnvironmentFutureProvider) {
		this.modelEnvironmentFutureProvider = modelEnvironmentFutureProvider;
	}
	
	@Override
	public Future<ModelEnvironment> apply(String accessId) throws RuntimeException {
		if (future == null || currentAccessId == null || !currentAccessId.equals(accessId)) {
			currentAccessId = accessId;
			future = modelEnvironmentFutureProvider.apply(accessId);
		}
		
		return future;
	}
	
	public Future<ModelEnvironment> reload() {
		future = modelEnvironmentFutureProvider.apply(currentAccessId);
		return future;
	}

}
