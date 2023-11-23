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
package com.braintribe.model.openapi.v3_0.export.legacytests.ioc;

import com.braintribe.common.lcd.NotImplementedException;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.DdraEndpoint;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.bvd.time.Now;
import com.braintribe.model.ddra.endpoints.api.v1.ApiV1DdraEndpoint;
import com.braintribe.model.deployment.DeploymentStatus;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.StandardIdentifiable;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.SimpleTypes;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.prompt.Description;
import com.braintribe.model.meta.data.prompt.Embedded;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.NeutralRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.NullRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.TestAccessRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.TestDomainRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.TestServiceRequestWithEntityProperty;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.TestServiceRequestWithResources;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.ZipRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.ZipRequestSimple;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceMetadataRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceRequestExtended;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceResponse;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceSimplepropsRequest;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
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
	public static final String CUSTOM_SERVICE_MODEL_NAME = "custom service model name";
	public static final String CUSTOM_SERVICE_MODEL_DESCRIPTION = "custom service model description";
	private static final String USECASE_DDRA = "ddra";
	public static GmMetaModel cortexModel;
	public static GmMetaModel testServiceModel;
	public static GmMetaModel testModel;
	public static SmoodAccess cortexAccess;

	public static String mappingSpecificUsecase(String path) {
		return USECASE_DDRA + ":" + path;
	}
	
	static class TestPersistenceInitializationContext implements PersistenceInitializationContext {

		private final ManagedGmSession session;
		
		public TestPersistenceInitializationContext(ManagedGmSession session) {
			super();
			this.session = session;
		}

		@Override
		public void setCurrentPersistenceStage(PersistenceStage stage) {
			throw new NotImplementedException();
		}

		@Override
		public ManagedGmSession getSession() {
			return session;
		}

		@Override
		public String getAccessId() {
			return "cortex";
		}

		@Override
		public PersistenceStage getStage(GenericEntity entity) {
			throw new NotImplementedException();
		}
		
	}

	public static IncrementalAccess cortexAccess() {
		if (cortexAccess != null)
			return cortexAccess;
		
		try {
			GmMetaModel webApiEndpointsModelInitial = DdraEndpoint.T.getModel().getMetaModel();
			GmMetaModel iDontKnowWhyButWeNeedToDependOnThisModel = Now.T.getModel().getMetaModel();
			GmMetaModel serviceApiModel = ServiceRequest.T.getModel().getMetaModel();
			GmMetaModel gmMetaModel = GenericEntity.T.getModel().getMetaModel();
			GmMetaModel cortexApiModel = GMF.getTypeReflection().getModel("tribefire.cortex:cortex-api-model").getMetaModel();
			cortexModel = model("tribefire.cortex:tribefire-cortex-model");
			cortexModel.getDependencies().add(cortexApiModel);
			cortexModel.getDependencies().add(webApiEndpointsModelInitial);
			cortexModel.getDependencies().add(iDontKnowWhyButWeNeedToDependOnThisModel);
			cortexAccess = GmTestTools.newSmoodAccessMemoryOnly("cortex", cortexModel);
			cortexAccess.getDatabase().setDefaultPartition("cortex");
			cortexAccess.getDatabase().setIgnorePartitions(false);

			// Note: The cortex model has to depend on all models it needs before creating this session. Still take
			// great care which models you edit in the initializer: the original one (created above this comment) or the
			// one cloned into the cortex session. Because they are of course different. Just keep it in your mind when
			// you can't understand what's going on. I had to learn it the hard way.
			// TODO: create proper initializer & wiring
			PersistenceGmSession session = GmTestTools.newSession(cortexAccess);
			
			cortexModel = cloneToSession(cortexModel, session);
			serviceApiModel = cloneToSession(serviceApiModel, session);
			gmMetaModel = cloneToSession(gmMetaModel, session);
			
			GmMetaModel defaultDomainModel = GMF.getTypeReflection().getModel("com.braintribe.gm:user-model").getMetaModel();
			GmMetaModel sessionDomainModel = cloneToSession(defaultDomainModel, session);
			sessionDomainModel.setName("test:default-domain-model");
			
			ServiceDomain serviceDomain = session.create(ServiceDomain.T);
			serviceDomain.setExternalId("serviceDomain:default");
			serviceDomain.setServiceModel(sessionDomainModel);
			
			GmMetaModel failureModel = cloneToSession(new NewMetaModelGeneration().buildMetaModel("test.model.failure", CollectionTools.getList(Failure.T)), session);
			
			testModel = cloneToSession(model("test.model", TestServiceSimplepropsRequest.T, TestServiceRequestExtended.T, TestServiceRequest.T, TestServiceMetadataRequest.T, TestServiceResponse.T, User.T, Role.T, Group.T),session);
			testServiceModel = cloneToSession(model("test.model2", com.braintribe.model.openapi.v3_0.export.legacytests.model.TestServiceRequest.T, TestServiceRequestWithResources.T, ZipRequest.T, ZipRequestSimple.T, TestServiceRequestWithEntityProperty.T, NullRequest.T, NeutralRequest.T, TestDomainRequest.T, TestAccessRequest.T, Reason.T),session);
			GmMetaModel ddraEndpointsModel = cloneToSession(webApiEndpointsModelInitial,session);
			GmMetaModel emptyModel = cloneToSession(model("empty.model"),session);
			
			testModel.getDependencies().add(failureModel);
			testModel.getDependencies().add(serviceApiModel);
			testServiceModel.getDependencies().add(serviceApiModel);
			
			sessionDomainModel.getDependencies().add(testServiceModel);
			
			cloneToSession(serviceDomain("test.domain1", testModel), session);
			cloneToSession(serviceDomain("test.domain2", testModel), session);
			cloneToSession(serviceDomain("test.empty.domain", emptyModel), session);
			
			Hidden hiddenDddraMapping = session.create(Hidden.T);
			Hidden hiddenDddraMappingPath = session.create(Hidden.T);
			UseCaseSelector mappingSpecificUsecaseAutoPath = session.create(UseCaseSelector.T);
			UseCaseSelector mappingSpecificUsecaseWithPath = session.create(UseCaseSelector.T);
			mappingSpecificUsecaseAutoPath.setUseCase(mappingSpecificUsecase("/test.domain2/" + TestServiceMetadataRequest.T.getTypeSignature()));
			mappingSpecificUsecaseWithPath.setUseCase(mappingSpecificUsecase("/meta/on/endpoint"));
			hiddenDddraMapping.setSelector(mappingSpecificUsecaseAutoPath);
			hiddenDddraMappingPath.setSelector(mappingSpecificUsecaseWithPath);
			
			Priority highPrio = session.create(Priority.T);
			Priority lowPrio = session.create(Priority.T);
			highPrio.setPriority(100);
			lowPrio.setPriority(-1);
			
			UseCaseSelector useCaseSelector = session.create(UseCaseSelector.T);
			useCaseSelector.setUseCase(USECASE_DDRA);
			
			BasicModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(testModel, (e) -> session.create(e));
			mdEditor.onEntityType(TestServiceRequest.T).addPropertyMetaData("mandatoryProperty", session.create(Mandatory.T));
			mdEditor.onEntityType(TestServiceResponse.T).addPropertyMetaData("intProperty", session.create(Mandatory.T));
			mdEditor.onEntityType(TestServiceRequestExtended.T).addPropertyMetaData("invisibleProperty", session.create(Hidden.T));
			mdEditor.onEntityType(TestServiceRequestExtended.T).addPropertyMetaData("veryHighPriorityProperty", highPrio);
			mdEditor.onEntityType(TestServiceRequestExtended.T).addPropertyMetaData("lowPriorityProperty", lowPrio);
			
			mdEditor.onEntityType(TestServiceMetadataRequest.T)
				.addPropertyMetaData("bigDecimalProperty", hiddenDddraMapping)
				.addPropertyMetaData("propertyWithInitializer", hiddenDddraMappingPath);

			Description description = session.create(Description.T);
			description.setDescription(cloneToSession(LocalizedString.create(CUSTOM_SERVICE_MODEL_DESCRIPTION), session));
			description.setSelector(useCaseSelector);
			
			Name name = session.create(Name.T);
			name.setName(cloneToSession(LocalizedString.create(CUSTOM_SERVICE_MODEL_NAME), session));
			name.setSelector(useCaseSelector);
			
			mdEditor.addModelMetaData(description, name);

			mdEditor = new BasicModelMetaDataEditor(testServiceModel, (e) -> session.create(e));
			mdEditor.onEntityType(TestServiceRequestWithEntityProperty.T).addPropertyMetaData("zipRequest", session.create(Embedded.T));
			
			Embedded embedId = session.create(Embedded.T);
			embedId.getIncludes().add(GenericEntity.id);
			
			mdEditor.onEntityType(TestAccessRequest.T).addPropertyMetaData("thing", embedId);
			
			mdEditor = new BasicModelMetaDataEditor(ddraEndpointsModel, (e) -> session.create(e));
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
			cortex.setServiceModel(ddraEndpointsModel);
			
			session.commit();
			
			TestMetaDataInitializer initializer = new TestMetaDataInitializer();
			TestPersistenceInitializationContext initContext = new TestPersistenceInitializationContext(session);
			initializer.initialize(initContext);
			
			session.commit();
			
			return cortexAccess;
		} catch (Exception e) {
			throw new RuntimeException("Error setting up cortex access.",e);
		}
	}

	private static <T extends GenericEntity> T cloneToSession(T toBeCloned, PersistenceGmSession session) {
		T clone = toBeCloned.clone(new StandardCloningContext() {
			
			
			@Override
			public <T> T getAssociated(GenericEntity entity) {
				String globalId = entity.getGlobalId();
				if (globalId != null) {
					T existing = session.findEntityByGlobalId(globalId);
					if (existing != null) {
						return existing;
					}
				}
				return super.getAssociated(entity);
			}
			
			
			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType,
					GenericEntity instanceToBeCloned) {
				
				return session.create(entityType);
			}
			
			
		});
		return clone;
	}
	
	private static GmMetaModel model(String name, EntityType<?>... entities) {
		return new NewMetaModelGeneration().buildMetaModel(name, CollectionTools.getList(entities));
	}
	
	private static ServiceDomain serviceDomain(String externalId, GmMetaModel model) {
		ServiceDomain domain = ServiceDomain.T.create();
		domain.setExternalId(externalId);
		domain.setServiceModel(model);
		domain.setName(externalId);
		return domain;
	}
	
	public static IncrementalAccess testAccess(boolean ignorePartitions) {
		return testAccess("test.access", ignorePartitions);
	}

	public static IncrementalAccess testAccess(String accessId, boolean ignorePartitions) {
		GmMetaModel metaModel = TestModelTestTools.createTestModelMetaModel();

		addMandatoryPropertyOnAnotherComplexEntity(metaModel);
		addTypeSpecificationOnStandardIdentifiable(metaModel);
		SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly(accessId, metaModel);
		if (!ignorePartitions) {
			access.getDatabase().setDefaultPartition(accessId);
			access.getDatabase().setIgnorePartitions(false);
		}
		addEntities(GmTestTools.newSession(access), ignorePartitions);

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

	private static void addEntities(PersistenceGmSession session, boolean ignoreParitions) {
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

		AnotherComplexEntity delegate = addAnotherComplexEntity(session, 0, 3);
		addComplexEntity(session, 0, 0, "0", delegate, null);
		addComplexEntity(session, 0, 1, "1", delegate, null);
		addComplexEntity(session, 0, 2, "2", delegate, null);
		addComplexEntity(session, 1, 3, "0", delegate, null);
		addComplexEntity(session, 1, 4, "1", delegate, null);
		addComplexEntity(session, 1, 5, "2", delegate, null);
		addComplexEntity(session, 2, 6, "0", delegate, null);
		addComplexEntity(session, 2, 7, "1", delegate, null);
		addComplexEntity(session, 2, 8, "2", delegate, "cp0");

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

	private static void addComplexEntity(PersistenceGmSession session, int intValue, long id, String stringValue, AnotherComplexEntity delegate, String partition) {
		ComplexEntity e = session.create(ComplexEntity.T);
		e.setIntegerProperty(intValue);
		e.setStringProperty(stringValue);
		e.setId(id);
		e.setAnotherComplexEntityProperty(delegate);
		if (partition != null) e.setPartition(partition);
	}

	private static AnotherComplexEntity addAnotherComplexEntity(PersistenceGmSession session, int depth, int maxDepth) {
		AnotherComplexEntity e = session.create(AnotherComplexEntity.T);
		e.setId((long) depth);
		e.setIntegerProperty(depth);
		if (depth < maxDepth) {
			e.setAnotherComplexEntityProperty(addAnotherComplexEntity(session, depth + 1, maxDepth));
		}
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
