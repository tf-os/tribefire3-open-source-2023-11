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
package com.braintribe.model.processing.rpc.test.wire.space;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.braintribe.model.processing.rpc.commons.api.service.ConfigurableServiceRegistry;
import com.braintribe.model.processing.rpc.commons.impl.service.ServiceDescriptorImpl;
import com.braintribe.model.processing.rpc.commons.impl.service.ServiceRegistryImpl;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestService;
import com.braintribe.model.processing.rpc.test.service.iface.basic.BasicTestServiceImpl;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestService;
import com.braintribe.model.processing.rpc.test.service.iface.streaming.StreamingTestServiceImpl;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessor;
import com.braintribe.model.processing.rpc.test.service.processor.basic.BasicTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.failure.FailureTestServiceProcessor;
import com.braintribe.model.processing.rpc.test.service.processor.failure.FailureTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadCaptureTestServiceProcessor;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadCaptureTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadResourceTestServiceProcessor;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.DownloadResourceTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.StreamingTestServiceProcessor;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.StreamingTestServiceProcessorRequest;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.UploadTestServiceProcessor;
import com.braintribe.model.processing.rpc.test.service.processor.streaming.UploadTestServiceProcessorRequest;
import com.braintribe.model.processing.securityservice.commons.service.AuthorizingServiceInterceptor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.CompositeServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.common.eval.AuthorizingServiceRequestEvaluator;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.provider.Hub;
import com.braintribe.provider.ThreadLocalStackedHolder;
import com.braintribe.thread.impl.ThreadLocalStack;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

@Managed
public class ServerCommonsSpace implements WireSpace {

	@Import
	private CommonsSpace commons;

	@Import
	private CryptoSpace crypto;

	@Import
	private ClientCommonsSpace clientCommons;

	@Managed
	public AuthorizingServiceInterceptor authorizingInterceptor() {
		AuthorizingServiceInterceptor bean = new AuthorizingServiceInterceptor();
		return bean;
	}

	@Managed
	private ServiceProcessor<ServiceRequest, Object> selectingServiceProcessor() {
		ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor();
		bean.register(CompositeRequest.T, compositeServiceProcessor());
		bean.register(SecurityRequest.T, commons.securityProcessor());
		bean.register(BasicTestServiceProcessorRequest.T, new BasicTestServiceProcessor());
		bean.register(FailureTestServiceProcessorRequest.T, new FailureTestServiceProcessor());
		bean.register(StreamingTestServiceProcessorRequest.T, new StreamingTestServiceProcessor());
		bean.register(UploadTestServiceProcessorRequest.T, new UploadTestServiceProcessor());
		bean.register(DownloadResourceTestServiceProcessorRequest.T, new DownloadResourceTestServiceProcessor());
		bean.register(DownloadCaptureTestServiceProcessorRequest.T, new DownloadCaptureTestServiceProcessor());
		
		bean.registerInterceptor("auth").registerForType(AuthorizableRequest.T, authorizingInterceptor());
		
		return bean;
	}

	@Managed
	private ConfigurableServiceRegistry serviceRegistry() {
		ServiceDescriptorImpl<BasicTestService> basic = new ServiceDescriptorImpl<>();
		basic.setService(new BasicTestServiceImpl());
		basic.setServiceId(BasicTestService.ID);
		basic.setServiceInterfaceClass(BasicTestService.class);

		ServiceDescriptorImpl<StreamingTestService> streaming = new ServiceDescriptorImpl<>();
		streaming.setService(new StreamingTestServiceImpl());
		streaming.setServiceId(StreamingTestService.ID);
		streaming.setServiceInterfaceClass(StreamingTestService.class);

		ServiceRegistryImpl bean = new ServiceRegistryImpl();
		bean.setServiceDescriptors(list(basic, streaming));

		return bean;
	}

	@Managed
	private Hub<UserSession> userSessionHolder() {
		return new ThreadLocalStackedHolder<>();
	}

	@Managed
	private CompositeServiceProcessor compositeServiceProcessor() {
		return new CompositeServiceProcessor();
	}

	@Managed
	public ThreadLocalStack<ServiceRequestContext> serviceContextStack() {
		return new ThreadLocalStack<>();
	}

	@Managed
	public ConfigurableServiceRequestEvaluator serviceRequestEvaluator() {
		ConfigurableServiceRequestEvaluator bean = new ConfigurableServiceRequestEvaluator();
		bean.setServiceProcessor(selectingServiceProcessor());
		bean.setExecutorService(serviceRequestEvaluatorExecutor());
		return bean;
	}

	@Managed
	private ExecutorService serviceRequestEvaluatorExecutor() {
		return Executors.newCachedThreadPool();
	}

	@Managed
	public AuthorizingServiceRequestEvaluator serviceRequestEvaluatorSelfAuthenticating() {
		AuthorizingServiceRequestEvaluator bean = new AuthorizingServiceRequestEvaluator();
		bean.setDelegate(serviceRequestEvaluator());
		bean.setUserSessionProvider(clientCommons.userSessionProvider());
		return bean;
	}
}
