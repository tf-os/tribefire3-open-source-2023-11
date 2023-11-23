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
package com.braintribe.model.processing.ddra.endpoints.rest.v2.ioc;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import com.braintribe.ddra.test.model.TestServiceMetadataRequest;
import com.braintribe.ddra.test.model.TestServiceRequest;
import com.braintribe.ddra.test.model.TestServiceRequestExtended;
import com.braintribe.ddra.test.model.TestServiceResponse;
import com.braintribe.ddra.test.model.TestServiceSimplepropsRequest;
import com.braintribe.gm.model.reason.meta.HttpStatusCode;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.ConfigurableCloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.ddra.endpoints.api.v1.WebApiUseCases;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.NeutralRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.NullRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.Person;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ResponseCodeOverridingRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestAmbigiousNestingRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestDeleteServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestDomainRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestGetServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestPostServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestPutServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestReasoningServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestServiceRequestWithEntityProperty;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestServiceRequestWithResources;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequestSimple;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.reason.IncompleteReason;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.reason.TestReason;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.testing.model.test.technical.features.AnotherComplexEntity;
import com.braintribe.testing.model.test.technical.features.CollectionEntity;
import com.braintribe.testing.model.test.technical.features.ComplexEntity;
import com.braintribe.testing.model.test.technical.features.PrimitiveTypesEntity;
import com.braintribe.testing.model.test.technical.features.SimpleEntity;
import com.braintribe.testing.model.test.testtools.TestModelTestTools;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.utils.CollectionTools;

public class TestAccessSpace {

	public static IncrementalAccess cortexAccess() {
		try {
			GmMetaModel ddraEndpointsModelInitial = ApiV1DdraEndpoint.T.getModel().getMetaModel();
			GmMetaModel reasonMetaDataModel = HttpStatusCode.T.getModel().getMetaModel();
			GmMetaModel iDontKnowWhyButWeNeedToDependOnThisModel = Now.T.getModel().getMetaModel();
			GmMetaModel cortexApiModel = GMF.getTypeReflection().getModel("tribefire.cortex:cortex-api-model").getMetaModel();
			GmMetaModel cortexModel = model("test:cortex-model");
			cortexModel.getDependencies().add(cortexApiModel);
			cortexModel.getDependencies().add(reasonMetaDataModel);
			cortexModel.getDependencies().add(ddraEndpointsModelInitial);
			cortexModel.getDependencies().add(iDontKnowWhyButWeNeedToDependOnThisModel);
			SmoodAccess cortexAccess = GmTestTools.newSmoodAccessMemoryOnly("cortex", cortexModel);
			cortexAccess.getDatabase().setDefaultPartition("cortex");
			cortexAccess.getDatabase().setIgnorePartitions(false);

			// Note: The cortex model has to depend on all models it needs before creating this session. Still take
			// great care which models you edit in the initializer: the original one (created above this comment) or the
			// one cloned into the cortex session. Because they are of course different. Just keep it in your mind when
			// you can't understand what's going on. I had to learn it the hard way.
			// TODO: create proper initializer & wiring
			PersistenceGmSession session = GmTestTools.newSession(cortexAccess);
			
			cortexModel = cloneToSession(cortexModel, session);
			
			GmMetaModel defaultDomainModel = GMF.getTypeReflection().getModel("com.braintribe.gm:user-model").getMetaModel();
			GmMetaModel sessionDomainModel = cloneToSession(defaultDomainModel, session);
			sessionDomainModel.setName("test:default-domain-model");
			
			ServiceDomain serviceDomain = session.create(ServiceDomain.T);
			serviceDomain.setExternalId("serviceDomain:default");
			serviceDomain.setServiceModel(sessionDomainModel);
			
			GmMetaModel failureModel = cloneToSession(new NewMetaModelGeneration().buildMetaModel("test.model.failure", CollectionTools.getList(Failure.T)), session);
			
			GmMetaModel testModel = cloneToSession(model("test.model", TestServiceSimplepropsRequest.T, TestServiceRequestExtended.T, TestServiceRequest.T, TestServiceMetadataRequest.T, TestServiceResponse.T, User.T, Role.T, Group.T),session);
			GmMetaModel testServiceModel = cloneToSession(model("test.model2", com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestServiceRequest.T, TestServiceRequestWithResources.T, ZipRequest.T, ZipRequestSimple.T, TestServiceRequestWithEntityProperty.T, NullRequest.T, NeutralRequest.T, ResponseCodeOverridingRequest.T, GmqlRequest.T, TestGetServiceRequest.T, OpenUserSessionWithUserAndPassword.T, TestPostServiceRequest.T, TestPutServiceRequest.T, TestDeleteServiceRequest.T, TestDomainRequest.T, TestReasoningServiceRequest.T, TestReason.T, IncompleteReason.T, TestAmbigiousNestingRequest.T, Person.T),session);
			GmMetaModel swaggerModel = cloneToSession(ddraEndpointsModelInitial,session);
			GmMetaModel emptyModel = cloneToSession(model("empty.model"),session);
			
			testModel.getDependencies().add(failureModel);
			
			sessionDomainModel.getDependencies().add(testServiceModel);
			
			cloneToSession(serviceDomain("test.domain1", testModel), session);
			cloneToSession(serviceDomain("test.domain2", testModel), session);
			cloneToSession(serviceDomain("test.empty.domain", emptyModel), session);
			
			UseCaseSelector mappingSpecificUsecaseAutoPath = session.create(UseCaseSelector.T);
			mappingSpecificUsecaseAutoPath.setUseCase(WebApiUseCases.mappingSpecificUseCase("/test.domain2/" + TestServiceMetadataRequest.T.getTypeSignature()));

			Hidden hiddenDddraMapping = session.create(Hidden.T);
			hiddenDddraMapping.setSelector(mappingSpecificUsecaseAutoPath);
			
			UseCaseSelector mappingSpecificUsecaseWithPath = session.create(UseCaseSelector.T);
			mappingSpecificUsecaseWithPath.setUseCase(WebApiUseCases.mappingSpecificUseCase("/meta/on/endpoint"));

			Hidden hiddenDddraMappingPath = session.create(Hidden.T);
			hiddenDddraMappingPath.setSelector(mappingSpecificUsecaseWithPath);
			
			Priority highPrio = session.create(Priority.T);
			highPrio.setPriority(100);

			Priority lowPrio = session.create(Priority.T);
			lowPrio.setPriority(-1);
			
			Mandatory mandatoryMd = session.create(Mandatory.T);

			BasicModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(testModel).withSession(session).done();
			mdEditor.onEntityType(TestServiceRequest.T).addPropertyMetaData("mandatoryProperty", mandatoryMd);
			mdEditor.onEntityType(TestServiceResponse.T).addPropertyMetaData("intProperty", mandatoryMd);
			mdEditor.onEntityType(TestServiceRequestExtended.T).addPropertyMetaData("invisibleProperty", session.create(Hidden.T));
			mdEditor.onEntityType(TestServiceRequestExtended.T).addPropertyMetaData("veryHighPriorityProperty", highPrio);
			mdEditor.onEntityType(TestServiceRequestExtended.T).addPropertyMetaData("lowPriorityProperty", lowPrio);

			mdEditor.onEntityType(TestServiceMetadataRequest.T)
				.addPropertyMetaData("bigDecimalProperty", hiddenDddraMapping)
				.addPropertyMetaData("propertyWithInitializer", hiddenDddraMappingPath);

			Embedded embeddedWithUsecase = session.create(Embedded.T);
			UseCaseSelector useCaseSelector = session.create(UseCaseSelector.T);
			useCaseSelector.setUseCase(WebApiUseCases.USECASE_DDRA);
			
			mdEditor = BasicModelMetaDataEditor.create(testServiceModel).withSession(session).done();
			mdEditor.onEntityType(TestServiceRequestWithEntityProperty.T).addPropertyMetaData("zipRequest", session.create(Embedded.T));
			mdEditor.onEntityType(ZipRequestSimple.T).addPropertyMetaData("resource", embeddedWithUsecase);
			mdEditor.onEntityType(TestServiceRequestWithResources.T).addPropertyMetaData("embedded", embeddedWithUsecase);
			mdEditor.onEntityType(TestAmbigiousNestingRequest.T).addPropertyMetaData("owner", session.create(Embedded.T));
			
			HttpStatusCode statusCode = session.create(HttpStatusCode.T);
			statusCode.setCode(555);
			
			mdEditor.onEntityType(IncompleteReason.T).addMetaData(statusCode);

			// Don't use a session for this one...
			mdEditor = BasicModelMetaDataEditor.create(ddraEndpointsModelInitial).done();
			mdEditor.onEntityType(ApiV1DdraEndpoint.T)
				.addPropertyMetaData("projection", hiddenDddraMapping)
				.addPropertyMetaData("depth", hiddenDddraMappingPath);
			
			com.braintribe.model.accessdeployment.IncrementalAccess access = session.create(com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess.T);
			access.setExternalId("test.access");
			access.setDeploymentStatus(DeploymentStatus.deployed);
			access.setMetaModel(testModel);
			access.setServiceModel(testServiceModel);
			
			com.braintribe.model.accessdeployment.IncrementalAccess cortex = session.create(com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess.T);
			cortex.setExternalId("cortex");
			cortex.setDeploymentStatus(DeploymentStatus.deployed);
			cortex.setMetaModel(cortexModel);
			cortex.setServiceModel(swaggerModel);
			
			session.commit();

			return cortexAccess;

		} catch (Exception e) {
			throw new RuntimeException("Error setting up cortex access.",e);
		}
	}

	private static <T extends GenericEntity> T cloneToSession(T toBeCloned, PersistenceGmSession session) {
		CloningContext cc = ConfigurableCloningContext.build() //
				.supplyRawCloneWith(session) //
				.withAssociatedResolver(e -> {
					String gid = e.getGlobalId();
					return gid == null ? null : session.findEntityByGlobalId(gid);
				}) //
				.done();

		return toBeCloned.clone(cc);
	}
	
	private static GmMetaModel model(String name, EntityType<?>... entities) {
		return new NewMetaModelGeneration().buildMetaModel(name, asList(entities));
	}
	
	private static ServiceDomain serviceDomain(String externalId, GmMetaModel model) {
		ServiceDomain domain = ServiceDomain.T.create();
		domain.setExternalId(externalId);
		domain.setServiceModel(model);
		domain.setName(externalId);
		return domain;
	}
	
	public static IncrementalAccess testAccess(boolean ignorePartitions, boolean addEntities) {
		return testAccess("test.access", ignorePartitions, addEntities);
	}

	private static IncrementalAccess testAccess(String accessId, boolean ignorePartitions, boolean addEntities) {
		GmMetaModel metaModel = TestModelTestTools.createTestModelMetaModel();

		addMandatoryPropertyOnAnotherComplexEntity(metaModel);
		addTypeSpecificationOnStandardIdentifiable(metaModel);
		SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly(accessId, metaModel);
		if (!ignorePartitions) {
			access.getDatabase().setDefaultPartition(accessId);
			access.getDatabase().setIgnorePartitions(false);
		}
		if (addEntities)
			addEntities(access, ignorePartitions);

		return access;
	}

	private static void addTypeSpecificationOnStandardIdentifiable(GmMetaModel metaModel) {
		GmMetaModel rootModel = metaModel.getDependencies().get(0);
		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(rootModel);
		TypeSpecification specification = TypeSpecification.T.create();
		specification.setType(getType(rootModel, SimpleTypes.TYPE_LONG.getTypeSignature()));
		editor.onEntityType(StandardIdentifiable.T.getTypeName()).addPropertyMetaData("id", specification);
	}

	private static void addMandatoryPropertyOnAnotherComplexEntity(GmMetaModel metaModel) {
		BasicModelMetaDataEditor editor = new BasicModelMetaDataEditor(metaModel);
		editor.onEntityType(AnotherComplexEntity.T).addPropertyMetaData("anotherComplexEntityProperty", Mandatory.T.create());
	}

	private static GmType getType(GmMetaModel metaModel, String typeSignature) {
		return metaModel.getTypes().stream().filter(type -> typeSignature.equals(type.getTypeSignature())).findFirst().get();
	}

	private static void addEntities(IncrementalAccess access, boolean ignoreParitions) {
		PersistenceGmSession session = GmTestTools.newSession(access);
		if (ignoreParitions) {
			addSimpleEntity(session, "se0", 0, null);
			addSimpleEntity(session, "se1", 1, null);
			addSimpleEntity(session, "se2", 2, null);
			addSimpleEntity(session, "se3", 3, null);
			addSimpleEntity(session, "se4", 4, null);
		} else {
			addSimpleEntity(session, "se", -1, "p0");
			addSimpleEntity(session, "se", -1, "p1");
			addSimpleEntity(session, "se", -1, "p2");
			addSimpleEntity(session, "se0", 0, "p0");
			addSimpleEntity(session, "se1", 1, "p0");
			addSimpleEntity(session, "se2", 2, "p0");
			addSimpleEntity(session, "se3", 3, "p0");
			addSimpleEntity(session, "se4", 4, "p0");
		}

		AnotherComplexEntity another = addAnotherComplexEntity(session, 0, 3);
		addComplexEntity(session, 0, 0, "0", another, null);
		addComplexEntity(session, 0, 1, "1", another, null);
		addComplexEntity(session, 0, 2, "2", another, null);
		addComplexEntity(session, 1, 3, "0", another, null);
		addComplexEntity(session, 1, 4, "1", another, null);
		addComplexEntity(session, 1, 5, "2", another, null);
		addComplexEntity(session, 2, 6, "0", another, null);
		addComplexEntity(session, 2, 7, "1", another, null);
		addComplexEntity(session, 2, 8, "2", another, "cp0");

		addCollectionEntity(session, 0, ignoreParitions);

		addPrimitiveTypesEntity(session, 0);

		session.commit();
	}

	private static void addSimpleEntity(PersistenceGmSession session, String name, long id, String partition) {
		SimpleEntity e = session.create(SimpleEntity.T);
		e.setBooleanProperty(true);
		e.setId(id);
		e.setStringProperty(name);
		if (partition != null) {
			e.setPartition(partition);
		}
	}

	private static void addComplexEntity(PersistenceGmSession session, //
			int intValue, long id, String stringValue, AnotherComplexEntity another, String partition) {

		ComplexEntity e = session.create(ComplexEntity.T);
		e.setIntegerProperty(intValue);
		e.setStringProperty(stringValue);
		e.setId(id);
		e.setAnotherComplexEntityProperty(another);
		if (partition != null)
			e.setPartition(partition);
	}

	private static AnotherComplexEntity addAnotherComplexEntity(PersistenceGmSession session, int depth, int maxDepth) {
		AnotherComplexEntity e = session.create(AnotherComplexEntity.T);
		e.setId((long) depth);
		e.setIntegerProperty(depth);
		if (depth < maxDepth)
			e.setAnotherComplexEntityProperty(addAnotherComplexEntity(session, depth + 1, maxDepth));

		return e;
	}

	private static void addCollectionEntity(PersistenceGmSession session, long id, boolean ignorePartition) {
		CollectionEntity e = session.create(CollectionEntity.T);
		e.setId(id);
		if(!ignorePartition)
			e.setPartition("cep0");
	}

	private static void addPrimitiveTypesEntity(PersistenceGmSession session, long id) {
		PrimitiveTypesEntity e = session.create(PrimitiveTypesEntity.T);
		e.setId(id);
	}
}
