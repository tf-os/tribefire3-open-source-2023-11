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
package tribefire.platform.wire.space.cortex.accesses;

import static com.braintribe.model.generic.reflection.Model.modelGlobalId;
import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static com.braintribe.wire.api.util.Sets.set;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletResponse;

import com.braintribe.gm._AccessApiModel_;
import com.braintribe.gm._BasicMetaModel_;
import com.braintribe.gm._BasicResourceModel_;
import com.braintribe.gm._DescriptiveModel_;
import com.braintribe.gm._FolderModel_;
import com.braintribe.gm._I18nModel_;
import com.braintribe.gm._MetaDataModel_;
import com.braintribe.gm._MetaModel_;
import com.braintribe.gm._ResourceApiModel_;
import com.braintribe.gm._ResourceModel_;
import com.braintribe.gm._ServiceApiModel_;
import com.braintribe.gm._TransientResourceModel_;
import com.braintribe.gm._UserModel_;
import com.braintribe.gm.model.reason.meta.HttpStatusCode;
import com.braintribe.gm.model.security.reason.AuthenticationFailure;
import com.braintribe.gm.model.security.reason.Forbidden;
import com.braintribe.gm.model.security.reason.SecurityReason;
import com.braintribe.model.access.collaboration.persistence.VirtualModelsPersistenceInitializer;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.accessapi.PersistenceRequest;
import com.braintribe.model.accessapi.QueryRequest;
import com.braintribe.model.accessdeployment.CollaborativeAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.aopaccessapi.AccessAspectAroundProceedRequest;
import com.braintribe.model.aopaccessapi.AccessAspectInterceptorRequest;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.cortexapi.access.ExplorerStyle;
import com.braintribe.model.crypto.configuration.hashing.HashingConfiguration;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.deployment.database.pool.ConfiguredDatabaseConnectionPool;
import com.braintribe.model.descriptive.HasDescription;
import com.braintribe.model.descriptive.HasExternalId;
import com.braintribe.model.descriptive.HasLocalizedDescription;
import com.braintribe.model.descriptive.HasLocalizedName;
import com.braintribe.model.descriptive.HasName;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.BinaryPersistence;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.extensiondeployment.HardwiredBinaryPersistence;
import com.braintribe.model.extensiondeployment.HardwiredBinaryProcessor;
import com.braintribe.model.extensiondeployment.HardwiredBinaryRetrieval;
import com.braintribe.model.extensiondeployment.HardwiredServicePostProcessor;
import com.braintribe.model.extensiondeployment.HardwiredServicePreProcessor;
import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.extensiondeployment.StateChangeProcessor;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.extensiondeployment.meta.PreEnrichResourceWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWithComponent;
import com.braintribe.model.extensiondeployment.meta.StreamWith;
import com.braintribe.model.extensiondeployment.meta.UploadWith;
import com.braintribe.model.folder.Folder;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmStringType;
import com.braintribe.model.meta.GmType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.components.AccessModelExtension;
import com.braintribe.model.meta.data.components.ServiceModelExtension;
import com.braintribe.model.meta.data.constraint.NonDeletable;
import com.braintribe.model.meta.data.constraint.NonInstantiable;
import com.braintribe.model.meta.data.constraint.Optional;
import com.braintribe.model.meta.data.constraint.Pattern;
import com.braintribe.model.meta.data.constraint.TypeSpecification;
import com.braintribe.model.meta.data.constraint.Unmodifiable;
import com.braintribe.model.meta.data.crypto.PropertyCrypting;
import com.braintribe.model.meta.data.display.Group;
import com.braintribe.model.meta.data.display.GroupAssignment;
import com.braintribe.model.meta.data.display.GroupPriority;
import com.braintribe.model.meta.data.prompt.CondensationMode;
import com.braintribe.model.meta.data.prompt.Condensed;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Inline;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.Outline;
import com.braintribe.model.meta.data.prompt.Priority;
import com.braintribe.model.meta.data.ui.ShowAdvancedCommit;
import com.braintribe.model.meta.selector.DisjunctionSelector;
import com.braintribe.model.meta.selector.EntitySignatureRegexSelector;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.meta.selector.NegationSelector;
import com.braintribe.model.meta.selector.PropertyNameSelector;
import com.braintribe.model.meta.selector.PropertyOfSelector;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.platformsetup.api.request.CloseTrunkAsset;
import com.braintribe.model.platformsetup.api.request.RenameAsset;
import com.braintribe.model.platformsetup.api.request.TransferAsset;
import com.braintribe.model.platformsetup.api.request.TrunkAssetRequest;
import com.braintribe.model.processing.cortex.CortexModelNames;
import com.braintribe.model.processing.cortex.priming.CortexModelsPersistenceInitializer;
import com.braintribe.model.processing.itw.analysis.JavaTypeAnalysis;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializer;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.source.StaticSource;
import com.braintribe.model.resource.source.TemplateSource;
import com.braintribe.model.resourceapi.enrichment.ResourceEnrichmentRequest;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.UploadResource;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.security.deployment.meta.AuthenticateWith;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.PlatformRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.domain.ServiceDomain;
import com.braintribe.model.stateprocessing.api.AbstractStateChangeProcessingRequest;
import com.braintribe.model.user.User;
import com.braintribe.utils.i18n.I18nTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.cortex._AopAccessApiModel_;
import tribefire.cortex._CheckApiModel_;
import tribefire.cortex._CortexApiModel_;
import tribefire.cortex._DatabaseDeploymentModel_;
import tribefire.cortex._DeploymentModel_;
import tribefire.cortex._PlatformAssetModel_;
import tribefire.cortex._PlatformSetupWorkbenchModel_;
import tribefire.cortex._StateProcessingApiModel_;
import tribefire.cortex.module.loading.ModuleLoader;
import tribefire.module.model.resource.ModuleSource;
import tribefire.platform.impl.initializer.BasePlatformCheckBundleInitializer;
import tribefire.platform.impl.initializer.CoreModelSecurityInitializer;
import tribefire.platform.impl.initializer.CortexConfigurationInitializer;
import tribefire.platform.impl.initializer.CortexDataInitializer;
import tribefire.platform.impl.initializer.DefaultDeployableInitializer;
import tribefire.platform.impl.initializer.DefaultResourceEnrichersConfigModelInitializer;
import tribefire.platform.impl.initializer.HardwiredDeployableInitializer;
import tribefire.platform.impl.initializer.KnownUseCasesInitializer;
import tribefire.platform.impl.initializer.MetaDataInitializer;
import tribefire.platform.impl.initializer.RequestValidatorInitializer;
import tribefire.platform.impl.initializer.ServiceDomainInitializer;
import tribefire.platform.wire.space.bindings.BindingsSpace;
import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.messaging.accesses.TransientMessagingDataAccessSpace;
import tribefire.platform.wire.space.module.ModuleInitializationSpace;
import tribefire.platform.wire.space.security.AuthenticatorsSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public class CortexAccessInitializersSpace implements WireSpace {

	public static final String packagedPlatformServiceModelName = "tribefire.cortex:tribefire-platform-service-model";
	public static final String platformServiceModelName = "tribefire.cortex:configured-tribefire-platform-service-model";
	public static final String platformServiceDomainName = "Platform Domain";

	public static final String defaultServiceModelName = "tribefire.cortex.services:tribefire-default-service-model";
	public static final String defaultServiceDomainName = "Default Domain";

	@Import
	private DeploymentSpace deployment;

	@Import
	private DefaultDeployablesSpace defaultDeployables;

	@Import
	private EnvironmentSpace environment;

	@Import
	private ResourceAccessSpace resourceAccess;

	@Import
	private TransientMessagingDataAccessSpace transientMessagingDataAccess;

	@Import
	private CortexAccessSpace cortex;

	@Import
	private SystemAccessCommonsSpace systemAccessCommons;

	@Import
	private PlatformSetupAccessSpace platformSetup;

	@Import
	private ModuleInitializationSpace moduleInitialization;

	@Import
	private BindingsSpace bindings;

	@Import
	private AuthenticatorsSpace authenticators;

	@Managed
	public List<PersistenceInitializer> initializers() {
		// @formatter:off
		return 
			list(
				cortexModelsPersistenceInitializer(),
				virtualModelsPersistenceInitializer(),
				hardwiredDeployableInitializer(),
				environmentDenotationRegistryInitializer(),
				knownUseCasesInitializer(),
				cortexModelMetaDataInitializer(),
				coreModelSecurityInitializer(),
				defaultDeployablesInitializer(),
				cortexConfigurationInitializer(),
				rootModelMetaDataInitializer(),
				userModelMetaDataInitializer(),
				descriptiveModelMetaDataInitializer(),
				metaModelMetaDataInitializer(),
				metaDataModelMetaDataInitializer(),
				basicMetaModelMetaDataInitializer(),
				folderModelMetaDataInitializer(),
				deploymentModelMetaDataInitializer(),
				serviceApiModelMetaDataInitializer(),
				accessApiModelMetaDataInitializer(),
				resourceApiModelMetaDataInitializer(),
				defaultResourceEnrichersConfigModelInitializer(),
				aopAcccessApiModelMetaDataInitializer(),
				stateProcessingApiModelMetaDataInitializer(),
				resourceModelMetaDataInitializer(),
				transientResourceModelMetaDataInitializer(),
				basicResourceModelMetaDataInitializer(),
				i18nModelMetaDataInitializer(),
				serviceDomainInitializer(),
				platformDomainModelInitializer(),
				defaultDomainModelInitializer(),
				checkApiModelMetaDataInitializer(),
				advancedPropertyGroupInitializer(),
				platformSetupWorkbenchModelInitializer(),
				cortexApiModelMetaDataInitializer(),
				platformSetupModelInitializer(),
				basePlatformCheckBundleInitializer(),
				requestValidatorInitializer(),
				allModulesInitializer(),
				uxModulesInitializer()
			);
		// @formatter:on
	}

	private CoreModelSecurityInitializer coreModelSecurityInitializer() {
		CoreModelSecurityInitializer bean = new CoreModelSecurityInitializer();
		return bean;
	}

	@Managed
	private CortexModelsPersistenceInitializer cortexModelsPersistenceInitializer() {
		CortexModelsPersistenceInitializer bean = new CortexModelsPersistenceInitializer();
		bean.setStorageBase(cortex.storageBase());

		return bean;
	}

	@Managed
	private VirtualModelsPersistenceInitializer virtualModelsPersistenceInitializer() {
		VirtualModelsPersistenceInitializer bean = new VirtualModelsPersistenceInitializer();
		bean.setVirtualModels(getVirtualModelsDefinitions());
		bean.setModelMetaDataGidsSupplier(() -> Collections.singleton("md:coreModel"));

		return bean;
	}

	private Map<String, Set<String>> getVirtualModelsDefinitions() {
		
		// @formatter:off
		Map<String, Set<String>> virtualModels = map(
			entry(
				defaultServiceModelName, 
				set(
					_AccessApiModel_.reflection.name(),
					_ResourceApiModel_.reflection.name()
				)
			),
			entry(
				platformServiceModelName, 
				set(packagedPlatformServiceModelName)
			)
		);
		// @formatter:on

		TribefireProductModels.productModels.stream() //
				.filter(m -> !m.dependencies.isEmpty()) //
				.forEach(m -> virtualModels.put(m.modelName, m.dependencies));

		return virtualModels;
	}

	@Managed
	private HardwiredDeployableInitializer hardwiredDeployableInitializer() {
		HardwiredDeployableInitializer bean = new HardwiredDeployableInitializer();
		bean.setHardwiredDenotationBinding(deployment.hardwiredBindings());
		return bean;
	}

	@Managed
	private CortexDataInitializer environmentDenotationRegistryInitializer() {
		CortexDataInitializer bean = new CortexDataInitializer();
		bean.setDataSupplier(() -> environmentDenotationRegistryDenotation());
		bean.setDataOrigin("environment-denonation-registry");
		return bean;
	}

	private com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry environmentDenotationRegistryDenotation() {
		com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry result = com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry.T
				.create();
		result.setGlobalId(com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry.ENVIRONMENT_DENOTATION_REGISTRY__GLOBAL_ID);
		result.setEntries(environment.environmentDenotations().entries());

		return result;
	}

	@Managed
	private DefaultDeployableInitializer defaultDeployablesInitializer() {
		DefaultDeployableInitializer bean = new DefaultDeployableInitializer();
		bean.setDeployables(defaultDeployables.defaultDeployables());
		return bean;
	}

	@Managed
	private KnownUseCasesInitializer knownUseCasesInitializer() {
		KnownUseCasesInitializer knownUseCasesInitializer = new KnownUseCasesInitializer();
		return knownUseCasesInitializer;
	}

	@Managed
	private CortexConfigurationInitializer cortexConfigurationInitializer() {
		CortexConfigurationInitializer bean = new CortexConfigurationInitializer();
		return bean;
	}

	private MetaDataInitializer cortexModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelName(CortexModelNames.TF_CORTEX_MODEL_NAME);
		bean.setMetaDataConfigurer((mdEditor, session) -> {
			mdEditor.addModelMetaData(showAdvancedCommitEditor(session));
			mdEditor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.id, typeSpecification(session));

			mdEditor.onEntityType(IncrementalAccess.T).addMetaData( //
					extendServiceModelWith_AccessApiModel_And_ResourceApiModel(session), //
					extendAccessModel_Set_Default_ResourceEnrichers(session) //
			);

			mdEditor.onEntityType(ServiceDomain.T).addMetaData(extendServiceModelWith_SecurityReasonModel(session));

			mdEditor.onEntityType(CollaborativeAccess.T).addMetaData( //
					extendDataModelWith_EveryIdIsString(session), //
					extendCsaModel_Set_StreamWith_ModuleSourceBinRetrieval(session) //
			);

			if (systemAccessCommons.isDistributedSetup())
				mdEditor.onEntityType(CollaborativeAccess.T).addMetaData(extendCsaModel_Set_StreamWith_CsaBinRetrieval(session));
		});

		return bean;
	}

	/** Add <tt>access-api-model</tt> and <tt>resource-api-model</tt> to every {@code IncrementalAccess} */
	private ServiceModelExtension extendServiceModelWith_AccessApiModel_And_ResourceApiModel(ManagedGmSession session) {
		GmMetaModel accessApiModel = session.getEntityByGlobalId(QueryRequest.T.getModel().globalId());
		GmMetaModel resourceApiModel = session.getEntityByGlobalId(UploadResource.T.getModel().globalId());

		ServiceModelExtension result = session.create(ServiceModelExtension.T, "md:service-model-extension:" + IncrementalAccess.T.getShortName());
		result.getModels().add(accessApiModel);
		result.getModels().add(resourceApiModel);

		return result;
	}

	/** Add <tt>access-api-model</tt> and <tt>resource-api-model</tt> to every {@code IncrementalAccess} */
	private ServiceModelExtension extendServiceModelWith_SecurityReasonModel(ManagedGmSession session) {
		GmMetaModel securityReasonModel = session.getEntityByGlobalId(SecurityReason.T.getModel().globalId());

		String securityReasonConfigurationModel = "synthetic:security-reason-configuration-model";
		GmMetaModel mdModel = session.create(GmMetaModel.T, modelGlobalId(securityReasonConfigurationModel));
		mdModel.setName(securityReasonConfigurationModel);
		mdModel.getDependencies().add(securityReasonModel);

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(mdModel).withSession(session).done();

		HttpStatusCode authFailureCode = httpStatusCode(session, "auth-failure", HttpServletResponse.SC_UNAUTHORIZED);
		HttpStatusCode forbiddenCode = httpStatusCode(session, "forbidden", HttpServletResponse.SC_FORBIDDEN);

		mdEditor.onEntityType(AuthenticationFailure.T).addMetaData(authFailureCode);
		mdEditor.onEntityType(Forbidden.T).addMetaData(forbiddenCode);

		ServiceModelExtension result = session.create(ServiceModelExtension.T, "md:service-model-extension:" + ServiceDomain.T.getShortName());
		result.getModels().add(mdModel);

		return result;
	}

	private HttpStatusCode httpStatusCode(ManagedGmSession session, String gidSuffix, int code) {
		HttpStatusCode unauthenticated = session.create(HttpStatusCode.T, "md:http-status-code/" + gidSuffix);
		unauthenticated.setCode(code);
		return unauthenticated;
	}

	/**
	 * In a distributed setup, configures {@code systemAccessCommons.csaBinaryRetrievalDeployable()} as {@link BinaryRetrieval} fore every
	 * {@code CollaborativeAccess}
	 */
	private AccessModelExtension extendAccessModel_Set_Default_ResourceEnrichers(ManagedGmSession session) {
		ResourceEnricher standardEnricher = session.getEntityByGlobalId(ResourceAccessSpace.standardPrePersistenceEnricherGlobalId);

		PreEnrichResourceWith enrichWith = session.create(PreEnrichResourceWith.T, "pre-enrich-resource-with:default");
		enrichWith.getPrePersistenceEnrichers().add(standardEnricher);

		GmMetaModel resourceModel = session.getEntityByGlobalId(ResourceSource.T.getModel().globalId());

		String standardEnrichWithConfigurationModel = "synthetic:standard-enrich-with-configuring-model";
		GmMetaModel mdModel = session.create(GmMetaModel.T, modelGlobalId(standardEnrichWithConfigurationModel));
		mdModel.setName(standardEnrichWithConfigurationModel);
		mdModel.getDependencies().add(resourceModel);

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(mdModel).withSession(session).done();
		mdEditor.onEntityType(ResourceSource.T).addMetaData(enrichWith);

		AccessModelExtension result = session.create(AccessModelExtension.T, "md:access-model-extension:enrich-with-standard-enricher");
		result.getModels().add(mdModel);

		return result;
	}

	/**
	 * In a distributed setup, configures {@code systemAccessCommons.csaBinaryRetrievalDeployable()} as {@link BinaryRetrieval} fore every
	 * {@code CollaborativeAccess}
	 */
	private AccessModelExtension extendCsaModel_Set_StreamWith_ModuleSourceBinRetrieval(ManagedGmSession session) {
		BinaryRetrieval moduleSourceBinRetrieval = session
				.getEntityByGlobalId(systemAccessCommons.moduleSourceBinaryRetrievalDeployable().getGlobalId());

		StreamWith streamWithMd = session.create(StreamWith.T, "synthetic:stream-with:module-source-bin-retrieval");
		streamWithMd.setRetrieval(moduleSourceBinRetrieval);

		GmMetaModel moduleResourceModel = session.getEntityByGlobalId(ModuleSource.T.getModel().globalId());

		String moduleSourceBinRetrievalConfigurationModel = "synthetic:module-source-bin-retrieval-configuring-model";
		GmMetaModel mdModel = session.create(GmMetaModel.T, modelGlobalId(moduleSourceBinRetrievalConfigurationModel));
		mdModel.setName(moduleSourceBinRetrievalConfigurationModel);
		mdModel.getDependencies().add(moduleResourceModel);

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(mdModel).withSession(session).done();
		mdEditor.onEntityType(ModuleSource.T).addMetaData(streamWithMd);

		AccessModelExtension result = session.create(AccessModelExtension.T, "md:access-model-extension/csa/module-source-retrieval-config");
		result.getModels().add(mdModel);

		return result;
	}

	/**
	 * In a distributed setup, configures {@code systemAccessCommons.csaBinaryRetrievalDeployable()} as {@link BinaryRetrieval} fore every
	 * {@code CollaborativeAccess}
	 */
	private AccessModelExtension extendCsaModel_Set_StreamWith_CsaBinRetrieval(ManagedGmSession session) {
		BinaryRetrieval csaBinRetrieval = session.getEntityByGlobalId(systemAccessCommons.csaBinaryRetrievalDeployable().getGlobalId());

		StreamWith streamWithMd = session.create(StreamWith.T, "synthetic:stream-with:csa-bin-retrieval");
		streamWithMd.setRetrieval(csaBinRetrieval);

		GmMetaModel resourceModel = session.getEntityByGlobalId(FileSystemSource.T.getModel().globalId());

		String csaBinRetrievalConfigurationModel = "synthetic:csa-bin-retrieval-configuring-model";
		GmMetaModel mdModel = session.create(GmMetaModel.T, modelGlobalId(csaBinRetrievalConfigurationModel));
		mdModel.setName(csaBinRetrievalConfigurationModel);
		mdModel.getDependencies().add(resourceModel);

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(mdModel).withSession(session).done();
		mdEditor.onEntityType(FileSystemSource.T).addMetaData(streamWithMd);

		AccessModelExtension result = session.create(AccessModelExtension.T, "md:access-model-extension/csa/fs-source-retrieval-config");
		result.getModels().add(mdModel);

		return result;
	}

	private AccessModelExtension extendDataModelWith_EveryIdIsString(ManagedGmSession session) {
		GmMetaModel rootModel = session.getEntityByGlobalId(GenericEntity.T.getModel().globalId());

		TypeSpecification stringTypeSpecification = session.create(TypeSpecification.T, "synthetic:id-string-type-specification");
		stringTypeSpecification.setType(session.getEntityByGlobalId("type:string"));

		String csaStringIdConfigurationModel = "synthetic:csa-string-id-configuring-model";
		GmMetaModel mdModel = session.create(GmMetaModel.T, modelGlobalId(csaStringIdConfigurationModel));
		mdModel.setName(csaStringIdConfigurationModel);
		mdModel.getDependencies().add(rootModel);

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(mdModel).withSession(session).done();
		mdEditor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.id, stringTypeSpecification);

		AccessModelExtension result = session.create(AccessModelExtension.T, "md:access-model-extension:every-id-is-string");
		result.getModels().add(mdModel);

		return result;
	}

	private MetaDataInitializer cortexApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_CortexApiModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEnumType(ExplorerStyle.class).addConstantMetaData("tribefireOrange", nameMdTribefireOrange(session));
			modelEditor.onEnumType(ExplorerStyle.class).addConstantMetaData("grayishBlue", nameMdGrayBlue(session));
			// by default we hide generic exposure of requests for swagger and GME.
			modelEditor.onEntityType(ServiceRequest.T).addMetaData(hiddenServiceRequestUiOrSwagger(session));

			// map all hardwired authenticators for their respective credential types
			authenticators.hardwiredComponents().forEach((type, component) -> {
				AuthenticateWith authenticateWith = session.create(AuthenticateWith.T);
				authenticateWith.setGlobalId("md:authenticate-with/" + component.getIdSuffix());
				authenticateWith.setProcessor(component.lookupDeployable(session));
				modelEditor.onEntityType(type).addMetaData(authenticateWith);
			});

		});

		return bean;
	}

	@Managed
	private MetaData nameMdTribefireOrange(ManagedGmSession session) {
		Name md = session.create(Name.T, "39d11ca1-0afc-4cba-b94a-c919685f1285");
		LocalizedString ls = session.create(LocalizedString.T, "9349fe76-4f4e-429e-b50c-8d86f924b316");
		ls.getLocalizedValues().put("default", "tribefire-orange (default)");
		md.setName(ls);
		return md;
	}

	@Managed
	private MetaData nameMdGrayBlue(ManagedGmSession session) {
		Name md = session.create(Name.T, "535cac14-d507-4bfc-a4a6-2035e9b9b808");
		LocalizedString ls = session.create(LocalizedString.T, "26341367-14ae-44c7-a270-1875d83ce2c4");
		ls.getLocalizedValues().put("default", "gray-blue");
		md.setName(ls);
		return md;
	}

	@Managed
	private MetaDataInitializer platformSetupWorkbenchModelInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_PlatformSetupWorkbenchModel_.reflection);

		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(TrunkAssetRequest.T).addPropertyMetaData(TrunkAssetRequest.asset, optional(session));

			modelEditor.onEntityType(TransferAsset.T).addPropertyMetaData("asset", optional(session));

			modelEditor.onEntityType(CloseTrunkAsset.T).addPropertyMetaData("name", optional(session));

			modelEditor.onEntityType(CloseTrunkAsset.T).addPropertyMetaData("groupId", optional(session));

			modelEditor.onEntityType(RenameAsset.T).addPropertyMetaData("asset", optional(session));

			modelEditor.onEntityType(TrunkAssetRequest.T).addPropertyMetaData(TrunkAssetRequest.roles, hiddenGmeGlobal(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer platformSetupModelInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_PlatformAssetModel_.reflection);

		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(PlatformAsset.T).addPropertyMetaData(PlatformAsset.groupId, unmodifiableInPropertyPanel(session));

			modelEditor.onEntityType(PlatformAsset.T).addPropertyMetaData(PlatformAsset.name, unmodifiableInPropertyPanel(session));

			modelEditor.onEntityType(PlatformAsset.T).addPropertyMetaData(PlatformAsset.version, unmodifiableInPropertyPanel(session));
		});

		return bean;
	}

	@Managed
	private MetaData showAdvancedCommitEditor(ManagedGmSession session) {
		ShowAdvancedCommit md = session.create(ShowAdvancedCommit.T, "251cb779-4d19-488b-b30e-92a5241af0d1");
		return md;
	}

	@Managed
	private MetaData typeSpecification(ManagedGmSession session) {
		TypeSpecification md = session.create(TypeSpecification.T, "dbe545b8-63a7-44eb-aa9d-dad0f718f101");
		md.setType(lookupStringType(session));
		md.setConflictPriority(1d);
		md.setImportant(true);

		return md;
	}

	private GmStringType lookupStringType(ManagedGmSession session) {
		return session.query().findEntity("type:string");
	}

	@Managed
	private MetaDataInitializer rootModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelName(GenericModelTypeReflection.rootModelName);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			// @formatter:off
			modelEditor.onEntityType(GenericEntity.T)
					.addMetaData(inlineUi(session))
					.addMetaData(groupPriority(session))
					.addPropertyMetaData(groupAssignmentBase(session))

					.addPropertyMetaData(GenericEntity.globalId,
								groupAssignmentTechnical(session),
								unmodifiableInGme(session),
								hiddenServiceRequestUi(session),
								hiddenAssemblyPanel(session)
							)
					.addPropertyMetaData(GenericEntity.partition, 
								groupAssignmentTechnical(session),
								unmodifiableInGme(session),
								hiddenServiceRequestUi(session),
								hiddenAssemblyPanel(session)
							)
					.addPropertyMetaData(GenericEntity.id,
								groupAssignmentTechnical(session),
								inline(session),
								hiddenServiceRequestUi(session),
								unmodifiableInGme(session),
								hiddenAssemblyPanel(session)
							)
					;
			// @formatter:on
		});

		return bean;
	}

	@Managed
	private MetaData hiddenGmeGlobal(ManagedGmSession session) {
		Hidden md = session.create(Hidden.T, "cc205727-1f0d-4d01-9149-bd2763cd9fe6");
		md.setSelector(gmeGlobalSelector(session));
		return md;
	}

	@Managed
	private MetaData hiddenServiceRequestUiOrSwagger(ManagedGmSession session) {
		Hidden md = session.create(Hidden.T, "md:hiddenServiceRequestUiAndSwagger");
		DisjunctionSelector selector = session.create(DisjunctionSelector.T, "selector:hiddenServiceRequestUiOrSwagger");
		selector.getOperands().add(serviceRequestPanelSelector(session));
		md.setSelector(selector);
		return md;
	}

	@Managed
	private MetaData hiddenServiceRequestUi(ManagedGmSession session) {
		Hidden md = session.create(Hidden.T, "08c97161-a16c-4404-98c8-f40ec289640a");
		md.setSelector(serviceRequestPanelSelector(session));
		return md;
	}

	@Managed
	private MetaData hiddenAssemblyPanel(ManagedGmSession session) {
		Hidden md = session.create(Hidden.T, "f10b18e7-d494-4ff0-917e-a741ee951dfe");
		md.setSelector(assemblyPanelSelector(session));
		return md;
	}

	@Managed
	private MetaData unmodifiableInGme(ManagedGmSession session) {
		Unmodifiable md = session.create(Unmodifiable.T, "md:id:unmodifiable-in-gme");
		md.setSelector(gmeGlobalSelector(session));
		return md;
	}

	@Managed
	private MetaData unmodifiableInPropertyPanel(ManagedGmSession session) {
		Unmodifiable md = session.create(Unmodifiable.T, "md:id:unmodifiable-in-property-panel");
		md.setSelector(propertyPanelSelector(session));
		return md;
	}

	@Managed
	private MetaData groupPriority(ManagedGmSession session) {
		Group baseGroup = acquireGroup(session, "Base", false);
		GroupPriority md = session.create(GroupPriority.T, "5eef56ce-4a82-41b3-9770-42e908d1ae47");
		md.setGroup(baseGroup);
		md.setPriority(1d);
		return md;
	}

	@Managed
	private MetaDataInitializer metaModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_MetaModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(GmType.T).addPropertyMetaData("typeSignature", priority_high(session));
			modelEditor.onEntityType(GmMetaModel.T).addMetaData(nonInstantiableUi(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer metaDataModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_MetaDataModel_.reflection);
		bean.setMetaDataConfigurer(this::assignGroupsToMetaData);

		return bean;
	}

	private void assignGroupsToMetaData(ModelMetaDataEditor modelEditor, ManagedGmSession session) {
		knownMetaDataGroups().entrySet().stream().forEach((e) -> {

			String groupName = e.getKey();
			String technicalGroupName = groupName.replaceAll("\\s", "-").replaceAll("\\W", "").toLowerCase();
			LocalizedString localizedGroupName = I18nTools.createLsWithGlobalId(groupName, "ls:group." + technicalGroupName);

			Group group = acquireGroup(session, groupName, false, "group:" + technicalGroupName);
			group.setLocalizedName(localizedGroupName);

			MetaData groupAssignmentMetaData = groupAssignmentForMetaData(session, group, technicalGroupName);

			List<String> groupPackages = e.getValue();
			switch (groupPackages.size()) {
				case 1:
					String singleGroupPackage = groupPackages.get(0);
					EntitySignatureRegexSelector singleSelector = session.create(EntitySignatureRegexSelector.T,
							"mdselector:groupassignment." + singleGroupPackage);
					singleSelector.setRegex(createPackagePattern(singleGroupPackage));
					groupAssignmentMetaData.setSelector(singleSelector);
					break;
				default:
					DisjunctionSelector disjunction = session.create(DisjunctionSelector.T, "md:disjunction.groupassignment." + technicalGroupName);

					for (String groupPackage : groupPackages) {
						EntitySignatureRegexSelector packageSelector = session.create(EntitySignatureRegexSelector.T,
								"mdselector:groupassignment." + groupPackage);
						packageSelector.setRegex(createPackagePattern(groupPackage));

						disjunction.getOperands().add(packageSelector);
					}

					groupAssignmentMetaData.setSelector(disjunction);
			}

			modelEditor.onEntityType(MetaData.T).addMetaData(groupAssignmentMetaData);
		});
	}

	private String createPackagePattern(String groupPackage) {
		return groupPackage.replaceAll("\\.", "\\\\.") + "\\..*";
	}

	@Managed
	private MetaDataInitializer basicMetaModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_BasicMetaModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(Group.T).addPropertyMetaData("localizedName", priority_mid(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer deploymentModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_DeploymentModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {

			modelEditor.onEntityType(Deployable.T) //
					.addPropertyMetaData("externalId", externalIdPattern(session)) //
					.addPropertyMetaData("metaData", hiddenGmeGlobal(session));

			modelEditor.onEntityType(HardwiredDeployable.T) //
					.addPropertyMetaData(notEditable(session)) //
					.addMetaData(notDeleteable(session)) //
					.addMetaData(notInstantiableGme(session));

			modelEditor.onEntityType(Module.T) //
					.addPropertyMetaData(notEditable(session)) //
					.addMetaData(notDeleteable(session)) //
					.addMetaData(notInstantiable(session));
		});

		return bean;
	}

	@Managed
	private MetaData optional(ManagedGmSession session) {
		Optional md = session.create(Optional.T, "4b864368-fc67-40f8-b61e-145fe2ec36ff");
		md.setConflictPriority(1d);
		return md;
	}

	@Managed
	private MetaData externalIdPattern(ManagedGmSession session) {
		Pattern md = session.create(Pattern.T, "8b2f6bbf-6594-4896-ae3e-444ef8841e57");
		md.setExpression("^[a-zA-Z0-9\\._\\-\\+@\\$]{1,255}$");
		return md;
	}

	@Managed
	private MetaData notDeleteable(ManagedGmSession session) {
		NonDeletable md = session.create(NonDeletable.T, "d762c55d-620b-4f96-a071-ff211906e4ad");
		return md;
	}

	@Managed
	private MetaData notInstantiable(ManagedGmSession session) {
		NonInstantiable md = session.create(NonInstantiable.T, "md:nonInstantiable");
		md.setSelector(gmeGlobalSelector(session));
		return md;
	}

	@Managed
	private MetaData notInstantiableGme(ManagedGmSession session) {
		NonInstantiable md = session.create(NonInstantiable.T, "7e080403-1d0a-4e69-8796-c4ab8f759d21");
		md.setSelector(gmeGlobalSelector(session));
		return md;
	}

	@Managed
	private MetaDataInitializer stateProcessingApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_StateProcessingApiModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(AbstractStateChangeProcessingRequest.T)
					.addMetaData(processWithComponent(session, StateChangeProcessor.T, "e5cdfffd-6ccb-4830-9268-ac3c1e4e958a"));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer serviceApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_ServiceApiModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(ServiceRequest.T).addPropertyMetaData("metaData", hiddenServiceRequestUiOrSwagger(session));
			// modelEditor.onEntityType(DispatchableRequest.T).addPropertyMetaData("serviceId", hiddenServiceRequestUi(session));
			modelEditor.onEntityType(AuthorizedRequest.T).addPropertyMetaData(AuthorizedRequest.sessionId, hiddenServiceRequestUiOrSwagger(session));
			modelEditor.onEntityType(DomainRequest.T).addPropertyMetaData(DomainRequest.domainId, hiddenServiceRequestUiOrSwagger(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer accessApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_AccessApiModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(PersistenceRequest.T).addMetaData(
					processWithComponent(session, com.braintribe.model.accessdeployment.IncrementalAccess.T, "4b6a7d3d-b98b-4bb7-aee2-1fa9a8002ea6"));
			// hide deprecated property of GmqlRequest.
			modelEditor.onEntityType(GmqlRequest.T).addPropertyMetaData("domainId", hidden(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer resourceApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_ResourceApiModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(ResourceEnrichmentRequest.T)
					.addMetaData(processWithComponent(session, ResourceEnricher.T, "11d1a065-d56c-460a-97b7-c8380cce1f5d"));
			modelEditor.onEntityType(BinaryPersistenceRequest.T)
					.addMetaData(processWithComponent(session, BinaryPersistence.T, "06c75e35-1eb1-4e05-a36a-c88b43de6098"));
			modelEditor.onEntityType(BinaryRetrievalRequest.T)
					.addMetaData(processWithComponent(session, BinaryRetrieval.T, "a37d746e-6fa3-4c95-be3d-f3f6d4ff91bd"));

		});
		return bean;
	}

	@Managed
	private SimplePersistenceInitializer defaultResourceEnrichersConfigModelInitializer() {
		return new DefaultResourceEnrichersConfigModelInitializer();
	}

	@Managed
	private MetaDataInitializer checkApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_CheckApiModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(CheckRequest.T)
					.addMetaData(processWithComponent(session, CheckProcessor.T, "0a4c6e16-af43-42a5-b515-ec8d55410045"));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer aopAcccessApiModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_AopAccessApiModel_.reflection);

		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(AccessAspectInterceptorRequest.T)
					.addMetaData(processWithComponent(session, AccessAspect.T, "949c8678-e5fe-483d-8345-123cf308db57"));
			modelEditor.onEntityType(AccessAspectAroundProceedRequest.T)
					.addMetaData(processWithComponent(session, IncrementalAccess.T, "d6da5313-5275-4e7b-aada-cd6aecf3ac33"));

		});

		return bean;
	}

	@Managed
	private MetaDataInitializer resourceModelMetaDataInitializer() {

		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_ResourceModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {

			String defaultDeployableId = resourceAccess.defaultBinaryPersistenceDeployable().getGlobalId();
			HardwiredBinaryPersistence defaultDeployable = acquire(session, HardwiredBinaryPersistence.T, defaultDeployableId);
			UploadWith uploadWith = session.create(UploadWith.T, "203a92c9-8601-44e8-b3b1-fc87aedde074");
			uploadWith.setPersistence(defaultDeployable);

			modelEditor.onEntityType(ResourceSource.T).addMetaData(uploadWith);
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer transientResourceModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_TransientResourceModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(FileResource.T) //
					.addMetaData(hiddenGmeGlobal(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer basicResourceModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_BasicResourceModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {

			String defaultDeployableId = resourceAccess.defaultBinaryPersistenceDeployable().getGlobalId();
			String fileSystemDeployableId = resourceAccess.fileSystemBinaryProcessorDeployable().getGlobalId();
			String templateDeployableId = resourceAccess.templateBinaryRetrievalDeployable().getGlobalId();

			HardwiredBinaryPersistence defaultDeployable = acquire(session, HardwiredBinaryPersistence.T, defaultDeployableId);
			HardwiredBinaryProcessor fileSystemDeployable = acquire(session, HardwiredBinaryProcessor.T, fileSystemDeployableId);
			HardwiredBinaryRetrieval templateDeployable = acquire(session, HardwiredBinaryRetrieval.T, templateDeployableId);

			UploadWith uploadWith = session.create(UploadWith.T, "4ac7df45-8e95-4e2a-9c13-0d133e17d680");
			uploadWith.setPersistence(defaultDeployable);

			modelEditor.onEntityType(ResourceSource.T).addMetaData(uploadWith);

			BinaryProcessWith binaryProcessWith = session.create(BinaryProcessWith.T, "c97db2a7-ad19-4104-bba4-0cb5db7f17a9");
			binaryProcessWith.setRetrieval(fileSystemDeployable);
			binaryProcessWith.setPersistence(fileSystemDeployable);

			StreamWith streamWithTemplate = session.create(StreamWith.T, "c43b1632-b9b7-4c01-af32-b206a5b6b6e6");
			streamWithTemplate.setRetrieval(templateDeployable);

			modelEditor.onEntityType(FileSystemSource.T).addMetaData(binaryProcessWith);
			modelEditor.onEntityType(StaticSource.T).addMetaData(binaryProcessWith);
			modelEditor.onEntityType(TemplateSource.T).addMetaData(streamWithTemplate);

		});

		return bean;
	}

	@Managed
	private MetaDataInitializer userModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_UserModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(User.T) //
					.addPropertyMetaData(User.password, userPassword_PropertyCrypting(session))
					.addPropertyMetaData(User.lastLogin, hiddenSwaggerSessionsResponseProperty(session))
					.addPropertyMetaData(User.groups, hiddenSwaggerSessionsResponseProperty(session))
					.addPropertyMetaData(User.roles, hiddenSwaggerSessionsResponseProperty(session))
					.addPropertyMetaData(User.password, hiddenSwaggerSessionsResponseProperty(session))
					.addPropertyMetaData(User.description, hiddenSwaggerSessionsResponseProperty(session))
					.addPropertyMetaData(User.picture, hiddenSwaggerSessionsResponseProperty(session));
			// TODO CWI
		});

		return bean;
	}

	@Managed
	private MetaData hiddenSwaggerSessionsResponseProperty(ManagedGmSession session) {
		Hidden md = session.create(Hidden.T, "md:hiddenSwaggerSessionsResponseProperty");
		md.setSelector(swaggerSessionsRequestsSelector(session));
		return md;
	}

	@Managed
	private DisjunctionSelector swaggerSessionsRequestsSelector(ManagedGmSession session) {
		DisjunctionSelector md = session.create(DisjunctionSelector.T, "selector:swaggerSessionsRequests");
		md.getOperands().add(swaggerSessionsCurrentUserRequestSelector(session));
		md.getOperands().add(swaggerSessionsValidateRequestSelector(session));
		return md;
	}

	@Managed
	private UseCaseSelector swaggerSessionsCurrentUserRequestSelector(ManagedGmSession session) {
		UseCaseSelector md = session.create(UseCaseSelector.T, "selector:swaggerSessionsCurrentUserRequest");
		md.setUseCase("openapi:/sessions/currentUser");
		return md;
	}

	@Managed
	private UseCaseSelector swaggerSessionsValidateRequestSelector(ManagedGmSession session) {
		UseCaseSelector md = session.create(UseCaseSelector.T, "selector:swaggerSessionsValidateRequest");
		md.setUseCase("openapi:/sessions/validate");
		return md;
	}

	@Managed
	private MetaDataInitializer descriptiveModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_DescriptiveModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(HasExternalId.T).addPropertyMetaData(HasExternalId.externalId, priority_high(session));
			// modelEditor.onEntityType(HasExternalId.T).addPropertyMetaData(HasExternalId.externalId,
			// groupAssignmentTechnical(session));
			modelEditor.onEntityType(HasName.T).addPropertyMetaData(HasName.name, priority_high(session));
			modelEditor.onEntityType(HasLocalizedName.T).addPropertyMetaData(HasLocalizedName.name, priority_high(session));
			modelEditor.onEntityType(HasDescription.T).addMetaData(outline(session));
			modelEditor.onEntityType(HasDescription.T).addPropertyMetaData(HasDescription.description, priority_mid(session));
			modelEditor.onEntityType(HasLocalizedDescription.T).addMetaData(outline(session));
			modelEditor.onEntityType(HasLocalizedDescription.T).addPropertyMetaData(HasLocalizedDescription.description, priority_mid(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer folderModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_FolderModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(Folder.T).addMetaData(condensation_Folder(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer i18nModelMetaDataInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_I18nModel_.reflection);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			modelEditor.onEntityType(LocalizedString.T).addMetaData(inline(session));
		});

		return bean;
	}

	@Managed
	private MetaDataInitializer advancedPropertyGroupInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelAr(_DatabaseDeploymentModel_.reflection);
		bean.setMetaDataConfigurer(this::configureAdvancedGroupPropertyMd);

		return bean;
	}

	private void configureAdvancedGroupPropertyMd(ModelMetaDataEditor mdEditor, ManagedGmSession session) {
		Group group = acquireGroup(session, "Advanced", true);
		group.setCollapsed(true);

		GmEntityType dbPoolEntityType = queryGmEntityType(session, ConfiguredDatabaseConnectionPool.T);

		PropertyOfSelector isDbPoolProperty = session.create(PropertyOfSelector.T, "isPropertyOf:" + dbPoolEntityType.getTypeSignature());
		isDbPoolProperty.setEntityType(dbPoolEntityType);

		NegationSelector notDbPoolProperty = session.create(NegationSelector.T, "not:" + isDbPoolProperty.getGlobalId());
		notDbPoolProperty.setOperand(isDbPoolProperty);

		GroupAssignment assignment = session.create(GroupAssignment.T, "085aba97-0c16-4f7c-92ab-8c32c5213127");
		assignment.setGroup(group);
		assignment.setSelector(notDbPoolProperty);
		assignment.setImportant(true);

		mdEditor.onEntityType(ConfiguredDatabaseConnectionPool.T).addPropertyMetaData(assignment);
	}

	@Managed
	private ServiceDomainInitializer serviceDomainInitializer() {
		// @formatter:off
		return
			new ServiceDomainInitializer()
				.register(
					"bee5aeaa-a87c-4697-9227-4f0120991f5d", 
					PlatformRequest.platformDomainId, 
					platformServiceModelName,
					platformServiceDomainName
				)
				.register(
					"6c1c7bd4-4dd6-4eb5-80e1-8244716b04f0", 
					CortexAccessSpace.defaultServiceDomainId, 
					defaultServiceModelName,
					defaultServiceDomainName
				)
				;
		// @formatter:on
	}

	private MetaDataInitializer platformDomainModelInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelName(platformServiceModelName);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			// by default we hide generic exposure of requests for swagger and GME.
			modelEditor.onEntityType(ServiceRequest.T).addMetaData(hiddenServiceRequestUiOrSwagger(session));
		});

		return bean;
	}

	private MetaDataInitializer defaultDomainModelInitializer() {
		MetaDataInitializer bean = new MetaDataInitializer();
		bean.setModelName(defaultServiceModelName);
		bean.setMetaDataConfigurer((modelEditor, session) -> {
			// by default we hide generic exposure of requests for swagger and GME.
			modelEditor.onEntityType(ServiceRequest.T).addMetaData(hiddenServiceRequestUiOrSwagger(session));
		});

		return bean;
	}

	@Managed
	public HardwiredServicePreProcessor httpStreamingPreProcessorDeployable() {
		HardwiredServicePreProcessor bean = HardwiredServicePreProcessor.T.create();
		bean.setName("HTTP Streaming PreProcessor");
		bean.setExternalId("preprocessor.http.streaming");
		bean.setGlobalId("hardwired:preprocessor/http.streaming");
		return bean;
	}

	@Managed
	public HardwiredServicePostProcessor httpStreamingPostProcessorDeployable() {
		HardwiredServicePostProcessor bean = HardwiredServicePostProcessor.T.create();
		bean.setName("HTTP Streaming PostProcessor");
		bean.setExternalId("postprocessor.http.streaming");
		bean.setGlobalId("hardwired:postprocessor/http.streaming");
		return bean;
	}

	@Managed
	private RequestValidatorInitializer requestValidatorInitializer() {
		RequestValidatorInitializer bean = new RequestValidatorInitializer();
		return bean;
	}

	@Managed
	private BasePlatformCheckBundleInitializer basePlatformCheckBundleInitializer() {
		return new BasePlatformCheckBundleInitializer();
	}

	/** @see ModuleLoader#getImplicitCortexInitializer */
	private PersistenceInitializer allModulesInitializer() {
		return moduleInitialization.implicitCortexInitialier();
	}

	private PersistenceInitializer uxModulesInitializer() {
		return moduleInitialization.uxModulesCortexInitializer();
	}

	@Managed
	private MetaData nonInstantiableUi(ManagedGmSession session) {
		NonInstantiable md = session.create(NonInstantiable.T, "c7333fc3-c778-4afa-9c79-00a7071bb115");
		md.setSelector(quickAccessPanelSelector(session));
		return md;
	}

	@Managed
	private MetaData priority_high(ManagedGmSession session) {
		Priority priority = session.create(Priority.T, "924ba112-5d42-455c-8ddb-bc1053156444");
		priority.setPriority(1d);
		return priority;
	}

	@Managed
	private MetaData priority_mid(ManagedGmSession session) {
		Priority priority = session.create(Priority.T, "191b28ac-98ea-4343-977b-452469a4a6ee");
		priority.setPriority(0.5d);
		return priority;
	}

	@Managed
	private MetaData priority_low(ManagedGmSession session) {
		Priority priority = session.create(Priority.T, "0ac24f58-ece4-4c8e-a283-5e33ec9b5797");
		priority.setPriority(-1d);
		return priority;
	}

	private MetaData processWithComponent(ManagedGmSession session, EntityType<? extends GenericEntity> componentType, String uuid) {
		ProcessWithComponent processWithComponent = session.create(ProcessWithComponent.T, uuid);

		GmEntityType gmComponentType = queryGmEntityType(session, componentType);

		processWithComponent.setComponentType(gmComponentType);
		return processWithComponent;
	}

	private GmEntityType queryGmEntityType(ManagedGmSession session, EntityType<? extends GenericEntity> componentType) {
		GmEntityType result = session.query().findEntity(JavaTypeAnalysis.resolveGlobalId(componentType.getJavaType()));

		return requireNonNull(result, () -> "EntityType not found: " + componentType.getTypeSignature());
	}

	@Managed
	private MetaData inline(ManagedGmSession session) {
		Inline inline = session.create(Inline.T, "7d9fa86e-d2ad-4024-aee8-c8815e7741f8");
		return inline;
	}

	@Managed
	private MetaData inlineUi(ManagedGmSession session) {
		Inline inline = session.create(Inline.T, "a84024f0-88a1-4cd5-9d4f-02f8bcd91fb5");
		inline.setSelector(notAssemblyOrThumbnailPanelEditor(session));
		return inline;
	}

	@Managed
	private MetaData groupAssignmentTechnical(ManagedGmSession session) {
		Group technicalGroup = acquireGroup(session, "Technical", true);
		GroupAssignment groupAssignment = session.create(GroupAssignment.T, "620987a8-4140-498f-b622-635363f5de3b");
		groupAssignment.setGroup(technicalGroup);
		groupAssignment.setImportant(true);
		groupAssignment.setConflictPriority(1d);
		return groupAssignment;
	}

	@Managed
	private MetaData groupAssignmentBase(ManagedGmSession session) {
		Group baseGroup = acquireGroup(session, "General", false);
		GroupAssignment groupAssignment = session.create(GroupAssignment.T, "84b17963-d04c-4ff2-a318-13084d5284de");
		groupAssignment.setGroup(baseGroup);
		groupAssignment.setImportant(true);

		DisjunctionSelector globalIdOrPartition = session.create(DisjunctionSelector.T, "098a3d2f-3e9e-432b-8ee1-8bfe677859c3");
		globalIdOrPartition.getOperands().add(globalIdPropertySelector(session));
		globalIdOrPartition.getOperands().add(partitionPropertySelector(session));

		NegationSelector notGlobalIdOrPartition = session.create(NegationSelector.T, "bd09535a-ea90-414c-819e-d025af8fb5ae");
		notGlobalIdOrPartition.setOperand(globalIdOrPartition);

		groupAssignment.setSelector(notGlobalIdOrPartition);
		return groupAssignment;
	}

	@Managed
	private MetaData groupAssignmentForMetaData(ManagedGmSession session, Group group, String technicalGroupName) {
		GroupAssignment groupAssignment = session.create(GroupAssignment.T, "md:groupAssignment." + technicalGroupName);
		groupAssignment.setGroup(group);
		groupAssignment.setImportant(false);
		groupAssignment.setConflictPriority(1d);
		return groupAssignment;

	}

	@Managed
	private PropertyNameSelector globalIdPropertySelector(ManagedGmSession session) {
		PropertyNameSelector ps = session.create(PropertyNameSelector.T, "f8888422-5bbd-4611-a88a-ce40f26904d4");
		ps.setPropertyName(GenericEntity.globalId);
		return ps;
	}

	@Managed
	private PropertyNameSelector partitionPropertySelector(ManagedGmSession session) {
		PropertyNameSelector ps = session.create(PropertyNameSelector.T, "10348e6d-8f1a-4bcc-9cbb-ebc70c670c9d");
		ps.setPropertyName(GenericEntity.partition);
		return ps;
	}

	@Managed
	private MetaData outline(ManagedGmSession session) {
		Outline outline = session.create(Outline.T, "7c6902e8-6de0-4260-afdf-67f39324038b");
		return outline;
	}

	@Managed
	private MetaData hidden(ManagedGmSession session) {
		Hidden hidden = session.create(Hidden.T, "547f9bdd-99b8-4668-aceb-45e42f141f83");
		return hidden;
	}

	@Managed
	private NegationSelector notAssemblyOrThumbnailPanelEditor(ManagedGmSession session) {
		NegationSelector bean = session.create(NegationSelector.T, "e63152b9-18d5-403d-b917-691b93ffa1c1");
		bean.setOperand(assemblyOrThumbnailPanel(session));
		return bean;
	}

	private DisjunctionSelector assemblyOrThumbnailPanel(ManagedGmSession session) {
		DisjunctionSelector bean = session.create(DisjunctionSelector.T, "323485e4-fc57-43d0-908d-8206eabef583");
		bean.getOperands().add(assemblyPanelSelector(session));
		bean.getOperands().add(thumbnailPanelSelector(session));
		return bean;
	}

	@Managed
	private MetaData notEditable(ManagedGmSession session) {
		Unmodifiable md = session.create(Unmodifiable.T, "2670a419-288c-407b-9ab7-ec01132d0a94");
		return md;
	}

	@Managed
	private UseCaseSelector gmeGlobalSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.gmeGlobalUseCase);
	}

	@Managed
	private UseCaseSelector propertyPanelSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.propertyPanelUseCase);
	}

	@Managed
	private UseCaseSelector gimaPanelSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.gimaUseCase);
	}

	@Managed
	private UseCaseSelector assemblyPanelSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.assemblyPanelUseCase);
	}

	@Managed
	private UseCaseSelector serviceRequestPanelSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.serviceRequestPanelUseCase);
	}

	@Managed
	private UseCaseSelector quickAccessPanelSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.quickAccessPanelUseCase);
	}

	@Managed
	private UseCaseSelector thumbnailPanelSelector(ManagedGmSession session) {
		return acquireGmeUseCaseSelector(session, KnownUseCase.thumbnailPanelUseCase);
	}

	private UseCaseSelector acquireGmeUseCaseSelector(ManagedGmSession session, KnownUseCase useCase) {
		String globalId = "selector:useCase/gme." + useCase.name();
		return acquireEntity(session, globalId, () -> {
			UseCaseSelector result = session.create(UseCaseSelector.T, globalId);
			result.setUseCase(useCase.getDefaultValue());

			return result;
		});
	}

	private <T extends GenericEntity> T acquireEntity(ManagedGmSession session, String globalId, Supplier<T> factory) {
		T bean = session.query().findEntity(globalId);
		if (bean == null) {
			bean = factory.get();
		}
		return bean;
	}

	private Group acquireGroup(ManagedGmSession session, String groupName, boolean collapsed, String globalId) {
		if (globalId == null)
			globalId = "group:" + groupName;

		Group group = session.query().findEntity(globalId);
		if (group == null) {
			group = session.create(Group.T, globalId);
			group.setName(groupName);
			group.setCollapsed(collapsed);
			LocalizedString displayName = session.create(LocalizedString.T, "ls:group-" + groupName).putDefault(groupName);
			group.setLocalizedName(displayName);
		}

		return group;
	}

	private Group acquireGroup(ManagedGmSession session, String groupName, boolean collapsed) {
		return acquireGroup(session, groupName, collapsed, null);
	}

	private <T extends GenericEntity> T acquire(ManagedGmSession session, EntityType<T> expectedType, String globalId) {
		T result = session.query().findEntity(globalId);

		if (result == null)
			throw new IllegalStateException("No entity of type '" + expectedType.getTypeSignature() + "' found with globalId " + globalId);

		if (!expectedType.isInstance(result))
			throw new IllegalStateException(
					"Entity is not an instance of '" + expectedType.getTypeSignature() + "'. GlobalId: '" + globalId + "', instance: " + result);

		return result;
	}

	@Managed
	private MetaData condensation_Folder(ManagedGmSession session) {
		String subFoldersGlobalId = JavaTypeAnalysis.propertyGlobalId(Folder.T.getTypeSignature(), "subFolders");
		GmProperty subFolderProperty = session.query().findEntity(subFoldersGlobalId);

		Condensed condensed = session.create(Condensed.T, "40c93088-d613-456b-964d-48365ebef9e0");
		condensed.setCondensationMode(CondensationMode.auto);
		condensed.setProperty(subFolderProperty);

		return condensed;
	}

	@Managed
	private PropertyCrypting userPassword_PropertyCrypting(ManagedGmSession session) {
		HashingConfiguration hashingConfig = session.create(HashingConfiguration.T, "b6b52677-a117-47e9-9854-54fbc33895c3");
		hashingConfig.setAlgorithm("SHA-256");
		hashingConfig.setEnableRandomSalt(true);
		hashingConfig.setRandomSaltSize(16);

		PropertyCrypting result = session.create(PropertyCrypting.T, "450e4a52-8f18-4427-9d2d-78f0f7ea207c");
		result.setCryptoConfiguration(hashingConfig);

		return result;
	}

	@Managed
	public Map<String, List<String>> knownMetaDataGroups() {
		Map<String, List<String>> bean = map();

		bean.put("Displaying and Prompting", asList("com.braintribe.model.meta.data.display", "com.braintribe.model.meta.data.prompt"));
		bean.put("Constraints", asList("com.braintribe.model.meta.data.constraint"));
		bean.put("Garbage Collection", asList("com.braintribe.model.meta.data.cleanup"));
		bean.put("Crypting", asList("com.braintribe.model.meta.data.crypto"));
		bean.put("Fulltext", asList("com.braintribe.model.meta.data.fulltext"));
		bean.put("Querying", asList("com.braintribe.model.meta.data.query"));
		bean.put("Runtime", asList("com.braintribe.model.meta.data.runtime"));
		bean.put("Processing", asList("com.braintribe.model.extensiondeployment.meta"));
		bean.put("Instance Security", asList("com.braintribe.model.meta.data.security"));
		bean.put("Hibernate Mapping", asList("com.braintribe.model.accessdeployment.hibernate.meta", "com.braintribe.model.accessdeployment.jpa.meta",
				"com.braintribe.model.accessdeployment.orm.meta"));
		bean.put("Smart Mapping", asList("com.braintribe.model.accessdeployment.smart.meta"));
		bean.put("Swagger", asList("com.braintribe.model.swagger.v2_0.meta"));

		return bean;
	}

}
