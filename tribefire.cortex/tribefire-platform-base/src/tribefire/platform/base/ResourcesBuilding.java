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
package tribefire.platform.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

import com.braintribe.codec.marshaller.api.MarshallException;
import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.common.lcd.function.CheckedFunction;
import com.braintribe.utils.IOTools;

import tribefire.module.api.ResourceHandle;

/**
 * @author peter.gazdik
 */
public class ResourcesBuilding {

	public static class PathResourcesBuilder extends AbstractResourcesBuilder {

		private final Path path;

		public static PathResourcesBuilder create(Path base, String relativePath) throws InvalidPathException {
			relativePath = sanitizeRelativePath(relativePath);
			Path path = base.resolve(relativePath);
			return new PathResourcesBuilder(path);
		}

		public static PathResourcesBuilder create(String absolutePath) throws InvalidPathException {
			Path path = Paths.get(absolutePath);
			return new PathResourcesBuilder(path);
		}

		public PathResourcesBuilder(Path path) {
			this.path = path;
		}
		
		@Override
		public URL asUrl() {
			try {
				return this.path.toUri().toURL();
			} catch (MalformedURLException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public Path asPath() {
			return this.path;
		}

		@Override
		public File asFile() {
			return this.path.toFile();
		}

		@Override
		public <T> T asAssembly(Marshaller marshaller, T defaultValue) {
			if (Files.exists(path))
				return asAssembly(marshaller);
			else
				return defaultValue;
		}
		
		@Override
		public InputStream asStream() {
			try {
				return Files.newInputStream(this.path);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
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

	public static class UrlResourcesBuilder extends AbstractResourcesBuilder {

		private final URL url;

		public static UrlResourcesBuilder create(String value) throws MalformedURLException {
			return new UrlResourcesBuilder(new URL(value));
		}

		public UrlResourcesBuilder(URL url) {
			this.url = url;
		}

		@Override
		public URL asUrl() {
			return this.url;
		}

		@Override
		public Path asPath() {
			throw new UnsupportedOperationException("Path is not supported for " + url);
		}

		@Override
		public File asFile() {
			throw new UnsupportedOperationException("File is not supported for " + url);
		}

		@Override
		public InputStream asStream() {
			try {
				return this.url.openStream();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

	public static class ClasspathResourcesBuilder extends AbstractResourcesBuilder {

		private final String originalPath;
		private URI uri;
		private final ClassLoader classLoader;

		public static ClasspathResourcesBuilder create(String value) {
			return new ClasspathResourcesBuilder(value, ClasspathResourcesBuilder.class.getClassLoader());
		}

		public static ClasspathResourcesBuilder create(String value, ClassLoader classLoader) {
			return new ClasspathResourcesBuilder(value, classLoader);
		}

		public ClasspathResourcesBuilder(String originalPath, ClassLoader classLoader) {
			this.classLoader = classLoader;
			this.originalPath = sanitizePath(originalPath);
		}

		private URI getUri() throws IOException {
			URI uri = findUri();
			if (uri == null)
				throw new IOException("Classpath resource no found: " + originalPath);

			return uri;
		}

		private URI findUri() throws IOException {
			if (uri == null) {
				URL url = classLoader.getResource(originalPath);
				if (url == null)
					return null;

				try {
					uri = url.toURI();
				} catch (URISyntaxException e) {
					throw new IOException("Malformed URI: " + originalPath, e);
				}
			}

			return uri;

		}

		@Override
		public URL asUrl() {
			try {
				return getUri().toURL();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public Path asPath() {
			try {
				URI pathUri = getUri();

				Path path = uriToPath(pathUri);
				if (path == null)
					throw new IOException("No path could be resolved from [ " + pathUri + " ]");

				return path;

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public File asFile() {
			Path path = asPath();
			try {
				return path.toFile();
			} catch (Exception e) {
				throw new UnsupportedOperationException("Cannot represent [ " + (uri != null ? uri : path) + " ] as a java.io.File", e);
			}
		}

		@Override
		public <T> T asAssembly(Marshaller marshaller, T defaultValue) {
			if (uriExists())
				return asAssembly(marshaller);
			else
				return defaultValue;
		}

		private boolean uriExists() {
			try {
				return findUri() != null;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		@Override
		public InputStream asStream() {
			try {
				return asUrl().openStream();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private static String sanitizePath(String relativePath) {
			if (relativePath == null || relativePath.equals("") || relativePath.equals("/"))
				return "";
			else if (relativePath.startsWith("/"))
				return relativePath.substring(1);
			else
				return relativePath;
		}

	}

	private static abstract class AbstractResourcesBuilder implements ResourceHandle {

		@Override
		public String asString(String encoding) {
			return processStream(stream -> IOTools.slurp(stream, encoding));
		}

		public <T> T asAssembly(Marshaller marshaller, T defaultValue) throws MarshallException {
			try {
				return asAssembly(marshaller);

			} catch (UncheckedIOException e) {
				if (e.getCause() instanceof FileNotFoundException)
					return defaultValue;
				else
					throw e;
			}
		}

		public <T> T asAssembly(Marshaller marshaller) throws MarshallException {
			return (T) processStream(marshaller::unmarshall);
		}

		@Override
		public Properties asProperties() {
			return processStream(IOTools::readAsProperties);
		}
		
		protected <T> T processStream(CheckedFunction<InputStream, T, IOException>  processor) {
			try (InputStream stream = asStream()) {
				return processor.apply(stream);

			} catch (IOException e) {
				throw new UncheckedIOException(resourceErrorMessage(), e);

			} catch (Exception e) {
				throw new MarshallException(resourceErrorMessage(), e);
			}
		}

		protected String resourceErrorMessage() {
			try {
				return "Error while processing resource with URL: " + asUrl();
			} catch (Exception e) {
				return "Error while processing resource whose URL cannot be resolved due to: " + e.getMessage();
			}
		}
	}

	public static Path uriToPath(URI uri) throws IOException {
		try {
			return Paths.get(uri);

		} catch (FileSystemNotFoundException notFoundEx) {

			try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap())) {
				return fs.provider().getPath(uri);

			} catch (RuntimeException | IOException e) {
				e.addSuppressed(notFoundEx);
				throw e;
			}

		}

	}
}
