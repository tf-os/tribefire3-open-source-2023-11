package com.braintribe.build.cmd.assets.impl;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.build.cmd.assets.impl.tfruntime.model.Backend;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Component;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.EnvironmentVariable;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Metadata;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.Spec;
import com.braintribe.build.cmd.assets.impl.tfruntime.model.TribefireRuntime;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.common.lcd.UnknownEnumException;
import com.braintribe.logging.Logger;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.natures.CoreWebContext;
import com.braintribe.model.asset.natures.MasterCartridge;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.model.platform.setup.api.CreateTribefireRuntimeManifest;
import com.braintribe.model.platform.setup.api.TribefireRuntimeManifestApiVersion;
import com.braintribe.model.platform.setup.api.tfruntime.CustomComponentSettings;
import com.braintribe.model.platform.setup.api.tfruntime.Database;
import com.braintribe.model.platform.setup.api.tfruntime.PersistentVolume;
import com.braintribe.model.platform.setup.api.tfruntime.Resources;
import com.braintribe.model.platform.setup.api.tfruntime.ResourcesSpecification;
import com.braintribe.model.setuppackage.PackagedPlatformAssetsByNature;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.model.setuppackage.RuntimeContainer;
import com.braintribe.utils.CollectionTools;
import com.braintribe.utils.MapTools;
import com.braintribe.utils.StringTools;

/**
 * Processes {@link CreateTribefireRuntimeManifest} requests.
 *
 * @author michael.lafite
 */
public class CreateTribefireRuntimeManifestProcessor {

	public enum ComponentType {

		// @formatter:off
		//                         CPU request / limit      Memory request / limit
		services("services",               500,   3000,               1024,   3072),
		controlcenter("control-center",    100,   1000,                192,    512),
		explorer("explorer",               100,   1000,                192,    512),
		modeler("modeler",                 100,   1000,                192,    512),
		webreader("web-reader",            100,   1000,                192,    512);
		// @formatter:on

		private final String componentTypeName;
		private final int cpuMillisRequest;
		private final int cpuMillisLimit;
		private final int memoryMegabytesRequest;
		private final int memoryMegabytesLimit;

		private ComponentType(String componentTypeName, int cpuMillisRequest, int cpuMillisLimit, int memoryMegabytesRequest,
				int memoryMegabytesLimit) {
			this.componentTypeName = componentTypeName;
			this.cpuMillisRequest = cpuMillisRequest;
			this.cpuMillisLimit = cpuMillisLimit;
			this.memoryMegabytesRequest = memoryMegabytesRequest;
			this.memoryMegabytesLimit = memoryMegabytesLimit;
		}

		public String getComponentTypeName() {
			return this.componentTypeName;
		}

		public Resources getDefaultResources() {
			ResourcesSpecification requestsResourcesSpecification = ResourcesSpecification.T.create();
			requestsResourcesSpecification.setCpu(this.cpuMillisRequest + "m");
			requestsResourcesSpecification.setMemory(this.memoryMegabytesRequest + "Mi");

			ResourcesSpecification limitsResourcesSpecification = ResourcesSpecification.T.create();
			limitsResourcesSpecification.setCpu(this.cpuMillisLimit + "m");
			limitsResourcesSpecification.setMemory(this.memoryMegabytesLimit + "Mi");

			Resources resources = Resources.T.create();
			resources.setRequests(requestsResourcesSpecification);
			resources.setLimits(limitsResourcesSpecification);

			return resources;
		}

		public static ComponentType byNatureAndGlobalId(PlatformAssetNature platformAssetNature, String globalId) {
			ComponentType result;

			if (platformAssetNature instanceof MasterCartridge) {
				result = services;
			} else if (platformAssetNature instanceof CoreWebContext) {
				if (globalId.startsWith("PlatformAsset:tribefire.cortex.controlcenter:tribefire-control-center#")) {
					result = controlcenter;
				} else if (globalId.startsWith("PlatformAsset:tribefire.cortex.explorer:tribefire-explorer#")
						|| globalId.startsWith("PlatformAsset:tribefire.app.explorer:tribefire-explorer#")) {
					result = ComponentType.explorer;
				} else if (globalId.startsWith("PlatformAsset:tribefire.cortex.modeler:tribefire-modeler#")
						|| globalId.startsWith("PlatformAsset:tribefire.app.modeler:tribefire-modeler#")) {
					result = modeler;
				} else if (globalId.startsWith("PlatformAsset:tribefire.cortex.webreader:tribefire-web-reader#")
						|| globalId.startsWith("PlatformAsset:tribefire.app.web-reader:tribefire-web-reader#")) {
					result = webreader;
				} else {
					throw new IllegalArgumentException("Unsupported global id: " + globalId);
				}
			} else if (platformAssetNature instanceof WebContext) {
				String regexPrefix = "PlatformAsset\\:.+\\:.+-";
				String regexSuffix = "\\#.+";

				if (globalId.matches(regexPrefix + "control-center" + regexSuffix)) {
					result = controlcenter;
				} else if (globalId.matches(regexPrefix + "explorer" + regexSuffix)) {
					result = explorer;
				} else if (globalId.matches(regexPrefix + "modeler" + regexSuffix)) {
					result = modeler;
				} else if (globalId.matches(regexPrefix + "web-reader" + regexSuffix)) {
					result = webreader;
				} else {
					throw new IllegalArgumentException(
							"Unsupported global id for asset of type " + WebContext.class.getSimpleName() + ": " + globalId);
				}

			} else {
				throw new IllegalArgumentException("Unsupported platform asset nature: " + platformAssetNature.entityType().getTypeName());
			}

			return result;
		}
	}

	public static final String TRIBEFIRE_RUNTIME_API_VERSION_PREFIX = "tribefire.cloud/";

	public static final String TRIBEFIRE_RUNTIME_KIND = "TribefireRuntime";
	public static final String TRIBEFIRE_RUNTIME_METADATA_LABELS_STAGE = "stage";

	public static final String ENVIRONMENT_VARIABLE_DEBUG_PORTS_ENABLED = "DEBUG_PORTS_ENABLED";

	public static final int TRIBEFIRE_RUNTIME_MAX_COMPONENT_NAME_LENGTH = 31;
	public static final int TRIBEFIRE_RUNTIME_MAX_RUNTIME_NAME_LENGTH = 32;

	private static final String TRIBEFIRE_JS_GLOBAL_ID_WITHOUT_VERSION = "PlatformAsset:tribefire.cortex.js:tribefire-js";

	private static final Logger logger = Logger.getLogger(CreateTribefireRuntimeManifestProcessor.class);

	public static String process(CreateTribefireRuntimeManifest request, PackagedPlatformSetup packagedPlatformSetup) {
		TribefireRuntime tribefireRuntime = createTribefireRuntime(request, packagedPlatformSetup);

		StringWriter sw = new StringWriter();

		new YamlMarshaller().marshall(sw, tribefireRuntime, tfRuntimeSerializationOpts());

		return sw.toString();
	}

	private static GmSerializationOptions tfRuntimeSerializationOpts() {
		return GmSerializationOptions.deriveDefaults() //
				.set(TypeExplicitnessOption.class, TypeExplicitness.polymorphic) //
				.inferredRootType(TribefireRuntime.T) //
				.build();
	}

	static TribefireRuntime createTribefireRuntime(CreateTribefireRuntimeManifest request, PackagedPlatformSetup packagedPlatformSetup) {

		TribefireRuntime tribefireRuntime = TribefireRuntime.T.create();
		tribefireRuntime.setApiVersion(TRIBEFIRE_RUNTIME_API_VERSION_PREFIX + request.getApiVersion());
		tribefireRuntime.setKind(TRIBEFIRE_RUNTIME_KIND);

		Metadata metadata = Metadata.T.create();
		tribefireRuntime.setMetadata(metadata);
		String runtimeName = request.getRuntimeName();
		if (runtimeName.length() > TRIBEFIRE_RUNTIME_MAX_RUNTIME_NAME_LENGTH) {
			throw new IllegalArgumentException("Invalid runtime name '" + runtimeName + "': Name has " + runtimeName.length()
					+ " characters, but allowed maximum is " + TRIBEFIRE_RUNTIME_MAX_RUNTIME_NAME_LENGTH + ".");
		}
		metadata.setName(runtimeName);
		metadata.setNamespace(request.getNamespace());

		String stage = request.getStage();
		if (request.getLabels().containsKey(TRIBEFIRE_RUNTIME_METADATA_LABELS_STAGE)) {
			String stageInLabels = request.getLabels().get(TRIBEFIRE_RUNTIME_METADATA_LABELS_STAGE);
			if (stage != null && stageInLabels != null && stage.equals(stageInLabels)) {
				throw new IllegalArgumentException(
						"Stage specified two times (i.e. also in labels), but with different values: " + stage + " vs " + stageInLabels);
			}
			if (stageInLabels == null) {
				throw new IllegalArgumentException("Stage specified in labels, but with null value. This is not allowed!");
			}
			stage = stageInLabels;
		}
		if (stage == null) {
			throw new IllegalArgumentException("No stage specified!");
		}

		if (!request.getLabels().isEmpty()) {
			metadata.getLabels().putAll(request.getLabels());
		}
		metadata.getLabels().put(TRIBEFIRE_RUNTIME_METADATA_LABELS_STAGE, stage);

		Spec spec = Spec.T.create();
		tribefireRuntime.setSpec(spec);

		spec.setDomain(request.getDomain());
		spec.setDatabaseType(request.getDatabaseType());
		Backend backend = Backend.T.create();
		spec.setBackend(backend);
		backend.setType(request.getBackendType());

		if (!request.getDatabases().isEmpty()) {
			tribefireRuntime.getSpec().setDatabases(request.getDatabases());
		}

		if (request.getDcsaConfig() != null) {
			Database dcsaConfig = request.getDcsaConfig();

			switch (request.getApiVersion()) {
				case v1alpha1:
					// nothing to do
					break;
				case v1:
					if (dcsaConfig.getName() != null) {
						logger.warn("DCSA database name has been specified as '" + dcsaConfig.getName()
								+ "'. Ignoring it, since the respective property was removed with API version "
								+ TribefireRuntimeManifestApiVersion.v1 + ".");
						dcsaConfig.setName(null);
					}
					if (dcsaConfig.getType() != null) {
						logger.warn("DCSA database type has been specified as '" + dcsaConfig.getType()
								+ "'. Ignoring it, since the respective property was removed with API version "
								+ TribefireRuntimeManifestApiVersion.v1 + ".");
						dcsaConfig.setType(null);
					}
					break;
				default:
					throw new UnknownEnumException(request.getApiVersion());
			}
			tribefireRuntime.getSpec().setDcsaConfig(dcsaConfig);
		}

		Component masterComponent = null;
		Map<String, String> inferredMasterEnvironmentVariables = new HashMap<>();

		for (RuntimeContainer container : packagedPlatformSetup.getContainers()) {

			PackagedPlatformAssetsByNature packagedPlatformAssetsByNature = container.getAssets().get(WebContext.T.getTypeSignature());
			if (packagedPlatformAssetsByNature == null || packagedPlatformAssetsByNature.getAssets().isEmpty()) {
				throw new RuntimeException(
						"Container " + container.getName() + " unexpectedly has no asset of nature " + WebContext.class.getName() + "!");
			}
			if (packagedPlatformAssetsByNature.getAssets().size() > 1) {
				throw new RuntimeException(
						"Container " + container.getName() + " unexpectedly has multiple assets of nature " + WebContext.class.getName() + "!"
								+ " This is not supported (yet) by Jinni. Please enable disjoint projection when packaging the platform setup.");
			}

			PlatformAsset webContextAsset = CollectionTools.getFirstElement(packagedPlatformAssetsByNature.getAssets()).getAsset();

			if (!isWebContextAssetMappedToComponent(webContextAsset)) {
				continue;
			}

			ComponentType componentType = ComponentType.byNatureAndGlobalId(webContextAsset.getNature(), webContextAsset.getGlobalId());

			Component component = Component.T.create();
			spec.getComponents().add(component);
			String componentName = container.getName();
			if (componentName.length() > TRIBEFIRE_RUNTIME_MAX_COMPONENT_NAME_LENGTH) {
				componentName = StringTools.getFirstNCharacters(componentName, TRIBEFIRE_RUNTIME_MAX_COMPONENT_NAME_LENGTH);
			}

			component.setName(componentName);
			component.setType(componentType.getComponentTypeName());
			component.setImage(request.getImagePrefix() + "/" + container.getName());
			component.setImageTag(request.getImageTag());

			if (container.getIsMaster()) {
				masterComponent = component;
			}

			// set default resources, i.e. defaults for requested/maximum cpu and memory
			// the actual values depend on the type, e.g. tribefire services/master require (much) more memory than e.g. tribefire explorer
			component.setResources(componentType.getDefaultResources());

			List<CustomComponentSettings> customComponentsSettings = getMatchingCustomComponentsSettings(component,
					request.getCustomComponentsSettings());

			for (CustomComponentSettings customComponentSettings : customComponentsSettings) {

				if (customComponentSettings.getLogLevel() != null) {
					component.setLogLevel(customComponentSettings.getLogLevel());
				}

				if (customComponentSettings.getReplicas() != null) {
					component.setReplicas(customComponentSettings.getReplicas());
				}

				if (customComponentSettings.getEnableJpda() != null && customComponentSettings.getEnableJpda()) {
					// we used to have an "enableJpda" flag also in the manifest, but then we decided
					// that we can also just directly set respective environment variable
					mergeEnvironmentVariables(component, MapTools.getStringMap(ENVIRONMENT_VARIABLE_DEBUG_PORTS_ENABLED, "true"));
				}

				if (customComponentSettings.getCustomHealthCheckPath() != null) {
					component.setCustomHealthCheckPath(customComponentSettings.getCustomHealthCheckPath());
				}

				if (customComponentSettings.getCustomPath() != null) {
					component.setApiPath(customComponentSettings.getCustomPath());
				}

				if (!customComponentSettings.getEnv().isEmpty()) {
					mergeEnvironmentVariables(component, customComponentSettings.getEnv());
				}

				if (customComponentSettings.getResources() != null) {
					mergeResources(component, customComponentSettings.getResources());
				}

				if (!customComponentSettings.getPersistentVolumes().isEmpty()) {
					mergePersistentVolumes(component, customComponentSettings.getPersistentVolumes());
				}
			}

		}

		mergeEnvironmentVariables(masterComponent, inferredMasterEnvironmentVariables);

		sortLists(tribefireRuntime);

		return tribefireRuntime;
	}

	static void sortLists(TribefireRuntime tribefireRuntime) {
		// ensure stable order for components and databases
		Collections.sort(tribefireRuntime.getSpec().getComponents(), Comparator.comparing(Component::getName));
		Collections.sort(tribefireRuntime.getSpec().getDatabases(), Comparator.comparing(Database::getName));
		tribefireRuntime.getSpec().getComponents().forEach(component -> {
			Collections.sort(component.getEnv(), Comparator.comparing(EnvironmentVariable::getName));
			Collections.sort(component.getPersistentVolumes(), Comparator.comparing(PersistentVolume::getName));
		});
	}

	static List<CustomComponentSettings> getMatchingCustomComponentsSettings(Component component,
			List<CustomComponentSettings> customComponentSettingsList) {
		return customComponentSettingsList.stream().filter(it -> (it.getName() != null && component.getName().equals(it.getName()))
				|| (it.getNameRegex() != null && component.getName().matches(it.getNameRegex()))).collect(Collectors.toList());
	}

	/**
	 * Adds the specified <code>environmentVariables</code>, possibly overriding existing variables (with same name). Afterwards sorts list of
	 * environment variables again.
	 */
	// it would be more efficient to just collect all variables in a map and later convert them to entities.
	private static void mergeEnvironmentVariables(Component component, Map<String, String> environmentVariables) {
		Map<String, String> existingEnvironmentVariables = component.getEnv().stream()
				.collect(Collectors.toMap(EnvironmentVariable::getName, EnvironmentVariable::getValue));
		Map<String, String> mergedEnvironmentVariables = new HashMap<>();
		// first we add old environment variables ...
		mergedEnvironmentVariables.putAll(existingEnvironmentVariables);
		// ... and then we add new ones, potentially overwriting old ones
		mergedEnvironmentVariables.putAll(environmentVariables);
		component.getEnv().clear();
		component.getEnv().addAll(toEnvironmentVariablesList(mergedEnvironmentVariables));
	}

	private static void mergePersistentVolumes(Component component, List<PersistentVolume> persistentVolumes) {
		for (PersistentVolume persistentVolume : persistentVolumes) {
			// remove existing persistent volumes with same name (if any)
			component.getPersistentVolumes().removeIf(it -> it.getName().equals(persistentVolume.getName()));
			component.getPersistentVolumes().add(persistentVolume);
		}
	}

	private static void mergeResources(Component component, Resources resources) {
		if (component.getResources() == null) {
			component.setResources(resources);
		} else {
			Resources existingResources = component.getResources();
			if (resources.getLimits() != null) {
				ResourcesSpecification existingResourcesSpecification = existingResources.getLimits();
				ResourcesSpecification resourcesSpecification = resources.getLimits();

				if (existingResourcesSpecification == null) {
					existingResources.setLimits(resourcesSpecification);
				} else {
					if (resourcesSpecification.getCpu() != null) {
						existingResourcesSpecification.setCpu(resourcesSpecification.getCpu());
					}
					if (resourcesSpecification.getMemory() != null) {
						existingResourcesSpecification.setMemory(resourcesSpecification.getMemory());
					}
				}
			}
			if (resources.getRequests() != null) {
				ResourcesSpecification existingResourcesSpecification = existingResources.getRequests();
				ResourcesSpecification resourcesSpecification = resources.getRequests();

				if (existingResourcesSpecification == null) {
					existingResources.setRequests(resourcesSpecification);
				} else {
					if (resourcesSpecification.getCpu() != null) {
						existingResourcesSpecification.setCpu(resourcesSpecification.getCpu());
					}
					if (resourcesSpecification.getMemory() != null) {
						existingResourcesSpecification.setMemory(resourcesSpecification.getMemory());
					}
				}
			}
		}
	}

	private static boolean isWebContextAssetMappedToComponent(PlatformAsset webContextAsset) {
		if (!(webContextAsset.getNature() instanceof WebContext)) {
			throw new IllegalArgumentException("Passed asset is not a web context asset: " + webContextAsset);
		}
		// tribefire-js is currently not mapped
		return !webContextAsset.getGlobalId().startsWith(TRIBEFIRE_JS_GLOBAL_ID_WITHOUT_VERSION);
	}

	static List<EnvironmentVariable> toEnvironmentVariablesList(Map<String, String> environmentVariablesMap) {
		List<EnvironmentVariable> result = new ArrayList<>();
		environmentVariablesMap.forEach((name, value) -> {
			EnvironmentVariable environmentVariable = EnvironmentVariable.T.create();
			environmentVariable.setName(name);
			environmentVariable.setValue(value);
			result.add(environmentVariable);
		});
		return result;
	}

	static void addEnvironmentVariable(List<EnvironmentVariable> target, String name, String value) {
		addEnvironmentVariables(target, MapTools.getStringMap(name, value));
	}

	static void addEnvironmentVariables(List<EnvironmentVariable> target, Map<String, String> environmentVariablesMap) {
		target.addAll(toEnvironmentVariablesList(environmentVariablesMap));
	}

}
