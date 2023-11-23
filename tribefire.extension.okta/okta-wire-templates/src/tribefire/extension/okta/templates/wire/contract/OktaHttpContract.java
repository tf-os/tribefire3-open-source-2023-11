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
package tribefire.extension.okta.templates.wire.contract;

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
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.okta.templates.api.OktaTemplateContext;

public interface OktaHttpContract extends WireSpace {

	DynamicHttpServiceProcessor dynamicHttpProcessor(OktaTemplateContext context);

	HttpGet httpGet(OktaTemplateContext context);

	HttpPost httpPost(OktaTemplateContext context);

	HttpPath httpPathForListUsers(OktaTemplateContext context);

	HttpPath httpPathForGetUser(OktaTemplateContext context);

	HttpPath httpPathForGetUserGroups(OktaTemplateContext context);

	HttpPath httpPathForListGroups(OktaTemplateContext context);

	HttpPath httpPathForListGroupMembers(OktaTemplateContext context);

	HttpPath httpPathForGetGroup(OktaTemplateContext context);

	HttpPath httpPathForListAppUsers(OktaTemplateContext context);

	HttpPath httpPathForListAppGroups(OktaTemplateContext context);

	HttpPath httpPathForGetOauthAccessToken(OktaTemplateContext context);

	HttpPath httpPathForGetClientSecretAccessToken(OktaTemplateContext context);

	HttpQueryParam httpQueryParamAsIsIfDeclared(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForQuery(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForGrantType(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForScope(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForClientAssertionType(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForClientAssertion(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForClientId(OktaTemplateContext context);

	HttpQueryParam httpQueryParamForClientSecret(OktaTemplateContext context);

	HttpConsumes httpConsumesXWwwFormUrlEncoded(OktaTemplateContext context);

	HttpHeaderParam httpHeaderParamForAuthorization(OktaTemplateContext context);

	HttpDefaultFailureResponseType httpDefaultFailureResponseType(OktaTemplateContext context);

	HttpClient oktaHttpClient(OktaTemplateContext context, OktaAuthenticationSupplier oktaAuthenticationSupplier);

	HttpBodyParam httpBodyParamForGrantType(OktaTemplateContext context);

	HttpBodyParam httpBodyParamForClientSecret(OktaTemplateContext context);

	HttpBodyParam httpBodyParamForScope(OktaTemplateContext context);

	HttpBodyParam httpBodyParamForClientId(OktaTemplateContext context);

}
