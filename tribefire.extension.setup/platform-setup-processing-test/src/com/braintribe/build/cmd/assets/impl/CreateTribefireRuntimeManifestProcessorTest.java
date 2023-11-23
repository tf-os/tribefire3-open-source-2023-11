package com.braintribe.build.cmd.assets.impl;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.braintribe.build.cmd.assets.impl.CreateTribefireRuntimeManifestProcessor.ComponentType;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Backend;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Component;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Metadata;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Spec;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.TribefireRuntime;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.CoreWebContext;
import com.braintribe.model.asset.natures.MasterCartridge;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.platform.setup.api.CreateTribefireRuntimeManifest;
import com.braintribe.model.platform.setup.api.TribefireRuntimeManifestApiVersion;
import com.braintribe.model.platform.setup.api.tfruntime.CredentialsSecretRef;
import com.braintribe.model.platform.setup.api.tfruntime.CustomComponentSettings;
import com.braintribe.model.platform.setup.api.tfruntime.Database;
import com.braintribe.model.platform.setup.api.tfruntime.LogLevel;
import com.braintribe.model.platform.setup.api.tfruntime.PersistentVolume;
import com.braintribe.model.platform.setup.api.tfruntime.Resources;
import com.braintribe.model.platform.setup.api.tfruntime.ResourcesSpecification;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.PackagedPlatformAssetsByNature;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.model.setuppackage.RuntimeContainer;
import com.braintribe.testing.internal.path.PathBuilder;
import com.braintribe.testing.test.AbstractTest;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.genericmodel.GmTools;

/**
 * Provides {@link CreateTribefireRuntimeManifestProcessor} tests.
 *
 * @author michael.lafite
 */
public class CreateTribefireRuntimeManifestProcessorTest extends AbstractTest {

	private static final boolean ENABLE_FILE_BASED_RUNTIME_COMPARISON = true;

	private enum TribefireComponent {
		// @formatter:off
		TribefireServices("tribefire.cortex.services", "tribefire-services", 2, 0, 1, ComponentType.services, MasterCartridge.T),
		TribefireControlCenter("tribefire.cortex.controlcenter", "tribefire-control-center", 2, 0, 1, ComponentType.controlcenter, CoreWebContext.T),
		TribefireExplorer("tribefire.app.explorer", "tribefire-explorer", 2, 0, 1, ComponentType.explorer, CoreWebContext.T),
		TribefireModeler("tribefire.app.modeler", "tribefire-modeler", 2, 0, 1, ComponentType.modeler, CoreWebContext.T);
		// @formatter:on

		private final String groupId;
		private final String name;
		private final int major;
		private final int minor;
		private final int revision;
		private final ComponentType type;
		private final String globalId;
		private final EntityType<? extends PlatformAssetNature> natureType;

		private TribefireComponent(String groupId, String name, int major, int minor, int revision, ComponentType type,
				EntityType<? extends PlatformAssetNature> natureType) {
			this.groupId = groupId;
			this.name = name;
			this.major = major;
			this.minor = minor;
			this.revision = revision;
			this.type = type;
			this.globalId = groupId + ":" + name + "#" + major + "." + minor + "." + revision;
			this.natureType = natureType;
		}
	}

	@Test
	public void testComponentType_byNatureAndGlobalId() {
		assertThat(ComponentType.byNatureAndGlobalId(MasterCartridge.T.create(), "PlatformAsset:tribefire.cortex.services:tribefire-services#1.2.3"))
				.isEqualTo(ComponentType.services);
		assertThat(ComponentType.byNatureAndGlobalId(CoreWebContext.T.create(),
				"PlatformAsset:tribefire.cortex.controlcenter:tribefire-control-center#1.2.3")).isEqualTo(ComponentType.controlcenter);
		assertThat(
				ComponentType.byNatureAndGlobalId(CoreWebContext.T.create(), "PlatformAsset:tribefire.app.explorer:tribefire-explorer#123.456.789"))
						.isEqualTo(ComponentType.explorer);
		assertThat(ComponentType.byNatureAndGlobalId(CoreWebContext.T.create(), "PlatformAsset:tribefire.app.modeler:tribefire-modeler#99.88.101"))
				.isEqualTo(ComponentType.modeler);
		assertThat(ComponentType.byNatureAndGlobalId(WebContext.T.create(), "PlatformAsset:jnj.dhf:jnj-dhf-web-reader#1.2.3"))
				.isEqualTo(ComponentType.webreader);
	}

	@Test
	public void testManifestCreation() {
		// *** create test setup ***
		PackagedPlatformSetup packagedPlatformSetup = createdTestPackagedPlatformSetup();

		// *** create test request ***
		// we pass the map, because method will put test values into it; we use these later to check result
		CreateTribefireRuntimeManifest request = createTestServiceRequest();

		// *** create expected result ***
		TribefireRuntime expectedTribefireRuntime = createdExpectedTribefireRuntime(request);

		// *** run request to get actual result, compare actual and expected runtime and assert they are equal ***
		runRequestAndAssertTribefireRuntimesAreEqual("base", packagedPlatformSetup, request, expectedTribefireRuntime);

		// *** this was the base test. now we modify request and expected runtime multiple times and compare again ***

		// add log level
		setLogLevelForComponent("tribefire-services", LogLevel.WARNING, request, expectedTribefireRuntime);
		runRequestAndAssertTribefireRuntimesAreEqual("loglevel", packagedPlatformSetup, request, expectedTribefireRuntime);

		// set replicas
		setReplicasForComponent("tribefire-services", 3, request, expectedTribefireRuntime);
		runRequestAndAssertTribefireRuntimesAreEqual("replicas", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add enableJpda
		setEnableJpdaForComponent("tribefire-services", false, request, expectedTribefireRuntime);
		setEnableJpdaForComponent("tribefire-explorer", null, request, expectedTribefireRuntime);
		runRequestAndAssertTribefireRuntimesAreEqual("enableJpda", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add customPath
		setCustomPathForComponent("tribefire-explorer", "/custom-explorer-path", request, expectedTribefireRuntime);
		setCustomPathForComponent("tribefire-modeler", "/custom-modeler-path", request, expectedTribefireRuntime);
		runRequestAndAssertTribefireRuntimesAreEqual("customPath", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add customHealthCheckPath
		setCustomHealthCheckPathForComponent("tribefire-services", "/custom-healhtz2", request, expectedTribefireRuntime);
		runRequestAndAssertTribefireRuntimesAreEqual("customHealthCheckPath", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add resources
		ResourcesSpecification limitsResourcesSpecification = ResourcesSpecification.T.create();
		limitsResourcesSpecification.setCpu("500m");
		limitsResourcesSpecification.setMemory("1024Mi");
		ResourcesSpecification requestsResourcesSpecification = ResourcesSpecification.T.create();
		requestsResourcesSpecification.setCpu("1000m");
		requestsResourcesSpecification.setMemory("2048Mi");
		Resources resources = Resources.T.create();
		resources.setRequests(requestsResourcesSpecification);
		resources.setLimits(limitsResourcesSpecification);

		setResourcesForComponent("tribefire-services", resources.clone(new StandardCloningContext()), request, expectedTribefireRuntime);
		runRequestAndAssertTribefireRuntimesAreEqual("resources", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add single database
		{
			Database database = Database.T.create();
			database.setName("documents-dev");
			database.setType("local");
			database.setEnvPrefixes(CollectionTools.getList("PREFIX1", "PREFIX2"));
			database.setInstanceDescriptor("jdbc:postgresql://123.123.1.2:5432/" + database.getName());
			CredentialsSecretRef credentialsSecretRef = CredentialsSecretRef.T.create();
			credentialsSecretRef.setName(database.getName() + "-database-credentials");
			database.setCredentialsSecretRef(credentialsSecretRef);
			request.getDatabases().add(database);
			expectedTribefireRuntime.getSpec().getDatabases().add(database);
		}
		runRequestAndAssertTribefireRuntimesAreEqual("database", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add another database
		{
			Database database = Database.T.create();
			database.setName("documents-x-dev");
			database.setType("local");
			database.setEnvPrefixes(CollectionTools.getList("ANOTHER_PREFIX"));
			database.setInstanceDescriptor("jdbc:postgresql://123.123.1.3:5432/" + database.getName());
			CredentialsSecretRef credentialsSecretRef = CredentialsSecretRef.T.create();
			credentialsSecretRef.setName(database.getName() + "-database-credentials");
			database.setCredentialsSecretRef(credentialsSecretRef);
			request.getDatabases().add(database);
			expectedTribefireRuntime.getSpec().getDatabases().add(database);
		}
		runRequestAndAssertTribefireRuntimesAreEqual("databases", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add DCSA database
		{
			Database database = Database.T.create();
			database.setInstanceDescriptor("jdbc:postgresql://123.123.1.3:5432/" + database.getName());
			CredentialsSecretRef credentialsSecretRef = CredentialsSecretRef.T.create();
			credentialsSecretRef.setName("dcsa-database-credentials");
			database.setCredentialsSecretRef(credentialsSecretRef);
			request.setDcsaConfig(database);
			expectedTribefireRuntime.getSpec().setDcsaConfig(database);
		}
		runRequestAndAssertTribefireRuntimesAreEqual("dcsa", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add single persistent volume
		{
			PersistentVolume persistentVolume = PersistentVolume.T.create();

			persistentVolume.setName("elastic-data");
			persistentVolume.setVolumeMountPath("/elastic");
			persistentVolume.setVolumeClaimName("elastic-pvc");
			addPersistentVolumesForComponent("tribefire-services", persistentVolume.clone(new StandardCloningContext()), request,
					expectedTribefireRuntime);
		}
		runRequestAndAssertTribefireRuntimesAreEqual("persistent-volume", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add another persistent volume
		{
			PersistentVolume persistentVolume = PersistentVolume.T.create();

			persistentVolume.setName("elastic-data2");
			persistentVolume.setVolumeMountPath("/elastic2");
			persistentVolume.setVolumeClaimName("elastic-pvc2");
			addPersistentVolumesForComponent("tribefire-services", persistentVolume, request, expectedTribefireRuntime);
		}
		runRequestAndAssertTribefireRuntimesAreEqual("persistent-volumes", packagedPlatformSetup, request, expectedTribefireRuntime);

		// add labels
		Map<String, String> labels = MapTools.getStringMap("key1", "val1", "key2", "val2");
		request.getLabels().putAll(labels);
		expectedTribefireRuntime.getMetadata().getLabels().putAll(labels);
		runRequestAndAssertTribefireRuntimesAreEqual("labels", packagedPlatformSetup, request, expectedTribefireRuntime);

		boolean printMarshalledRequest = true;
		if (printMarshalledRequest) {
			YamlMarshaller marshaller = new YamlMarshaller();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			marshaller.marshall(baos, request,
					GmSerializationOptions.deriveDefaults().set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic).build());

			String marshalledRequest = baos.toString();

			System.out.println("\nMarshalled Request:\n" + marshalledRequest + "\n");
		}

		boolean runFullProcessingIncludingMarshallingAndPrintResult = true;
		if (runFullProcessingIncludingMarshallingAndPrintResult) {
			String marshalledResult = CreateTribefireRuntimeManifestProcessor.process(request, packagedPlatformSetup);
			// assert no indentation - we had a marshaller bug
			assertThat(marshalledResult).doesNotStartWith(" ");
			System.out.println("\nMarshalled Result:\n" + marshalledResult + "\n");
		}
	}

	private static Map<String, String> getEnvironmentVariablesForComponent(Component component,
			List<CustomComponentSettings> customComponentsSettings) {
		List<CustomComponentSettings> filteredCustomComponentsSettings = CreateTribefireRuntimeManifestProcessor
				.getMatchingCustomComponentsSettings(component, customComponentsSettings);

		Map<String, String> result = new HashMap<>();
		filteredCustomComponentsSettings.forEach(it -> result.putAll(it.getEnv()));

		return result;
	}

	private void setLogLevelForComponent(String componentName, LogLevel logLevel, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).setLogLevel(logLevel);
		getComponent(expectedTribefireRuntime, componentName).setLogLevel(logLevel);
	}

	private void setReplicasForComponent(String componentName, Integer replicas, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).setReplicas(replicas);
		getComponent(expectedTribefireRuntime, componentName).setReplicas(replicas);
	}

	private void setEnableJpdaForComponent(String componentName, Boolean enableJpda, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).setEnableJpda(enableJpda);
		// in runtime we only set if true
		if (enableJpda != null && enableJpda) {
			CreateTribefireRuntimeManifestProcessor.addEnvironmentVariable(getComponent(expectedTribefireRuntime, componentName).getEnv(),
					CreateTribefireRuntimeManifestProcessor.ENVIRONMENT_VARIABLE_DEBUG_PORTS_ENABLED, "true");
		}
	}

	private void setCustomPathForComponent(String componentName, String customPath, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).setCustomPath(customPath);
		getComponent(expectedTribefireRuntime, componentName).setApiPath(customPath);
	}

	private void setCustomHealthCheckPathForComponent(String componentName, String customHealthCheckPath, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).setCustomHealthCheckPath(customHealthCheckPath);
		getComponent(expectedTribefireRuntime, componentName).setCustomHealthCheckPath(customHealthCheckPath);
	}

	private void setResourcesForComponent(String componentName, Resources resources, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).setResources(resources);
		getComponent(expectedTribefireRuntime, componentName).setResources(resources);
	}

	private void addPersistentVolumesForComponent(String componentName, PersistentVolume persistentVolume, CreateTribefireRuntimeManifest request,
			TribefireRuntime expectedTribefireRuntime) {
		acquireCustomComponentSettings(request, componentName).getPersistentVolumes().add(persistentVolume);
		getComponent(expectedTribefireRuntime, componentName).getPersistentVolumes().add(persistentVolume);
	}

	static PackagedPlatformSetup createdTestPackagedPlatformSetup() {
		PackagedPlatformSetup packagedPlatformSetup = PackagedPlatformSetup.T.create();

		PackagedPlatformAssetsByNature allGlobalAssets = PackagedPlatformAssetsByNature.T.create();
		PackagedPlatformAssetsByNature webContextGlobalAssets = PackagedPlatformAssetsByNature.T.create();

		packagedPlatformSetup.getAssets().put(PlatformAssetNature.T.getTypeSignature(), webContextGlobalAssets);
		packagedPlatformSetup.getAssets().put(WebContext.T.getTypeSignature(), webContextGlobalAssets);

		for (TribefireComponent tribefireComponent : TribefireComponent.values()) {
			PlatformAssetNature nature = tribefireComponent.natureType.create();
			nature.setGlobalId(tribefireComponent.natureType.getShortName() + ":" + tribefireComponent.globalId);

			PlatformAsset platformAsset = PlatformAsset.T.create();
			platformAsset.setGlobalId(PlatformAsset.T.getShortName() + ":" + tribefireComponent.globalId);
			platformAsset.setNature(nature);
			platformAsset.setGroupId(tribefireComponent.groupId);
			platformAsset.setName(tribefireComponent.name);
			platformAsset.setVersion(tribefireComponent.major + "." + tribefireComponent.minor);
			platformAsset.setResolvedRevision("" + tribefireComponent.revision);

			PackagedPlatformAsset packagedPlatformAsset = PackagedPlatformAsset.T.create();
			packagedPlatformAsset.setAsset(platformAsset);

			PackagedPlatformAssetsByNature packagedPlatformAssetsByNature = PackagedPlatformAssetsByNature.T.create();
			packagedPlatformAssetsByNature.getAssets().add(packagedPlatformAsset);

			RuntimeContainer runtimeContainer = RuntimeContainer.T.create();
			runtimeContainer.setName(tribefireComponent.name);
			runtimeContainer.getAssets().put(WebContext.T.getTypeSignature(), packagedPlatformAssetsByNature);

			allGlobalAssets.getAssets().add(packagedPlatformAsset);
			webContextGlobalAssets.getAssets().add(packagedPlatformAsset);

			packagedPlatformSetup.getContainers().add(runtimeContainer);

			if (tribefireComponent.equals(TribefireComponent.TribefireServices)) {
				runtimeContainer.setIsMaster(true);
				packagedPlatformSetup.setMasterContainer(runtimeContainer);
			}
		}

		return packagedPlatformSetup;
	}

	private static CreateTribefireRuntimeManifest createTestServiceRequest() {
		CreateTribefireRuntimeManifest request = CreateTribefireRuntimeManifest.T.create();
		request.setApiVersion(TribefireRuntimeManifestApiVersion.v1);
		request.setRuntimeName("test-runtime");
		request.setNamespace("test-namespace");
		request.setDomain("staging.tribefire.cloud");
		request.setStage("dev");
		request.setDatabaseType("local");
		request.setBackendType("etcd");
		request.setImagePrefix("docker.artifactory.example.org/test");
		request.setImageTag("1.0-latest");

		Map<String, String> sharedCustomEnvironmentVariablesMap = new HashMap<>();
		sharedCustomEnvironmentVariablesMap.put("BUILD_ID", "123");
		sharedCustomEnvironmentVariablesMap.put("BUILD_URL",
				"https://ci.example.org/path/to/build/" + sharedCustomEnvironmentVariablesMap.get("BUILD_ID"));
		{
			CustomComponentSettings customComponentSettings = CustomComponentSettings.T.create();
			customComponentSettings.setNameRegex(".*");
			customComponentSettings.getEnv().putAll(sharedCustomEnvironmentVariablesMap);
			request.getCustomComponentsSettings().add(customComponentSettings);
		}

		Map<String, String> customMasterEnvironmentVariablesMap = new HashMap<>();
		customMasterEnvironmentVariablesMap.put("SOME_SETTING", "abc");
		customMasterEnvironmentVariablesMap.put("SOME_OTHER_SETTING", "x y z");
		{
			CustomComponentSettings customComponentSettings = CustomComponentSettings.T.create();
			customComponentSettings.setName(TribefireComponent.TribefireServices.name);
			customComponentSettings.getEnv().putAll(customMasterEnvironmentVariablesMap);
			request.getCustomComponentsSettings().add(customComponentSettings);
		}

		return request;
	}

	private static TribefireRuntime createdExpectedTribefireRuntime(CreateTribefireRuntimeManifest request) {
		TribefireRuntime expectedTribefireRuntime = TribefireRuntime.T.create();
		expectedTribefireRuntime
				.setApiVersion(CreateTribefireRuntimeManifestProcessor.TRIBEFIRE_RUNTIME_API_VERSION_PREFIX + request.getApiVersion());
		expectedTribefireRuntime.setKind(CreateTribefireRuntimeManifestProcessor.TRIBEFIRE_RUNTIME_KIND);

		Metadata metadata = Metadata.T.create();
		expectedTribefireRuntime.setMetadata(metadata);
		expectedTribefireRuntime.getMetadata().setName(request.getRuntimeName());
		expectedTribefireRuntime.getMetadata().setNamespace(request.getNamespace());
		expectedTribefireRuntime.getMetadata().getLabels().put(CreateTribefireRuntimeManifestProcessor.TRIBEFIRE_RUNTIME_METADATA_LABELS_STAGE,
				request.getStage());

		Spec spec = Spec.T.create();
		expectedTribefireRuntime.setSpec(spec);
		spec.setDomain(request.getDomain());
		spec.setDatabaseType(request.getDatabaseType());

		Backend backend = Backend.T.create();
		spec.setBackend(backend);
		backend.setType(request.getBackendType());

		for (TribefireComponent tribefireComponent : TribefireComponent.values()) {
			Component component = Component.T.create();

			component.setName(StringTools.getFirstNCharacters(tribefireComponent.name,
					CreateTribefireRuntimeManifestProcessor.TRIBEFIRE_RUNTIME_MAX_COMPONENT_NAME_LENGTH));
			component.setType(tribefireComponent.type.getComponentTypeName());
			component.setImage(request.getImagePrefix() + "/" + tribefireComponent.name);
			component.setImageTag(request.getImageTag());
			component.setResources(tribefireComponent.type.getDefaultResources());

			Map<String, String> environmentVariables = getEnvironmentVariablesForComponent(component, request.getCustomComponentsSettings());
			CreateTribefireRuntimeManifestProcessor.addEnvironmentVariables(component.getEnv(), environmentVariables);

			expectedTribefireRuntime.getSpec().getComponents().add(component);
		}

		return expectedTribefireRuntime;
	}

	private CustomComponentSettings acquireCustomComponentSettings(CreateTribefireRuntimeManifest request, String componentName) {
		CustomComponentSettings result;
		System.out.println(request.getCustomComponentsSettings());
		List<CustomComponentSettings> customComponentsSettings = request.getCustomComponentsSettings().stream()
				.filter(it -> it.getName() != null && it.getName().equals(componentName)).toList();
		if (customComponentsSettings.size() > 1) {
			throw new IllegalArgumentException(
					"Multiple " + CustomComponentSettings.class.getSimpleName() + " instances specified for component " + componentName + "!");
		} else if (customComponentsSettings.isEmpty()) {
			result = CustomComponentSettings.T.create();
			result.setName(componentName);
			request.getCustomComponentsSettings().add(result);
		} else {
			result = customComponentsSettings.get(0);
		}
		return result;
	}

	private Component getComponent(TribefireRuntime tribefireRuntime, String componentName) {
		Component result;
		List<Component> componentList = tribefireRuntime.getSpec().getComponents().stream().filter(it -> it.getName().matches(componentName))
				.toList();
		if (componentList.size() > 1) {
			throw new IllegalArgumentException(
					"Multiple " + Component.class.getSimpleName() + " instances specified for component " + componentName + "!");
		} else if (componentList.isEmpty()) {
			throw new IllegalArgumentException("No " + Component.class.getSimpleName() + " instance specified for component " + componentName + "!");
		} else {
			result = componentList.get(0);
		}
		return result;
	}

	private void runRequestAndAssertTribefireRuntimesAreEqual(String testId, PackagedPlatformSetup packagedPlatformSetup,
			CreateTribefireRuntimeManifest request, TribefireRuntime expectedTribefireRuntime) {
		TribefireRuntime actualTribefireRuntime = CreateTribefireRuntimeManifestProcessor.createTribefireRuntime(request, packagedPlatformSetup);
		CreateTribefireRuntimeManifestProcessor.sortLists(expectedTribefireRuntime);

		String actualTribefireRuntimeAsString = GmTools.getDescription(actualTribefireRuntime);
		String expectedTribefireRuntimeAsString = GmTools.getDescription(expectedTribefireRuntime);

		if (ENABLE_FILE_BASED_RUNTIME_COMPARISON) {
			PathBuilder pathBuilder = PathBuilder.withSettings().baseDirIsTempDir().pathIsFile().pathMayExist().createParents();
			File expectedTribefireRuntimeFile = pathBuilder.path(CreateTribefireRuntimeManifestProcessorTest.class.getName(), testId, "expected.txt")
					.buildFile();
			File actualTribefireRuntimeFile = pathBuilder.path(CreateTribefireRuntimeManifestProcessorTest.class.getName(), testId, "actual.txt")
					.buildFile();
			FileTools.writeStringToFile(actualTribefireRuntimeFile, actualTribefireRuntimeAsString);
			FileTools.writeStringToFile(expectedTribefireRuntimeFile, expectedTribefireRuntimeAsString);
			System.out.println("[diff tool] " + actualTribefireRuntimeFile.getAbsolutePath() + " " + expectedTribefireRuntimeFile.getAbsolutePath());
		}

		assertThat(actualTribefireRuntimeAsString).isEqualToWithVerboseErrorMessageAndLogging(expectedTribefireRuntimeAsString);
	}
}
