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
package tribefire.extension.okta.templates.wire.space;

import java.util.concurrent.atomic.AtomicInteger;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.http.client.GmHttpClient;
import com.braintribe.model.deployment.http.client.HttpClient;
import com.braintribe.model.deployment.http.meta.HttpConsumes;
import com.braintribe.model.deployment.http.meta.HttpDefaultFailureResponseType;
import com.braintribe.model.deployment.http.meta.HttpPath;
import com.braintribe.model.deployment.http.meta.methods.HttpGet;
import com.braintribe.model.deployment.http.meta.methods.HttpPost;
import com.braintribe.model.deployment.http.meta.params.HttpBodyParam;
import com.braintribe.model.deployment.http.meta.params.HttpHeaderParam;
import com.braintribe.model.deployment.http.meta.params.HttpQueryParam;
import com.braintribe.model.deployment.http.processor.DynamicHttpServiceProcessor;
import com.braintribe.model.meta.selector.DeclaredPropertySelector;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.okta.deployment.model.OktaClientSecretTokenAuthenticationSupplier;
import tribefire.extension.okta.model.OktaError;
import tribefire.extension.okta.templates.api.OktaTemplateContext;
import tribefire.extension.okta.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.okta.templates.wire.contract.OktaHttpContract;
import tribefire.extension.okta.templates.wire.contract.OktaModelsContract;
import tribefire.extension.okta.templates.wire.contract.OktaTemplatesContract;
import tribrefire.extension.okta.common.OktaCommons;

@Managed
public class OktaHttpSpace implements WireSpace, OktaHttpContract, OktaCommons {

	private static final Logger logger = Logger.getLogger(OktaHttpSpace.class);

	@Import
	private OktaModelsContract models;

	@Import
	private OktaTemplatesContract templates;

	@Import
	private ExistingInstancesContract existing;

	@Import
	private OktaTemplatesContract initializer;

	private static AtomicInteger counter = new AtomicInteger(0);

	@Override
	@Managed
	public DynamicHttpServiceProcessor dynamicHttpProcessor(OktaTemplateContext context) {
		DynamicHttpServiceProcessor bean = context.create(DynamicHttpServiceProcessor.T, InstanceConfiguration.currentInstance());
		bean.setName(OKTA_HTTP_PROCESSOR_NAME + " " + context.getContext());
		bean.setExternalId(context.getIdPrefix() + "." + OKTA_HTTP_PROCESSOR_EXTERNALID);
		return bean;
	}

	@Managed
	@Override
	public HttpGet httpGet(OktaTemplateContext context) {
		HttpGet bean = context.create(HttpGet.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	@Override
	public HttpPost httpPost(OktaTemplateContext context) {
		HttpPost bean = context.create(HttpPost.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForListUsers(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_USERS);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForGetUser(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_USER);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForGetUserGroups(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_USER_GROUPS);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForListGroups(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_GROUPS);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForListGroupMembers(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_LIST_GROUP_MEMBERS);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForGetGroup(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_GROUP);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForListAppUsers(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_LIST_APP_USERS);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForListAppGroups(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_LIST_APP_GROUPS);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForGetOauthAccessToken(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath(OKTA_HTTP_PATH_GET_OAUTH_ACCESS_TOKEN);
		return bean;
	}

	@Managed
	@Override
	public HttpPath httpPathForGetClientSecretAccessToken(OktaTemplateContext context) {
		HttpPath bean = context.create(HttpPath.T, InstanceConfiguration.currentInstance());
		bean.setPath("");
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamAsIsIfDeclared(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setSelector(declaredPropertySelector(context));
		bean.setConflictPriority(0d);
		return bean;
	}

	@Managed
	private DeclaredPropertySelector declaredPropertySelector(OktaTemplateContext context) {
		DeclaredPropertySelector bean = context.create(DeclaredPropertySelector.T, InstanceConfiguration.currentInstance());
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForQuery(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("q");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForGrantType(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("grant_type");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForScope(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("scope");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForClientAssertionType(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("client_assertion_type");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForClientAssertion(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("client_assertion");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForClientId(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("client_id");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpQueryParam httpQueryParamForClientSecret(OktaTemplateContext context) {
		HttpQueryParam bean = context.create(HttpQueryParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("client_secret");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpConsumes httpConsumesXWwwFormUrlEncoded(OktaTemplateContext context) {
		HttpConsumes bean = context.create(HttpConsumes.T, InstanceConfiguration.currentInstance());
		bean.setMimeType("application/x-www-form-urlencoded");
		bean.setConflictPriority(1d);
		return bean;
	}

	@Managed
	@Override
	public HttpHeaderParam httpHeaderParamForAuthorization(OktaTemplateContext context) {
		HttpHeaderParam bean = context.create(HttpHeaderParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName(OKTA_HTTP_PARAM_AUTHORIZATION);
		return bean;
	}

	@Managed
	@Override
	public HttpDefaultFailureResponseType httpDefaultFailureResponseType(OktaTemplateContext context) {
		HttpDefaultFailureResponseType bean = context.create(HttpDefaultFailureResponseType.T, InstanceConfiguration.currentInstance());
		bean.setResponseType(context.lookup("type:" + OktaError.T.getTypeSignature()));
		return bean;
	}

	@Override
	@Managed
	public HttpClient oktaHttpClient(OktaTemplateContext context, OktaAuthenticationSupplier oktaAuthenticationSupplier) {
		GmHttpClient bean = context.create(GmHttpClient.T, InstanceConfiguration.currentInstance());
		int count = counter.incrementAndGet();
		bean.setName(OKTA_HTTP_CONNECTOR_NAME + " " + context.getName() + " (" + count + ")");
		bean.setExternalId(context.getIdPrefix() + "." + OKTA_HTTP_CONNECTOR_EXTERNALID + "." + count);

		String oktaUrl = null;
		if (oktaAuthenticationSupplier instanceof OktaClientSecretTokenAuthenticationSupplier cs) {
			oktaUrl = cs.getTokenUrl();
		} else {
			oktaUrl = context.getServiceBaseUrl();
		}

		if (oktaUrl == null) {
			logger.warn("OKTA_CLIENT_SECRET_TOKEN_URL is not configured which is required to get the Okta HTTP Client functional.");
		}
		bean.setBaseUrl(oktaUrl);
		return bean;
	}

	@Managed
	@Override
	public HttpBodyParam httpBodyParamForGrantType(OktaTemplateContext context) {
		HttpBodyParam bean = context.create(HttpBodyParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("grant_type");
		bean.setConflictPriority(1d);
		return bean;
	}
	@Managed
	@Override
	public HttpBodyParam httpBodyParamForClientSecret(OktaTemplateContext context) {
		HttpBodyParam bean = context.create(HttpBodyParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("client_secret");
		bean.setConflictPriority(1d);
		return bean;
	}
	@Managed
	@Override
	public HttpBodyParam httpBodyParamForScope(OktaTemplateContext context) {
		HttpBodyParam bean = context.create(HttpBodyParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("scope");
		bean.setConflictPriority(1d);
		return bean;
	}
	@Managed
	@Override
	public HttpBodyParam httpBodyParamForClientId(OktaTemplateContext context) {
		HttpBodyParam bean = context.create(HttpBodyParam.T, InstanceConfiguration.currentInstance());
		bean.setParamName("client_id");
		bean.setConflictPriority(1d);
		return bean;
	}

}
