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

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.http.meta.HttpDefaultFailureResponseType;
import com.braintribe.model.deployment.http.meta.HttpProcessWith;
import com.braintribe.model.extensiondeployment.meta.PreProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.typecondition.origin.IsDeclaredIn;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.Unmodifiable;
import com.braintribe.model.meta.selector.EntityTypeSelector;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.okta.api.model.AuthorizedOktaRequest;
import tribefire.extension.okta.api.model.OktaRequest;
import tribefire.extension.okta.api.model.auth.GetAccessToken;
import tribefire.extension.okta.api.model.auth.GetClientSecretAccessToken;
import tribefire.extension.okta.api.model.auth.GetOauthAccessToken;
import tribefire.extension.okta.api.model.auth.HasAuthorization;
import tribefire.extension.okta.api.model.user.GetGroup;
import tribefire.extension.okta.api.model.user.GetUser;
import tribefire.extension.okta.api.model.user.GetUserGroups;
import tribefire.extension.okta.api.model.user.ListAppGroups;
import tribefire.extension.okta.api.model.user.ListAppUsers;
import tribefire.extension.okta.api.model.user.ListGroupMembers;
import tribefire.extension.okta.api.model.user.ListGroups;
import tribefire.extension.okta.api.model.user.ListUsers;
import tribefire.extension.okta.deployment.model.OktaAuthenticationSupplier;
import tribefire.extension.okta.model.OktaError;
import tribefire.extension.okta.templates.api.OktaTemplateContext;
import tribefire.extension.okta.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.okta.templates.wire.contract.OktaHttpContract;
import tribefire.extension.okta.templates.wire.contract.OktaMetaDataContract;
import tribefire.extension.okta.templates.wire.contract.OktaModelsContract;
import tribefire.extension.okta.templates.wire.contract.OktaTemplatesContract;
import tribrefire.extension.okta.common.OktaCommons;

@Managed
public class OktaMetaDataSpace implements WireSpace, OktaMetaDataContract, OktaCommons {

	private static final Logger logger = Logger.getLogger(OktaMetaDataSpace.class);

	@Import
	private OktaModelsContract models;

	@Import
	private OktaTemplatesContract templates;

	@Import
	private ExistingInstancesContract existing;

	@Import
	private OktaTemplatesContract initializer;

	@Import
	private OktaHttpContract http;

	@Override
	public void configureMetaData(OktaTemplateContext context) {

		configureDataModelMetaData(context);
		configureApiModelMetaData(context);

	}

	private void configureApiModelMetaData(OktaTemplateContext context) {

		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(models.configuredOktaApiModel(context));

		// OktaRequest related

		editor.onEntityType(OktaRequest.T).addMetaData(oktaRequestProcessingMds(context));
		editor.onEntityType(OktaRequest.T).addMetaData(httpDefaultFailureResponseType(context));
		editor.onEntityType(AuthorizedOktaRequest.T).addMetaData(preProcessWithAuthorization(context));

		editor.onEntityType(ListUsers.T).addMetaData(http.httpGet(context), http.httpPathForListUsers(context));
		editor.onEntityType(ListUsers.T).addPropertyMetaData(http.httpQueryParamAsIsIfDeclared(context));
		editor.onEntityType(ListUsers.T).addPropertyMetaData(ListUsers.query, http.httpQueryParamForQuery(context));
		editor.onEntityType(GetUser.T).addMetaData(http.httpGet(context), http.httpPathForGetUser(context));
		editor.onEntityType(GetUserGroups.T).addMetaData(http.httpGet(context), http.httpPathForGetUserGroups(context));
		editor.onEntityType(ListGroups.T).addMetaData(http.httpGet(context), http.httpPathForListGroups(context));
		editor.onEntityType(ListGroups.T).addPropertyMetaData(ListGroups.query, http.httpQueryParamForQuery(context));
		editor.onEntityType(ListGroupMembers.T).addMetaData(http.httpGet(context), http.httpPathForListGroupMembers(context));
		editor.onEntityType(GetGroup.T).addMetaData(http.httpGet(context), http.httpPathForGetGroup(context));
		editor.onEntityType(ListAppUsers.T).addMetaData(http.httpGet(context), http.httpPathForListAppUsers(context));
		editor.onEntityType(ListAppGroups.T).addMetaData(http.httpGet(context), http.httpPathForListAppGroups(context));

		if (context.getDefaultAuthenticationSupplier() != null) {
			editor.onEntityType(HasAuthorization.T)
					.addMetaData(preProcessWithConfiguredAuthorization(context, context.getDefaultAuthenticationSupplier()));
		}

		configureAuthorizationRequests(context, editor);
	}

	@Override
	public void configureAuthorizationRequests(OktaTemplateContext context, ModelMetaDataEditor editor) {
		editor.onEntityType(GetAccessToken.T).addPropertyMetaData(GetOauthAccessToken.grantType, http.httpBodyParamForGrantType(context));
		editor.onEntityType(GetAccessToken.T).addPropertyMetaData(GetOauthAccessToken.scope, http.httpBodyParamForScope(context));

		editor.onEntityType(GetOauthAccessToken.T).addMetaData(http.httpPost(context), http.httpPathForGetOauthAccessToken(context),
				http.httpConsumesXWwwFormUrlEncoded(context));
		editor.onEntityType(GetOauthAccessToken.T).addPropertyMetaData(GetOauthAccessToken.clientAssertionType,
				http.httpQueryParamForClientAssertionType(context));
		editor.onEntityType(GetOauthAccessToken.T).addPropertyMetaData(GetOauthAccessToken.clientAssertion,
				http.httpQueryParamForClientAssertion(context));

		editor.onEntityType(GetClientSecretAccessToken.T).addMetaData(http.httpPost(context), http.httpPathForGetClientSecretAccessToken(context),
				http.httpConsumesXWwwFormUrlEncoded(context));
		editor.onEntityType(GetClientSecretAccessToken.T).addPropertyMetaData(GetClientSecretAccessToken.clientId,
				http.httpBodyParamForClientId(context));
		editor.onEntityType(GetClientSecretAccessToken.T).addPropertyMetaData(GetClientSecretAccessToken.clientSecret,
				http.httpBodyParamForClientSecret(context));

		editor.onEntityType(HasAuthorization.T).addPropertyMetaData(HasAuthorization.authorization, http.httpHeaderParamForAuthorization(context));

	}

	@Managed
	public PreProcessWith preProcessWithAuthorization(OktaTemplateContext context) {
		PreProcessWith bean = context.create(PreProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(initializer.authorizationPreProcessor(context));
		return bean;
	}

	@Managed
	public PreProcessWith preProcessWithConfiguredAuthorization(OktaTemplateContext context, OktaAuthenticationSupplier authSupplier) {
		PreProcessWith bean = context.create(PreProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(initializer.configuredAuthorizationPreProcessor(context, authSupplier));
		return bean;
	}

	@Managed
	public MetaData[] oktaRequestProcessingMds(OktaTemplateContext context) {
		MetaData[] bean = new MetaData[] { processWithOktaHttpProcessor(context), httpProcessWithOktaClient(context) };
		return bean;
	}

	@Managed
	private ProcessWith processWithOktaHttpProcessor(OktaTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setProcessor(http.dynamicHttpProcessor(context));
		return bean;
	}

	@Managed
	private HttpProcessWith httpProcessWithOktaClient(OktaTemplateContext context) {
		HttpProcessWith bean = context.create(HttpProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setClient(http.oktaHttpClient(context, context.getAccessAuthenticationSupplier()));
		return bean;
	}

	@Managed
	public HttpDefaultFailureResponseType httpDefaultFailureResponseType(OktaTemplateContext context) {
		HttpDefaultFailureResponseType bean = context.create(HttpDefaultFailureResponseType.T, InstanceConfiguration.currentInstance());
		bean.setResponseType(context.lookup("type:" + OktaError.T.getTypeSignature()));
		return bean;
	}

	private void configureDataModelMetaData(OktaTemplateContext context) {
		ModelMetaDataEditor editor = new BasicModelMetaDataEditor(models.configuredOktaAccessModel(context));

		editor.onEntityType(GenericEntity.T).addPropertyMetaData(unmodifiableIfDeclaredInOktaModel(context));
	}

	@Managed
	public Unmodifiable unmodifiableIfDeclaredInOktaModel(OktaTemplateContext context) {
		Unmodifiable bean = context.create(Unmodifiable.T, InstanceConfiguration.currentInstance());
		bean.setSelector(declaredInOktaModelSelector(context));
		return bean;
	}

	@Managed
	private EntityTypeSelector declaredInOktaModelSelector(OktaTemplateContext context) {
		EntityTypeSelector bean = context.create(EntityTypeSelector.T, InstanceConfiguration.currentInstance());
		bean.setTypeCondition(isDeclaredInOktaModelCondition(context));
		return bean;
	}
	@Managed
	private IsDeclaredIn isDeclaredInOktaModelCondition(OktaTemplateContext context) {
		IsDeclaredIn bean = context.create(IsDeclaredIn.T, InstanceConfiguration.currentInstance());
		bean.setModelName(OKTA_DATA_MODEL_NAME);
		return bean;
	}

}
