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
package tribefire.extension.okta.processing.service;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.service.api.ServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.okta.api.model.auth.HasAuthorization;
import tribefire.extension.okta.processing.auth.AuthenticationSupplier;
import tribrefire.extension.okta.common.OktaCommons;

public class OktaAuthorizationPreProcessor implements ServicePreProcessor<ServiceRequest>, OktaCommons {

	private AuthenticationSupplier authenticationSupplier;

	// ***************************************************************************************************
	// Setters
	// ***************************************************************************************************

	@Required
	@Configurable
	public void setAuthenticationSupplier(AuthenticationSupplier authenticationSupplier) {
		this.authenticationSupplier = authenticationSupplier;
	}

	// ***************************************************************************************************
	// Processing
	// ***************************************************************************************************

	@Override
	public ServiceRequest process(ServiceRequestContext requestContext, ServiceRequest request) {
		if (request instanceof HasAuthorization) {
			HasAuthorization hasAuthorization = (HasAuthorization) request;
			String authorization = hasAuthorization.getAuthorization();
			if (authorization == null && authenticationSupplier != null) {
				authenticationSupplier.authorizeRequest(hasAuthorization);
			}
		}
		return request;
	}

}