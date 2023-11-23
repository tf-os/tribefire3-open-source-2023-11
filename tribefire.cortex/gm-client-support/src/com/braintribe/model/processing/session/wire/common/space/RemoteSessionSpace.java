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
package com.braintribe.model.processing.session.wire.common.space;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.access.common.EvaluatingAccessService;
import com.braintribe.model.processing.accessory.impl.PlatformModelAccessoryFactory;
import com.braintribe.model.processing.accessory.impl.PmeSupplierViaDdsa;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceAccessFactory;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceUrlBuilderSupplier;
import com.braintribe.model.processing.resource.streaming.access.RestResourceUrlBuilder;
import com.braintribe.model.processing.securityservice.commons.provider.SessionAuthorizationFromUserSessionProvider;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceUrlBuilder;
import com.braintribe.model.processing.session.impl.BasicPersistenceGmSessionFactory;
import com.braintribe.model.processing.session.wire.common.contract.CommonContract;
import com.braintribe.model.processing.session.wire.common.contract.RemoteAuthenticationContract;
import com.braintribe.model.processing.session.wire.common.contract.RemoteEvaluatorContract;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.securityservice.OpenUserSession;
import com.braintribe.model.securityservice.OpenUserSessionResponse;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class RemoteSessionSpace implements WireSpace {

	@Import
	private RemoteEvaluatorContract remoteEvaluator;

	@Import
	private RemoteAuthenticationContract remoteAuthentication;

	@Import
	private CommonContract common;

	@Managed
	public Supplier<PersistenceGmSession> cortexSessionSupplier() {
		return () -> internalRemoteSessionFactory().newSession("cortex");
	}

	@Managed
	public SessionAuthorizationFromUserSessionProvider sessionAuthorizationProvider() {
		SessionAuthorizationFromUserSessionProvider bean = new SessionAuthorizationFromUserSessionProvider();
		bean.setUserSessionProvider(userSessionSupplier());
		return bean;
	}

	@Managed
	public BasicPersistenceGmSessionFactory remoteSessionFactory() {
		BasicPersistenceGmSessionFactory bean = new BasicPersistenceGmSessionFactory();

		bean.setRequestEvaluator(remoteEvaluator.evaluator());
		bean.setAccessService(new EvaluatingAccessService(remoteEvaluator.evaluator()));
		bean.setSessionAuthorizationProvider(sessionAuthorizationProvider());
		bean.setModelAccessoryFactory(userModelAccessoryFactory());
		bean.setResourceAccessFactory(resourceAccessFactory());

		return bean;
	}

	@Managed
	public PlatformModelAccessoryFactory userModelAccessoryFactory() {
		PlatformModelAccessoryFactory bean = new PlatformModelAccessoryFactory();
		bean.setModelEssentialsSupplier(modelEssentialsSupplier());
		bean.setUserRolesProvider(this::getUserRoles);

		return bean;
	}

	private Set<String> getUserRoles() {
		UserSession userSession = userSessionSupplier().get();

		return userSession == null ? null : userSession.getEffectiveRoles();
	}

	@Managed
	public PmeSupplierViaDdsa modelEssentialsSupplier() {
		PmeSupplierViaDdsa bean = new PmeSupplierViaDdsa();
		bean.setEvaluator(remoteEvaluator.evaluator());

		return bean;
	}

	@Managed
	public Supplier<UserSession> userSessionSupplier() {
		return () -> remoteEvaluator.userSessionResolver().acquireUserSession(this::evalOpenUserSessionRequest);
	}

	private OpenUserSessionResponse evalOpenUserSessionRequest(OpenUserSession request) {
		return (OpenUserSessionResponse) remoteEvaluator.evaluator().eval(request).get();
	}

	@Managed
	private BasicPersistenceGmSessionFactory internalRemoteSessionFactory() {
		BasicPersistenceGmSessionFactory bean = new BasicPersistenceGmSessionFactory();

		ConfigurableServiceRequestEvaluator evaluator = remoteEvaluator.evaluator();

		bean.setRequestEvaluator(evaluator);
		bean.setAccessService(new EvaluatingAccessService(evaluator));

		return bean;
	}

	@Managed
	private BasicResourceAccessFactory resourceAccessFactory() {
		BasicResourceAccessFactory bean = new BasicResourceAccessFactory();
		bean.setStreamPipeFactory(common.streamPipeFactory());
		bean.setUrlBuilderSupplier(basicResourceUrlBuilderSupplier());
		return bean;
	}

	@Managed
	private URL streamingUrl(String suffix) {
		try {
			return new URL(remoteAuthentication.tfServicesUrl() + suffix);
		} catch (MalformedURLException e) {
			throw Exceptions.unchecked(e, "Could not generate streaming url");
		}
	}

	@Managed
	private BasicResourceUrlBuilderSupplier basicResourceUrlBuilderSupplier() {
		BasicResourceUrlBuilderSupplier bean = new BasicResourceUrlBuilderSupplier();
		bean.setBaseStreamingUrl(streamingUrl("/streaming"));
		bean.setSessionIdProvider(() -> userSessionSupplier().get().getSessionId());
		bean.setResponseMimeType("application/json");
		return bean;
	}

	@Managed
	// TODO: Enable this as soon as rest v2 supports streaming resource binaries
	private Function<Resource, ResourceUrlBuilder> restResourceUrlBuilderSupplier() {
		return (resource) -> {
			RestResourceUrlBuilder bean = new RestResourceUrlBuilder();
			bean.setBaseUrl(streamingUrl("/rest/v2"));
			bean.sessionId(userSessionSupplier().get().getSessionId());
			bean.setResponseMimeType("application/json");
			bean.setResource(resource);

			return bean;
		};

	}
}
