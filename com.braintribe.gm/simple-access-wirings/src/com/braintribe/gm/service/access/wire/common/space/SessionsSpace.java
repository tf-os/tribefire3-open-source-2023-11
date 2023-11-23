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
package com.braintribe.gm.service.access.wire.common.space;

import com.braintribe.gm.service.access.SimpleAccessService;
import com.braintribe.gm.service.access.SimpleAccessServiceModeAccessoryFactory;
import com.braintribe.gm.service.access.SimpleResourceProcessor;
import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.space.CommonServiceProcessingSpace;
import com.braintribe.model.processing.resource.streaming.access.BasicResourceAccessFactory;
import com.braintribe.model.processing.securityservice.commons.provider.SessionAuthorizationFromUserSessionProvider;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.impl.BasicPersistenceGmSessionFactory;
import com.braintribe.model.resourceapi.base.ResourceRequest;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.utils.stream.api.StreamPipes;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class SessionsSpace implements CommonAccessProcessingContract {
	@Import
	private CommonAccessProcessingSpace commonAccessProcessing;
	@Import
	private CommonServiceProcessingSpace commonServiceProcessing;
	@Import
	private AccessProcessingConfigurationContract accessProcessingConfiguration;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		CommonAccessProcessingContract.super.onLoaded(configuration);

		accessProcessingConfiguration.registerAccessConfigurer(this::configureServices);
	}

	private void configureServices(AccessProcessingConfiguration config) {
		config.registerAccessRequestProcessor(ResourceRequest.T, simpleResourceProcessor());
	}

	@Managed
	private SimpleResourceProcessor simpleResourceProcessor() {
		SimpleResourceProcessor bean = new SimpleResourceProcessor();
		return bean;
	}

	@Override
	@Managed
	public PersistenceGmSessionFactory sessionFactory() {
		BasicPersistenceGmSessionFactory bean = new BasicPersistenceGmSessionFactory();
		bean.setAccessService(accessService());
		bean.setRequestEvaluator(commonServiceProcessing.evaluator());
		bean.setSessionAuthorizationProvider(sessionAuthorizationProvider());
		bean.setModelAccessoryFactory(modelAccessoryFactory());
		bean.setResourceAccessFactory(resourceAccessFactory());
		return bean;
	}

	@Managed
	public BasicResourceAccessFactory resourceAccessFactory() {
		BasicResourceAccessFactory bean = new BasicResourceAccessFactory();
		bean.setUrlBuilderSupplier(null);
		bean.setStreamPipeFactory(StreamPipes.fileBackedFactory());
		return bean;
	}

	@Managed
	public SessionAuthorizationFromUserSessionProvider sessionAuthorizationProvider() {
		SessionAuthorizationFromUserSessionProvider bean = new SessionAuthorizationFromUserSessionProvider();
		bean.setUserSessionProvider(() -> AttributeContexts.peek().findAttribute(UserSessionAspect.class).orElse(null));
		return bean;
	}

	@Managed
	private SimpleAccessService accessService() {
		return commonAccessProcessing.accessRegistry().getSimpleAccessService();
	}

	@Managed
	private SimpleAccessServiceModeAccessoryFactory modelAccessoryFactory() {
		SimpleAccessServiceModeAccessoryFactory bean = new SimpleAccessServiceModeAccessoryFactory();
		bean.setAccessService(accessService());
		return bean;
	}

}
