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

import static com.braintribe.utils.ReflectionTools.findPublicField;
import static com.braintribe.utils.ReflectionTools.getNoArgumentConstructor;
import static com.braintribe.utils.ReflectionTools.getStaticFieldValue;
import static com.braintribe.utils.ReflectionTools.newInstance;
import static java.lang.reflect.Modifier.isStatic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.codec.marshaller.api.GmDeserializationOptions;
import com.braintribe.codec.marshaller.yaml.YamlMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.module.WireModule;

import tribefire.descriptor.model.ModuleDescriptor;
import tribefire.descriptor.model.ModulePackagingInfo;
import tribefire.module.wire.contract.TribefireModuleContract;

/**
 * @author peter.gazdik
 */
/* package */ class WiringLoader {

	private static final GmDeserializationOptions packagingInfoYamlOptions = GmDeserializationOptions.deriveDefaults() //
			.setInferredRootType(ModulePackagingInfo.T) //
			.build();

	private final ModuleDescriptor moduleDescriptor;

	/**
	 * The wiringClassLoader is the loader to load the {@link TribefireModuleContract} implementation and any other relevant
	 * contracts. Therefore, it is the classLoader that "sees" the module jar, so either the same as platform class loader
	 * (if the module was fully integrated), or the {@link ModuleClassLoader}.
	 */
	private final ClassLoader wiringClassLoader;

	public WiringLoader(ModuleDescriptor moduleDescriptor, ClassLoader wiringClassLoader) {
		this.moduleDescriptor = moduleDescriptor;
		this.wiringClassLoader = wiringClassLoader;
	}

	public ClassLoader getWiringClassLoader() {
		return wiringClassLoader;
	}

	/**
	 * Loads the module's {@link WireModule} based on the {@link ModulePackagingInfo} read from "packaging-info.yml" file.
	 */
	public WireModule loadModuleWiring() {
		Class<? extends WireModule> moduleWiringClass = loadModuleWiringClass();

		WireModule result = loadFromStaticFieldIfRelevantFieldExists(moduleWiringClass);

		if (result == null)
			result = loadViaNoArgConstructorIfItExists(moduleWiringClass);

		if (result == null)
			throw new GenericModelException("Unable to load module: " + moduleName() + ". Cannot retrieve module's WireModule instance."
					+ " No public static property called 'INSTANCE' or public no-arg constructor were found!");

		return result;
	}

	private Class<? extends WireModule> loadModuleWiringClass() {
		ModulePackagingInfo packagingInfo = loadModulePackagingInfo();

		return loadModuleWiringClass(packagingInfo);
	}

	private ModulePackagingInfo loadModulePackagingInfo() {
		URL resource = getPackagingInfo();

		try (InputStream in = resource.openStream()) {
			return (ModulePackagingInfo) new YamlMarshaller().unmarshall(in, packagingInfoYamlOptions);

		} catch (IOException e) {
			throw new GenericModelException("Unable to load module: " + moduleName() + ". Error while reading 'packaging-info.yml'.");
		}
	}

	private URL getPackagingInfo() {
		List<URL> allResources = listAllPackagingInfos();

		List<URL> resourceOfModule = allResources.stream() //
				.filter(this::isResourceOfCurrentModule) //
				.collect(Collectors.toList());

		switch (resourceOfModule.size()) {
			case 0:
				throw new GenericModelException("Unable to load module: " + moduleName() + ", 'packaging-info.yml' not found. List of all resources: "
						+ allResources.stream().map(u -> u.toString()).collect(Collectors.joining(",")) + ", list of current module: "
						+ resourceOfModule.stream().map(u -> u.toString()).collect(Collectors.joining(",")) + ", artifactId: "
						+ moduleDescriptor.getArtifactId());
			case 1:
				return resourceOfModule.get(0);
			default:
				throw new GenericModelException("Unable to load module: " + moduleName() + ", Multiple 'packaging-info.yml' files found. URLs: "
						+ resourceOfModule.stream().map(URL::getPath).collect(Collectors.joining(", ")));
		}
	}

	private ArrayList<URL> listAllPackagingInfos() {
		try {
			return Collections.list(wiringClassLoader.getResources("packaging-info.yml"));

		} catch (IOException e) {
			throw new RuntimeException("Error while retrieving all `packaging-info.yml` files", e);
		}
	}

	private boolean isResourceOfCurrentModule(URL url) {
		return moduleDescriptor.getArtifactId().equals(resolveArtifactIdOf(url));
	}

	private String resolveArtifactIdOf(URL url) {
		switch (url.getProtocol()) {
			case "file":
				return resolveArtifactIdFromFile(url);
			case "jar":
				return resolveArtifactIdFromJar(url);
			default:
				throw new IllegalArgumentException(
						"Unsupported URL protocol for resolving `packaging-info.yml` file for module: " + moduleDescriptor.name() + " URL: " + url);
		}
	}

	// /xyz-module/classes/packaging-info.yml
	private String resolveArtifactIdFromFile(URL url) {
		File file = new File(url.getFile());
		return file.getParentFile().getParentFile().getName();
	}

	// /a/b/c/xyz-module-5.6.1.jar!/packaging-info.yml
	private String resolveArtifactIdFromJar(URL url) {
		String path = url.getPath();
		String s = StringTools.removeSuffix(path, ".jar!/packaging-info.yml");

		int i = s.lastIndexOf("/");
		s = (i < 0) ? s : s.substring(i + 1);

		s = StringTools.removeSuffixIfEligible(s, "-pc");
		s = StringTools.removeSuffixIfEligible(s, "-rc");

		i = s.lastIndexOf("-");
		if (i < 0)
			throw new IllegalArgumentException("Unsupported URL format. URL: " + url);
		s = s.substring(0, i);

		return s;
	}

	private Class<? extends WireModule> loadModuleWiringClass(ModulePackagingInfo pi) {
		String wireModuleClassName = pi.getWireModule();

		try {
			return Class.forName(wireModuleClassName, true, wiringClassLoader).asSubclass(WireModule.class);

		} catch (ClassNotFoundException e) {
			throw new GenericModelException("Unable to load module: " + moduleName() + ". Wire module class not found: " + wireModuleClassName, e);

		} catch (Throwable t) {
			Exceptions.contextualize(t, "Unable to load module: " + moduleName());
			throw t;
		}
	}

	private static WireModule loadFromStaticFieldIfRelevantFieldExists(Class<? extends WireModule> moduleWiringClass) {
		Field f = findPublicField(moduleWiringClass, "INSTANCE");

		boolean isFieldWeCanUse = f != null && isStatic(f.getModifiers()) && WireModule.class.isAssignableFrom(f.getType());

		return isFieldWeCanUse ? getStaticFieldValue(f) : null;
	}

	private static WireModule loadViaNoArgConstructorIfItExists(Class<? extends WireModule> moduleWiringClass) {
		Constructor<? extends WireModule> c = getNoArgumentConstructor(moduleWiringClass);

		return c == null ? null : newInstance(c);
	}

	private String moduleName() {
		return moduleDescriptor.name();
	}

}
