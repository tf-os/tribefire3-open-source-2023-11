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
package tribefire.cortex.openapi_initializer;

import static com.braintribe.utils.lcd.CollectionTools.getList;

import java.util.Set;
import java.util.stream.Stream;

import com.braintribe.model.DdraEndpoint;
import com.braintribe.model.DdraEndpointHeaders;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.ddra.endpoints.TypeExplicitness;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraDeleteEntitiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraDeleteEntitiesEndpointBase;
import com.braintribe.model.ddra.endpoints.v2.DdraGetEntitiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.DdraGetEntitiesEndpointBase;
import com.braintribe.model.ddra.endpoints.v2.DdraManipulateEntitiesEndpoint;
import com.braintribe.model.ddra.endpoints.v2.HasNoAbsenceInformation;
import com.braintribe.model.ddra.endpoints.v2.RestV2Endpoint;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.ConjunctionSelector;
import com.braintribe.model.meta.selector.DisjunctionSelector;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.service.api.AuthorizableRequest;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.initializer.support.api.WiredInitializerContext;
import tribefire.cortex.initializer.support.impl.AbstractInitializer;
import tribefire.cortex.openapi_initializer.wire.OpenapiInitializerModuleWireModule;
import tribefire.cortex.openapi_initializer.wire.contract.ExistingInstancesContract;
import tribefire.cortex.openapi_initializer.wire.contract.OpenapiInitializerModuleMainContract;

public class OpenapiInitializer extends AbstractInitializer<OpenapiInitializerModuleMainContract> {
	@Override
	public WireTerminalModule<OpenapiInitializerModuleMainContract> getInitializerWireModule() {
		return OpenapiInitializerModuleWireModule.INSTANCE;
	}

	@Override
	public void initialize(PersistenceInitializationContext context, WiredInitializerContext<OpenapiInitializerModuleMainContract> initializerContext,
			OpenapiInitializerModuleMainContract initializerMainContract) {

		ManagedGmSession session = context.getSession();

		ExistingInstancesContract existingInstances = initializerMainContract.existingInstances();

		createMetadata(session, existingInstances);
		createDdraMappings(session, existingInstances);
	}

	private void createDdraMappings(ManagedGmSession session, ExistingInstancesContract existingInstancesContract) {
		ApiV1DdraEndpoint endpointPrototype = session.create(ApiV1DdraEndpoint.T);
		endpointPrototype.setAccept(getList("text/yaml"));
		endpointPrototype.setTypeExplicitness(TypeExplicitness.never);

		Set<DdraMapping> ddraMappings = existingInstancesContract.ddraConfiguration().getMappings();

		Stream.of( //
				createOpenapiRequestMapping(session, endpointPrototype, existingInstancesContract.openapiServicesRequestType(), "services"), //
				createOpenapiRequestMapping(session, endpointPrototype, existingInstancesContract.openapiEntitiesRequestType(), "entities"), //
				createOpenapiRequestMapping(session, endpointPrototype, existingInstancesContract.openapiPropertiesRequestType(), "properties") //
		).forEach(ddraMappings::add);
	}

	private void createMetadata(ManagedGmSession session, ExistingInstancesContract existingInstances) {

		ModelMetaDataEditor editor = BasicModelMetaDataEditor.create(existingInstances.ddraEndpointsModel()).withSession(session).done();

		MetaDataSelector usecaseOpenapi = createUseCaseSelector(session, "openapi");
		MetaDataSelector usecaseSimpleView = createUseCaseSelector(session, "openapi:simple");
		MetaDataSelector usecaseAdvancedView = createUseCaseSelector(session, "openapi:advanced");
		MetaDataSelector usecaseIncludeSessionId = createUseCaseSelector(session, "openapi:include-session-id");
		MetaDataSelector flatMimeTypesSelector = flatMimeTypesSelector(session);

		NegationSelector noAdvancedView = session.create(NegationSelector.T, "useCase:openapi:negation:advanced");
		noAdvancedView.setOperand(usecaseAdvancedView);

		Hidden hiddenSimpleView = createHiddenMetadata(session, flatMimeTypesSelector, usecaseSimpleView, "simple");

		Hidden hiddenAlways = createHiddenMetadata(session, flatMimeTypesSelector, usecaseOpenapi, "openapi");

		Hidden hiddenDefaultView = createHiddenMetadata(session, flatMimeTypesSelector, usecaseOpenapi, "openapi-default");

		Visible visibleIncludeSessionId = session.create(Visible.T, "meta:visible:include-session-id");
		visibleIncludeSessionId.setSelector(usecaseIncludeSessionId);
		visibleIncludeSessionId.setConflictPriority(1d);
		visibleIncludeSessionId.setImportant(true);

		// Add metadata on ddra-endpoints-model

		for (Property p : DdraEndpoint.T.getProperties()) {
			setDescription(session, editor, usecaseOpenapi, p, DdraEndpoint.T);

			editor.onEntityType(DdraEndpoint.T).addPropertyMetaData(p, hiddenSimpleView);
		}

		for (Property p : ApiV1DdraEndpoint.T.getDeclaredProperties()) {
			editor.onEntityType(ApiV1DdraEndpoint.T).addPropertyMetaData(p, hiddenSimpleView);
		}

		setDescriptionsForPropertiesOf(session, editor, usecaseOpenapi, //
				DdraGetEntitiesEndpoint.T, //
				DdraGetEntitiesEndpointBase.T, //
				DdraDeleteEntitiesEndpoint.T, //
				DdraDeleteEntitiesEndpointBase.T, //
				DdraManipulateEntitiesEndpoint.T);

		editor.onEntityType(DdraEndpoint.T).addPropertyMetaData("computedDepth", hiddenAlways);
		editor.onEntityType(RestV2Endpoint.T) //
				.addPropertyMetaData("sessionId", hiddenAlways) //
				.addPropertyMetaData(AuthorizableRequest.sessionId, visibleIncludeSessionId);

		editor.onEntityType(HasNoAbsenceInformation.T).addPropertyMetaData("noAbsenceInformation", hiddenDefaultView);
		editor.onEntityType(DdraEndpointHeaders.T).addPropertyMetaData("accept", hiddenDefaultView);
		editor.onEntityType(DdraEndpointHeaders.T).addPropertyMetaData("contentType", hiddenDefaultView);

		// Add metadata on service-api-model
		editor = BasicModelMetaDataEditor.create(existingInstances.serviceApiModel()).withSession(session).done();

		editor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.globalId, hiddenAlways);
		editor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.id, hiddenAlways);
		editor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.partition, hiddenAlways);

		editor.onEntityType(AuthorizableRequest.T) //
				.addPropertyMetaData(AuthorizableRequest.sessionId, hiddenDefaultView) //
				.addPropertyMetaData(AuthorizableRequest.sessionId, visibleIncludeSessionId);

		editor.onEntityType(DomainRequest.T).addPropertyMetaData(DomainRequest.domainId, hiddenDefaultView);
		editor.onEntityType(ServiceRequest.T).addPropertyMetaData("metaData", hiddenDefaultView);
	}

	private MetaDataSelector flatMimeTypesSelector(ManagedGmSession session) {
		MetaDataSelector usecaseOpenapiUrlencoded = createUseCaseSelector(session, "openapi:application/x-www-form-urlencoded");
		MetaDataSelector usecaseOpenapiMultipart = createUseCaseSelector(session, "openapi:multipart/form-data");

		DisjunctionSelector flatMimeTypesSelector = session.create(DisjunctionSelector.T, "openapi:flat-mime-types");
		flatMimeTypesSelector.getOperands().add(usecaseOpenapiUrlencoded);
		flatMimeTypesSelector.getOperands().add(usecaseOpenapiMultipart);

		return flatMimeTypesSelector;
	}

	private Hidden createHiddenMetadata(ManagedGmSession session, MetaDataSelector selector1, MetaDataSelector selector2, String globalIdSuffix) {
		ConjunctionSelector conjunctionSelector = session.create(ConjunctionSelector.T, selector1.getGlobalId() + ":" + globalIdSuffix);
		conjunctionSelector.getOperands().add(selector1);
		conjunctionSelector.getOperands().add(selector2);

		Hidden hidden = session.create(Hidden.T, "meta:hidden:openapi:" + globalIdSuffix);
		hidden.setSelector(conjunctionSelector);

		return hidden;
	}

	private MetaData description(String propertyName, String descriptionText, ManagedGmSession session) {

		LocalizedString localizedDescription = session.create(LocalizedString.T, "localizedDescription:" + propertyName);
		localizedDescription.put(LocalizedString.LOCALE_DEFAULT, descriptionText);

		Description description = session.create(Description.T, "meta:description:" + propertyName);
		description.setDescription(localizedDescription);

		return description;
	}

	private UseCaseSelector createUseCaseSelector(ManagedGmSession session, String useCase) {
		UseCaseSelector usecaseSelector = session.create(UseCaseSelector.T, "useCase:" + useCase);
		usecaseSelector.setUseCase(useCase);

		return usecaseSelector;
	}

	private void setDescription(ManagedGmSession session, ModelMetaDataEditor editor, MetaDataSelector usecaseOpenapi, Property p,
			EntityType<?> type) {
		String description = (String) OpenapiInitializerConstants.ENDPOINT_PROPERTIES_DESCRIPTIONS.get(p.getName());

		if (description != null) {

			MetaData descriptionMetadata = description(type.getShortName() + "." + p.getName(), description, session);
			descriptionMetadata.setSelector(usecaseOpenapi);
			editor.onEntityType(type).addPropertyMetaData(p.getName(), descriptionMetadata);
		}
	}

	private void setDescriptionsForPropertiesOf(ManagedGmSession session, ModelMetaDataEditor editor, MetaDataSelector usecaseOpenapi,
			EntityType<?>... types) {
		for (EntityType<?> entityType : types) {
			for (Property property : entityType.getDeclaredProperties()) {
				setDescription(session, editor, usecaseOpenapi, property, entityType);
			}
		}
	}

	private DdraMapping createOpenapiRequestMapping(ManagedGmSession session, ApiV1DdraEndpoint endpointPrototype, GmEntityType gmType, String path) {
		DdraMapping openapiServices = session.create(DdraMapping.T);

		openapiServices.setRequestType(gmType);
		openapiServices.setMethod(DdraUrlMethod.GET);
		openapiServices.setEndpointPrototype(endpointPrototype);
		openapiServices.setPath("/openapi/" + path);
		openapiServices.setDefaultServiceDomain("cortex");

		return openapiServices;
	}

}
