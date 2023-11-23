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

import com.braintribe.cartridge.common.processing.RequiredTypeEnsurer;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.logging.ThreadRenamer;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.common.EvaluatingAccessService;
import com.braintribe.model.accessory.ModelRetrievingRequest;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.common.ThreadNamingInterceptor;
import com.braintribe.model.processing.service.common.eval.ConfigurableServiceRequestEvaluator;
import com.braintribe.model.processing.session.wire.common.contract.CommonContract;
import com.braintribe.model.processing.session.wire.common.contract.HttpClientProviderContract;
import com.braintribe.model.processing.session.wire.common.contract.RemoteAuthenticationContract;
import com.braintribe.model.processing.session.wire.common.contract.RemoteEvaluatorContract;
import com.braintribe.model.processing.webrpc.client.GmWebRpcRemoteServiceProcessor;
import com.braintribe.model.processing.webrpc.client.RemoteAuthentifyingInterceptor;
import com.braintribe.model.processing.webrpc.client.UnmarshallingLenienatingInterceptor;
import com.braintribe.model.processing.webrpc.client.UserSessionResolver;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

@Managed
public class RemoteEvaluatorSpace implements RemoteEvaluatorContract {
	@Import
	private RemoteAuthenticationContract remoteAuthentication;

	@Import
	private RemoteSessionSpace remoteSession;

	@Import
	private CommonContract common;

	@Import
	private HttpClientProviderContract httpClientProvider;

	@Override
	@Managed
	public RemoteAuthentifyingInterceptor remotifyingInterceptor() {
		RemoteAuthentifyingInterceptor bean = new RemoteAuthentifyingInterceptor();
		bean.setUserSessionResolver(userSessionResolver());

		return bean;
	}

	@Override
	@Managed
	public UserSessionResolver userSessionResolver() {
		UserSessionResolver bean = new UserSessionResolver();
		bean.setCredentials(remoteAuthentication.credentials());

		return bean;
	}

	@Override
	@Managed
	public ConfigurableServiceRequestEvaluator evaluator() {
		ConfigurableServiceRequestEvaluator bean = new ConfigurableServiceRequestEvaluator();
		bean.setExecutorService(common.executorService());
		bean.setServiceProcessor(dispatchingServiceProcessor());
		return bean;
	}

	@Managed
	private ConfigurableDispatchingServiceProcessor dispatchingServiceProcessor() {
		ConfigurableDispatchingServiceProcessor bean = new ConfigurableDispatchingServiceProcessor();
		bean.register(ServiceRequest.T, remoteServiceProcessor());
		bean.registerInterceptor("remotify").register(remotifyingInterceptor());
		bean.registerInterceptor("model-unmarshalling-lenience").registerForType(ModelRetrievingRequest.T, unmarshallingLenienator());

		return bean;
	}

	@Managed
	private UnmarshallingLenienatingInterceptor unmarshallingLenienator() {
		UnmarshallingLenienatingInterceptor bean = new UnmarshallingLenienatingInterceptor();

		return bean;
	}

	@Managed
	private ThreadNamingInterceptor threadNamingInterceptor() {
		ThreadNamingInterceptor bean = new ThreadNamingInterceptor();
		bean.setThreadRenamer(new ThreadRenamer(true));
		return bean;
	}

	@Managed
	private GmWebRpcRemoteServiceProcessor remoteServiceProcessor() {
		GmWebRpcRemoteServiceProcessor bean = new GmWebRpcRemoteServiceProcessor();
		bean.setUrl(remoteAuthentication.tfServicesUrl() + "/rpc");
		bean.setStreamPipeFactory(common.streamPipeFactory());
		bean.setMarshaller(bin2Marshaller());
		bean.setHttpClientProvider(this.httpClientProvider.httpClientProvider());
		return bean;
	}

	@Managed
	private Marshaller bin2Marshaller() {
		Bin2Marshaller bean = new Bin2Marshaller();
		bean.setRequiredTypesReceiver(requiredTypesReceiver());
		return bean;
	}

	@Managed
	private RequiredTypeEnsurer requiredTypesReceiver() {
		RequiredTypeEnsurer bean = new RequiredTypeEnsurer();
		bean.setAccessService(accessService());
		return bean;
	}

	@Override
	@Managed
	public AccessService accessService() {
		EvaluatingAccessService bean = new EvaluatingAccessService(evaluator());
		return bean;
	}

}
