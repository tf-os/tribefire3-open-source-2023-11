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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.gm.model.reason.Reasons;
import com.braintribe.gm.model.reason.essential.InvalidArgument;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.api.ReasonedServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.security.deployment.meta.AuthenticateWith;
import com.braintribe.model.securityservice.AuthenticateCredentials;
import com.braintribe.model.securityservice.AuthenticateCredentialsResponse;
import com.braintribe.model.securityservice.credentials.Credentials;
import com.braintribe.utils.lcd.LazyInitialized;

public class MetaDataDispatchingAuthenticator implements ReasonedServiceProcessor<AuthenticateCredentials, AuthenticateCredentialsResponse> {
	private static final Logger log = Logger.getLogger(MetaDataDispatchingAuthenticator.class);

	private LazyInitialized<ModelAccessory> cortexModelAccessorySupplier;
	private ReasonedServiceProcessor<AuthenticateCredentials, AuthenticateCredentialsResponse> defaultDelegate;

	@Required
	public void setCortexModelAccessorySupplier(Supplier<ModelAccessory> cortexModelAccessorySupplier) {
		this.cortexModelAccessorySupplier = new LazyInitialized<>(cortexModelAccessorySupplier);
	}

	@Configurable
	public void setDefaultDelegate(ReasonedServiceProcessor<AuthenticateCredentials, AuthenticateCredentialsResponse> defaultDelegate) {
		this.defaultDelegate = defaultDelegate;
	}

	@Override
	public Maybe<? extends AuthenticateCredentialsResponse> processReasoned(ServiceRequestContext context, AuthenticateCredentials request) {
		Credentials credentials = request.getCredentials();

		if (credentials == null)
			return Reasons.build(InvalidArgument.T).text("AuthenticateCredentials.credentials must not be null").toMaybe();

		List<AuthenticateWith> authenticateWiths = cortexModelAccessorySupplier.get().getCmdResolver().getMetaData().entity(credentials)
				.meta(AuthenticateWith.T).list();

		AuthenticateCredentials authenticateCredentials = AuthenticateCredentials.T.create();
		authenticateCredentials.setProperties(request.getProperties());
		authenticateCredentials.setCredentials(request.getCredentials());

		List<AuthenticationFailure> authProblems = new ArrayList<>(authenticateWiths.size());
		LazyHolder<String> logCorrelationId = LazyHolder.from(() -> UUID.randomUUID().toString());

		if (authenticateWiths.isEmpty() && defaultDelegate != null)
			return defaultDelegate.processReasoned(context, request);

		// TODO: should we somehow collect meaning full masked reasons
		for (AuthenticateWith authenticateWith : authenticateWiths) {
			String processorExternalId = authenticateWith.getProcessor().getExternalId();
			authenticateCredentials.setServiceId(processorExternalId);
			Maybe<? extends AuthenticateCredentialsResponse> maybeResponse = authenticateCredentials.eval(context).getReasoned();

			if (maybeResponse.isSatisfied()) {
				return maybeResponse;
			}

			Reason reason = maybeResponse.whyUnsatisfied();

			if (reason instanceof AuthenticationFailure && reason.type() != AuthenticationFailure.T)
				authProblems.add((AuthenticationFailure) reason);

			log.debug(() -> "Credentials " + credentials + " not authenticated with processor with externalId " + processorExternalId + ". (context="
					+ logCorrelationId.get() + "). Reason: " + reason.stringify());
		}

		switch (authProblems.size()) {
			case 0:
				return Reasons.build(AuthenticationFailure.T)
						.text("Authentication Failure with log correlation (context=" + logCorrelationId.get() + ").").toMaybe();
			case 1:
				return authProblems.get(0).asMaybe();
			default:
				return Reasons.build(AuthenticationFailure.T)
						.text("Authentication Failure with log correlation (context=" + logCorrelationId.get() + ").")
						.enrich(r -> r.getReasons().addAll(authProblems)).toMaybe();
		}
	}
}
