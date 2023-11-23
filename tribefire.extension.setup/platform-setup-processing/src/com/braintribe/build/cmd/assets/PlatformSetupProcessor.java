// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets;

import static com.braintribe.console.ConsoleOutputs.brightBlack;
import static com.braintribe.console.ConsoleOutputs.brightWhite;
import static com.braintribe.console.ConsoleOutputs.green;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.styled;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.console.ConsoleOutputs.yellow;
import static com.braintribe.console.ConsoleStyles.FG_YELLOW;
import static com.braintribe.exception.Exceptions.unchecked;
import static com.braintribe.setup.tools.TfSetupOutputs.fileName;
import static com.braintribe.setup.tools.TfSetupTools.writeYml;
import static com.braintribe.utils.ZipTools.unzip;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs.solution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.braintribe.build.cmd.assets.api.ArtifactResolutionContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.build.cmd.assets.api.PlatformAssetNatureBuilder;
import com.braintribe.build.cmd.assets.api.preprocessing.PlatformAssetPreprocessor;
import com.braintribe.build.cmd.assets.api.preprocessing.PlatformAssetPreprocessorContext;
import com.braintribe.build.cmd.assets.impl.BuildDockerImagesProcessor;
import com.braintribe.build.cmd.assets.impl.CreateBackupProcessor;
import com.braintribe.build.cmd.assets.impl.CreateTribefireRuntimeManifestProcessor;
import com.braintribe.build.cmd.assets.impl.DependencyPrinting;
import com.braintribe.build.cmd.assets.impl.DisjointCollector;
import com.braintribe.build.cmd.assets.impl.GetAssetsFromPackagedPlatformSetupProcessor;
import com.braintribe.build.cmd.assets.impl.IncrementRevisionsProcessor;
import com.braintribe.build.cmd.assets.impl.PackagedPlatformSetupBuilder;
import com.braintribe.build.cmd.assets.impl.PathReducer;
import com.braintribe.build.cmd.assets.impl.RestoreBackupProcessor;
import com.braintribe.build.cmd.assets.impl.SimplePlatformAssetResolutionContext;
import com.braintribe.build.cmd.assets.impl.UpdateGroupVersionProcessor;
import com.braintribe.build.cmd.assets.impl.check.process.CheckGroupProcessor;
import com.braintribe.build.cmd.assets.impl.check.process.CheckReport;
import com.braintribe.build.cmd.assets.impl.modules.DebugFolderMerger;
import com.braintribe.build.cmd.assets.impl.modules.ModuleFolderMerger;
import com.braintribe.build.cmd.assets.impl.views.backup.BackupArtifactsProcessor;
import com.braintribe.build.cmd.assets.impl.views.backup.RestoreArtifactsProcessor;
import com.braintribe.build.cmd.assets.impl.views.lockversions.GetLockedVersionsProcessor;
import com.braintribe.build.cmd.assets.impl.views.lockversions.LockVersionsProcessor;
import com.braintribe.build.cmd.assets.impl.views.setuprepositoryconfiguration.SetupRepositoryConfigurationProcessor;
import com.braintribe.build.cmd.assets.legacy.MavenInstallAssetTransfer;
import com.braintribe.build.cmd.assets.wire.artifact.ArtifactResolutionWireModule;
import com.braintribe.build.cmd.assets.wire.artifact.contract.ArtifactResolutionContract;
import com.braintribe.cartridge.common.processing.configuration.url.model.RegistryEntry;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.codec.string.DateCodec;
import com.braintribe.common.attribute.common.CallerEnvironment;
import com.braintribe.common.lcd.Pair;
import com.braintribe.console.ConsoleOutputs;
import com.braintribe.console.output.ConsoleOutputContainer;
import com.braintribe.devrock.env.api.DevEnvironment;
import com.braintribe.devrock.mc.api.resolver.ArtifactDataResolution;
import com.braintribe.devrock.mc.api.resolver.ArtifactPartResolver;
import com.braintribe.devrock.mc.api.resolver.DependencyResolver;
import com.braintribe.devrock.mc.core.commons.ArtifactResolutionUtil;
import com.braintribe.devrock.mc.core.declared.commons.HashComparators;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.DevelopmentEnvironmentContract;
import com.braintribe.devrock.mc.core.wirings.configuration.contract.RepositoryConfigurationContract;
import com.braintribe.devrock.mc.core.wirings.env.configuration.EnvironmentSensitiveConfigurationWireModule;
import com.braintribe.devrock.mc.core.wirings.venv.contract.VirtualEnvironmentContract;
import com.braintribe.devrock.model.repositoryview.resolution.RepositoryViewResolution;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Maybe;
import com.braintribe.gm.model.reason.essential.NotFound;
import com.braintribe.logging.Logger;
import com.braintribe.model.artifact.compiled.CompiledArtifactIdentification;
import com.braintribe.model.artifact.compiled.CompiledDependencyIdentification;
import com.braintribe.model.artifact.essential.PartIdentification;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.AssetAggregator;
import com.braintribe.model.asset.natures.LicensePriming;
import com.braintribe.model.asset.natures.ManipulationPriming;
import com.braintribe.model.asset.natures.MasterCartridge;
import com.braintribe.model.asset.natures.PlatformAssetNature;
import com.braintribe.model.asset.natures.TribefireWebPlatform;
import com.braintribe.model.asset.natures.WebContext;
import com.braintribe.model.asset.preprocessing.AssetPreprocessing;
import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.deployment.database.connector.GenericDatabaseConnectionDescriptor;
import com.braintribe.model.deployment.database.pool.ConfiguredDatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.deployment.database.pool.HikariCpConnectionPool;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.IsAssignableTo;
import com.braintribe.model.generic.typecondition.stringifier.TypeConditionStringifier;
import com.braintribe.model.messaging.expert.Messaging;
import com.braintribe.model.platform.setup.api.BackupArtifacts;
import com.braintribe.model.platform.setup.api.BuildDockerImages;
import com.braintribe.model.platform.setup.api.CheckGroup;
import com.braintribe.model.platform.setup.api.CreateBackup;
import com.braintribe.model.platform.setup.api.CreateProject;
import com.braintribe.model.platform.setup.api.CreateTribefireRuntimeManifest;
import com.braintribe.model.platform.setup.api.Encrypt;
import com.braintribe.model.platform.setup.api.FileSystemPlatformSetupConfig;
import com.braintribe.model.platform.setup.api.GetAssetDependencies;
import com.braintribe.model.platform.setup.api.GetAssetsFromPackagedPlatformSetup;
import com.braintribe.model.platform.setup.api.GetLockedVersions;
import com.braintribe.model.platform.setup.api.IncrementRevisions;
import com.braintribe.model.platform.setup.api.InstallLicense;
import com.braintribe.model.platform.setup.api.LockVersions;
import com.braintribe.model.platform.setup.api.PackagePlatformSetup;
import com.braintribe.model.platform.setup.api.PackagePlatformSetupAsZip;
import com.braintribe.model.platform.setup.api.PlatformSetupConfig;
import com.braintribe.model.platform.setup.api.PredefinedComponent;
import com.braintribe.model.platform.setup.api.ProjectDescriptor;
import com.braintribe.model.platform.setup.api.RestoreArtifacts;
import com.braintribe.model.platform.setup.api.RestoreBackup;
import com.braintribe.model.platform.setup.api.SetupDependencyConfig;
import com.braintribe.model.platform.setup.api.SetupInfo;
import com.braintribe.model.platform.setup.api.SetupLocalTomcatPlatform;
import com.braintribe.model.platform.setup.api.SetupLocalTomcatPlatformForDocker;
import com.braintribe.model.platform.setup.api.SetupRepositoryConfiguration;
import com.braintribe.model.platform.setup.api.SetupRequest;
import com.braintribe.model.platform.setup.api.UpdateGroupVersion;
import com.braintribe.model.platform.setup.api.data.SetupDescriptor;
import com.braintribe.model.platform.setup.info.RuntimeUpdateInfo;
import com.braintribe.model.platformsetup.PlatformSetup;
import com.braintribe.model.plugin.jdbc.JdbcPlugableDcsaSharedStorage;
import com.braintribe.model.processing.core.expert.api.MutableDenotationMap;
import com.braintribe.model.processing.core.expert.impl.PolymorphicDenotationMap;
import com.braintribe.model.processing.license.glf.LicenseTools;
import com.braintribe.model.processing.service.api.OutputConfig;
import com.braintribe.model.processing.service.api.OutputConfigAspect;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.setuppackage.PackagedPlatformAsset;
import com.braintribe.model.setuppackage.PackagedPlatformAssetsByNature;
import com.braintribe.model.setuppackage.PackagedPlatformSetup;
import com.braintribe.model.tomcat.platform.TfRestRealm;
import com.braintribe.model.tomcat.platform.TomcatServiceDescriptor;
import com.braintribe.model.version.Version;
import com.braintribe.provider.Holder;
import com.braintribe.setup.tools.TemplateVariableProvider;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.OsTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.UnixTools;
import com.braintribe.utils.date.ExtSimpleDateFormat;
import com.braintribe.utils.encryption.Cryptor;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.utils.paths.PathCollectors;
import com.braintribe.utils.paths.PathList;
import com.braintribe.utils.paths.UniversalPath;
import com.braintribe.utils.stream.ReferencingFileInputStream;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.model.MergeContext;
import com.braintribe.ve.api.VirtualEnvironment;
import com.braintribe.ve.impl.ContextualizedVirtualEnvironment;
import com.braintribe.ve.impl.OverridingEnvironment;
import com.braintribe.ve.impl.StandardEnvironment;
import com.braintribe.velocity.VelocityProjector;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.context.WireContextBuilder;
import com.braintribe.wire.api.module.WireModule;
import com.braintribe.wire.api.module.WireTerminalModule;

import tribefire.cortex.asset.resolving.ng.api.AssetDependencyResolver;
import tribefire.cortex.asset.resolving.ng.api.AssetResolutionContext;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolution;
import tribefire.cortex.asset.resolving.ng.api.PlatformAssetResolvingContext;
import tribefire.cortex.asset.resolving.ng.impl.PlatformAssetSolution;

public class PlatformSetupProcessor extends AbstractDispatchingServiceProcessor<SetupRequest, Object> implements InitializationAware, PlatformAssetDistributionConstants {

	private static final Logger logger = Logger.getLogger(PlatformSetupProcessor.class);

	private final MutableDenotationMap<PlatformAssetNature, List<String>> natureParts = new PolymorphicDenotationMap<>();
	private final MutableDenotationMap<AssetPreprocessing, PlatformAssetPreprocessor<?>> assetPreProcessors = new PolymorphicDenotationMap<>();
	private final MutableDenotationMap<PlatformAssetNature, PlatformAssetNatureBuilder<?>> experts = new PolymorphicDenotationMap<>();

	// Collectors which are not acquired by nature builders
	private List<Supplier<PlatformAssetCollector>> staticCollectors = Collections.emptyList();
	private VirtualEnvironment virtualEnvironment = new StandardEnvironment();
	
	private class McConfigWireModule implements WireTerminalModule<RepositoryConfigurationContract> {
		private final ServiceRequestContext context;

		public McConfigWireModule(ServiceRequestContext context) {
			super();
			this.context = context;
		}
		
		@Override
		public void configureContext(WireContextBuilder<?> contextBuilder) {
			File devEnvRoot = context.findAttribute(DevEnvironment.class).map(DevEnvironment::getRootPath).orElse(null);
			contextBuilder.bindContract(DevelopmentEnvironmentContract.class, () -> devEnvRoot);
			contextBuilder.bindContract(VirtualEnvironmentContract.class, () -> virtualEnvironment);
		}
		
		@Override
		public List<WireModule> dependencies() {
			return Collections.singletonList(EnvironmentSensitiveConfigurationWireModule.INSTANCE);
		}
	}

	public <P extends AssetPreprocessing, E extends PlatformAssetPreprocessor<P>> void registerAssetPreprocessor(EntityType<P> denotationType, E expert) {
		assetPreProcessors.put(denotationType, expert);
	}

	public <N extends PlatformAssetNature, E extends PlatformAssetNatureBuilder<N>> void registerExpert(EntityType<N> denotationType, E expert) {
		experts.put(denotationType, expert);
	}

	@Override
	public void postConstruct() {
		experts.entryStream().forEach(e -> {
			EntityType<? extends PlatformAssetNature> natureType = e.getKey();
			PlatformAssetNatureBuilder<?> expert = e.getValue();
			natureParts.put(natureType, expert.relevantParts());
		});
	}

	@Configurable
	public void setVirtualEnvironment(VirtualEnvironment virtualEnvironment) {
		this.virtualEnvironment = virtualEnvironment;
	}

	@Configurable
	public void setStaticCollectors(List<Supplier<PlatformAssetCollector>> staticCollectors) {
		this.staticCollectors = staticCollectors;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<SetupRequest, Object> dispatching) {
		// @formatter:off
		dispatching.register(PackagePlatformSetup.T,                 (c, r) -> packagePlatformSetup(c, r));
		dispatching.register(PackagePlatformSetupAsZip.T,            (c, r) -> packagePlatformSetupAsZip(c, r));
		dispatching.register(SetupLocalTomcatPlatform.T,             (c, r) -> setupLocalTomcatPlatform(c, r));
		dispatching.register(BuildDockerImages.T,                    (c, r) -> buildDockerImages(c, r));
		dispatching.register(LockVersions.T,                         (c, r) -> lockVersions(r));
		dispatching.register(CreateProject.T,                        (c, r) -> createProject(c, r));
		dispatching.register(CreateTribefireRuntimeManifest.T,       (c, r) -> createTribefireRuntimeManifest(r));
		dispatching.register(GetAssetsFromPackagedPlatformSetup.T,   (c, r) -> getAssetsFromPackagedPlatformSetup(c, r));
		dispatching.register(CreateBackup.T,                         (c, r) -> createBackup(r));
		dispatching.register(RestoreBackup.T,                        (c, r) -> restoreBackup(r));
		dispatching.register(Encrypt.T,                              (c, r) -> encrypt(r));
		dispatching.register(InstallLicense.T,                       (c, r) -> installLicense(c, r));
		dispatching.register(GetAssetDependencies.T,                 (c, r) -> getAssetDependencies(r));
		dispatching.register(SetupRepositoryConfiguration.T,         (c, r) -> setupRepositoryConfiguration(r));
		dispatching.register(GetLockedVersions.T,                    (c, r) -> getLockedVersions(r));
		dispatching.register(BackupArtifacts.T,                      (c, r) -> backupArtifacts(r));
		dispatching.register(RestoreArtifacts.T,                     (c, r) -> restoreArtifacts(r));
		dispatching.register(UpdateGroupVersion.T,                   (c, r) -> updateGroupVersion(r));
		dispatching.register(IncrementRevisions.T,                   (c, r) -> incrementRevisions(r));
		dispatching.register(CheckGroup.T,                           (c, r) -> checkGroup(r));
		// @formatter:on
	}
	
	private Object getAssetDependencies(GetAssetDependencies denotation) {
		SimplePlatformAssetResolutionContext context = new SimplePlatformAssetResolutionContext(virtualEnvironment, denotation);
		try (WireContext<ArtifactResolutionContract> arContext = Wire.context(new ArtifactResolutionWireModule(virtualEnvironment, false))) {
			resolveSetupDependency(context, arContext.contract(), denotation);
			return null;
		}
	}
	
	private Pair<RepositoryViewResolution, SortedSet<PlatformAssetSolution>> resolveSetupDependency(PlatformAssetResolvingContext context, ArtifactResolutionContext arContext,
			SetupDependencyConfig denotation) {

		String setupDependencyAsStr = denotation.getSetupDependency();

		@SuppressWarnings("deprecation")
		String terminalDependencyAsStr = denotation.getTerminalDependency();
		if (terminalDependencyAsStr != null)
			println("terminalDependency property is deprecated. Use project property instead.");
		
		String projectDependencyAsStr = denotation.getProject();
		if (projectDependencyAsStr == null)
			projectDependencyAsStr = terminalDependencyAsStr;
		
		if (projectDependencyAsStr == null && setupDependencyAsStr == null)
			throw new IllegalArgumentException("At least project or setupDependency must be set!");
		
		if (setupDependencyAsStr == null)
			setupDependencyAsStr = projectDependencyAsStr;
		
		CompiledDependencyIdentification setupDependency = CompiledDependencyIdentification.parseAndRangify(setupDependencyAsStr, true);
		
		CompiledDependencyIdentification projectDependency = setupDependency;
		if (projectDependencyAsStr != null) {
			projectDependency = CompiledDependencyIdentification.parseAndRangify(projectDependencyAsStr, true);
		}
		
		// TODO: check how the virtual environment finds its way to this place in gin and jinni
//		OverrideableVirtualEnvironment virtualEnvironment = new OverrideableVirtualEnvironment();
//		virtualEnvironment.setEnvironmentOverrides(denotation.getVirtualEnvironmentVariables());
//		virtualEnvironment.setPropertyOverrides(denotation.getVirtualSystemProperties());
		
		println(
				sequence(
						text("Resolving setup dependencies for:  "),
						brightBlack(setupDependency.getGroupId() + ":"),
						text(setupDependency.getArtifactId()),
						brightBlack("#"),
						green(setupDependency.getVersion().asString())
						)
				);
		
		List<CompiledDependencyIdentification> dependencies = new ArrayList<>();
		
		// TODO check twice
		if (projectDependency != null && !HashComparators.compiledDependencyIdentification.compare(projectDependency, setupDependency)) {
			dependencies.add(projectDependency);
		}

		dependencies.add(setupDependency);

		AssetResolutionContext assetDepContext = AssetResolutionContext.build() //
				.natureParts(natureParts) //
				.selectorFiltering(true) //
				.includeDocumentation(!denotation.getNoDocu()) //
				.designtime(context.isDesigntime()) //
				.runtime(context.isRuntime()) //
				.tags(context.getTags()) //
				.stage(context.getStage()) //
				.lenient(true) //
				.session(context.session()) //
				.done();

		AssetDependencyResolver assetDepResolver = arContext.assetDependencyResolver();
		
		PlatformAssetResolution paResolution = assetDepResolver.resolve(assetDepContext, dependencies);
		
		// TODO: PlatformAssetResolver should do something about reasoning
		if (paResolution.hasFailed()) {
			// print error tree
			println(text(""));

			ArtifactResolutionUtil.printFailedResolution(paResolution.artifactResolution());
			
			throw new IllegalStateException("Error while resolving. See problems in protocol output");
		}

		PlatformAssetSolution setupDependencySolution = paResolution.getSolutionFor(setupDependency);
		
		if (setupDependencySolution == null) {
			throw new IllegalArgumentException("setupDependency " + setupDependency.asString() + " is not an asset or was filtered due to asset resolution configuration (e.g. documentation exclusion).");
		}
		
		PlatformAsset setupAsset = setupDependencySolution.asset;
		PlatformAssetSolution projectSolution = paResolution.getSolutionFor(projectDependency);
		PlatformAsset projectAsset = projectSolution.asset;

		PlatformSetup setup = context.session().create(PlatformSetup.T);
		setup.setSetupAsset(setupAsset);
		setup.setTerminalAsset(projectAsset);
		setup.setId("asset:setup");
		setup.setGlobalId("asset:setup");

		// show dependency tree
		printAssetDependencyTree(context, paResolution);		
		return Pair.of(arContext.repositoryReflection().getRepositoryViewResolution(), paResolution.getSolutions());
	}

	private Neutral buildDockerImages(ServiceRequestContext requestContext, BuildDockerImages request) {
		
		/* TODO: either find a better way to ensure that we get an OverridingEnvironment or find another way to switch
		 * to MC offline mode inside BuildDockerImagesProcessor */
		VirtualEnvironment ve = virtualEnvironment;
		if (ve instanceof ContextualizedVirtualEnvironment) {
			ve = ContextualizedVirtualEnvironment.environment();
		}
		if (!(ve instanceof OverridingEnvironment)) {
			throw new IllegalStateException("Expected " + VirtualEnvironment.class.getSimpleName() + " to be an instance of "
					+ OverridingEnvironment.class.getSimpleName() + ", but instance " + ve + " of type "
					+ ve.getClass().getName() + " isn't! This is a bug which must be fixed in the code.");
		}

		BuildDockerImagesProcessor.process(requestContext, request, (OverridingEnvironment) ve);

		return Neutral.NEUTRAL;
	}
	
	private String updateGroupVersion(UpdateGroupVersion request) {
		return UpdateGroupVersionProcessor.process(request);
	}
	
	private Neutral incrementRevisions(IncrementRevisions request) {
		IncrementRevisionsProcessor.process(request);
		return Neutral.NEUTRAL;
	}
	
	private Neutral lockVersions(LockVersions request) {
		LockVersionsProcessor.process(request, virtualEnvironment);
		return Neutral.NEUTRAL;
	}
	
	private List<String> getAssetsFromPackagedPlatformSetup(@SuppressWarnings("unused") ServiceRequestContext requestContext,
			GetAssetsFromPackagedPlatformSetup request) {
		
		PackagedPlatformSetup packagedPlatformSetup = readPackagedPlatformSetupFromFile(request.getPackagedPlatformSetupFilePath());

		return GetAssetsFromPackagedPlatformSetupProcessor.process(request, packagedPlatformSetup);
	}
	
	private Neutral createBackup(CreateBackup request) {
		ConsoleOutputs.println(ConsoleOutputs.brightBlack(CreateBackupProcessor.process(request)));
		return Neutral.NEUTRAL;
	}
	
	private Neutral restoreBackup(RestoreBackup request) {
		ConsoleOutputs.println(ConsoleOutputs.brightBlack(RestoreBackupProcessor.process(request)));
		return Neutral.NEUTRAL;
	}

	private Neutral createTribefireRuntimeManifest(CreateTribefireRuntimeManifest request) {

		File targetFile = new File(request.getTargetFilePath());
		if (targetFile.exists() && !targetFile.isFile()) {
			throw new IllegalArgumentException("Configured target " + targetFile + " already exists and is not a file!");
		}

		PackagedPlatformSetup packagedPlatformSetup = readPackagedPlatformSetupFromFile(request.getPackagedPlatformSetupFilePath());

		String tribefireRuntimeManifest = CreateTribefireRuntimeManifestProcessor.process(request, packagedPlatformSetup);
		
		FileTools.writeStringToFile(targetFile, tribefireRuntimeManifest);

		println("Created tribefire runtime manifest: " + targetFile);
		return Neutral.NEUTRAL;
	}

	private SetupInfo setupLocalTomcatPlatform(ServiceRequestContext requestContext, SetupLocalTomcatPlatform denotation) {
		String denotationInstallationPath = denotation.getInstallationPath();
		if (denotationInstallationPath == null) {
			String project = denotation.getProject();
			if (project == null)
				throw new NullPointerException("'installationPath' must be set if 'project' is not specified.");

			denotationInstallationPath = CallerEnvironment.resolveRelativePath(replaceIllegalCharacters(project)).getPath();
			denotation.setInstallationPath(denotationInstallationPath);
		}

		// basic SSL checks
		validateSslParameters(denotation);
		
		SetupLocalTomcatContext context = new SetupLocalTomcatContext(denotation);
		
		if (denotation.getTomcatAuthenticationRealm() instanceof TfRestRealm) {
			TfRestRealm tfRestRealm = (TfRestRealm)denotation.getTomcatAuthenticationRealm();

			if (tfRestRealm.getTfsUrl() == null)
				tfRestRealm.setTfsUrl(context.getTribefireServicesUrl());
		}
		
		if (CommonTools.isEmpty(denotation.getPackageBaseDir()))
			denotation.setPackageBaseDir(denotation.getInstallationPath() + "/package");
		

		// ensure existence of directories & check write access
		ensureExistenceAndWriteAccessForDirs(denotation);
		

		SetupInfo setupInfo = SetupInfo.T.create();

		PackagePlatformSetup packagePlatformSetup = createPackagePlatformSetupRequestFrom(denotation);

		PackagedPlatformSetup packagedPlatformSetup = packagePlatformSetup.eval(requestContext).get();

		String packageBaseDirAsStr = packagePlatformSetup.getPackageBaseDir();
		File packageBaseDir = new File(packageBaseDirAsStr);

		setupInfo.setPackageBaseDir(packageBaseDir.getAbsolutePath());

		File installationPath = new File(denotation.getInstallationPath());

		installationPath.mkdirs();

		RuntimeUpdateInfo runtimeUpdateInfo = acquireRuntimeUpdateInfo(installationPath);

		File runtimeDir = setupTomcat(installationPath, runtimeUpdateInfo, packagedPlatformSetup);

		
		/* For Docker image building we use disjoint projection, but this is not supported yet by
		 * SetupLocalTomcatPlatform. We still want to use Jinni to set up the (base) Tomcat instance. Therefore we omit
		 * certain things such as assets. -> Idea is to get those parts of Tomcat which are shred between all Docker
		 * images with disjoint projection. */
		boolean omitContainerSpecificAssets = false;
		boolean omitRunSpecificValues = false;
		if (denotation instanceof SetupLocalTomcatPlatformForDocker) {
			omitContainerSpecificAssets = ((SetupLocalTomcatPlatformForDocker) denotation).getOmitContainerSpecificAssets();
			omitRunSpecificValues = ((SetupLocalTomcatPlatformForDocker) denotation).getOmitRunSpecificValues();
		}
		
		
		if (!omitContainerSpecificAssets) {
			// transfer storage
			setupStorage(denotation, packageBaseDir, installationPath);
		}

		// write predefined component configuration if given
		setupPredefinedComponentConfiguration(denotation, installationPath);

		// configure tomcat service descriptor
		configureTomcatServiceDescriptor(denotation);
		
		File sourceDocumentationDir = packageBaseDir.toPath().resolve(PROJECTION_NAME_MASTER).resolve("documentation").toFile();
		boolean documentationPackaged = sourceDocumentationDir.exists();
		
		if (!omitContainerSpecificAssets) {
			// write dynamic properties and merge with asset properties and write to conf/tribefire.properties
			setupRuntimeProperties(context, denotation, packagedPlatformSetup, packageBaseDir, installationPath, documentationPackaged);
		}
		
		// project templates (i.e. the templates in the tomcat-runtime-asset in projection folder)
		projectRuntimeTemplates(denotation, runtimeDir, installationPath);
		
		// transfer keystore file
		transferKeystoreFile(runtimeDir, denotation.getSslKeystoreFile());	

		// transfer additional libraries to tomcat's lib folder
		List<Resource> additionalLibraries = denotation.getAdditionalLibraries();
		if (!additionalLibraries.isEmpty())
			transferAdditionalLibraries(runtimeDir, additionalLibraries);		
		
		// set file permissions
		if (OsTools.isUnixSystem())
			setUnixFilePermissions(runtimeDir);

		createContextDirectory(runtimeDir);
		
		transferRewriteConfigFile(runtimeDir, denotation.getUrlRewriteConfigFile());

		setupProjectLogFileNamesIfPossible(runtimeDir, denotation.getProjectDescriptor());

		if (!omitContainerSpecificAssets) {
			setupWebApps(runtimeUpdateInfo, packageBaseDir, runtimeDir);
	
			setupModules(denotation, packageBaseDir, installationPath);
	
			setupPlugins(packageBaseDir, installationPath);
			
			setupDocumentation(runtimeDir, sourceDocumentationDir, documentationPackaged);
		}

		setupDebug(denotation, packageBaseDir, installationPath, runtimeDir);
		
		writeSetupInfo(installationPath, denotation, packagedPlatformSetup, packageBaseDir, omitRunSpecificValues);
		
		if (denotation.getDeletePackageBaseDir()) {
			try {
				FileTools.deleteRecursivelySymbolLinkAware(packageBaseDir);
			} catch (Exception e) {
				logger.error("Error while deleting package: " + packageBaseDir.getAbsolutePath(), e);
			}
		}

		writeUpdateInfo(installationPath, runtimeUpdateInfo);

		return setupInfo;
	}

	/**
	 * TODO As the tomcat-runtime asset will be injected via dependency in the near future, this code will be removed soon. 
	 * A respective nature builder will take care of this. 
	 */
	private File setupTomcat(File installationPath, RuntimeUpdateInfo runtimeUpdateInfo, PackagedPlatformSetup packagedPlatformSetup) {
		String setupMajorMinorVersion = getTribefireMajorMinorVersionFromSetup(packagedPlatformSetup);
		String tomcatRuntimeDependency = TOMCAT_ASSET_ID + "#" + setupMajorMinorVersion;
		
		return callWithArtifactResolution(artifactResolution -> {
			CompiledDependencyIdentification tomcatDependency = CompiledDependencyIdentification.parseAndRangify(tomcatRuntimeDependency, true);
			
			DependencyResolver dependencyResolver = artifactResolution.dependencyResolver();
			Maybe<CompiledArtifactIdentification> resolvedDependencyMaybe = dependencyResolver.resolveDependency(tomcatDependency);
			
			if (resolvedDependencyMaybe.isUnsatisfied()) {
				throw new NoSuchElementException("Tomcat runtime asset dependency could not be resolved: " + tomcatRuntimeDependency);
			}
			
			CompiledArtifactIdentification tomcatArtifact = resolvedDependencyMaybe.get();
			
			File runtimeDir = new File(installationPath, "runtime");

			println("\nTomcat:");
			// check if we are already with the latest version of the runtime asset
			if (isUpdateRequired(runtimeUpdateInfo, tomcatArtifact)) {
				updateTomcat(runtimeUpdateInfo, artifactResolution.dataResolver(), tomcatArtifact, runtimeDir, installationPath);
			}
			else {
				outSolution("    Tomcat runtime is up to date: ", tomcatArtifact);
			}
			return runtimeDir;

		}, false);
	}
	
	private String getTribefireMajorMinorVersionFromSetup(PackagedPlatformSetup packagedPlatformSetup) {
		PackagedPlatformAssetsByNature potentialNullPointerHelper = packagedPlatformSetup.getAssets().get(TribefireWebPlatform.T.getTypeSignature());
		Set<PackagedPlatformAsset> webPlatformAssets = potentialNullPointerHelper == null ? Collections.emptySet()
				: potentialNullPointerHelper.getAssets();
		
		if (webPlatformAssets.size() > 1) {
			throw new IllegalStateException(
					"Unexpectedly found multiple " + TribefireWebPlatform.T.getShortName() + " assets in setup: " + webPlatformAssets);
		}
		
		PlatformAsset asset = CollectionTools.getFirstElement(webPlatformAssets).getAsset();
		return asset.getVersion();
	}

	private void writeSetupInfo(File installationPath, SetupLocalTomcatPlatform denotation, PackagedPlatformSetup packagedPlatformSetup, File packageBaseDir, boolean omitRunSpecificValues) {
		UniversalPath setupInfoPath = UniversalPath.start(installationPath.getPath()).push(PlatformAssetDistributionConstants.FILENAME_SETUP_INFO_DIR);
		
		// copy packaged setup-info to installation setup-info
		File setupInfoSource = UniversalPath.start(packageBaseDir.getPath()).push(PROJECTION_NAME_MASTER).push(PlatformAssetDistributionConstants.FILENAME_SETUP_INFO_DIR).toFile();
		File setupInfoTarget = setupInfoPath.toFile();
		
		if (setupInfoSource.exists()) {
			if (setupInfoTarget.exists())
				FileTools.deleteRecursivelySymbolLinkAware(setupInfoTarget);
			
			outFileTransfer("\nCopying packaged setup-info", "", setupInfoSource.getAbsolutePath(), setupInfoTarget.getAbsolutePath());
			
			FileTools.copyRecursivelyAndKeepSymbolLinks(setupInfoSource, setupInfoTarget);
		}
		
		File setupInfoFile = setupInfoPath.push(PlatformAssetDistributionConstants.FILENAME_SETUP_DESCRIPTOR).toFile();
		outFile("\nWriting setup description to ", setupInfoFile.getAbsolutePath());
		
		SetupDescriptor desc = SetupDescriptor.T.create();
		
		if (!omitRunSpecificValues) {
			desc.setSetupDate(new Date());
			/* user name and password aren't necessarily run specific, but the idea is that we can run the same request
			 * also on a different host with a different user, thus we omit these too. */
			desc.setSetupBy(System.getProperty("user.name"));
			desc.setSetupHost(determineHostname());
		}
		
		desc.setProjectDescriptor(denotation.getProjectDescriptor());

		String setupDependencyAssetName = denotation.getSetupDependency();
		
		CompiledArtifactIdentification setupDependency = CompiledArtifactIdentification.parse(setupDependencyAssetName);
		
		// assets
		Set<String> assets = new HashSet<>();
		String primarySetupAsset = null;
		for (PackagedPlatformAssetsByNature p : packagedPlatformSetup.getAssets().values()) {
			
			// TODO Note! The license topic needs to be discussed from scratch, having the open source strategy in mind.
			if(p.getTypeSignature().equals(ManipulationPriming.T.getTypeSignature())) {
				Optional<PlatformAsset> licenseAssetOptional = p.getAssets()
						.stream().map(PackagedPlatformAsset::getAsset).filter(a -> a.getName().equals(TRIBEFIRE_LICENSE)).findFirst();
				
				if (licenseAssetOptional.isPresent()) {
					PlatformAsset license = licenseAssetOptional.get();
					desc.setLicenseAssetMajorMinorVersion(license.getVersion());
				}
			}
			
			for (PackagedPlatformAsset a : p.getAssets()) {
				PlatformAsset asset = a.getAsset();
				String assetName = asset.qualifiedRevisionedAssetName();
				if (setupDependency.getGroupId().equals(asset.getGroupId()) && setupDependency.getArtifactId().equals(asset.getName())) {
					primarySetupAsset = assetName;
					desc.setVersion(asset.getVersion() + '.' + asset.getResolvedRevision());
				}
				
				assets.add(assetName);
			}
		}
		
		// setup assets
		desc.setPrimarySetupDependency(setupDependencyAssetName);
		desc.setPrimarySetupAsset(primarySetupAsset);
		
		desc.getSetupDependencies().add(setupDependencyAssetName);
		desc.getSetupAssets().add(primarySetupAsset);
		
		desc.setAssets(assets.stream().sorted().collect(Collectors.toList())); 
		
		// write into file
		FileTools.write(setupInfoFile).usingWriter(writer -> writeYml(desc, writer));

		outFileTransfer("\nCopying ", "", PlatformAssetDistributionConstants.FILENAME_PACKAGED_PLATFORM_SETUP, setupInfoPath.toFilePath());

		File packagedPlatformSetupFile = new File(packageBaseDir, PlatformAssetDistributionConstants.FILENAME_PACKAGED_PLATFORM_SETUP);
		FileTools.copyFileOrDirectory(packagedPlatformSetupFile, setupInfoPath.push(PlatformAssetDistributionConstants.FILENAME_PACKAGED_PLATFORM_SETUP).toFile());
	}

	private void transferRewriteConfigFile(File runtimeDir, Resource urlRewriteConfigFile) {
		if (urlRewriteConfigFile == null)
			return;
		
		String path = UniversalPath.start(runtimeDir.getPath()) //
				.push("host").push("conf").push("Catalina").push("localhost").push(PlatformAssetDistributionConstants.FILENAME_REWRITECONFIG).toFilePath();
		
		transferResource(urlRewriteConfigFile, path, PlatformAssetDistributionConstants.FILENAME_REWRITECONFIG);
	}

	private void setupProjectLogFileNamesIfPossible(File runtimeDir, ProjectDescriptor project) {
		if (project == null || StringTools.isEmpty(project.getName()))
			return;

		File file = new File(runtimeDir, "host/conf/tribefire-services_logging.properties");
		String fileContent = FileTools.readStringFromFile(file);

		String original = "fileKey = tribefire-services";
		String replacement = "fileKey = " + project.getName();
		fileContent = fileContent.replace(original, replacement);

		FileTools.writeStringToFile(file, fileContent);
	}

	private void transferKeystoreFile(File runtimeDir, Resource keystoreFile) {
		if (keystoreFile == null)
			return;
		
		String path = UniversalPath.start(runtimeDir.getPath()) //
				.push("host").push("conf").push(PlatformAssetDistributionConstants.FILENAME_KEYSTORE).toFilePath();
		
		transferResource(keystoreFile, path, PlatformAssetDistributionConstants.FILENAME_KEYSTORE);
	}
	
	private void transferResource(Resource resource, String path, String fileName) {
		outFileTransfer("Transferring ", "", fileName, path);
		try(InputStream is = resource.openStream(); OutputStream os = new FileOutputStream(path)) {
			
			IOTools.transferBytes(is, os, IOTools.BUFFER_SUPPLIER_64K);
			
		} catch (IOException e) {
			throw new UncheckedIOException("Error while copying " + fileName + " file " + resource, e);
		}
	}

	/**
	 * Ensures that following directories are writable:
	 * <ul>
	 * <li>installationPath</li>
	 * <li>packagBaseDir</li>
	 * <li>directories defined via {@link SetupLocalTomcatPlatform#getCheckWriteAccessForDirs() checkWriteAccessForDirs}.</li>
	 * </ul>
	 * 
	 * <p>
	 * If {@link SetupLocalTomcatPlatform#getCheckWriteAccessForDirs() checkWriteAccessForDirs} contains a path referencing system
	 * property ${TRIBEFIRE_INSTALLATION_ROOT_DIR}, the property is resolved. <br />
	 * <i>No other system properties are supported!</i>
	 * 
	 * <p>
	 * If a directory does not exist, it is created.
	 * 
	 * @throws IllegalStateException
	 *             if any of the directories could not be created and/or are not writable as well as if a directory path
	 *             contains an unsupported system property reference.
	 * 
	 */
	private void ensureExistenceAndWriteAccessForDirs(SetupLocalTomcatPlatform denotation) {
		List<String> dirsToCheck = new ArrayList<>();
		String installationPathAsStr = denotation.getInstallationPath();
		
		dirsToCheck.add(installationPathAsStr);
		dirsToCheck.add(denotation.getPackageBaseDir());
		
		ensureInstallationRelative(installationPathAsStr, dirsToCheck, denotation.getLogFilesDir());
		ensureInstallationRelative(installationPathAsStr, dirsToCheck, denotation.getTempDir());
		
		
		List<String> checkWriteAccessForDirs = denotation.getCheckWriteAccessForDirs();
		
		// may contain directories containing unsupported system properties inside the path
		List<String> dirsInvalid = new ArrayList<>();
		for (String d : checkWriteAccessForDirs) {
			
			Template t = Template.parse(d);
			MergeContext mc = new MergeContext();
			TemplateVariableProvider vp = new TemplateVariableProvider(INSTALLATION_ROOT_DIR, installationPathAsStr);
			mc.setVariableProvider(vp);
			
			String merged = t.merge(mc);

			if (vp.getFailed())
				dirsInvalid.add(d);
			else
				dirsToCheck.add(merged);
		}
		
		
		// may contain directories which are not writable or could not be created
		List<String> dirsNotWriteable = new ArrayList<>();
		File installationPath = new File(installationPathAsStr);
		for (String dir : dirsToCheck) {
			
			File f = new File(dir);
			if (!f.isAbsolute() && f.getParent() != null && !f.getParent().equals(installationPathAsStr)) {
				f = new File(installationPath, dir);
			}
			
			if (!f.exists()) {
				boolean created = f.mkdirs();
				if (!created) {
					dirsNotWriteable.add(dir);
					continue;
				}
			}
			
			Path path = f.toPath();
			if (!(Files.isWritable(path) && Files.isDirectory(path))) {
				dirsNotWriteable.add(dir);
			}
		}

		
		StringBuilder sb = null;
		boolean failed = false;
		
		if (!dirsNotWriteable.isEmpty()) {
			failed = true;

			sb = new StringBuilder();
			sb.append("Missing write access. Check following directories for existence and permissions:\n"
					+ dirsNotWriteable.stream().map(d -> "  " + d).collect(Collectors.joining("\n")));
		}
		
		if (!dirsInvalid.isEmpty()) {
			if (sb == null)
				 sb = new StringBuilder();
			else
				sb.append("\n\n");
			
			failed = true;
			sb.append("Unsupported system property reference:\n" 
			+ dirsInvalid.stream().map(d -> "  " + d).collect(Collectors.joining("\n")));
		}
		
		if (failed)
			throw new IllegalStateException(sb.toString());
		
	}

	private void ensureInstallationRelative(String denotationInstallationPath, List<String> directoriesToCheck, String directory) {
		if (directory != null) {
			File dirFile = new File(directory);
			
			if (!dirFile.isAbsolute()) {
				directoriesToCheck.add(new File(denotationInstallationPath, directory).getAbsolutePath());
			}
		}
	}

	private void configureTomcatServiceDescriptor(SetupLocalTomcatPlatform denotation) {
		TomcatServiceDescriptor tomcatServiceDescriptor = denotation.getTomcatServiceDescriptor();
		ProjectDescriptor projectDescriptor = denotation.getProjectDescriptor();
		
		if (projectDescriptor != null && tomcatServiceDescriptor != null) {
			
			String serviceName = tomcatServiceDescriptor.getName();
			String serviceDisplayName = tomcatServiceDescriptor.getDisplayName();
			
			if (serviceName == null)
				tomcatServiceDescriptor.setName(projectDescriptor.getName());
			
			if (serviceDisplayName == null)
				tomcatServiceDescriptor.setDisplayName(projectDescriptor.getDisplayName());
			
		}
		
	}

	private void validateSslParameters(SetupLocalTomcatPlatform denotation) {
		if(denotation.getSslKeystoreFile() != null && denotation.getSslKeystorePassword() == null)
			throw new IllegalArgumentException(
					"In case 'sslKeystoreFile' is set, the 'sslKeystorePassword' must be set as well! If the 'sslKeystoreFile'"
							+ " was generated without a password, set 'sslKeystorePassword' to empty string.");

		if(denotation.getSslKeystorePassword() != null && denotation.getSslKeystoreFile() == null) {
			throw new IllegalArgumentException("In case 'sslKeystorePassword' is set, the 'sslKeystoreFile' must be set as well!");
		}
	}

	private void createContextDirectory(File runtimeDir) {
		String contextDirPath = PathList.create().push(runtimeDir.getPath()).push("host").push("conf").push("Catalina").push("localhost").toSlashPath();

		File contextDirFile = new File(contextDirPath);
		if (!contextDirFile.exists())
			contextDirFile.mkdirs();
	}

	private Neutral installLicense(ServiceRequestContext requestContext, InstallLicense request) {

		Resource license = request.getFile();
		LicenseTools.validateLicense(license::openStream, license);
		
		PlatformAsset project = PlatformAsset.T.create();
		project.setGroupId("tribefire.cortex.assets");
		project.setName(TRIBEFIRE_LICENSE);
		
		project.setVersion(request.getVersion());
		project.setResolvedRevision("1-pc");

		project.setNature(LicensePriming.T.create());
		project.setNatureDefinedAsPart(true); // TODO remove

		// install asset to local repository to make it available
		File baseFolder;
		try {
			baseFolder = Files.createTempDirectory("tmp-assets").toFile();
		} catch (IOException e) {
			throw new UncheckedIOException("Error while creating tmp directory for asset installation of project " + project.qualifiedRevisionedAssetName(), e);
		}

		String localRepoDir = localRepoDir(requestContext);

		println("Installing license asset " + project.qualifiedRevisionedAssetName() + " to local repo " + localRepoDir);

		MavenInstallAssetTransfer transfer = new MavenInstallAssetTransfer(project, requestContext, baseFolder, new File(localRepoDir), false);
		transfer.addStaticPart(license::openStream, "license:glf");
		transfer.transfer();


		return Neutral.NEUTRAL;
	}
	
	private String encrypt(Encrypt request) {
		String algorithm = request.getAlgorithm();
		String keyFactoryAlgorithm = request.getKeyFactoryAlgorithm();
		String secret = request.getSecret();
		int keyLength = request.getKeyLength();
		String value = request.getValue();

        try {
        	String encoded = Cryptor.encrypt(secret, algorithm, keyFactoryAlgorithm, keyLength, value);

			println(ConsoleOutputs.brightBlack(encoded));

			return encoded;

        } catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while encrypting value");
		}
	}

	private String replaceIllegalCharacters(String project) {
		return project.replace(':', '.').replace('#', '-');
	}

	private static void setUnixFilePermissions(File runtimeDir) {
		File binFolder = new File(new File(runtimeDir, "host"), "bin");
		outFile("Detected unix system. Applying file permissions to ", binFolder.getAbsolutePath());

		UnixTools.setDefaultShPermissions(binFolder);
	}

	private void writeUpdateInfo(File installationPath, RuntimeUpdateInfo runtimeUpdateInfo) {
		Path updateInfoFile = Paths.get(installationPath.getPath(), PlatformAssetDistributionConstants.FILENAME_UPDATE_DIR, PlatformAssetDistributionConstants.FILENAME_RUNTIME_UPDATE_INFO);

		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		FileTools.write(updateInfoFile).usingOutputStream( //
				os -> marshaller.marshall(os, runtimeUpdateInfo, prettiness(OutputPrettiness.high)));
	}

	private void updateTomcat(RuntimeUpdateInfo runtimeUpdateInfo, ArtifactPartResolver partResolver,
			CompiledArtifactIdentification tomcatSolution, File runtimeDir, File installationPath) {

		runtimeUpdateInfo.setGroupId(tomcatSolution.getGroupId());
		runtimeUpdateInfo.setArtifactId(tomcatSolution.getArtifactId());
		runtimeUpdateInfo.setVersion(tomcatSolution.getVersion().asString());

		outSolution("    Retrieving tomcat runtime ", tomcatSolution);

		Maybe<ArtifactDataResolution> zipPartMaybe = partResolver.resolvePart(tomcatSolution, PartIdentification.create("runtime", "zip"));
		
		if (zipPartMaybe.isUnsatisfiedBy(NotFound.T))
			throw new NoSuchElementException("Missing runtime:zip part for artifact: " + tomcatSolution.asString());
			
		ArtifactDataResolution zipPart = zipPartMaybe.get();

		Maybe<InputStream> streamMaybe = zipPart.openStream();
		

		try {
			if (streamMaybe.isUnsatisfiedBy(NotFound.T))
				throw new NoSuchElementException("Missing runtime:zip part for artifact: " + tomcatSolution.asString());
			
			try (InputStream is = streamMaybe.get()) {
				outSolution("    Updating tomcat runtime ", tomcatSolution);
				
				FileTools.deleteRecursivelySymbolLinkAware(runtimeDir);
				FileTools.ensureFolderExists(runtimeDir);
				
				Path updateDir = Paths.get(installationPath.getPath(), PlatformAssetDistributionConstants.FILENAME_UPDATE_DIR);
				Files.createDirectories(updateDir);
	
				Path projectionDir = updateDir.resolve(PlatformAssetDistributionConstants.DIRNAME_PROJECTION);
	
				if (Files.exists(projectionDir))
					FileTools.deleteDirectoryRecursively(projectionDir.toFile());
	
				String splitPath = PlatformAssetDistributionConstants.DIRNAME_PROJECTION + "/";
	
				
				unzip(is, runtimeDir, //
						pathName -> pathName.startsWith(splitPath) ? updateDir.resolve(pathName).toFile() : null //
				);
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while transferring tomcat files to installationPath: " + installationPath.getAbsolutePath());
		}

	}

	private void projectRuntimeTemplates(SetupLocalTomcatPlatform denotation, File runtimeDir, File installationPath) {
		println("\nProjecting runtime templates");
		Path sourceFolder = Paths.get(installationPath.getPath(), PlatformAssetDistributionConstants.FILENAME_UPDATE_DIR, PlatformAssetDistributionConstants.DIRNAME_PROJECTION);

		if (!Files.exists(sourceFolder))
			return;

		VelocityProjector velocityProjector = new VelocityProjector(sourceFolder.toFile(), runtimeDir, denotation);
		velocityProjector.run();
		
	}

	private void transferAdditionalLibraries(File runtimeDir, List<Resource> additionalLibraries) {
		println("\nTransferring additional libraries");
		
		UniversalPath path = UniversalPath.start(runtimeDir.getPath()).push("host").push("lib");
		
		for(Resource library : additionalLibraries) {
			
			String name = library.getName();
			if (name == null) {
				if (library instanceof FileResource) {
					File file = new File(((FileResource) library).getPath());
					name = file.getName();
				} else {
					throw new IllegalStateException("Name must be set on resource entity " + library);
				}
				
			}
			
			String targetFilePath = path.push(name).toFilePath();
			try(InputStream is = library.openStream(); OutputStream os = new FileOutputStream(targetFilePath)) {
				
				outFileTransfer("Transferring ", "", name, path.toFilePath());
				IOTools.transferBytes(is, os, IOTools.BUFFER_SUPPLIER_64K);
				
			} catch (IOException e) {
				throw new UncheckedIOException("Error while copying library file " + library, e);
			}
		}
	}
	
	private void setupPredefinedComponentConfiguration(SetupLocalTomcatPlatform denotation, File installationPath) {
		List<RegistryEntry> registryEntries = new ArrayList<>();
		List<RegistryEntry> sharedRegistryEntries = new ArrayList<>();
		
		Map<PredefinedComponent, GenericEntity> predefinedComponents = denotation.getPredefinedComponents();
		
		for (Map.Entry<PredefinedComponent, GenericEntity> entry: predefinedComponents.entrySet()) {
			PredefinedComponent component = entry.getKey();
			RegistryEntry registryEntry = RegistryEntry.T.create();
			registryEntry.setBindId(component.getBindId());
			registryEntry.setDenotation(deriveDenotation(component, entry.getValue(), denotation, false));
			
			registryEntries.add(registryEntry);
		}
		
		DatabaseConnectionPool defaultDbConnection = (DatabaseConnectionPool) predefinedComponents.get(PredefinedComponent.DEFAULT_DB);
		
		if (defaultDbConnection != null) {
			for (PredefinedComponent use: PredefinedComponent.values()) {
				if (!predefinedComponents.containsKey(use)) {
					GenericEntity derivedDenotation = deriveDenotation(use, defaultDbConnection, denotation, true);
					
					if (derivedDenotation != null) {
						RegistryEntry registryEntry = RegistryEntry.T.create();
						registryEntry.setBindId(use.getBindId());
						registryEntry.setDenotation(derivedDenotation);
						registryEntries.add(registryEntry);
					}
				}
			}
		}
		
		int seq = 1;
		for (GenericEntity customComponent: denotation.getCustomComponents()) {
			RegistryEntry registryEntry = RegistryEntry.T.create();
			registryEntry.setBindId("custom-component-" + seq++ );
			registryEntry.setDenotation(customComponent);
			registryEntries.add(registryEntry);
		}
		
		// split messaging registry entries in shared configuration
		Iterator<RegistryEntry> it = registryEntries.iterator();
		
		while (it.hasNext()) {
			RegistryEntry entry = it.next();
			GenericEntity candidate = entry.getDenotation();
			
			if (candidate instanceof Messaging) {
				sharedRegistryEntries.add(entry);
				it.remove();
			}
		}
		
		// write the shared and tfs exclusive configuration json
		writeConfigurationJson(installationPath, FILE_CONFIGURATION_SHARED_JSON, sharedRegistryEntries);
		writeConfigurationJson(installationPath, FILE_CONFIGURATION_JSON, registryEntries);
	}

	private void writeConfigurationJson(File installationPath, String fileName, List<RegistryEntry> registryEntries) {
		if (!registryEntries.isEmpty()) {
			Path path = installationPath.toPath().resolve("conf").resolve(fileName);
			
			println("\nBinding predefined components for " + registryEntries.stream().map(RegistryEntry::getBindId).collect(Collectors.joining(", ")) + " in " + path.toString());

			try {
				Files.createDirectories(path.getParent());
				
				JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
				try (OutputStream out = Files.newOutputStream(path)) {
					marshaller.marshall(out, registryEntries, prettiness(OutputPrettiness.mid));
				}
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private GenericEntity deriveDenotation(PredefinedComponent use, GenericEntity denotation, SetupLocalTomcatPlatform request, boolean defaulting) {
		
		switch (use) {
			case MQ:
			case ADMIN_USER:
				return defaulting? null: denotation;
				
			case DCSA_SHARED_STORAGE: {
				JdbcPlugableDcsaSharedStorage sharedStorage = null;
				
				if (denotation instanceof JdbcPlugableDcsaSharedStorage) {
					sharedStorage = (JdbcPlugableDcsaSharedStorage)denotation;

				} else if (denotation instanceof HikariCpConnectionPool) {
					sharedStorage = JdbcPlugableDcsaSharedStorage.T.create();
					sharedStorage.setDatabaseConnectionPool((HikariCpConnectionPool)denotation);

				} else {
					
					if (denotation instanceof ConfiguredDatabaseConnectionPool) {
						ConfiguredDatabaseConnectionPool configuredDatabaseConnectionPool = (ConfiguredDatabaseConnectionPool)denotation;
						denotation = configuredDatabaseConnectionPool.getConnectionDescriptor();
					}
					
					if (denotation instanceof GenericDatabaseConnectionDescriptor) {
						GenericDatabaseConnectionDescriptor genericDatabaseConnectionDescriptor = (GenericDatabaseConnectionDescriptor)denotation;
						sharedStorage = JdbcPlugableDcsaSharedStorage.T.create();
						sharedStorage.setDriver(genericDatabaseConnectionDescriptor.getDriver());
						sharedStorage.setUsername(genericDatabaseConnectionDescriptor.getUser());
						sharedStorage.setPassword(genericDatabaseConnectionDescriptor.getPassword());
						sharedStorage.setUrl(genericDatabaseConnectionDescriptor.getUrl());
						
					}
				}
				
				if (sharedStorage == null) {
					throw new IllegalStateException("Unsupported denotation type for " + PredefinedComponent.DCSA_SHARED_STORAGE + ": " + denotation);
				}
				
				
				if (StringTools.isEmpty(sharedStorage.getProject())) {
					ProjectDescriptor projectDescriptor = request.getProjectDescriptor();
					if (projectDescriptor != null)
						sharedStorage.setProject(projectDescriptor.getName());
					else
						sharedStorage.setProject("tribefire");
				}
				
				return sharedStorage;
			}
			
			default:
				return denotation;
		}
	}
	
	private static class SetupLocalTomcatContext {
		private String tribefireServicesUrl;
		private String localBaseUrl;
		
		public SetupLocalTomcatContext(SetupLocalTomcatPlatform request) {
			if (request.getEnforceHttps())
				this.localBaseUrl = "https://localhost:" + request.getHttpsPort();
			else
				this.localBaseUrl = "http://localhost:" + request.getHttpPort();
			
			
			String configuredServicesUrl = request.getRuntimeProperties().get("TRIBEFIRE_SERVICES_URL");
			
			if (configuredServicesUrl != null) {
				this.tribefireServicesUrl = configuredServicesUrl;
			} else {
				this.tribefireServicesUrl = localBaseUrl + "/" + PlatformAssetDistributionConstants.TRIBEFIRE_SERVICES;
			}
		}
		
		public String getLocalBaseUrl() {
			return localBaseUrl;
		}
		
		public String getTribefireServicesUrl() {
			return tribefireServicesUrl;
		}
		
	}

	
	private void setupRuntimeProperties(SetupLocalTomcatContext context, SetupLocalTomcatPlatform denotation,
			PackagedPlatformSetup packagedPlatformSetup, File packageBaseDir, File installationPath, boolean documentationPackaged) {

		String cartridgesUriPrimingRelativePath = PathList.create().push("csa-priming").push("cortex").push(PlatformAssetDistributionConstants.CARTRIDGES_URI_PRIMING_FOLDER).push("data.man").toSlashPath();

		Properties dynamicProperties = new Properties() {
			private static final long serialVersionUID = 1L;

			/* Interceptor code to block assignment of calculated properties in case they where explicitly given by the request */
			@Override
			public synchronized Object setProperty(String key, String value) {
				if (denotation.getRuntimeProperties().containsKey(key))
					return null;
				else 
					return super.setProperty(key, value);
			}
		};

		// set tribefire-services specific runtime properties
		dynamicProperties.setProperty("TRIBEFIRE_SERVICES_URL", context.getTribefireServicesUrl());
		dynamicProperties.setProperty("TRIBEFIRE_PUBLIC_SERVICES_URL", "/" + PlatformAssetDistributionConstants.TRIBEFIRE_SERVICES);
		dynamicProperties.setProperty(PlatformAssetDistributionConstants.TRIBEFIRE_LOCAL_BASE_URL, context.getLocalBaseUrl());

		dynamicProperties.setProperty("TRIBEFIRE_CONFIGURATION_DIR", "${TRIBEFIRE_INSTALLATION_ROOT_DIR}/conf");
		dynamicProperties.setProperty("TRIBEFIRE_STORAGE_DIR", "${TRIBEFIRE_INSTALLATION_ROOT_DIR}/storage");
		dynamicProperties.setProperty("TRIBEFIRE_CACHE_DIR", "${TRIBEFIRE_STORAGE_DIR}/cache");
		dynamicProperties.setProperty("TRIBEFIRE_REPO_DIR", "${TRIBEFIRE_STORAGE_DIR}/repository");

		if (documentationPackaged)
			dynamicProperties.setProperty("TRIBEFIRE_DOCUMENTATION_URL", "/documentation/index.html");
		
		String tempDir = denotation.getTempDir();
		if (tempDir != null) {
			if (!new File(tempDir).isAbsolute())
				tempDir = "${TRIBEFIRE_INSTALLATION_ROOT_DIR}/" + tempDir;
			
			dynamicProperties.setProperty("TRIBEFIRE_TMP_DIR", tempDir);
		}
		else {
			dynamicProperties.setProperty("TRIBEFIRE_TMP_DIR", "${TRIBEFIRE_STORAGE_DIR}/tmp");
		}
		
		
		dynamicProperties.setProperty("TRIBEFIRE_DATA_DIR", "${TRIBEFIRE_STORAGE_DIR}/databases");
		dynamicProperties.setProperty("TRIBEFIRE_PLUGINS_DIR", "${TRIBEFIRE_INSTALLATION_ROOT_DIR}/plugins");
		dynamicProperties.setProperty("TRIBEFIRE_MANIPULATION_PRIMING", cartridgesUriPrimingRelativePath);

		// Platform Assets
		dynamicProperties.setProperty("TRIBEFIRE_PLATFORM_SETUP_SUPPORT", "true");
		dynamicProperties.setProperty("TRIBEFIRE_KEEP_TRANSFERRED_ASSET_DATA", "false");

		if (denotation.getAcceptSslCertificates()) {
			dynamicProperties.setProperty("TRIBEFIRE_ACCEPT_SSL_CERTIFICATES", String.valueOf(true));
		}
		
		
		// tenant id and project name
		ProjectDescriptor projectDescriptor = denotation.getProjectDescriptor();
		if (projectDescriptor != null) {
			dynamicProperties.setProperty("TRIBEFIRE_TENANT_ID", projectDescriptor.getName());
			dynamicProperties.setProperty("TRIBEFIRE_PROJECT_NAME", projectDescriptor.getName());
		} else {
			dynamicProperties.setProperty("TRIBEFIRE_PROJECT_NAME", "tf");
		}
		
		// node id
		dynamicProperties.setProperty("TRIBEFIRE_NODE_ID", "${TRIBEFIRE_PROJECT_NAME}@${TRIBEFIRE_HOSTNAME}#${TRIBEFIRE_JVM_UUID}");

		
		File confDir = new File(installationPath, "conf");
		confDir.mkdirs();

		writeDcsaSharedStorageMaybe(confDir, denotation);

		File cartridgesUriPrimingFile = new File(confDir, cartridgesUriPrimingRelativePath);
		cartridgesUriPrimingFile.getParentFile().mkdirs();

		for (PlatformAsset asset : getPlatformAssetsByNature(packagedPlatformSetup, WebContext.T)) {
			if (!(asset.getNature() instanceof MasterCartridge)) {
				String name = TfSetupTools.natureSensitiveAssetName(asset);

				String envVarName = name.replaceAll("[.-]", "_").toUpperCase() + "_URL";
				dynamicProperties.setProperty(envVarName, "/" + name);
				
				if (envVarName.equals("TRIBEFIRE_WEB_READER_URL")) {
					// web reader URLs have been used inconsistently in the past.
					// as a work-around we create two properties.
					envVarName = "TRIBEFIRE_WEBREADER_URL";
					dynamicProperties.setProperty(envVarName, "/" + name);
				}
			}
		}

		// concat dynamic properties with master projection properties and project to conf/tribefire.properties
		File propertiesFile = new File(confDir, PlatformAssetDistributionConstants.FILENAME_TRIBEFIRE_PROPERTIES);
		File masterPropertiesFile = new File(packageBaseDir, PathList.create().push(PROJECTION_NAME_MASTER).push(FOLDER_ENVIRONMENT).push(FILE_TRIBEFIRE_PROPERTIES).toFilePath());

		try (OutputStream out = new FileOutputStream(propertiesFile)) {
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			try {
				dynamicProperties.store(writer, "local tomcat setup dynamic properties.\n"
						+ "Please note that property " + PlatformAssetDistributionConstants.TRIBEFIRE_LOCAL_BASE_URL
						+ " will be removed soon, but is currently still used for cartridge synchronization!");
			}
			finally {
				writer.flush();
			}

			if (masterPropertiesFile.exists()) {
				try (InputStream in = new FileInputStream(masterPropertiesFile)) {
					IOTools.transferBytes(in, out, IOTools.BUFFER_SUPPLIER_8K);
				}
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}

	}

	private void writeDcsaSharedStorageMaybe(File confDir, SetupLocalTomcatPlatform denotation) {
		DcsaSharedStorage dss = denotation.getDcsaSharedStorage();
		if (dss == null)
			return;
		
		File dcsaSsFile = new File(confDir, PlatformAssetDistributionConstants.FILENAME_DEFAULT_DCSA_SS);

		FileTools.write(dcsaSsFile).usingWriter(w -> TfSetupTools.writeYml(dss, w));
	}

	private static PackagedPlatformSetup readPackagedPlatformSetupFromFile(String packagedPlatformSetupFilePath) {
		File ppsFile = new File(packagedPlatformSetupFilePath);
		if (!ppsFile.exists())
			throw new IllegalArgumentException("Configured packaged platform setup " + ppsFile + " doesn't exist!");

		if (!ppsFile.isFile())
			throw new IllegalArgumentException("Configured packaged platform setup " + ppsFile + " is not a file!");		
		
		return (PackagedPlatformSetup) FileTools.read(ppsFile).fromInputStream(new JsonStreamMarshaller()::unmarshall);
	}

	private Set<PlatformAsset> getPlatformAssetsByNature(PackagedPlatformSetup packagedPlatformSetup, EntityType<? extends PlatformAssetNature> nature) {
		return getPlatformAssetsByNature(packagedPlatformSetup, nature.getTypeSignature());
	}
	
	public static Set<PlatformAsset> getPlatformAssetsByNature(PackagedPlatformSetup packagedPlatformSetup, String nature) {
		PackagedPlatformAssetsByNature assetsByNature = packagedPlatformSetup.getAssets().get(nature);

		if (assetsByNature == null)
			return Collections.EMPTY_SET;

		return assetsByNature.getAssets().stream() //
				.map(PackagedPlatformAsset::getAsset) //
				.collect(Collectors.toSet());
	}
	
	private void setupStorage(SetupLocalTomcatPlatform denotation, File packageBaseDir, File installationPath) {
		File storageFolder = new File(installationPath, "storage");

		println("\nStorage");
		outFile("    Setting up storage ", storageFolder.getAbsolutePath());

		// clean existing storage?
		if (storageFolder.exists()) {
			// backup existing storage
			if (!denotation.getBackupStorage()) {
				FileTools.deleteDirectoryRecursivelyUnchecked(storageFolder);

			} else {
				File storageBackupFolder = new File(storageFolder.getParentFile(), storageFolder.getName() + "-backup");

				outFile("    Deleting previous storage backup at ", storageBackupFolder.getAbsolutePath());
				FileTools.deleteDirectoryRecursivelyUnchecked(storageBackupFolder);

				outFile("    Taking backup of previous storage to ", storageBackupFolder.getAbsolutePath());
				storageFolder.renameTo(storageBackupFolder);
			}
		}

		File packagedStorage = PathList.create().push(packageBaseDir.getAbsolutePath()).push(PROJECTION_NAME_MASTER).push("storage").toFile();

		if (packagedStorage.exists()) {
			outFileTransfer("    Transfering storage ", "", packagedStorage.getAbsolutePath(), storageFolder.getAbsolutePath());
			FileTools.copyDirectoryUnchecked(packagedStorage, storageFolder);
		}
	}

	private void setupPlugins(File packageBaseDir, File installationPath) {
		setupExtensions(packageBaseDir, installationPath, "plugins");
	}

	private void setupModules(SetupLocalTomcatPlatform request, File packageBaseDir, File installationPath) {
		File target = new File(installationPath, "modules");
		File source = extensionFile(packageBaseDir, "modules");
		
		if (StringTools.isEmpty(request.getDebugProject()) || !source.exists() || !target.exists())
			setupExtensions(packageBaseDir, installationPath, "modules");
		else
			ModuleFolderMerger.merge(source, target);
	}

	private void setupExtensions(File packageBaseDir, File installationPath, String pluginsOrModules) {
		// plugins -> plugin, modules -> module
		String pluginOrModule = StringTools.removeLastNCharacters(pluginsOrModules, 1);

		// cleanup an older incarnation of the plugin folder if it exists
		File extensionsFolder = new File(installationPath, pluginsOrModules);
		FileTools.deleteIfExists(extensionsFolder);

		// transfer the plugins from package to the tomcat setup
		Holder<Boolean> projectedExtensions = new Holder<>(false);

		Path defaultExtensionsPath = extensionPath(packageBaseDir, pluginsOrModules);

		outFileTransfer("\nCopying " + pluginsOrModules, "", defaultExtensionsPath.toString(), extensionsFolder.getAbsolutePath());

		if (Files.exists(defaultExtensionsPath)) {
			try (Stream<Path> s = listFiles(defaultExtensionsPath)) {
				s //
						.peek(p -> projectedExtensions.accept(true)) //
						.map(Path::toFile) //
						.forEach(source -> setupExtension(source, extensionsFolder));
			}
		}

		if (!projectedExtensions.get())
			println("    No " + pluginOrModule + " found to be set up.");
	}

	private File extensionFile(File packageBaseDir, String pluginsOrModules) {
		return extensionPath(packageBaseDir, pluginsOrModules).toFile();
	}

	private Path extensionPath(File packageBaseDir, String pluginsOrModules) {
		return packageBaseDir.toPath().resolve(PROJECTION_NAME_MASTER).resolve(pluginsOrModules);
	}

	private void setupExtension(File source, File pluginOrModuleFolder) {
		outFile("    Copying: ", source.getName());

		File target = new File(pluginOrModuleFolder, source.getName());

		FileTools.copyFileOrDirectory(source, target);
	}

	private void setupDebug(SetupLocalTomcatPlatform denotation, File packageBaseDir, File installationPath, File runtimeDir) {
		File sourceDebugDir = packageBaseDir.toPath().resolve(PROJECTION_NAME_MASTER).resolve("debug").toFile();
		if (!sourceDebugDir.exists())
			return;

		File targetDebugDir = new File(installationPath, "debug");

		DebugFolderMerger.merge(sourceDebugDir, targetDebugDir);

		prepareTomcatContexFile(sourceDebugDir, runtimeDir);

		File reSetupFile = new File(targetDebugDir, "setup.yml");
		if (!reSetupFile.exists())
			FileTools.write(reSetupFile).usingOutputStream(os -> new YamlMarshaller().marshall(os, denotation));
	}

	private void prepareTomcatContexFile(File sourceDebugDir, File runtimeDir) {
		File contextFile = runtimeDir.toPath().resolve("host/conf/Catalina/localhost/tribefire-services.xml").toFile();
		if (contextFile.exists())
			return;

		String debugProjectFolderName = getDebugDirName(sourceDebugDir);
		FileTools.write(contextFile).lines(tomcatContextFileContent(debugProjectFolderName));
	}

	private String getDebugDirName(File dir) {
		File[] dirs = dir.listFiles(File::isDirectory);
		if (dirs.length == 1)
			return dirs[0].getName();
		else
			throw new IllegalStateException("Exactly one folder is expected in the debug folder ' " + dir.getAbsolutePath()
					+ ", but these were found: " + Arrays.toString(dirs));
	}

	private List<String> tomcatContextFileContent(String debugProjectFolderName) {
		return asList(//
				"<Context reloadable=\"true\" docBase=\"DEBUG_PROJECT/context\" workDir=\"DEBUG_PROJECT/work\">"
						.replace("DEBUG_PROJECT", "${catalina.base}/../../debug/" + debugProjectFolderName),
				"<!-- Extra info begin -->", //
				"<JarScanner scanClassPath=\"false\" scanManifest=\"false\"/>", //
				"<!-- Extra info end -->", //
				// this causes warning "No rules found matching [Context/Logger]" (see also COREPA-458)
				//"\t<Logger className=\"org.apache.catalina.logger.SystemOutLogger\" verbosity=\"4\" timestamp=\"true\"/>", //
				"\t<Loader className=\"org.apache.catalina.loader.DevLoader\" reloadable=\"true\"/>", //
				"</Context>" //
		);
	}

	private void setupDocumentation(File runtimeDir, File sourceDocumentationDir, boolean documentationPackaged) {
		File targetDocumentationDir = UniversalPath.from(runtimeDir).push("host").push("webapps").push("documentation").toFile();
		FileTools.deleteIfExists(targetDocumentationDir);
		
		if (!documentationPackaged) {
			println("\nNo documentation found to be set up.");
			return;
		}
		
		println("\nCopying documentation files.");
		FileTools.copyFileOrDirectory(sourceDocumentationDir, targetDocumentationDir);
	}
	
	private void setupWebApps(RuntimeUpdateInfo updateRuntimeInfo, File packageBaseDir, File runtimeDir) {
		println("\nWebapps:");

		File runtimeWebappsDir = PathList.create().push(runtimeDir.getPath()).push("host").push("webapps").toFile();

		// delete webapps from older setup
		for (String webappToBeDeleted: updateRuntimeInfo.getAssetWebApps()) {
			File file = new File(runtimeWebappsDir, webappToBeDeleted);
			if (file.exists())
				FileTools.deleteRecursivelySymbolLinkAware(file);
		}
		
		List<String> assetWebApps = Collections.emptyList();

		// copy and memorize asset web apps
		Path defaultWebappsPath = packageBaseDir.toPath().resolve(PROJECTION_NAME_MASTER).resolve("webapps");

		if (Files.exists(defaultWebappsPath))
			try (Stream<Path> s = listFiles(defaultWebappsPath)) {
				assetWebApps = s //
						.map(Path::toFile) //
						.map(packageWebappFile -> extractOrCopyPackageWebappFileToRuntimeWebappDir(packageWebappFile, runtimeWebappsDir)) //
						.collect(Collectors.toList());
			}

		if (assetWebApps.isEmpty())
			println("    No webapps found to be set up.");

		// This is not used for anything !!!
		updateRuntimeInfo.setAssetWebApps(assetWebApps);
	}

	private static Stream<Path> listFiles(Path path) {
		try {
			return Files.list(path);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private String extractOrCopyPackageWebappFileToRuntimeWebappDir(File packageWebappFile, File runtimeWebappsDir) {
		String assetWebApp = FileTools.getNameWithoutExtension(packageWebappFile.getName());
		File target = new File(runtimeWebappsDir, assetWebApp);

		if (packageWebappFile.isDirectory())
			copyWebapp(packageWebappFile, target);
		else
			unzipWebapp(packageWebappFile, target);

		return assetWebApp;
	}

	private void copyWebapp(File packageWebappFile, File target) {
		FileTools.ensureFolderExists(target);
		FileTools.copyRecursivelyAndKeepSymbolLinks(packageWebappFile, target);
		
	}

	private void unzipWebapp(File packageWebappFile, File target) {
		outFileTransfer("Extracting webapp ", packageWebappFile.getName(), packageWebappFile.getParentFile().getAbsolutePath(),
				target.getParentFile().getAbsolutePath());
		unzip(packageWebappFile, target);
	}

	private PackagePlatformSetup createPackagePlatformSetupRequestFrom(FileSystemPlatformSetupConfig prototype) {
		PackagePlatformSetup setup = PackagePlatformSetup.T.create();

		for (Property property: FileSystemPlatformSetupConfig.T.getProperties()) {
			property.set(setup, property.get(prototype));
		}

		return setup;
	}

	private RuntimeUpdateInfo acquireRuntimeUpdateInfo(File installationPath) {
		Path updateInfoFile = Paths.get(installationPath.getPath(), PlatformAssetDistributionConstants.FILENAME_UPDATE_DIR, PlatformAssetDistributionConstants.FILENAME_RUNTIME_UPDATE_INFO);

		if (Files.exists(updateInfoFile)) {
			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
			try (InputStream in = Files.newInputStream(updateInfoFile)) {
				RuntimeUpdateInfo updateInfo = (RuntimeUpdateInfo) marshaller.unmarshall(in);
				return updateInfo;
			}
			catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while reading " + PlatformAssetDistributionConstants.FILENAME_RUNTIME_UPDATE_INFO + " from " + updateInfoFile.toAbsolutePath());
			}
		}
		else {
			RuntimeUpdateInfo runtimeUpdateInfo = RuntimeUpdateInfo.T.create();
			return runtimeUpdateInfo;
		}
	}

	private boolean isUpdateRequired(RuntimeUpdateInfo updateInfo, CompiledArtifactIdentification tomcatArtifact) {
		return !(CommonTools.equals(updateInfo.getGroupId(), tomcatArtifact.getGroupId()) &&
				CommonTools.equals(updateInfo.getArtifactId(), tomcatArtifact.getArtifactId()) &&
						CommonTools.equals(updateInfo.getVersion(), tomcatArtifact.getVersion().asString()));
	}

	private PackagedPlatformSetup packagePlatformSetupAsZip(ServiceRequestContext requestContext, PackagePlatformSetupAsZip denotation) {
		try {
			// prepare temporary files
			DateCodec dateCodec = new DateCodec("YYYY-MM-dd-hh-mm-ss");
			String dateAsStr = dateCodec.encode(new Date());
			String prefix = "setup-package-" + dateAsStr + '-';
			Path packageBaseDir = Files.createTempDirectory(prefix);

			// do the actual packaging
			PackagePlatformSetup packagePlatformSetup = PackagePlatformSetup.T.create();
			packagePlatformSetup.setPackageBaseDir(packageBaseDir.toString());
			
			for(Property p : PlatformSetupConfig.T.getProperties()) {
				Object value = p.get(denotation);
				p.set(packagePlatformSetup, value);
			}
			
			PackagedPlatformSetup packagedPlatformSetup = packagePlatformSetup.eval(requestContext).get();
			
			Path zipFile = Files.createTempFile(prefix, ".zip");

			// turning temporary package file structure to a zip file and deleting the file structure
			zip(packageBaseDir, zipFile, false);

			FileTools.deleteRecursivelySymbolLinkAware(packageBaseDir.toFile());

			// enrich result with transient archive resource
			File watchedFile = zipFile.toFile();
			FileTools.deleteFileWhenOrphaned(watchedFile);

			Resource zipResource = Resource.createTransient(() -> new ReferencingFileInputStream(watchedFile));

			packagedPlatformSetup.setArchive(zipResource);

			return packagedPlatformSetup;
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while zipping stream package for request: " + denotation);
		}
	}

	private void zip(Path sourceDir, Path targetFile, boolean includeRootFolder) {
		class DirectoryInfo {
			String name;
			int childCount;
			public DirectoryInfo(String name) {
				super();
				this.name = name;
			}
		}

		try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(targetFile))) {

			Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
				private final Stack<DirectoryInfo> stack = new Stack<>();

				private void increaseChildCount() {
					if (!stack.isEmpty()) {
						stack.peek().childCount++;
					}
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					increaseChildCount();

					createZipEntry(file.getFileName().toString());

					try (InputStream in = Files.newInputStream(file)) {
						IOTools.transferBytes(in, zout);
					}

					zout.flush();
					zout.closeEntry();

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					increaseChildCount();

					stack.push(new DirectoryInfo(dir.getFileName().toString()));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					DirectoryInfo info = stack.peek();
					if (info.childCount == 0) {
						createZipEntry("");
						zout.closeEntry();
					}
					stack.pop();
					return FileVisitResult.CONTINUE;
				}

				private void createZipEntry(String leafName) throws IOException {
					Stream<DirectoryInfo> pathStream = stack.stream();
					if (!includeRootFolder)
						pathStream = pathStream.skip(1);
					zout.putNextEntry(new ZipEntry(Stream.concat(pathStream.map(i -> i.name), Stream.of(leafName)).collect(PathCollectors.slashPath)));
				}
			});
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while zipping stream package from [" + sourceDir.toAbsolutePath() +"] to: " + targetFile.toAbsolutePath());
		}
	}
	
	private Neutral setupRepositoryConfiguration(SetupRepositoryConfiguration denotation) {	
		new SetupRepositoryConfigurationProcessor().process(denotation, virtualEnvironment);
		return Neutral.NEUTRAL;
	}

	private List<String> getLockedVersions(GetLockedVersions denotation) {
		File tempDir = FileTools.createNewTempDir("jinni-get-locked-versions-" + new ExtSimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date()));
		
		SetupRepositoryConfigurationProcessor setupRepositoryConfigurationProcessor = new SetupRepositoryConfigurationProcessor();
		SetupRepositoryConfiguration setupRepositoryConfiguration = SetupRepositoryConfiguration.T.create();
		setupRepositoryConfiguration.setInstallationPath(tempDir.getAbsolutePath());
		setupRepositoryConfiguration.setRepositoryViews(denotation.getRepositoryViews());

		List<String> viewsSolutions = setupRepositoryConfigurationProcessor.process(setupRepositoryConfiguration, virtualEnvironment, denotation.getIncludeDependencies());
		// created by the SetupRepositoryConfiguration request executed above
		File repositoryConfiguration = new File(tempDir, FILE_REPOSITORY_CONFIGURATION);
		List<String> lockedVersions = GetLockedVersionsProcessor.process(repositoryConfiguration, denotation.getIncludeViews() ? viewsSolutions : null);

		if (denotation.getOnlyGroups()) {
			return GetLockedVersionsProcessor.groupLockedVersionsByGroupId(lockedVersions);
		}
		return lockedVersions;
	}

	private Neutral backupArtifacts(BackupArtifacts denotation) {
		if (CommonTools.isEmpty(virtualEnvironment.getEnv(DEVROCK_REPOSITORY_CONFIGURATION))) {
			throw new IllegalStateException(
					"Please set env variable '" + DEVROCK_REPOSITORY_CONFIGURATION + "' that points to a repository configuration.");
		}
		
		File repositoryConfigurationFile = new File(virtualEnvironment.getEnv(DEVROCK_REPOSITORY_CONFIGURATION));
		if(!repositoryConfigurationFile.exists()) {
			throw new IllegalStateException(
					"File with repository configuration not found. Path: " + repositoryConfigurationFile.getAbsolutePath());
		}
		
		if (CommonTools.isEmpty(denotation.getArtifacts()) && CommonTools.isEmpty(denotation.getYamlFile())) {
			throw new IllegalStateException(
					"Please set the artifacts to download. Either '" + BackupArtifacts.artifacts + "' or '" + BackupArtifacts.file + "' needs to be set.");
		} 
		
		List<String> artifactsToDownload = new ArrayList<String>();	
		if(!CommonTools.isEmpty(denotation.getArtifacts())) {
			artifactsToDownload.addAll(denotation.getArtifacts());
		}
		
		if (!CommonTools.isEmpty(denotation.getYamlFile())) {
			File artifactsFile = new File(denotation.getYamlFile());
			if (!artifactsFile.exists()) {
				throw new IllegalStateException("File with artifacts to download not found. Path: " + denotation.getYamlFile());
			}
			try {
				List<String> unmarshalledArtifacts = (List<String>) FileTools.read(artifactsFile).fromInputStream(it -> new YamlMarshaller().unmarshall(it));
				artifactsToDownload.addAll(unmarshalledArtifacts);				
			} catch(Exception e) {
				throw new IllegalStateException("Check if passed file is in YAML format.");
			}
		}

		BackupArtifactsProcessor.process(artifactsToDownload, denotation.getGenerateMavenMetadata(), virtualEnvironment);
		return Neutral.NEUTRAL;
	}
	
	
	private CheckReport checkGroup(CheckGroup denotation) {
		return CheckGroupProcessor.process(denotation.getGroupFolder(), denotation.getEnableFixes());
	}
	
	private Neutral restoreArtifacts(RestoreArtifacts denotation) {
		File backupFolder = new File(denotation.getFolder());
		if (!backupFolder.exists()) {
			throw new IllegalStateException("Backup folder with artifacts to restore not found. Path: " + denotation.getFolder());
		}
		
		File backupReportFile = new File(denotation.getFolder(), BackupArtifactsProcessor.BACKUP_REPORT);
		if (!backupReportFile.exists()) {
			throw new IllegalStateException("Backup folder does not have a report file. Path: " + backupReportFile.getAbsolutePath());
		}

		RestoreArtifactsProcessor.process(denotation, virtualEnvironment);
		return Neutral.NEUTRAL;
	}

	private PackagedPlatformSetup packagePlatformSetup(ServiceRequestContext requestContext, PackagePlatformSetup denotation) {
		PackagedPlatformSetupBuilder packagedPlatformSetupBuilder = new PackagedPlatformSetupBuilder();
		packagedPlatformSetupBuilder.getMasterContainer().setName(DisjointCollector.getRuntimeContainerName(
				packagedPlatformSetupBuilder.getMasterContainer().getName(), denotation.getShortenRuntimeContainerNames()));
		
		IsAssignableTo natureCondition = TypeConditions.isAssignableTo(PlatformAssetNature.T);
		
		withArtifactResolution(artifctResolution -> {
			PlatformAssetDistributionContextImpl context = resolveAndProcessAssets( //
					requestContext, denotation, artifctResolution, packagedPlatformSetupBuilder, natureCondition);
	
			// write runtime properties
			if(!denotation.getRuntimeProperties().isEmpty())
				appendRuntimeProperties(denotation, context.getPackageBaseDir());
			
			writePackagedPlatformSetup(packagedPlatformSetupBuilder, context);
		}, denotation.getDebugJs());
		
		return packagedPlatformSetupBuilder.getPackagedPlatformSetup();
	}

	private void withArtifactResolution(Consumer<ArtifactResolutionContext> callable, boolean jsDebug) {
		try (WireContext<ArtifactResolutionContract> wireContext = Wire.context(new ArtifactResolutionWireModule(virtualEnvironment, jsDebug))) {
			callable.accept(wireContext.contract());
		}
	}
	
	private <T> T callWithArtifactResolution(Function<ArtifactResolutionContext, T> callable, boolean jsDebug) {
		try (WireContext<ArtifactResolutionContract> wireContext = Wire.context(new ArtifactResolutionWireModule(virtualEnvironment, jsDebug))) {
			return callable.apply(wireContext.contract());
		}
	}
	
	private PlatformAssetDistributionContextImpl resolveAndProcessAssets(ServiceRequestContext requestContext,
			FileSystemPlatformSetupConfig denotation, ArtifactResolutionContext artifactResolution,
			PackagedPlatformSetupBuilder packagedPlatformSetupBuilder, TypeCondition natureTypeCondition) {
		
		String denotationPackageBaseDir = denotation.getPackageBaseDir();
		
		String packageBaseDirAsStr = (denotationPackageBaseDir != null) ? denotationPackageBaseDir : "package";
		File packageBaseDir = new File(CallerEnvironment.resolveRelativePath(packageBaseDirAsStr).getPath());

		if (packageBaseDir.exists()) {
			FileTools.deleteRecursivelySymbolLinkAware(packageBaseDir);
		}
		packageBaseDir.mkdirs();

		
		// get output configuration specification from denotation type
		boolean verbose = requestContext.getAspect(OutputConfigAspect.class, OutputConfig.empty).verbose();
		
		PlatformAssetStorageRecorder recorder = new PlatformAssetStorageRecorder();


		PlatformAssetDistributionContextImpl context = new PlatformAssetDistributionContextImpl(requestContext, denotation, virtualEnvironment,
				recorder, artifactResolution, packageBaseDir, packagedPlatformSetupBuilder, verbose);
		
		Pair<RepositoryViewResolution, SortedSet<PlatformAssetSolution>> resolutionResult = resolveSetupDependency(context, artifactResolution, denotation);
		RepositoryViewResolution repositoryViewResolution = resolutionResult.first();
		SortedSet<PlatformAssetSolution> solutions = resolutionResult.second();

		// filter for valid natures
		Set<PlatformAsset> invalidNatureAssets = new HashSet<>();
		for (PlatformAssetSolution solution : solutions) {
			PlatformAsset asset = solution.asset;
			if(!natureTypeCondition.matches(asset.getNature().entityType())) {
				invalidNatureAssets.add(asset);
			}
		}
		
		if(!invalidNatureAssets.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Error while resolving assets. There exist natures that do not match given nature filter criteria: ");
			sb.append(TypeConditionStringifier.stringify(natureTypeCondition));
			sb.append("\n\nMismatching assets:\n");
			invalidNatureAssets.stream().map(PlatformAsset::qualifiedRevisionedAssetName).sorted().forEach(a -> sb.append("  - " + a + "\n"));
			
			throw new IllegalStateException(sb.toString());
		}
			
		
		if (packagedPlatformSetupBuilder != null)
			registerAllAssets(packagedPlatformSetupBuilder, solutions);

		// if assets where resolved with view assets (to reduce artifact visibility) then this information is propagated here
		writeViewResolutionInfo(context, repositoryViewResolution);
		
		// asset preprocessing
		preProcessAssets(solutions);

		// actually process stuff -> create zip et al
		processSolutions(context, solutions);
		
		return context;
	}

	private void writeViewResolutionInfo(PlatformAssetDistributionContextImpl context, RepositoryViewResolution repositoryViewResolution) {
		if (repositoryViewResolution != null) {
			File setupInfoFolder =  context.projectionBaseFolder(false).push(PlatformAssetDistributionConstants.FILENAME_SETUP_INFO_DIR).toFile();
			setupInfoFolder.mkdirs();
			File repositoryViewResolutionFile = new File(setupInfoFolder, PlatformAssetDistributionConstants.FILENAME_REPOSITORY_VIEW_RESOLUTION);
			FileTools.write(repositoryViewResolutionFile).usingWriter(writer -> writeYml(repositoryViewResolution, writer));
		}
	}
	
	// Helpers
	
	private void appendRuntimeProperties(PlatformSetupConfig denotation, File packageBaseDir) {
		File propertiesFile = new File(packageBaseDir, PathList.create().push(PROJECTION_NAME_MASTER).push(FOLDER_ENVIRONMENT).push(FILE_TRIBEFIRE_PROPERTIES).toFilePath());

		if(!propertiesFile.exists())
			propertiesFile.getParentFile().mkdirs();
		
		outFile("Writing runtime properties resulting from request configuration to ", FILE_TRIBEFIRE_PROPERTIES);

		try (Writer writer = new FileWriter(propertiesFile, true)) {
			writer.append("# ");
			writer.append("runtime properties resulting from request configuration:");
			writer.append('\n');
			for (Map.Entry<String, String> entry: denotation.getRuntimeProperties().entrySet()) {
				writer.append(entry.getKey());
				writer.append('=');
				writer.append(TfSetupTools.escapePropertyValue(entry.getValue()));
				writer.append('\n');
			}
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while writing runtime properties to " + propertiesFile.getAbsolutePath());
		}
	}
	
	private void registerAllAssets(PackagedPlatformSetupBuilder packagedPlatformSetupBuilder, SortedSet<PlatformAssetSolution> solutions) {
		for (PlatformAssetSolution classifiedSolution: solutions) {
			packagedPlatformSetupBuilder.register(classifiedSolution.asset);
		}
	}

	private void printAssetDependencyTree(PlatformAssetResolvingContext context, PlatformAssetResolution classifiedSolutionsCollector) {
		DependencyPrinting dependencyPrinting = new DependencyPrinting(false);
		
		dependencyPrinting.printAssetDependencyTree(context, classifiedSolutionsCollector);
	}

	private void preProcessAssets(SortedSet<PlatformAssetSolution> solutions) {
		PlatformAssetPreprocessorContext preprocessorContext = new PlatformAssetPreprocessorContext() { /* noop */ };

		for (PlatformAssetSolution classifiedSolution : solutions) {
			PlatformAsset asset = classifiedSolution.asset;

			for (AssetPreprocessing assetPreprocessing: asset.getNature().getAssetPreprocessings()) {
				println("Preprocessing " + assetPreprocessing.entityType().getShortName() + " from asset: "  + asset.qualifiedRevisionedAssetName());
				PlatformAssetPreprocessor<AssetPreprocessing> processor = assetPreProcessors.get(assetPreprocessing);
				processor.process(preprocessorContext, asset, assetPreprocessing);
			}
		}
	}

	/**
	 * actually process the solutions
	 */
	private void processSolutions(PlatformAssetDistributionContextImpl context, SortedSet<PlatformAssetSolution> solutions) {
		preDistProcessing();

		for (PlatformAssetSolution classifiedSolution : solutions) {
			PlatformAsset asset = classifiedSolution.asset;
			
			if (asset.getNature().type() == AssetAggregator.T)
				continue;

			String version = asset.versionWithRevision();

			// TODO: normalize and rather control general output with verbosity of some kind
			boolean protocolize = !asset.getPlatformProvided();
			
			if (protocolize) {
				ConsoleOutputs.println(sequence(
						text("Processing "),
						brightWhite(classifiedSolution.nature.entityType().getShortName()),
						text("\n  asset: "),
						brightBlack(asset.getGroupId() + ":"),
						text(asset.getName()),
						brightBlack("#"),
						version.endsWith("-pc")?
							yellow(version):
							green(version)
					)
				);
			}

			// pre processing for cross cutting concerns
			preExpertProcessing();

			// identify expert
			PlatformAssetNatureBuilder<PlatformAssetNature> platformAssetNatureBuilder;
			try {
				platformAssetNatureBuilder = experts.get(classifiedSolution.nature);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			// tune context to current classifiedSolution to pass the asset and nature to the expert
			context.setClassifiedSolution(classifiedSolution);

			// process
			platformAssetNatureBuilder.transfer(context);

			// determine default PackagedPlatformAsset.pathInPackage
			determineDefaultPackagePath(context);

			// post processing for cross cutting concerns
			postExpertProcessing();

			if (protocolize)
				ConsoleOutputs.println();
		}

		postDistProcessing(context);


	}

	private void determineDefaultPackagePath(PlatformAssetDistributionContextImpl context) {
		PlatformAsset asset = context.getAsset();
		PackagedPlatformSetupBuilder packagedPlatformSetupBuilder = context.getPackagedPlatformSetupBuilder();
		Stream<File> associatedFiles = context.getAssociatedFiles(asset);

		PathReducer pathReducer = new PathReducer();

		associatedFiles.map(File::toPath).forEach(pathReducer::process);

		Path reducedPath = pathReducer.getReducedPath();

		if (reducedPath != null) {
			PackagedPlatformAsset packagedAsset = packagedPlatformSetupBuilder.get(asset);
			File packageBaseDir = context.getPackageBaseDir();
			
			Path relativeAssetPath = packageBaseDir.toPath().relativize(reducedPath);

			String pathInPackage = relativeAssetPath.toString().replace(File.separatorChar, '/');
			packagedAsset.setPathInPackage(pathInPackage);
			context.getPackagedPlatformSetupBuilder().getMasterContainerIndexer().register(packagedAsset);
		}
	}

	private void postExpertProcessing() {
		// noop

	}

	private void preExpertProcessing() {
		// noop

	}

	private void preDistProcessing() {
		// TODO Auto-generated method stub

	}

	private void postDistProcessing(PlatformAssetDistributionContext context) {
		processCollectors(context);
	}
	
	private void writePackagedPlatformSetup(PackagedPlatformSetupBuilder packagedPlatformSetupBuilder, PlatformAssetDistributionContext context) {
		outFile("\nWriting asset overview to ", PlatformAssetDistributionConstants.FILENAME_PACKAGED_PLATFORM_SETUP);

		// write any storage config.json file in the according folder
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();

		File packagedPlatformSetupFile = new File(context.getPackageBaseDir(), PlatformAssetDistributionConstants.FILENAME_PACKAGED_PLATFORM_SETUP);

		try (OutputStream out = new FileOutputStream(packagedPlatformSetupFile)) {
			marshaller.marshall(out, packagedPlatformSetupBuilder.getPackagedPlatformSetup(), prettiness(OutputPrettiness.high));
		} catch (Exception e) {
			throw unchecked(e, "Error while writing packaged platform setup overview: " + packagedPlatformSetupFile);
		}
	}

	private void processCollectors(PlatformAssetDistributionContext context) {
		println("Processing collectors");
		Stream<PlatformAssetCollector> allCollectors = Stream.concat(context.coalescingBuildersStream(), staticCollectors.stream().map(Supplier::get));
		Set<PlatformAssetCollector> alreadyExecuted = new HashSet<>();
		for (Iterator<PlatformAssetCollector> iterator = allCollectors.iterator(); iterator.hasNext();) {
			PlatformAssetCollector collector = iterator.next();
			transferCollector(context, alreadyExecuted, collector);
		}
	}

	private void transferCollector(PlatformAssetDistributionContext context, Set<PlatformAssetCollector> alreadyExecuted, PlatformAssetCollector collector) {
		if (!alreadyExecuted.add(collector)) {
			return;
		}

		collector.priorCollectors()//
			.stream()
			.map(context::findCollector)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.forEach(c -> transferCollector(context, alreadyExecuted, c));

		collector.transfer(context);

	}

	private Object createProject(ServiceRequestContext requestContext, CreateProject denotation) {

		String qualifiedName = denotation.getQualifiedName();
		CompiledArtifactIdentification projectSolution = CompiledArtifactIdentification.parse(qualifiedName);

		PlatformAsset project = PlatformAsset.T.create();
		project.setGroupId(projectSolution.getGroupId());
		project.setName(projectSolution.getArtifactId());

		Version version = projectSolution.getVersion();
		String versionAsStr = version.asString();

		if (!versionAsStr.matches("\\d*\\.\\d*"))
			throw new IllegalArgumentException("Invalid project version: " + versionAsStr + ". Expected major.minor only.");

		project.setVersion(version.getMajor() + "." + version.getMinor());

		project.setResolvedRevision("0");

		project.setNature(AssetAggregator.T.create());
		project.setNatureDefinedAsPart(true); // TODO remove

		// manage dependencies
		List<PlatformAssetDependency> projectQualifiedDependencies = project.getQualifiedDependencies();

		denotation.getDependencies() //
		.stream() //
		.map(d -> createAssetFromDependencyName(d, false)) //
		.forEach(d -> {
			projectQualifiedDependencies.add(d);
		});

		denotation.getGlobalSetupCandidates() //
		.stream() //
		.map(d -> createAssetFromDependencyName(d, true)) //
		.forEach(d -> {
			projectQualifiedDependencies.add(d);
		});

		// install asset to local repository to make it available
		File baseFolder;
		try {
			baseFolder = Files.createTempDirectory("tmp-assets").toFile();
		} catch (IOException e) {
			throw new UncheckedIOException("Error while creating tmp directory for asset installation of project " + project.qualifiedAssetName(), e);
		}

		String localRepoDir = localRepoDir(requestContext);

		outFile("Using local repo ", localRepoDir);

		MavenInstallAssetTransfer transfer = new MavenInstallAssetTransfer(project, requestContext, baseFolder, new File(localRepoDir), false);
		transfer.transfer();

		println("Created project " + project.qualifiedRevisionedAssetName());

		return null;
	}

	private String localRepoDir(ServiceRequestContext context) {
		try (WireContext<RepositoryConfigurationContract> wireContext = Wire.context(new McConfigWireModule(context))) {
			return wireContext.contract().repositoryConfiguration().get().getLocalRepositoryPath();

		} catch (Exception e) {
			return System.getProperty("user.home") + "\\.m2\\repository";
		}
	}

	private PlatformAssetDependency createAssetFromDependencyName (String condensedDependencyName, boolean isGlobal) {
		CompiledArtifactIdentification solution = CompiledArtifactIdentification.parse(condensedDependencyName);

		PlatformAsset asset = PlatformAsset.T.create();
		asset.setName(solution.getArtifactId());
		asset.setGroupId(solution.getGroupId());
		asset.setVersion(solution.getVersion().asString());
		asset.setNatureDefinedAsPart(true); // TODO remove

		PlatformAssetDependency assetDependency = PlatformAssetDependency.T.create();
		assetDependency.setAsset(asset);

		if(isGlobal)
			assetDependency.setIsGlobalSetupCandidate(true);

		return assetDependency;
	}

	private static GmSerializationOptions prettiness(OutputPrettiness prettyness) {
		return GmSerializationOptions.defaultOptions.derive().setOutputPrettiness(prettyness).build();
	}

	public static void outFile(String prefix, String fileName) {
		println(fileOutput(prefix, fileName));
	}

	public static ConsoleOutputContainer fileOutput(String prefix, String fileName) {
		return sequence( //
				text(prefix), //
				fileName(fileName) //
		);
	}
	
	public static void outSolution(String prefix, CompiledArtifactIdentification solution) {
		println(sequence( //
				text(prefix), //
				solution(solution) //
			));
	}

	public static void outFileTransfer(String prefix, String object, String source, String target) {
		outFileTransfer(prefix, FG_YELLOW, object, source, target);
	}
	
	public static void outFileTransfer(String prefix, int style, String object, String source, String target) {
		println(sequence( //
				text(prefix), styled(style, text(object)), //
				text(" FROM "), fileName(source), //
				text(" TO "), fileName(target) //
		));
	}

	public static String determineHostname() {
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = NetworkTools.getNetworkAddress().getHostName();
		}
		return hostname;
	}

}
