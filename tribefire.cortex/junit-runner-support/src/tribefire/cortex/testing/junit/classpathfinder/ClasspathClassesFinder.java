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
package tribefire.cortex.testing.junit.classpathfinder;

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;

/**
 * Utility class to find classes within the class path, both inside and outside of jar files. Inner and anonymous
 * classes are not being considered in the first place.
 */
public class ClasspathClassesFinder {

	private static final int CLASS_SUFFIX_LENGTH = ".class".length();

	private final ClasspathSuiteTester tester;

	private static final Logger log = Logger.getLogger(ClasspathClassesFinder.class);
	
	public ClasspathClassesFinder(String[] acceptedClassNames) {
		Class<?>[] baseTypes = { Object.class };

		this.tester = new ClasspathSuiteTester(acceptedClassNames, baseTypes, new Class[0]);
	}

	public List<Class<?>> find() {
		return findClassesInRoots(getClasspathRoots());
	}

	public List<String> getClasspathRoots() {
		return Arrays.stream(resolveClassLoaderURLs()) //
				.flatMap(ClasspathClassesFinder::resolveManifestDependencies) //
				.distinct() //
				.map(URL::getFile) //
				.collect(Collectors.toList());
	}

	private URL[] resolveClassLoaderURLs() {
		ClassLoader cl = getClass().getClassLoader();
		if (cl instanceof URLClassLoader) {
			return ((URLClassLoader) cl).getURLs();
		}

		throw new IllegalStateException("Cannot examine classes on the classpath as the Classloader is not a URLClassLoader, but: " + cl);
	}

	private static Stream<URL> resolveManifestDependencies(URL classRoot) {
		Optional<Manifest> possibleManifest;

		try {
			if (!isJarFile(classRoot.getPath())) {
				possibleManifest = Optional.empty();
			} else {
				URL jarUrl = new URL("jar", "", classRoot + "!/");
				JarURLConnection connection = (JarURLConnection) jarUrl.openConnection();
				possibleManifest = Optional.ofNullable(connection.getManifest());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		Stream<URL> jarDeps = possibleManifest //
				.map(m -> m.getMainAttributes().getValue("Class-Path")) //
				.map(classPath -> Stream.of(classPath.split(" ")) //
						.map(entryPath -> relativize(classRoot, entryPath)) //
						.filter(url -> url != null) //
				) //
				.orElse(Stream.empty());

		return Stream.concat(Stream.of(classRoot), jarDeps);
	}

	// Only called when processing Manifest Class-Path entries
	private static URL relativize(URL context, String path) {
		Path relativePath = Paths.get(path);
		if (relativePath.isAbsolute()) {
//			throw new IllegalArgumentException("Absolute path '" + path + "' found in 'Class-Path' attribute of jar manifest at '" + context + "'.");
			log.warn("Absolute path '" + path + "' found in 'Class-Path' attribute of jar manifest at '" + context + "'.");
			return null;
		}

		String contextPath = context.getPath();

		if (contextPath.contains(":") && contextPath.startsWith("/")) {
			contextPath = contextPath.substring(1);
		}

		Path dependencyPath = Paths.get(contextPath, "../", path) // we need to go one additional folder upwards because
																	// the path is relative to the .jar's parent folder
				.normalize();

		if (!dependencyPath.toFile().exists()) {
//			 throw new UncheckedIOException(new FileNotFoundException("Jar at '" + context + "' depends on non-existing '" + dependencyPath
//			 + "' according to the 'Class-Path' entry in its Manifest file."));
			log.warn("Jar at '" + context + "' depends on non-existing '" + dependencyPath
					+ "' according to the 'Class-Path' entry in its Manifest file.");
			return null;
		}
		try {
			return dependencyPath.toUri().toURL();
		} catch (MalformedURLException e) {
			throw Exceptions.unchecked(e, "Could not generate URL from jar manifest entry");
		}
	}

	private List<Class<?>> findClassesInRoots(List<String> roots) {
		List<Class<?>> classes = newList();
		for (String root : roots) {
			gatherClassesInRoot(new File(root), classes);
		}

		return classes;
	}

	private void gatherClassesInRoot(File classRoot, List<Class<?>> classes) {
		Iterable<String> relativeFilenames = new NullIterator<String>();
		if (isJarFile(classRoot.getName())) {
			try {
				relativeFilenames = new JarFilenameIterator(classRoot);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else if (classRoot.isDirectory()) {
			relativeFilenames = new RecursiveFilenameIterator(classRoot);
		}
		gatherClasses(classes, relativeFilenames);
	}

	private static boolean isJarFile(String classRootFilename) {
		return classRootFilename.endsWith(".jar") || classRootFilename.endsWith(".JAR");
	}

	private void gatherClasses(List<Class<?>> classes, Iterable<String> filenamesIterator) {
		for (String fileName : filenamesIterator) {
			if (!isClassFile(fileName)) {
				continue;
			}
			String className = classNameFromFile(fileName);
			if (!tester.acceptClassName(className)) {
				continue;
			}
			if (!tester.acceptInnerClass() && isInnerClass(className)) {
				continue;
			}
			try {
				Class<?> clazz = Class.forName(className, false, getClass().getClassLoader());
				if (clazz == null || clazz.isLocalClass() || clazz.isAnonymousClass()) {
					continue;
				}
				if (tester.acceptClass(clazz)) {
					classes.add(clazz);
				}
			} catch (ClassNotFoundException cnfe) {
				// ignore not instantiable classes
			} catch (NoClassDefFoundError ncdfe) {
				// ignore not instantiable classes
			} catch (ExceptionInInitializerError ciie) {
				// ignore not instantiable classes
			} catch (UnsatisfiedLinkError ule) {
				// ignore not instantiable classes
			}
		}
	}

	private boolean isInnerClass(String className) {
		return className.contains("$");
	}

	private boolean isClassFile(String classFileName) {
		return classFileName.endsWith(".class");
	}

	private String classNameFromFile(String classFileName) {
		// convert /a/b.class to a.b
		String s = replaceFileSeparators(cutOffExtension(classFileName));
		if (s.startsWith(".")) {
			return s.substring(1);
		}
		return s;
	}

	private String replaceFileSeparators(String s) {
		String result = s.replace(File.separatorChar, '.');
		if (File.separatorChar != '/') {
			// In Jar-Files it's always '/'
			result = result.replace('/', '.');
		}
		return result;
	}

	private String cutOffExtension(String classFileName) {
		return classFileName.substring(0, classFileName.length() - CLASS_SUFFIX_LENGTH);
	}

}