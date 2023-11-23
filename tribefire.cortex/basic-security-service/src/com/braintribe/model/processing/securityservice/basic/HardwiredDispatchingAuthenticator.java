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
package com.braintribe.model.processing.securityservice.basic;

import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.reason.essential.UnsupportedOperation;
import com.braintribe.gm.model.security.reason.InvalidCredentials;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.credentials.Credentials;

public class HardwiredDispatchingAuthenticator
		implements ReasonedServiceProcessor<AuthenticateCredentials, AuthenticateCredentialsResponse> {

	private PolymorphicDenotationMap<Credentials, ReasonedServiceProcessor<AuthenticateCredentials, ? extends AuthenticateCredentialsResponse>> authenticators = new PolymorphicDenotationMap<>();

	public <T extends Credentials> void registerAuthenticator(EntityType<T> type,
			ReasonedServiceProcessor<AuthenticateCredentials, ? extends AuthenticateCredentialsResponse> authenticator) {
		authenticators.put(type, authenticator);
	}

	@Override
	public Maybe<? extends AuthenticateCredentialsResponse> processReasoned(ServiceRequestContext context, AuthenticateCredentials request) {
		Credentials credentials = request.getCredentials();

		if (credentials == null)
			return Reasons.build(InvalidArgument.T).text("AuthenticateCredentials.credentials must not be null").toMaybe();

		ReasonedServiceProcessor<AuthenticateCredentials, ? extends AuthenticateCredentialsResponse> authenticator = authenticators.find(credentials);

		if (authenticator == null)
			return Reasons.build(InvalidCredentials.T).text("Invalid Credentials") //
					.cause(Reasons.build(UnsupportedOperation.T).text("Credential type " + credentials.type().getTypeSignature() + " not supported.")
							.toReason())
					.toMaybe();

		return authenticator.processReasoned(context, request);
	}
}
