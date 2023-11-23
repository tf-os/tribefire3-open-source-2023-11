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
package tribefire.extension.webapi.web_api_client.wire.space;

import java.util.Set;

import com.braintribe.model.deployment.http.processor.ConfigurableHttpServiceProcessor;
import com.braintribe.model.deployment.http.processor.DynamicHttpServiceProcessor;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.http.resolver.AbstractContextResolver;
import com.braintribe.model.processing.http.resolver.DynamicContextResolver;
import com.braintribe.model.processing.http.resolver.StaticContextResolver;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.processing.http.client.HttpClient;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class WebApiClientServiceProcessorSpace implements WireSpace {

	@Import
	// TODO: relate to TribefirePlatformContract after it got the web unrelated methods
	private TribefireWebPlatformContract tfPlatform;
	
	@Managed
	public ServiceProcessor<ServiceRequest, Object> configurableServiceProcessor(ExpertContext<ConfigurableHttpServiceProcessor> context) {
		com.braintribe.model.processing.http.WebApiClientServiceProcessor bean = new com.braintribe.model.processing.http.WebApiClientServiceProcessor();
		bean.setHttpContextResolver(staticContextResolver(context));
		return bean;
	}

	@Managed
	public ServiceProcessor<ServiceRequest, Object> metaDataMappedServiceProcessor(ExpertContext<DynamicHttpServiceProcessor> context) {
		com.braintribe.model.processing.http.WebApiClientServiceProcessor bean = new com.braintribe.model.processing.http.WebApiClientServiceProcessor();
		bean.setHttpContextResolver(dynamicContextResolver(context));
		return bean;
	}

	private DynamicContextResolver dynamicContextResolver(ExpertContext<DynamicHttpServiceProcessor> context) {
		DynamicHttpServiceProcessor deployable = context.getDeployable();
		DynamicContextResolver bean = new DynamicContextResolver();
		bean.setClientResolver(c -> context.resolve(c, com.braintribe.model.deployment.http.client.HttpClient.T));
		bean.setModelAccessoryFactory(tfPlatform.requestUserRelated().modelAccessoryFactory());
		Set<String> resolverUseCases = deployable.getResolverUseCases();
		if (!resolverUseCases.isEmpty()) {
			bean.setResolverUseCases(resolverUseCases);
		}
		return bean;
	}

	private AbstractContextResolver staticContextResolver(ExpertContext<ConfigurableHttpServiceProcessor> context) {
		ConfigurableHttpServiceProcessor deployable = context.getDeployable();
		com.braintribe.model.deployment.http.client.HttpClient clientDeployable = deployable.getClient();
		HttpClient httpClient = context.resolve(clientDeployable, com.braintribe.model.deployment.http.client.HttpClient.T);

		ServiceDomain domain = deployable.getServiceDomain();
		GmMetaModel serviceModel = domain.getServiceModel();
		ModelAccessory modelAccessory = tfPlatform.systemUserRelated().modelAccessoryFactory().getForModel(serviceModel.getName());

		StaticContextResolver bean = new StaticContextResolver();
		bean.setHttpClient(httpClient);
		bean.setModelAccessory(modelAccessory);
		Set<String> resolverUseCases = deployable.getResolverUseCases();
		if (!resolverUseCases.isEmpty())
			bean.setResolverUseCases(resolverUseCases);

		return bean;
	}
}
