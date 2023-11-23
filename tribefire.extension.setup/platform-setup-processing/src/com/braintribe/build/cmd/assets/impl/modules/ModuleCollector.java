// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================

package com.braintribe.build.cmd.assets.impl.modules;

import static com.braintribe.build.cmd.assets.PlatformSetupProcessor.fileOutput;
import static com.braintribe.build.cmd.assets.impl.modules.ArtifactListProducer.writePackagingToFile;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.ARTIFACT_ID;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.CLASSPATH_FILE_NAME;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.GROUP_ID;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.LIB_FOLDER_NAME;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.SOLUTIONS_FILE_NAME;
import static com.braintribe.build.cmd.assets.impl.modules.ModuleSetupHelper.VERSION;
import static com.braintribe.console.ConsoleOutputs.println;
import static com.braintribe.console.ConsoleOutputs.sequence;
import static com.braintribe.console.ConsoleOutputs.text;
import static com.braintribe.model.deployment.Module.moduleGlobalId;
import static com.braintribe.setup.tools.TfSetupTools.writeYml;
import static com.braintribe.utils.ZipTools.unzip;
import static com.braintribe.utils.lcd.CollectionTools2.mapValues;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Collections.singletonList;
import static tribefire.cortex.asset.resolving.ng.impl.ArtifactOutputs.solution;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.braintribe.build.cmd.assets.PlatformAssetDistributionConstants;
import com.braintribe.build.cmd.assets.api.PlatformAssetCollector;
import com.braintribe.build.cmd.assets.api.PlatformAssetDistributionContext;
import com.braintribe.build.cmd.assets.impl.WebContextTransfer;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsClasspathOptimizer;
import com.braintribe.build.cmd.assets.impl.modules.api.TfsContext;
import com.braintribe.build.cmd.assets.impl.modules.impl.GreedyClasspathOptimizer;
import com.braintribe.build.cmd.assets.impl.modules.impl.TfSetupResolver;
import com.braintribe.build.cmd.assets.impl.modules.model.ComponentSetup;
import com.braintribe.build.cmd.assets.impl.modules.model.TfSetup;
import com.braintribe.build.cmd.assets.impl.modules.service.CreateTfServicesDebugProject;
import com.braintribe.devrock.mc.api.commons.PartIdentifications;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.artifact.analysis.AnalysisArtifact;
import com.braintribe.model.artifact.analysis.AnalysisDependency;
import com.braintribe.model.asset.PlatformAsset;
import com.braintribe.model.asset.PlatformAssetDependency;
import com.braintribe.model.asset.natures.TribefireModule;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.platform.setup.api.PlatformSetupConfig;
import com.braintribe.setup.tools.TfSetupTools;
import com.braintribe.setup.tools.TfsConsolePrinter;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.OsTools;
import com.braintribe.utils.lcd.CommonTools;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.utils.paths.PathList;
import com.braintribe.utils.xml.parser.DomParser;

import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.descriptor.model.ModulesDescriptor;

/**
 * <h3>Note regarding resolving the jar files for modules and their dependencies.</h3>
 * 
 * Some jar files may be requested with a classifier, e.g.(io.netty:netty-transport-native-epoll#3.12.. with "linux-x86_64"). In this case we cannot
 * download (enrich the solution with) regular {@link PartIdentifications#jar}, but have to download the jar for that classifier. This jar is used not
 * only to place jar on the classpath, but also when checking whether a given artifact is a GM API, for example.
 * 
 * TODO rewrite
 * 
 * For now, we have implemented this in such a way that we retrieve the classifier information from each solution via the
 * {@link AnalysisArtifact#getDependers()} property (followed by {@link AnalysisDependency#getClassifier()}), and we throw an exception if more than
 * one classifier is being used for a given solution.
 * 
 * THIS MIGHT STILL CAUSE PROBLEMS when optimizing classpaths and the same solution in different modules would be requested with different classifiers
 * - one of them could be omitted (first would be promoted to main classpath, other would be considered to already be there based on the solution
 * identification, which doesn't involve classifiers). It is, however, not clear how to handle this.
 * 
 * @see TfSetupResolver
 */
public class ModuleCollector implements PlatformAssetCollector, PlatformAssetDistributionConstants {

	public static final String CONTEXT_ZIP = "context:zip";
	public static final String RESOURCES_ZIP = "resources:zip";

	private PlatformAsset tfWebPlatformAsset;
	private final List<PlatformAsset> moduleAssets = newList();
	private final List<PlatformAsset> modelAssets = newList();
	private final List<PlatformAsset> libraryAssets = newList();

	private final Map<PlatformAsset, Optional<File>> assetToZipPart = newMap();
	private final Map<PlatformAsset, ModuleDescriptor> assetToModuleDescriptor = newMap();

	public void setTfWebPlatformAsset(PlatformAsset tfWebPlatformAsset, Optional<File> contextPart) {
		this.tfWebPlatformAsset = tfWebPlatformAsset;
		this.assetToZipPart.put(tfWebPlatformAsset, contextPart);
	}

	public void addModuleAsset(PlatformAsset moduleAsset, Optional<File> resourcesPart) {
		moduleAssets.add(moduleAsset);
		assetToZipPart.put(moduleAsset, resourcesPart);
	}

	public void addModelAsset(PlatformAsset modelAsset) {
		modelAssets.add(modelAsset);
	}

	public void addLibraryAsset(PlatformAsset libraryAsset) {
		libraryAssets.add(libraryAsset);
	}

	// #############################################################
	// ## . . . . . . . . . . . Transfer . . . . . . . . . . . . .##
	// #############################################################

	@Override
	public void transfer(PlatformAssetDistributionContext context) {
		if (isModularSetup())
			new ModuleTransfer(context).run();
	}

	public boolean isModularSetup() {
		return tfWebPlatformAsset != null;
	}

	private class ModuleTransfer {

		private static final String WEB_INF = "WEB-INF";
		private static final String CONTEXT = "context";
		private static final String CORTEX_MODELS_TXT = "cortex-models.txt";

		private static final String META_INF_CONTEXT_PATH = "META-INF/context.xml";
		private static final String PACKAGING_XML_PATH = "WEB-INF/Resources/Packaging/packaging.xml";

		private static final String MODULES_YML = "modules.yml";
		private static final String DEBUG_DIR_NAME = "debug";

		private final PlatformAssetDistributionContext context;
		private final String[] debugProject;
		private final boolean debugJs;
		private final boolean preProcessClasspaths;

		private final Path publicResourcePath = new File("public").toPath();
		private final Path storagePublicResourcesDir;

		private final List<ModuleDescriptor> moduleDescriptors = newList();

		private TfSetup tfSetup;
		private ComponentSetup platformSetup;

		private final TfsConsolePrinter printer = new TfsConsolePrinter();

		public ModuleTransfer(PlatformAssetDistributionContext context) {
			PlatformSetupConfig request = context.request();

			this.context = context;
			this.debugProject = resolveDebugProject(request);
			this.debugJs = request.getDebugJs();
			this.preProcessClasspaths = request.getPreProcessClasspaths();

			this.storagePublicResourcesDir = context.storagePublicResourcesFolder(false).toPath();
		}

		private static String[] resolveDebugProject(PlatformSetupConfig request) {
			if (!request.getDebugJava())
				return ModuleSetupHelper.parseCondensedArtifact(request.getDebugProject());

			String[] sd = ModuleSetupHelper.parseCondensedArtifact(request.getSetupDependency());
			return new String[] { sd[0], sd[1] + "-debug", sd[2] };
		}

		// ##########################################################################
		// ## . . . . . . . . ACTUAL TRIBEFIRE PACKAGE PREPARATION . . . . . . . . ##
		// ##########################################################################

		public void run() {
			printer.out("\nPreparing tribefire platform setup");

			printer.up();

			resolveSetup();
			preparePlatform();
			prepareModules();

			printer.down();
		}

		private void resolveSetup() {
			printResolvingSetup();

			tfSetup = TfSetupResolver.resolve(newTfsContext());
			platformSetup = tfSetup.platformSetup;
		}

		private void printResolvingSetup() {
			printer.out("Resolving setup (" + //
					moduleAssets.size() + " modules, " + libraryAssets.size() + " libraries, " + modelAssets.size() + " models)");
		}

		private TfsContext newTfsContext() {
			// TEMP WORKAROUND!!! to also force all models on the classpath
			// These models can come from cartridges as well
			libraryAssets.addAll(modelAssets);

			return new TfsContext(tfWebPlatformAsset, moduleAssets, modelAssets, libraryAssets, context.artifactResolutionContext(),
					newClasspathOptimizer(), preProcessClasspaths, context.doVerboseOutput());
		}

		private TfsClasspathOptimizer newClasspathOptimizer() {
			return new GreedyClasspathOptimizer();
		}

		// #############################################################
		// ## . . . . Priming the web-platform (TF services) . . . . .##
		// #############################################################

		private void preparePlatform() {
			printer.newLine();
			printPerparingComponent("web-platform", platformSetup);
			printer.up();

			primeCortexModels();

			if (isForDebug())
				prepareTfServicesDebugProject();
			else
				prepareTfsWebApp();

			printer.down();
		}

		//
		// Regular deployment
		//

		private void prepareTfsWebApp() {
			File servicesDir = resolveServicesDir();

			printer.out(fileOutput("Tribefire services web-app: ", servicesDir.getAbsolutePath()));

			preparePlatformContext(servicesDir);
			prepareLibDir(platformSetup.classpath, new File(servicesDir, WEB_INF));
			createPackagingXml(servicesDir);
		}

		private void primeCortexModels() {
			File modelNamesFile = context.storageAccessDataFolder("cortex").push(CORTEX_MODELS_TXT).toFile();

			printer.out(fileOutput("Cortex models: ", modelNamesFile.getAbsolutePath()));

			FileTools.write(modelNamesFile).lines(modelNames());
		}

		private Stream<String> modelNames() {
			return modelAssets.stream() //
					.map(PlatformAsset::versionlessName) //
					.sorted();
		}

		private File resolveServicesDir() {
			return projectionBaseFolder().push(WebContextTransfer.WEBAPP_FOLDER_NAME).push(TRIBEFIRE_SERVICES).toFile();
		}

		//
		// Debug deployment
		//

		private void prepareTfServicesDebugProject() {
			File debugProjectDir = new File(debugFolder(), debugProject[ARTIFACT_ID]);
			File contextDir = new File(debugProjectDir, CONTEXT);

			printer.out(fileOutput("Eclipse project for TF services debugging: ", debugProjectDir.getAbsolutePath()));

			println("");
			prepareDebugProjectFromTemplate(debugProjectDir);
			preparePlatformContext(contextDir);
			createPackagingXml(contextDir);
			println("");
		}

		private void prepareDebugProjectFromTemplate(File debugProjectDir) {
			CreateTfServicesDebugProject request = prepareDebugProjectCreationRequest(debugProjectDir);
			context.requestContext().eval(request).get();
		}

		private static final String dependencyTemplate = //
				"<dependency><groupId>%s</groupId><artifactId>%s</artifactId><version>%s</version>%s<exclusions><exclusion/></exclusions></dependency>";

		private CreateTfServicesDebugProject prepareDebugProjectCreationRequest(File debugProjectDir) {
			CreateTfServicesDebugProject result = CreateTfServicesDebugProject.T.create();
			result.setGroupId(debugProject[GROUP_ID]);
			result.setArtifactId(debugProject[ARTIFACT_ID]);
			result.setVersion(debugProject[VERSION]);
			result.setDependencies(dependenciesForPom());
			result.setInstallationPath(debugProjectDir.getAbsolutePath());
			result.setOverwrite(true);

			return result;
		}

		private List<String> dependenciesForPom() {
			return platformSetup.classpath.stream() //
					.flatMap(this::toDependencyEntry) //
					.collect(Collectors.toList());
		}

		private Stream<String> toDependencyEntry(AnalysisArtifact s) {
			return ModuleSetupHelper.streamSortedJarClassifiersOf(s) //
					.map(classifier -> String.format( //
							dependencyTemplate, s.getGroupId(), s.getArtifactId(), toDependencyVersion(s), toClassifierTag(classifier)));
		}

		private String toDependencyVersion(AnalysisArtifact s) {
			return s.getVersion();
		}

		private String toClassifierTag(String classifier) {
			return StringTools.isEmpty(classifier) ? "" : "<classifier>" + classifier + "</classifier>";
		}

		private void preparePlatformContext(File servicesDir) {
			File zipFile = getContextZip();

			unzip(zipFile, servicesDir);

			if ((debugJs || debugProject != null) && OsTools.isUnixSystem())
				patchMetaInfContext(servicesDir);
		}

		private void patchMetaInfContext(File servicesDir) {
			File contextFile = new File(servicesDir, META_INF_CONTEXT_PATH);

			try {
				Document document = DomParser.load().from(contextFile);

				Element resourcesElement = document.createElement("Resources");
				resourcesElement.setAttribute("allowLinking", "true");

				document.getDocumentElement().appendChild(resourcesElement);

				DomParser.write().from(document).to(contextFile);

			} catch (Exception e) {
				throw Exceptions.unchecked(e, "Error while updating " + META_INF_CONTEXT_PATH);
			}
		}

		private void createPackagingXml(File servicesDir) {
			File xmlFile = new File(servicesDir, PACKAGING_XML_PATH);

			writePackagingToFile(platformSetup, xmlFile);
		}

		// #############################################################
		// ## . . . . . . . . . . Priming modules . . . . . . . . . . ##
		// #############################################################

		private void prepareModules() {
			Map<AnalysisArtifact, List<String>> solution2CpEntry = prepareCommonModulesLibDir();

			for (ComponentSetup moduleSetup : tfSetup.moduleSetups)
				prepareModule(moduleSetup, solution2CpEntry);

			writeModulesDescriptor();
		}

		private Map<AnalysisArtifact, List<String>> prepareCommonModulesLibDir() {
			printer.out("Preparing lib folder for all " + tfSetup.moduleSetups.size() + " modules");

			Map<AnalysisArtifact, List<File>> solution2JarFile = prepareLibDir(tfSetup.allModulesCpSolutions, modulesFolder());

			return mapValues(solution2JarFile, this::filesToCpEntries, TfSetupTools.analysisArtifactMap());
		}

		// copy module libs to respective module folders
		private void prepareModule(ComponentSetup moduleSetup, Map<AnalysisArtifact, List<String>> solution2CpEntry) {
			printPerparingComponent("module", moduleSetup);

			File moduleDir = resolveModuleDir(moduleSetup);

			prepareModuleDescriptor(moduleSetup, moduleDir);
			prepareModuleClasspath(moduleSetup, moduleDir, solution2CpEntry);
			prepareResourcesDir(moduleSetup, moduleDir);
		}

		private void prepareModuleDescriptor(ComponentSetup moduleSetup, File moduleDir) {
			ModuleDescriptor moduleDescriptor = createModuleDescriptor(moduleSetup, moduleDir);
			moduleDescriptors.add(moduleDescriptor);
		}

		private ModuleDescriptor createModuleDescriptor(ComponentSetup moduleSetup, File moduleDir) {
			PlatformAsset moduleAsset = moduleSetup.descriptor.asset;
			TribefireModule nature = (TribefireModule) moduleAsset.getNature();

			ModuleDescriptor result = acquireModuleDescriptor(moduleAsset);
			result.setJarPath(moduleJarPath(moduleSetup));
			result.setAccessIds(nature.configuredAccesses().collect(Collectors.toSet()));
			result.setPath(moduleDir.getName());
			result.setDependedModules(moduleDependenciesOf(moduleAsset));

			return result;
		}

		private Set<ModuleDescriptor> moduleDependenciesOf(PlatformAsset moduleAsset) {
			return moduleAsset.getQualifiedDependencies().stream() //
					.filter(platAssDependency -> !platAssDependency.getSkipped()) //
					.map(PlatformAssetDependency::getAsset) //
					.flatMap(this::findCoveringModulesAssets) //
					.map(this::acquireModuleDescriptor) //
					.collect(Collectors.toSet());
		}

		/**
		 * Returns a Stream of all modules which through their dependencies cover all the modules reachable from given asset.
		 * <p>
		 * If the asset is a module, we just need to return the module itself. However, it might be a different asset, say aggregator, which then
		 * references other modules, say modA and modB, which are independent. In this case, we want to return a Stream that provides both of these
		 * modules.
		 */
		private Stream<PlatformAsset> findCoveringModulesAssets(PlatformAsset asset) {
			if (isTribefireModuleAsset(asset))
				return Stream.of(asset);

			Set<PlatformAsset> visited = newSet();
			List<PlatformAsset> modules = newList();

			traverseAssets(asset, visited, modules);

			return modules.stream();
		}

		private void traverseAssets(PlatformAsset asset, Set<PlatformAsset> visited, List<PlatformAsset> modules) {
			if (!visited.add(asset))
				return;

			if (isTribefireModuleAsset(asset)) {
				modules.add(asset);
				return;
			}

			for (PlatformAssetDependency pad : asset.getQualifiedDependencies())
				if (!pad.getSkipped())
					traverseAssets(pad.getAsset(), visited, modules);
		}

		private boolean isTribefireModuleAsset(PlatformAsset asset) {
			return asset.getNature() instanceof TribefireModule;
		}

		private ModuleDescriptor acquireModuleDescriptor(PlatformAsset moduleAsset) {
			return assetToModuleDescriptor.computeIfAbsent(moduleAsset, this::newModuleDescriptor);
		}

		private ModuleDescriptor newModuleDescriptor(PlatformAsset moduleAsset) {
			ModuleDescriptor result = ModuleDescriptor.T.create();
			result.setArtifactId(moduleAsset.getName());
			result.setGroupId(moduleAsset.getGroupId());
			result.setVersion(moduleAsset.getVersion());
			result.setModuleGlobalId(moduleGlobalId(result.getGroupId(), result.getArtifactId()));

			return result;
		}

		private String moduleJarPath(ComponentSetup moduleSetup) {
			AnalysisArtifact moduleSolution = moduleSetup.descriptor.assetSolution;
			File moduleJarFile = TfSetupTools.getPartFile(moduleSolution, PartIdentifications.jar);

			// is on the module's classpath
			if (!moduleSetup.classpath.isEmpty())
				return "<module>";

			// is on main classpath, debug project -> i.e. it's in the local maven repo
			if (isForDebug())
				return moduleJarFile.getAbsolutePath();

			// is on main classpath, no debug -> it's in the TF services lib folder
			return moduleJarFile.getName();
		}

		private File resolveModuleDir(ComponentSetup moduleSetup) {
			PlatformAsset asset = moduleSetup.descriptor.asset;

			String moduleName = asset.qualifiedAssetName();
			String moduleDirName = FileTools.replaceIllegalCharactersInFileName(moduleName, "_");

			return new File(modulesFolder(), moduleDirName);
		}

		private Map<AnalysisArtifact, List<File>> prepareLibDir(Collection<AnalysisArtifact> cpSolutions, File libParentDir) {
			printer.up().out("Copying " + cpSolutions.size() + " jars").down();

			File libDir = new File(libParentDir, LIB_FOLDER_NAME);

			Map<AnalysisArtifact, List<File>> cpFiles = newMap(cpSolutions.size());

			for (AnalysisArtifact cpSolution : cpSolutions) {
				// Get all jar files
				List<File> jarFiles = ModuleSetupHelper.getAllJarFiles(cpSolution);

				if (!isForDebug())
					for (File jarFile : jarFiles)
						FileTools.copyFileToDirectory(jarFile, libDir);

				cpFiles.put(cpSolution, jarFiles);
			}

			return cpFiles;
		}

		private void prepareModuleClasspath(ComponentSetup moduleSetup, File moduleDir, Map<AnalysisArtifact, List<String>> solution2CpEntry) {
			List<AnalysisArtifact> cpSolutions = moduleSetup.classpath;

			if (shouldForceNativeModuleInSolutionsFile(cpSolutions)) {
				cpSolutions = singletonList(moduleSetup.descriptor.assetSolution);
				solution2CpEntry.computeIfAbsent(moduleSetup.descriptor.assetSolution, this::cpEntriesForArtifact);
			}

			storeClasspathFile(moduleDir, cpSolutions, solution2CpEntry);

			if (isForDebug())
				storeSolutionsFile(moduleDir, cpSolutions);
		}

		/**
		 * Motivated by supporting module resources in debug mode - even if module is native, we still write the module's artifact into the classpath
		 * file so that we can read the actual location (written by AC) and thus find the resources in an eclipse project.
		 * <p>
		 * Not that writing the classpath entry doesn't make the ModuleLoader will consider it a custom-classpath module - this is resolved based on
		 * {@link ModuleDescriptor#getJarPath()}.
		 */
		private boolean shouldForceNativeModuleInSolutionsFile(List<AnalysisArtifact> cpSolutions) {
			return isForDebug() && cpSolutions.isEmpty();
		}

		private List<String> cpEntriesForArtifact(AnalysisArtifact artifact) {
			return filesToCpEntries(ModuleSetupHelper.getAllJarFiles(artifact));
		}

		private List<String> filesToCpEntries(List<File> files) {
			Function<File, String> fileToPathMapping = isForDebug() ? File::getAbsolutePath : File::getName;

			return files.stream() //
					.map(fileToPathMapping) //
					.collect(Collectors.toList());
		}

		private void storeClasspathFile(File dir, List<AnalysisArtifact> cpSolutions, Map<AnalysisArtifact, List<String>> solution2CpEntry) {
			Stream<String> cpEntries = cpSolutions.stream().map(solution2CpEntry::get).flatMap(List::stream);

			File classpathFile = new File(dir, CLASSPATH_FILE_NAME);

			FileTools.write(classpathFile).lines(cpEntries);
		}

		private void storeSolutionsFile(File dir, List<AnalysisArtifact> cpSolutions) {
			Stream<String> solutionsEntries = cpSolutions.stream().flatMap(this::condensedNameWithClassifiers);

			File solutionsFile = new File(dir, SOLUTIONS_FILE_NAME);

			FileTools.write(solutionsFile).lines(solutionsEntries);
		}

		private Stream<String> condensedNameWithClassifiers(AnalysisArtifact artifact) {
			String s = artifact.asString();

			return ModuleSetupHelper.streamSortedJarClassifiersOf(artifact) //
					.map(classifier -> s + classifierPart(classifier));
		}

		private static final String DELIMITER_CLASSIFIER = "|";

		private String classifierPart(String c) {
			return CommonTools.isEmpty(c) ? "" : DELIMITER_CLASSIFIER + c;
		}

		private void prepareResourcesDir(ComponentSetup moduleSetup, File moduleDir) {
			findZipPart(moduleSetup)//
					.ifPresent( //
							f -> unzip(f, new File(moduleDir, "resources"), this::mapIfPublicResource));
		}

		private File mapIfPublicResource(String path) {
			Path filePath = new File(path).toPath();

			if (!filePath.startsWith(publicResourcePath))
				return null;

			Path relativePathWithinPublicDir = publicResourcePath.relativize(filePath);
			return storagePublicResourcesDir.resolve(relativePathWithinPublicDir).toFile();
		}

		private boolean isForDebug() {
			return debugProject != null;
		}

		private File getContextZip() {
			return findZipPart(platformSetup).orElseThrow(() -> new GenericModelException(
					CONTEXT_ZIP + " part not found for solution: " + platformSetup.descriptor.assetSolution.asString()));
		}

		private Optional<File> findZipPart(ComponentSetup componentSetup) {
			return assetToZipPart.get(componentSetup.descriptor.asset);
		}

		private void printPerparingComponent(String moduleOrWebPlatform, ComponentSetup componentSetup) {
			printer.out(sequence( //
					text("Preparing " + moduleOrWebPlatform + ": "), //
					solution(componentSetup.descriptor.assetSolution), //
					text(componentSetup.classpath.isEmpty() ? "  [native]" : "")//
			));
		}

		private void writeModulesDescriptor() {
			ModuleSetupBackwardCompatibility.ensureBc(tfSetup, moduleDescriptors);

			File modulesYmlFile = new File(modulesFolder(), MODULES_YML);

			ModulesDescriptor modulesDescriptor = ModulesDescriptor.T.create();
			modulesDescriptor.setModules(moduleDescriptors);

			FileTools.write(modulesYmlFile).usingWriter(writer -> writeYml(modulesDescriptor, writer));
		}

		private File modulesFolder() {
			return projectionBaseFolder().push(MODULES_DIR_NAME).toFile();
		}

		private File debugFolder() {
			return projectionBaseFolder().push(DEBUG_DIR_NAME).toFile();
		}

		private PathList projectionBaseFolder() {
			return context.projectionBaseFolder(false);
		}
	}

}
