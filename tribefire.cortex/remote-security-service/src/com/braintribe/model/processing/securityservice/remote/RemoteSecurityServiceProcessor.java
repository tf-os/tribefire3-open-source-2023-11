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
package com.braintribe.model.processing.securityservice.remote;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.tribefire.connector.RemoteTribefireConnection;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.api.exceptions.AuthenticationException;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;

/**
 * Implementation of both the {@link SecurityService} interface and the Service Processor for {@link SecurityRequest}
 * that forwards all calls to a remote Tribefire instance. It requires a {@link RemoteTribefireConnection} connection
 * object.
 * 
 * @author roman.kurmanowytsch
 *
 */
public class RemoteSecurityServiceProcessor implements ServiceProcessor<SecurityRequest, Object> {

	protected Evaluator<ServiceRequest> evaluator;
	protected Function<String, User> virtualUserResolver;
	private Supplier<PersistenceGmSession> clientGmSessionProvider;
	private RemoteTribefireConnection con;

	@Required
	@Configurable
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.evaluator = evaluator;
	}

	@Override
	public Object process(ServiceRequestContext requestContext, SecurityRequest request) throws AuthenticationException {

		requireNonNull(requestContext, "requestContext must not be null");
		requireNonNull(request, "request must not be null");

		Object response = request.eval(evaluator).get();
		return response;

	}

	@Required
	@Configurable
	public void setClientGmSessionProvider(Supplier<PersistenceGmSession> clientGmSessionProvider) {
		this.clientGmSessionProvider = clientGmSessionProvider;
	}
	@Required
	@Configurable
	public void setRemoteTribefireConnection(RemoteTribefireConnection con) {
		this.con = con;
	}

}
