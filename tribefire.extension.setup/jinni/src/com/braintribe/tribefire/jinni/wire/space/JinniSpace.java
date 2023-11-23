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
package com.braintribe.tribefire.jinni.wire.space;

import static com.braintribe.utils.lcd.StringTools.camelCaseToSocialDistancingCase;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.bin.Bin2Marshaller;
import com.braintribe.codec.marshaller.common.BasicConfigurableMarshallerRegistry;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.stax.StaxMarshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.devrock.devenv.processing.DevEnvManagementProcessor;
import com.braintribe.devrock.env.processing.DevEnvAwareInterceptor;
import com.braintribe.devrock.model.devenv.api.DevEnvManagementRequest;
import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.gm.config.wire.contract.ModeledConfigurationContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.access.collaboration.CsaStatePersistence;
import com.braintribe.model.access.collaboration.CsaStatePersistenceImpl;
import com.braintribe.model.access.collaboration.offline.CollaborativeAccessOfflineManager;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.jinni.api.CheckLicense;
import com.braintribe.model.jinni.api.GenerateShellCompletionScript;
import com.braintribe.model.jinni.api.Help;
import com.braintribe.model.jinni.api.History;
import com.braintribe.model.jinni.api.JinniOptions;
import com.braintribe.model.jinni.api.JinniReflectionRequest;
import com.braintribe.model.jinni.api.SpawnedJinniRequest;
import com.braintribe.model.jinni.api.UpdateDevrockAntTasks;
import com.braintribe.model.jinni.api.UpdateJinniRequest;
import com.braintribe.model.jinni.api.template.CreateArtifactsRequest;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.mapping.Alias;
import com.braintribe.model.platform.setup.api.SetupRequest;
import com.braintribe.model.processing.dataio.FileBasedGmPathValueStore;
import com.braintribe.model.processing.manipulation.marshaller.ManMarshaller;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.meta.oracle.EntityTypeOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.platform.setup.wire.contract.PlatformSetupDependencyEnvironmentContract;
import com.braintribe.model.processing.platform.setup.wire.space.PlatformSetupSpace;
import com.braintribe.model.processing.service.common.CompositeServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.service.api.CompositeRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.template.processing.wire.space.ArtifactTemplateProcessingSpace;
import com.braintribe.tribefire.jinni.support.CheckLicenseProcessor;
import com.braintribe.tribefire.jinni.support.HelpProcessor;
import com.braintribe.tribefire.jinni.support.JinniReflectionProcessor;
import com.braintribe.tribefire.jinni.support.SpawnedJinniRequestProcessor;
import com.braintribe.tribefire.jinni.support.UpdateDevrockAntTasksProcessor;
import com.braintribe.tribefire.jinni.support.UpdateJinniProcessor;
import com.braintribe.tribefire.jinni.support.request.alias.AliasProcessor;
import com.braintribe.tribefire.jinni.support.request.alias.JinniAlias;
import com.braintribe.tribefire.jinni.support.request.completion.GenerateShellCompletionScriptProcessor;
import com.braintribe.tribefire.jinni.support.request.history.HistoryProcessor;
import com.braintribe.tribefire.jinni.support.request.history.JinniHistory;
import com.braintribe.tribefire.jinni.support.template.CreateArtifactsProcessor;
import com.braintribe.tribefire.jinni.wire.contract.HelpProcessorConfigurationContract;
import com.braintribe.tribefire.jinni.wire.contract.JinniConfDirContract;
import com.braintribe.tribefire.jinni.wire.contract.JinniContract;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.extension.artifact.management.api.model.request.ArtifactManagementRequest;
import tribefire.extension.artifact.management.processing.ArtifactManagementProcessor;
import tribefire.extension.hydrux.setup.model.HydruxSetupRequest;
import tribefire.extension.hydrux.setup.processing.HydruxSetupProcessor;
import tribefire.extension.js.model.api.request.JsSetupRequest;
import tribefire.extension.js.processing.wire.contract.JsSetupContract;
import tribefire.extension.setup.dev_env_generator.wire.space.DevEnvGeneratorSpace;
import tribefire.extension.setup.dev_env_generator_api.model.CreateDevEnv;
import tribefire.extension.setup.model.jinni.config.DevrockAntTaskConfiguration;
import tribefire.extension.setup.model.jinni.config.DevrockUpdateConfiguration;
import tribefire.extension.xmi.argo.exchange.processor.ArgoExchangeProcessor;
import tribefire.extension.xmi.model.exchange.api.ArgoExchangeRequest;
import tribefire.extension.xml.schemed.processing.wire.contract.XsdAnalyzingProcessorContract;
import tribefire.extension.xml.schemed.service.AnalyzeXsdRequest;

@Managed
public class JinniSpace implements JinniContract {

	@Import
	private PlatformSetupSpace platformSetup;

	@Import
	private JinniConfDirContract jinniConfDir;
	
	@Import
	private ArtifactTemplateProcessingSpace templateProcessing;

	@Import
	private DevEnvGeneratorSpace devEnvGenerator;

	@Import
	private HelpProcessorConfigurationContract helpProcessorConfiguration;

	@Import
	private PlatformSetupDependencyEnvironmentContract dependencyEnvironment;

	@Import
	private XsdAnalyzingProcessorContract xsdAnalyzing;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;

	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	@Import
	private JsSetupContract jsSetup;
	
	@Import 
	private ModeledConfigurationContract modeledConfiguration;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
	}

	/**
	 * @see ServiceDomainSpace#modelAccessoryFactory
	 */
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		// interceptor configuration
		bean.removeInterceptor("auth");
		// TODO: activate after review
		// bean.registerInterceptor("dev-env-aware").register(DevEnvAwareInterceptor.INSTANCE);
		// registration of request to processor mappings

		// reflective api requests of jinni
		bean.register(CompositeRequest.T, compositeProcessor());
		bean.register(Help.T, helpProcessor());
		bean.register(GenerateShellCompletionScript.T, generateShellCompletionScriptProcessor());
		bean.register(JinniReflectionRequest.T, jinniReflectionProcessor());
		bean.register(History.T, historyProcessorExpert());
		bean.register(com.braintribe.model.jinni.api.ListAliases.T, aliasProcessor());
		bean.register(UpdateJinniRequest.T, updateJinniProcessor());
		bean.register(UpdateDevrockAntTasks.T, updateBtAntTasksProcessor());
		bean.register(SpawnedJinniRequest.T, spawnedJinniRequestProcessor());
		bean.register(CheckLicense.T, checkLicenseProcessor());

		// platform setup processing requests
		bean.register(SetupRequest.T, platformSetup.platformSetupProcessor());

		// artifact template processing requests
		bean.register(ArtifactTemplateRequest.T, templateProcessing.artifactTemplateProcessor());

		bean.register(CreateArtifactsRequest.T, createArtifactsProcessor());

		// dev-env management processing requests
		bean.register(DevEnvManagementRequest.T, devEnvManagementProcessor());

		bean.register(CreateDevEnv.T, devEnvGenerator.devEnvGenerator());
		
		// schemed xml requests
		bean.register(AnalyzeXsdRequest.T, xsdAnalyzing.xsdAnalyzingProcessor());

		// JS Ux Setup requests
		bean.register(JsSetupRequest.T, jsSetup.jsSetupProcessor());

		bean.register(HydruxSetupRequest.T, hydruxSetupProcessor());

		// Artifact Management
		bean.register(ArtifactManagementRequest.T, artifactManagementProcessor());

		// CSA Persistence Management
		bean.register(CollaborativePersistenceRequest.T, offlineCsaPersistenceProcessor());

		// XMI Processing
		bean.register(ArgoExchangeRequest.T, argoExchangeProcessor());

		// AOP
		bean.registerInterceptor("dev-env").register(DevEnvAwareInterceptor.INSTANCE);
	}

	@Managed
	private CheckLicenseProcessor checkLicenseProcessor() {
		CheckLicenseProcessor bean = new CheckLicenseProcessor();
		bean.setLicenseDir(this.jinniConfDir.confDir());
		return bean;
	}

	@Managed
	private DevEnvManagementProcessor devEnvManagementProcessor() {
		DevEnvManagementProcessor bean = new DevEnvManagementProcessor();
		return bean;
	}

	@Managed
	private CreateArtifactsProcessor createArtifactsProcessor() {
		CreateArtifactsProcessor bean = new CreateArtifactsProcessor();
		bean.setGroupFolder(new File("."));
		return bean;
	}

	@Managed
	private HydruxSetupProcessor hydruxSetupProcessor() {
		HydruxSetupProcessor bean = new HydruxSetupProcessor();
		bean.setGroupFolder(new File("."));

		return bean;
	}

	@Managed
	private CollaborativeAccessOfflineManager offlineCsaPersistenceProcessor() {
		File baseFolder = new File(".");

		CollaborativeAccessOfflineManager bean = new CollaborativeAccessOfflineManager();
		bean.setBaseFolder(baseFolder);
		bean.setCsaStatePersistence(csaStatePersistence(baseFolder));

		return bean;
	}

	public CsaStatePersistence csaStatePersistence(File baseFolder) {
		CsaStatePersistenceImpl result = new CsaStatePersistenceImpl();
		result.setPathValueStore(fileBasedKeyValueStore(baseFolder));

		return result;
	}

	private FileBasedGmPathValueStore fileBasedKeyValueStore(File baseFolder) {
		FileBasedGmPathValueStore bean = new FileBasedGmPathValueStore();
		bean.setRootDir(baseFolder);
		bean.setSerializationOptions(GmSerializationOptions.defaultOptions.derive().outputPrettiness(OutputPrettiness.high).build());
		bean.setMarshaller(jsonMarshaller());

		return bean;
	}

	@Override
	@Managed
	public Map<String, EntityType<?>> shortcuts() {
		Map<String, EntityType<?>> bean = new LinkedHashMap<>();

		ModelAccessory modelAccessory = helpProcessorConfiguration.modelAccessory();

		ModelOracle oracle = modelAccessory.getOracle();
		CmdResolver cmdResolver = modelAccessory.getCmdResolver();
		EntityTypeOracle entityTypeOracle = oracle.findEntityTypeOracle(GenericEntity.T);
		GmMetaModel serviceApiModel = entityTypeOracle.asGmEntityType().declaringModel();

		Set<GmEntityType> requestTypes = entityTypeOracle.getSubTypes() //
				.transitive() //
				.includeSelf() //
				.onlyInstantiable() //
				.asGmTypes();

		for (GmEntityType requestType : requestTypes) {
			if (requestType.getDeclaringModel() != serviceApiModel) {
				EntityType<?> reflectionType = requestType.reflectionType();

				String shortcut = camelCaseToSocialDistancingCase(reflectionType.getShortName());
				bean.put(shortcut, reflectionType);

				for (Alias alias : cmdResolver.getMetaData().entityType(requestType).meta(Alias.T).list()) {
					String aliasName = alias.getName();

					if (aliasName != null)
						bean.put(aliasName, reflectionType);
				}
			}
		}

		return bean;
	}

	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}

	@Override
	public ModelAccessory modelAccessory() {
		return helpProcessorConfiguration.modelAccessory();
	}

	@Override
	@Managed
	public Map<EntityType<?>, Function<JinniOptions, ? extends ServiceRequest>> defaultRequests() {
		Map<EntityType<?>, Function<JinniOptions, ? extends ServiceRequest>> bean = new LinkedHashMap<>();

		return bean;
	}

	@Managed
	public SpawnedJinniRequestProcessor spawnedJinniRequestProcessor() {
		SpawnedJinniRequestProcessor bean = new SpawnedJinniRequestProcessor();
		return bean;
	}

	@Managed
	public JinniReflectionProcessor jinniReflectionProcessor() {
		JinniReflectionProcessor bean = new JinniReflectionProcessor();
		bean.setInstallationDir(dependencyEnvironment.installationDir());
		bean.setModeledConfiguration(modeledConfiguration.config());
		bean.setTypeShortcuts(shortcuts());
		return bean;
	}

	@Managed
	public CompositeServiceProcessor compositeProcessor() {
		CompositeServiceProcessor bean = new CompositeServiceProcessor();
		return bean;
	}

	@Managed
	public HelpProcessor helpProcessor() {
		HelpProcessor bean = new HelpProcessor();
		bean.setModelAccessory(helpProcessorConfiguration.modelAccessory());
		return bean;
	}

	@Managed
	public GenerateShellCompletionScriptProcessor generateShellCompletionScriptProcessor() {
		GenerateShellCompletionScriptProcessor bean = new GenerateShellCompletionScriptProcessor();
		bean.setModelAccessory(helpProcessorConfiguration.modelAccessory());
		return bean;
	}

	@Override
	@Managed
	public JinniHistory history() {
		JinniHistory bean = new JinniHistory();
		bean.setInstallationDir(dependencyEnvironment.installationDir());
		bean.setModelAccessory(helpProcessorConfiguration.modelAccessory());
		return bean;
	}

	@Override
	@Managed
	public JinniAlias alias() {
		JinniAlias bean = new JinniAlias();
		bean.setInstallationDir(dependencyEnvironment.installationDir());
		bean.setVirtualEnvironment(dependencyEnvironment.virtualEnvironment());
		return bean;
	}

	@Managed
	public UpdateJinniProcessor updateJinniProcessor() {
		UpdateJinniProcessor bean = new UpdateJinniProcessor();
		bean.setJinniInstallationDir(dependencyEnvironment.installationDir());
		bean.setVirtualEnvironment(dependencyEnvironment.virtualEnvironment());
		bean.setAdditionalRepositoryConfigurationLocations(additionalUpdateRepositoryConfigurationLocations());
		return bean;
	}
	
	@Managed
	private List<Path> additionalUpdateRepositoryConfigurationLocations() {
		return modeledConfiguration.config(DevrockUpdateConfiguration.T).getRepositoryConfigurationLocations() //
			.stream() //
			.map(l -> Paths.get(l).normalize()).collect(Collectors.toList());
	}

	@Managed
	public UpdateDevrockAntTasksProcessor updateBtAntTasksProcessor() {
		UpdateDevrockAntTasksProcessor bean = new UpdateDevrockAntTasksProcessor();
		
		bean.setAdditionalRepositoryConfigurationLocations(additionalUpdateRepositoryConfigurationLocations());
		bean.setVirtualEnvironment(dependencyEnvironment.virtualEnvironment());
		
		File configuredLibDir = Optional.ofNullable(modeledConfiguration.config(DevrockAntTaskConfiguration.T).getAntLibFolder()).map(File::new).orElse(null);
		
		if (configuredLibDir != null && configuredLibDir.exists()) {
			try {
				bean.setLibDir(configuredLibDir.getCanonicalFile());
				bean.setLibDirOrigin("conf/devrock-ant-task-configuration.yaml");
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		else {
			String antHome = dependencyEnvironment.virtualEnvironment().getEnv("ANT_HOME");

			if (antHome != null) {
				File antHomeFile = new File(antHome);
				File libDir = new File(antHomeFile, "lib");
				bean.setLibDir(libDir);
				bean.setLibDirOrigin("ANT_HOME environment variable");
			}
			else {
				bean.setLibDirOrigin("Lib folder was not configured by either ANT_HOME environment variable or conf/devrock-ant-task-configuration.yaml");
			}
		}
		
		return bean;
	}

	@Managed
	public HistoryProcessor historyProcessorExpert() {
		HistoryProcessor bean = new HistoryProcessor();
		bean.setJinniHistory(history());
		bean.setJinniAlias(alias());
		return bean;
	}

	@Managed
	public AliasProcessor aliasProcessor() {
		AliasProcessor bean = new AliasProcessor();
		bean.setJinniAlias(alias());
		return bean;
	}

	@Managed
	private ArtifactManagementProcessor artifactManagementProcessor() {
		ArtifactManagementProcessor bean = new ArtifactManagementProcessor();
		bean.setVirtualEnvironment(dependencyEnvironment.virtualEnvironment());

		return bean;
	}

	@Managed
	private ArgoExchangeProcessor argoExchangeProcessor() {
		ArgoExchangeProcessor bean = new ArgoExchangeProcessor();
		bean.setVirtualEnvironment(dependencyEnvironment.virtualEnvironment());
		return bean;
	}

	@Override
	@Managed
	public BasicConfigurableMarshallerRegistry marshallerRegistry() {
		BasicConfigurableMarshallerRegistry bean = new BasicConfigurableMarshallerRegistry();

		bean.registerMarshaller("application/json", jsonMarshaller());
		bean.registerMarshaller("application/yaml", yamlMarshaller());
		bean.registerMarshaller("text/yaml", yamlMarshaller());
		bean.registerMarshaller("text/xml", xmlMarshaller());
		bean.registerMarshaller("gm/xml", binMarshaller());
		bean.registerMarshaller("gm/man", manMarshaller());
		return bean;
	}

	@Managed
	private JsonStreamMarshaller jsonMarshaller() {
		return new JsonStreamMarshaller();
	}

	@Managed
	private ManMarshaller manMarshaller() {
		return new ManMarshaller();
	}

	@Managed
	private Bin2Marshaller binMarshaller() {
		return new Bin2Marshaller();
	}

	@Managed
	private StaxMarshaller xmlMarshaller() {
		return new StaxMarshaller();
	}

	@Override
	@Managed
	public YamlMarshaller yamlMarshaller() {
		return new YamlMarshaller();
	}

}
