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
package tribefire.extension.webapi.http_client.wire.space;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;

import com.braintribe.cartridge.common.wire.contract.HttpClientBindersContract;
import com.braintribe.model.deployment.http.client.GmHttpClient;
import com.braintribe.model.deployment.http.client.HttpCredentials;
import com.braintribe.model.deployment.http.client.credentials.HttpUsernamePasswordCredentials;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.processing.http.client.HttpClient;
import com.braintribe.utils.logging.LogLevels;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.module.api.WireContractBindingBuilder;
import tribefire.module.wire.contract.ResourceProcessingContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class HttpClientModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private HttpClientBindersSpace httpClientBinders;

	@Import
	private HttpClientBindersContract httpConnectorComponents;

	@Import
	private HttpSpace http;

	@Import
	private ResourceProcessingContract resourceProcessing;

	//
	// WireContracts
	//

	@Override
	public void bindWireContracts(WireContractBindingBuilder bindings) {
		// Bind wire contracts to make them available for other modules.
		// Note that the Contract class cannot be defined in this module, but must be in a gm-api artifact.
		bindings.bind(HttpClientBindersContract.class, httpClientBinders);
	}

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// Bind deployment experts for deployable denotation types.
		// Note that the basic component binders (for e.g. serviceProcessor or incrementalAccess) can be found via
		// tfPlatform.deployment().binders().

		bindings.bind(GmHttpClient.T).component(httpConnectorComponents.httpClient()).expertFactory(this::gmHttpClient);
	}

	@Managed
	private HttpClient gmHttpClient(ExpertContext<GmHttpClient> context) {
		GmHttpClient deployable = context.getDeployable();
		com.braintribe.processing.http.client.GmHttpClient bean = new com.braintribe.processing.http.client.GmHttpClient();
		bean.setBaseUrl(deployable.getBaseUrl());
		bean.setMarshallerRegistry(tfPlatform.marshalling().registry());
		bean.setHttpClientProvider(http.clientProvider());
		bean.setHttpRequestConfig(buildRequestConfig(deployable));
		bean.setCredentials(getCredentials(deployable));
		bean.setRequestLogging(LogLevels.convert(deployable.getRequestLogging()));
		bean.setResponseLogging(LogLevels.convert(deployable.getResponseLogging()));
		bean.setEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setStreamPipeFactory(resourceProcessing.streamPipeFactory());

		return bean;
	}

	private Credentials getCredentials(com.braintribe.model.deployment.http.client.HttpClient deployable) {
		HttpCredentials credentials = deployable.getCredentials();

		if (credentials != null) {
			if (credentials instanceof HttpUsernamePasswordCredentials) {
				HttpUsernamePasswordCredentials userPasswordCredentials = (HttpUsernamePasswordCredentials) credentials;
				return new UsernamePasswordCredentials(userPasswordCredentials.getUser(), userPasswordCredentials.getPassword());
			} else {
				throw new IllegalArgumentException("Unsuppored HttpCredentials configured: " + credentials);
			}
		}

		return null;
	}

	private RequestConfig buildRequestConfig(GmHttpClient deployable) {
		Builder configBuilder = RequestConfig.custom();

		configBuilder.setAuthenticationEnabled(deployable.getAuthenticationEnabled());
		configBuilder.setRedirectsEnabled(deployable.getRedirectsEnabled());
		configBuilder.setRelativeRedirectsAllowed(deployable.getRelativeRedirectsAllowed());
		configBuilder.setContentCompressionEnabled(deployable.getContentCompressionEnabled());

		setIfNotNull(deployable::getProxy, v -> configBuilder.setProxy(HttpHost.create(v)));
		setIfNotNull(deployable::getLocalAddress, v -> setLocalAddress(configBuilder, v));
		setIfNotNull(deployable::getCookieSpec, configBuilder::setCookieSpec);
		setIfNotNull(deployable::getConnectTimeout, configBuilder::setConnectTimeout);
		setIfNotNull(deployable::getConnectionRequestTimeout, configBuilder::setConnectionRequestTimeout);
		setIfNotNull(deployable::getSocketTimeout, configBuilder::setSocketTimeout);
		setIfNotNull(deployable::getMaxRedirects, configBuilder::setMaxRedirects);

		return configBuilder.build();
	}

	private void setLocalAddress(Builder configBuilder, String localAddress) {
		try {
			configBuilder.setLocalAddress(InetAddress.getByName(localAddress));
		} catch (UnknownHostException e) {
			throw new RuntimeException("Unknown local address: " + localAddress + " supplied", e);
		}
	}

	private <T> void setIfNotNull(Supplier<T> supplier, Consumer<T> consumer) {
		T value = supplier.get();
		if (value != null) {
			consumer.accept(value);
		}
	}

}
