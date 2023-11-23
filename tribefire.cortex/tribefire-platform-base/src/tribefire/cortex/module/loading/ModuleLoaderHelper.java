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
package tribefire.cortex.module.loading;

import static com.braintribe.model.deployment.Module.PLATFORM_MODULE_GLOBAL_ID;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.braintribe.codec.marshaller.api.DecodingLenience;
import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.cortex.deployment.EnvironmentDenotationRegistry;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.utils.FileTools;

import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.descriptor.model.ModulesDescriptor;
import tribefire.descriptor.model.PlatformDescriptor;

/**
 * @author peter.gazdik
 */
/* package */ class ModuleLoaderHelper {

	private static final String RESOURCES_FOLDER_NAME = "resources";

	private static final ClassLoader platformClassLoader = GenericEntity.class.getClassLoader();

	private static final Logger log = Logger.getLogger(ModuleLoaderHelper.class);

	/**
	 * Let's make properties lenient so that if new properties are added, they are ignored by older module-loaders and thus we stay backwards
	 * compatible.
	 */
	private static final DecodingLenience marshallerLenience = new DecodingLenience() //
			.propertyLenient(true);

	private static final GmDeserializationOptions moduleDescriptorYamlOptions = GmDeserializationOptions.deriveDefaults() //
			.setInferredRootType(ModulesDescriptor.T) //
			.setDecodingLenience(marshallerLenience) //
			.build();

	public static PlatformDescriptor platformDescriptor(String groupId, String artifactId) {
		PlatformDescriptor result = PlatformDescriptor.T.create();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);

		return result;
	}

	public static Module moduleDenotation(Module module, ModuleDescriptor md) {
		return moduleDenotation(module, md.getModuleGlobalId(), md.getGroupId(), md.getArtifactId());
	}

	public static Module moduleDenotation(Module module, String globalId, String groupId, String artifactId) {
		module.setGlobalId(globalId);
		module.setName(artifactId);
		module.setGroupId(groupId);

		return module;
	}

	// ###############################################################
	// ## . . . . . . . . . Read Module Descriptors . . . . . . . . ##
	// ###############################################################

	public static List<ModuleDescriptor> readModuleDescriptors(File modulesFolder) {
		File file = new File(modulesFolder, "modules.yml");
		if (!file.exists())
			throw new GenericModelException("Cannot load modules, 'modules.yml' not found in the modules folder: " + modulesFolder.getAbsolutePath());

		ModulesDescriptor modulesDescriptor = FileTools.read(file).fromInputStream(ModuleLoaderHelper::unmarshallFromYaml);

		return modulesDescriptor.getModules();
	}

	public static File getModuleBaseDir(File modulesDir, ModuleDescriptor moduleDescriptor) {
		File result = new File(modulesDir, moduleDescriptor.getPath()).toPath().normalize().toFile(); // make nicer in debug
		if (!result.isDirectory())
			throw new GenericModelException("Module folder configured in 'modules.yml' does not exist: " + result.getAbsolutePath());

		return result;
	}

	private static <T> T unmarshallFromYaml(InputStream in) {
		return (T) new YamlMarshaller().unmarshall(in, moduleDescriptorYamlOptions);
	}

	// ###############################################################
	// ## . . . . . . . . Preparing Wiring Loader . . . . . . . . . ##
	// ###############################################################

	/* package */ static WiringLoader createWiringLoader(ModuleDescriptor currentModule, File modulesLibFolder, File moduleBaseFolder) {

		if (hasOwnClasspath(currentModule)) {
			URL[] urls = resolveModuleClasspath(modulesLibFolder, moduleBaseFolder);
			// For now, for convenience, we want the module-specific CL to also see ITW types
			ClassLoader itwClassLoader = (ClassLoader) GMF.getTypeReflection().getItwClassLoader();
			ModuleClassLoader moduleClassLoader = ModuleClassLoader.create(urls, itwClassLoader, currentModule);

			return new WiringLoader(currentModule, moduleClassLoader);

		} else {
			return new WiringLoader(currentModule, platformClassLoader);
		}
	}

	private static boolean hasOwnClasspath(ModuleDescriptor currentModule) {
		return currentModule.getJarPath().equals("<module>");
	}

	private static URL[] resolveModuleClasspath(File modulesLibFolder, File moduleBaseFolder) {
		Path classpathFilePath = resolveClasspathPath(moduleBaseFolder);

		try (Stream<String> lines = linesSafe(classpathFilePath)) {
			return lines //
					.map(File::new) //
					.map(relativeFileResolver(modulesLibFolder)) //
					.map(FileTools::toURL) //
					.toArray(URL[]::new);
		}
	}

	/** Relative paths in .classpath are to be resolved relative to "modules/lib" folder. */
	private static Function<File, File> relativeFileResolver(File modulesLibFolder) {
		return f -> f.isAbsolute() ? f : new File(modulesLibFolder, f.getPath());
	}

	private static Path resolveClasspathPath(File moduleBaseFolder) {
		File classpathFile = moduleClasspathFile(moduleBaseFolder);

		if (!classpathFile.exists() || classpathFile.isDirectory())
			throw new GenericModelException("Unable to load module: " + moduleBaseFolder.getName() + ", .classpath file does not exist.");

		return Paths.get(classpathFile.getAbsolutePath());
	}

	/**
	 * Resolves the path at which resources of given module are located.
	 * <p>
	 * The result is a folder called "resources", either inside the "modules" folder of our setup or inside the java project, if run in debug mode
	 * with this module in workspace.
	 */
	public static Path resolveResourcesPath(String moduleArtifactId, File moduleBaseFolder) {
		File debugResourcesFolder = findResourcesFolderOwner(moduleArtifactId, moduleBaseFolder);

		return debugResourcesFolder.toPath().resolve(RESOURCES_FOLDER_NAME);
	}

	/**
	 * Normally, the resources folder is inside the moduleBaseFolder (inside the "modules" folder), but in debug it can be a java project. We can tell
	 * by looking at the first entry in the classpath file - if it is a folder, it was written by AC as the IDE compiler output. So we locate it's
	 * parent with the name of the module's artifact id, which is our java project.
	 */
	private static File findResourcesFolderOwner(String moduleArtifactId, File moduleBaseFolder) {
		File classpathFile = moduleClasspathFile(moduleBaseFolder);
		if (classpathFile.length() == 0)
			return moduleBaseFolder;

		try (Stream<String> lines = linesSafe(classpathFile.toPath())) {
			Optional<String> maybeFirstCpEntry = lines.findFirst();
			if (!maybeFirstCpEntry.isPresent())
				// should be unreachable, we already checked file is not empty
				return moduleBaseFolder;

			File file = new File(maybeFirstCpEntry.get());
			if (!file.isDirectory())
				return moduleBaseFolder;

			// in case we are debugging and AC has written a folder, i.e. the project is opened in IDE
			while (true) {
				file = file.getParentFile();
				if (file == null)
					return moduleBaseFolder;

				if (file.getName().equals(moduleArtifactId))
					// file denotes the java project of our artifact
					return file;
			}
		}
	}

	private static File moduleClasspathFile(File moduleBaseFolder) {
		return new File(moduleBaseFolder, "classpath");
	}

	private static Stream<String> linesSafe(Path classpathFilePath) {
		try {
			return Files.lines(classpathFilePath);

		} catch (IOException e) {
			throw new GenericModelException("Error while reading the lines from: " + classpathFilePath, e);
		}
	}

	// ###############################################################
	// ## . . . . . . . . . . Cortex priming . . . . . . . . . . . .##
	// ###############################################################

	public static void createPlatformModuleAndAssingToAllDeployables(PersistenceInitializationContext ctx, String groupId, String artifactId) {
		ManagedGmSession session = ctx.getSession();

		Module platformModule = ModuleLoaderHelper.moduleDenotation(session.create(Module.T), PLATFORM_MODULE_GLOBAL_ID, groupId, artifactId);

		List<Deployable> deployables = session.query().entities(EntityQueryBuilder.from(Deployable.T).done()).list();

		// Entities from EnvironmentDenotationRegistry cannot be automatically bound to platform 
		EnvironmentDenotationRegistry edr = session.getEntityByGlobalId(EnvironmentDenotationRegistry.ENVIRONMENT_DENOTATION_REGISTRY__GLOBAL_ID);
		deployables.removeAll(edr.getEntries().values());

		for (Deployable platformDeployable : deployables) {
			platformDeployable.setModule(platformModule);
			log.info("Auto-assigning platform as module for deployable: " + platformDeployable.shortDescription());
		}
	}

}
