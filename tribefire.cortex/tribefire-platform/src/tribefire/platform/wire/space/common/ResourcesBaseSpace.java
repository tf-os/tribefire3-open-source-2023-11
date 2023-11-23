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
package tribefire.platform.wire.space.common;
// ============================================================================
// BRAINTRIBE TECHNOLOGY GMBH - www.braintribe.com
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2018 - All Rights Reserved
// It is strictly forbidden to copy, modify, distribute or use this code without written permission
// To this file the Braintribe License Agreement applies.
// ============================================================================



import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.web.api.WebApps;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.api.resource.ResourcesBuilder;
import tribefire.platform.base.ResourcesBuilding;

@Managed
public abstract class ResourcesBaseSpace implements WireSpace {

	protected static Map<String, Function<String, ResourcesBuilder>> builders = new HashMap<>();

	@Import
	protected EnvironmentSpace environment;

	protected void onLoaded() {
		builders.put("server", this::server);
		builders.put("webinf", this::webInf);
		builders.put("file", this::file);
		builders.put("classpath", this::classpath);
	}

	@Managed
	public Path webinfPath() {
		Path bean = resolveWebInfPath();
		return bean;
	}

	@Managed
	public Path serverPath() {
		try {
			Path bean = Paths.get(TribefireRuntime.getContainerRoot());
			return bean;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve server root path");
		}
	}
	
	@Managed
	public Path installationRootPath() {
		try {
			Path bean = Paths.get(TribefireRuntime.getInstallationRoot());
			return bean;
		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to resolve installation root path");
		}
	}

	/**
	 * Resolves a path relative to the WEB-INF directory.
	 * <p>
	 * The following calls are equivalent:
	 * 
	 * <pre>
	 * webInf("my-sub-folder");
	 * resource("webinf:my-sub-folder");
	 * </pre>
	 * 
	 * @param path
	 *            A path relative to the WEB-INF directory
	 * @return A {@link ResourcesBuilder} for further operation on the resolved resource reference.
	 */
	public ResourcesBuilder webInf(String path) {
		return PathResourcesBuilder.create(webinfPath(), resolve(path));
	}

	/**
	 * Resolves a path relative to the container's root directory.
	 * <p>
	 * The following calls are equivalent:
	 * 
	 * <pre>
	 * server("my-sub-folder");
	 * resource("server:my-sub-folder");
	 * resource("${TRIBEFIRE_CONTAINER_ROOT_DIR}/my-sub-folder");
	 * </pre>
	 * 
	 * @param path
	 *            A path relative to the container's root directory
	 * @return A {@link ResourcesBuilder} for further operation on the resolved resource reference.
	 */
	public ResourcesBuilder server(String path) {
		return PathResourcesBuilder.create(serverPath(), resolve(path));
	}

	/**
	 * Resolves a classpath path
	 * <p>
	 * The following calls are equivalent:
	 * 
	 * <pre>
	 * classpath("com/braintribe/my-resource.ini");
	 * resource("classpath:com/braintribe/my-resource.ini");
	 * 
	 * @param path
	 *            A classpath path
	 * @return A {@link ResourcesBuilder} for further operation on the resolved resource reference.
	 */
	public ResourcesBuilder classpath(String path) {
		return ClasspathResourcesBuilder.create(resolve(path));
	}

	/**
	 * Resolves the given path as an absolute or prefixed path.
	 * <p>
	 * It supports placeholders referencing tribefire runtime properties (system properties and environment variables).
	 * <p>
	 * The following prefixes are supported: {@code file:}, {@code server:}, {@code webinf:} and {@code classpath:}.
	 * <p>
	 * {@code file:} precedes an absolute path to a file resource. This prefix can be omitted, as it is the default
	 * behavior when no prefix is given.
	 * <p>
	 * {@code server:} precedes a path relative to the container's root directory. Equivalent of calling
	 * {@link #server(String)}.
	 * <p>
	 * {@code webinf:} precedes a path relative to the WEB-INF directory. Equivalent of calling {@link #webInf(String)}.
	 * <p>
	 * {@code classpath:} precedes a classpath path. Equivalent of calling {@link #classpath(String)}.
	 * 
	 * @param path
	 *            The path to be resolved.
	 * @return A {@link ResourcesBuilder} for further operation on the resolved resource reference.
	 */
	public ResourcesBuilder resource(String path) {

		path = resolve(path);

		int i = path.indexOf(":");

		if (i < 1) {
			return PathResourcesBuilder.create(path);
		}

		String[] parts = path.split(":");

		Function<String, ResourcesBuilder> builderProvider = builders.get(parts[0]);

		if (builderProvider == null) {
			return url(path);
		}

		return builderProvider.apply(parts[1]);

	}

	/**
	 * <p>
	 * Resolves possible place-holders in the provided String.
	 */
	protected String resolve(String url) {

		if (url == null) {
			throw new IllegalArgumentException("URL cannot be null");
		}

		String resolvedUrl = environment.resolve(url);

		return resolvedUrl;

	}

	private ResourcesBuilder url(String url) {
		ResourcesBuilder builder = null;
		try {
			builder = PathResourcesBuilder.create(url);
		} catch (InvalidPathException pathEx) {
			try {
				builder = UrlResourcesBuilder.create(url);
			} catch (Exception urlEx) {
				IOException e = new IOException("Failed to get a resource buider for [ " + url + " ]", urlEx);
				e.addSuppressed(pathEx);
				throw new UncheckedIOException(e);
			}
		}
		return builder;
	}

	protected ResourcesBuilder file(String url) {
		return PathResourcesBuilder.create(url);
	}

	public static class PathResourcesBuilder extends ResourcesBuilding.PathResourcesBuilder implements ResourcesBuilder {

		public static PathResourcesBuilder create(Path base, String relativePath) throws InvalidPathException {
			relativePath = sanitizeRelativePath(relativePath);
			Path path = base.resolve(relativePath);
			return new PathResourcesBuilder(path);
		}

		public static PathResourcesBuilder create(String absolutePath) throws InvalidPathException {
			Path path = Paths.get(absolutePath);
			return new PathResourcesBuilder(path);
		}

		private PathResourcesBuilder(Path path) {
			super(path);
		}
		
		private static String sanitizeRelativePath(String relativePath) {
			if (relativePath == null || relativePath.equals("") || relativePath.equals("/")) {
				return ".";
			} else if (relativePath.startsWith("/")) {
				return relativePath.substring(1);
			}
			return relativePath;
		}

	}

	public static class UrlResourcesBuilder extends ResourcesBuilding.UrlResourcesBuilder implements ResourcesBuilder {

		public static UrlResourcesBuilder create(String value) throws MalformedURLException {
			return new UrlResourcesBuilder(new URL(value));
		}

		private UrlResourcesBuilder(URL url) {
			super(url);
		}

	}

	public static class ClasspathResourcesBuilder extends  ResourcesBuilding.ClasspathResourcesBuilder implements ResourcesBuilder {

		public static ClasspathResourcesBuilder create(String value) {
			return new ClasspathResourcesBuilder(value, ClasspathResourcesBuilder.class.getClassLoader());
		}

		public static ClasspathResourcesBuilder create(String value, ClassLoader classLoader) {
			return new ClasspathResourcesBuilder(value, classLoader);
		}

		private ClasspathResourcesBuilder(String originalPath, ClassLoader classLoader) {
			super(originalPath, classLoader);
		}

	}

	protected Path resolveWebInfPath() {
		Path path = null;
		try {
			URI uri = WebApps.servletContext().getResource("/WEB-INF").toURI();
			path = ResourcesBuilding.uriToPath(uri);
		} catch (Exception invalidUriEx) {
			try {
				path = Paths.get(WebApps.servletContext().getRealPath("/WEB-INF"));
			} catch (Exception realPathEx) {
				realPathEx.addSuppressed(invalidUriEx);
				throw Exceptions.unchecked(realPathEx, "Failed to resolve WEB-INF path");
			}
		}
		return path;
	}


}
