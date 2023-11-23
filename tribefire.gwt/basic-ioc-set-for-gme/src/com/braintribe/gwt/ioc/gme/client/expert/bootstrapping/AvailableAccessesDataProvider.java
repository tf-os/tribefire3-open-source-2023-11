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

import java.util.function.Supplier;

import com.braintribe.gwt.async.client.Future;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.bapi.AvailableAccesses;


/**
 * Provider responsible for providing a {@link Future} for the {@link AvailableAccesses}.
 * @author michel.docouto
 *
 */
public class AvailableAccessesDataProvider implements Supplier<Future<AvailableAccesses>> {
	
	private BootstrappingRequest bootstrappingRequest;
	private Future<AvailableAccesses> future;
	
	/**
	 * Configures the required request responsible for loading the {@link AvailableAccesses}.
	 */
	@Required
	public void setBootstrappingRequest(BootstrappingRequest bootstrappingRequest) {
		this.bootstrappingRequest = bootstrappingRequest;
	}

	@Override
	public Future<AvailableAccesses> get() throws RuntimeException {
		if (future == null) {
			future = new Future<>();
			bootstrappingRequest.addBootstrappingRequestListener(new BootstrappingRequestListener() {
				@Override
				public void onFailure(Throwable t) {
					future.onFailure(t);
				}
			});
			
			bootstrappingRequest.getAccessEval().get(future);
		}
		
		return future;
	}
	
	

}
