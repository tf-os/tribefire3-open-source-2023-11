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
package com.braintribe.gm.service.wire.common.space;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.braintribe.gm.service.impl.DomainIdServiceAroundProcessor;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.processing.securityservice.commons.service.AuthorizingServiceInterceptor;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.CompositeServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.common.ElapsedTimeMeasuringInterceptor;
import com.braintribe.model.processing.service.common.ThreadNamingInterceptor;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.thread.impl.ThreadContextScopingImpl;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class CommonServiceProcessingSpace implements CommonServiceProcessingContract {

	@Import
	private ServiceProcessingConfigurationSpace serviceProcessingConfiguration;

	@Managed
	public ThreadContextScopingImpl threadContextScoping() {
		ThreadContextScopingImpl bean = new ThreadContextScopingImpl();
		return bean;
	}

	@Override
	@Managed
	public ConfigurableServiceRequestEvaluator evaluator() {
		ConfigurableServiceRequestEvaluator bean = new ConfigurableServiceRequestEvaluator();
		bean.setExecutorService(executorService());
		bean.setServiceProcessor(selectingServiceProcessor());
		return bean;
	}

	@Managed
	public ExecutorService executorService() {
		return Executors.newFixedThreadPool(200);
	}

	@Managed
	public ConfigurableDispatchingServiceProcessor selectingServiceProcessor() {
		ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor();

		bean.register(CompositeRequest.T, compositeServiceProcessor());
		bean.register(SecurityRequest.T, securityServiceProcesor());

		bean.registerInterceptor(THREAD_NAMING_INTERCEPTOR_ID).register(threadNamingInterceptor());
		bean.registerInterceptor(TIME_MEASURING_INTERCEPTOR_ID).register(ElapsedTimeMeasuringInterceptor.INSTANCE);
		bean.registerInterceptor(AUTH_INTERCEPTOR_ID).registerForType(AuthorizableRequest.T, authorizingServiceInterceptor());
		bean.registerInterceptor(DOMAIN_ID_INTERCEPTOR_ID).registerForType(ServiceRequest.T, DomainIdServiceAroundProcessor.INSTANCE);

		serviceProcessingConfiguration.serviceConfigurers().forEach(c -> c.accept(bean));

		return bean;
	}

	@Managed
	public InMemorySecurityServiceProcessor securityServiceProcesor() {
		InMemorySecurityServiceProcessor bean = new InMemorySecurityServiceProcessor();

		serviceProcessingConfiguration.securityConfigurers().forEach(c -> c.accept(bean));

		return bean;
	}

	@Managed
	public AuthorizingServiceInterceptor authorizingServiceInterceptor() {
		AuthorizingServiceInterceptor bean = new AuthorizingServiceInterceptor();
		return bean;
	}

	@Managed
	public ThreadNamingInterceptor threadNamingInterceptor() {
		ThreadNamingInterceptor bean = new ThreadNamingInterceptor();
		bean.setThreadRenamer(new ThreadRenamer(true));
		return bean;
	}

	@Managed
	public CompositeServiceProcessor compositeServiceProcessor() {
		CompositeServiceProcessor bean = new CompositeServiceProcessor();
		return bean;
	}
}
