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
package com.braintribe.model.processing.accessrequest.api;

import com.braintribe.model.accessapi.AccessRequest;

class AccessRequestDispatchingDelegate<P extends AccessRequest, R> extends AbstractDispatchingAccessRequestProcessor<P, R> {

	private DispatchConfiguration dispatching;
	
	public AccessRequestDispatchingDelegate(DispatchConfigurator configurator) {
		configurator.configureDispatching(dispatching);
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration dispatching) {
		this.dispatching = dispatching;
	}

}
