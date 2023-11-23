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
package com.braintribe.processing.test.web.undertow;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import com.braintribe.logging.Logger;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.RequestDumpingHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.api.ServletInfo;

/**
 * <p>
 * Managed {@link Undertow} server instance intended be used for test purposes only.
 *
 * <p>
 * New instances can be created and configured with the create() methods:
 *
 * <ul>
 * <li>{@link #create(String)}
 * <li>{@link #create(String, int, String)}
 * </ul>
 *
 */
public final class UndertowServer {

	private static final Logger log = Logger.getLogger(UndertowServer.class);

	// current running servers started by this classloader
	private static final Map<Integer, UndertowServer> SERVERS = new ConcurrentHashMap<>();

	// setup, default values
	private static final String SERVER_HOST = "localhost";
	private static final int SERVER_PORT_MIN = 7000;
	private static final int SERVER_PORT_MAX = 9999;
	private static final int STARTUP_MAX_RETRIES = 10;
	private static final boolean DUMP_REQUESTS = false;

	// instance variables
	private Undertow server;
	private URL serverUrl;
	private URL contextUrl;
	private Map<String, URL> servletUrls;
	private Path instanceTempFolder;

	private UndertowServer(Undertow server, URL serverUrl, URL contextUrl, Map<String, String> configuredServlets, Path instanceTempFolder) {

		this.server = server;
		this.serverUrl = serverUrl;
		this.contextUrl = contextUrl;
		this.instanceTempFolder = instanceTempFolder;

		log.info(() -> "Started Undertow server on [ " + serverUrl + " ]");

		if (configuredServlets != null && !configuredServlets.isEmpty()) {
			servletUrls = new HashMap<>(configuredServlets.size());
			String urlBase = contextUrl.toString();
			for (Entry<String, String> entry : configuredServlets.entrySet()) {
				try {
					URL servletUrl = new URI(urlBase + "/" + entry.getValue()).normalize().toURL();
					servletUrls.put(entry.getKey(), servletUrl);
					log.info(() -> "Servlet [ " + entry.getKey() + " ] is mapped to [ " + servletUrl + " ]");
				} catch (Exception e) {
					throw new UndeclaredThrowableException(e);
				}
			}
		}

	}

	public static UndertowServerBuilder create(String contextPath) {
		return create(SERVER_HOST, SERVER_PORT_MIN, contextPath);
	}

	public static UndertowServerBuilder create(String host, int port, String contextPath) {
		return new UndertowServerBuilderImpl(host, port, contextPath);
	}

	public static interface UndertowServerBuilder {

		UndertowServerBuilder addServlet(String name, HttpServlet instance, String... mappings);

		UndertowServerBuilder addServlet(String name, HttpServlet instance, boolean multipart, String... mappings);

		UndertowServerBuilder addServlet(String name, HttpServlet instance, Path multipartPath, String... mappings);

		UndertowServerBuilder addFilter(String name, Filter filter);

		UndertowServerBuilder addFilterServletNameMapping(String filterName, String mapping, DispatcherType dispatcher);

		UndertowServerBuilder addFilterUrlMapping(String filterName, String mapping, DispatcherType dispatcher);

		UndertowServerBuilder addListener(Class<? extends EventListener> listenerClass);

		UndertowServerBuilder addInitParameter(String key, String value);

		UndertowServerBuilder tempPath(Path tempPath);

		UndertowServerBuilder dumpRequests();

		UndertowServer start();

	}

	private static class UndertowServerBuilderImpl implements UndertowServerBuilder {

		private String host;
		private int initialPort;
		private String contextPath;
		private Map<String, ServletEntry> servlets = new HashMap<>();
		private DeploymentInfo deploymentInfo;
		private Path tempPath;
		private boolean tempPathInternal;
		private boolean dumpRequests = DUMP_REQUESTS;

		class ServletEntry {
			HttpServlet instance;
			String[] mappings;
			boolean multipart;
			Path multipartPath;
		}

		public UndertowServerBuilderImpl(String host, int port, String contextPath) {

			this.host = host;
			this.initialPort = port;
			this.contextPath = contextPath;

			// @formatter:off
			deploymentInfo =
				Servlets.deployment()
					.setClassLoader(UndertowServer.class.getClassLoader())
					.setContextPath(this.contextPath)
					.setDeploymentName(this.contextPath + ".war");
			// @formatter:on

		}

		@Override
		public UndertowServerBuilder addServlet(String name, HttpServlet instance, String... mappings) {
			addServlet(name, instance, mappings, false, null);
			return this;
		}

		@Override
		public UndertowServerBuilder addServlet(String name, HttpServlet instance, boolean multipart, String... mappings) {
			addServlet(name, instance, mappings, true, null);
			return this;
		}

		@Override
		public UndertowServerBuilder addServlet(String name, HttpServlet instance, Path multipartPath, String... mappings) {
			addServlet(name, instance, mappings, true, multipartPath);
			return this;
		}

		@Override
		public UndertowServerBuilder addFilter(String name, Filter filter) {
			deploymentInfo.addFilter(Servlets.filter(name, Filter.class, new FilterInstanceFactory(filter)));
			return this;
		}

		@Override
		public UndertowServerBuilder addFilterServletNameMapping(String filterName, String mapping, DispatcherType dispatcher) {
			deploymentInfo.addFilterServletNameMapping(filterName, mapping, dispatcher);
			return this;
		}

		@Override
		public UndertowServerBuilder addFilterUrlMapping(String filterName, String mapping, DispatcherType dispatcher) {
			deploymentInfo.addFilterUrlMapping(filterName, mapping, dispatcher);
			return this;
		}

		@Override
		public UndertowServerBuilder addListener(Class<? extends EventListener> listenerClass) {
			ListenerInfo listenerInfo = new ListenerInfo(listenerClass);
			deploymentInfo.addListener(listenerInfo);
			return this;
		}

		@Override
		public UndertowServerBuilder addInitParameter(String key, String value) {
			deploymentInfo.addInitParameter(key, value);
			return this;
		}

		@Override
		public UndertowServerBuilder tempPath(Path tempPath) {
			this.tempPath = tempPath;
			return this;
		}

		@Override
		public UndertowServerBuilder dumpRequests() {
			this.dumpRequests = true;
			return this;
		}

		private Path retrieveTempPath() {

			if (this.tempPath != null) {
				return this.tempPath;
			}

			// no custom path given, we create our own which we'll have to clean up later.
			this.tempPath = createTempFolder();
			this.tempPathInternal = true;

			return this.tempPath;

		}

		private Path createTempFolder() {
			try {
				return Files.createTempDirectory("undertow-");
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to create a dedicated temporary folder for this Undertow instance", e);
			}
		}

		@Override
		public UndertowServer start() {

			Map<String, String> servletMappings = new HashMap<>();

			// configure servlets
			for (Entry<String, ServletEntry> e : servlets.entrySet()) {

				String servletName = e.getKey();
				ServletEntry entry = e.getValue();

				ServletInfo servletInfo = Servlets.servlet(servletName, HttpServlet.class, new BasicInstanceFactory(entry.instance));

				if (entry.multipart) {
					if (entry.multipartPath != null) {
						servletInfo.setMultipartConfig(Servlets.multipartConfig(entry.multipartPath.toString(), -1, -1, -1));
					} else {
						servletInfo.setMultipartConfig(Servlets.multipartConfig(retrieveTempPath().toString(), -1, -1, -1));
					}
				}

				if (entry.mappings != null) {
					for (String mapping : entry.mappings) {
						servletMappings.putIfAbsent(servletName, mapping);
						servletInfo.addMapping(mapping);
					}
				}

				deploymentInfo.addServlet(servletInfo);

			}

			ServletContainer servletContainer = Servlets.newContainer();

			DeploymentManager manager = servletContainer.addDeployment(deploymentInfo);

			manager.deploy();

			try {
				HttpHandler handler = Handlers.path(Handlers.redirect(contextPath)).addPrefixPath(contextPath, manager.start());

				if (dumpRequests) {
					handler = new RequestDumpingHandler(handler);
				}

				if (log.isDebugEnabled()) {
					log.debug("Starting Undertow server.");
				}

				Holder<Integer> portHolder = new Holder<>();

				Undertow undertow = tryStart(handler, host, initialPort, portHolder);

				Integer port = portHolder.get();

				final URL serverUrl = new URL("http", host, port, "/");

				final URL contextUrl = serverUrl.toURI().resolve(contextPath).toURL();

				UndertowServer server = new UndertowServer(undertow, serverUrl, contextUrl, servletMappings, tempPathInternal ? tempPath : null);

				SERVERS.put(port, server);

				return server;

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (Exception e) {
				throw new UndeclaredThrowableException(e);
			}

		}

		protected Undertow tryStart(HttpHandler httpHandler, String host, int initialPort, Consumer<Integer> usedPortConsumer) throws Exception {

			for (int port = initialPort, retry = STARTUP_MAX_RETRIES;; port++) {

				if (SERVERS.get(port) != null) {
					// We already used this port.
					continue;
				}

				try {

					Undertow server = Undertow.builder().addHttpListener(port, host).setHandler(httpHandler).build();

					server.start();

					usedPortConsumer.accept(port);

					return server;

				} catch (Exception e) {

					// Somebody else is using the port.

					String msg = "Failed to start server on [ " + SERVER_HOST + ":" + port + " ]: " + e.getMessage();

					if (port == SERVER_PORT_MAX || retry == 0) {
						log.error(msg, e);
						throw e;
					}

					log.debug(msg + ". Retrying...");

					retry--;

					continue;

				}
			}

		}

		private void addServlet(String name, HttpServlet instance, String[] mappings, boolean multipart, Path multipartPath) {
			ServletEntry entry = new ServletEntry();
			entry.instance = instance;
			entry.mappings = mappings;
			entry.multipart = multipart;
			entry.multipartPath = multipartPath;
			servlets.put(name, entry);
		}
	}

	static final class Holder<T> implements Consumer<T>, Supplier<T> {

		private T value;

		@Override
		public void accept(T value) {
			this.value = value;
		}

		@Override
		public T get() {
			return value;
		}

	}

	public Undertow getServer() {
		return server;
	}

	public URL getServerUrl() {
		return serverUrl;
	}

	public URL getContextUrl() {
		return contextUrl;
	}

	public URL getServletUrl(String servletName) {
		if (servletUrls == null) {
			return null;
		}
		return servletUrls.get(servletName);
	}

	public void stop() {

		log.debug(() -> "Stopping Undertow server running on [ " + serverUrl + " ]");

		try {
			if (server != null) {
				synchronized (this) {
					if (server != null) {
						server.stop();
						server = null;
						log.info(() -> "Stopped Undertow server running on [ " + serverUrl + " ]");
					} else {
						log.warn(() -> "Undertow server running on [ " + serverUrl + " ] was already stopped");
					}
				}
			} else {
				log.warn(() -> "Undertow server running on [ " + serverUrl + " ] was already stopped");
			}

		} catch (Exception e) {
			throw new RuntimeException("Could not stop Undertow server running on [ " + serverUrl + " ]: " + e.getMessage(), e);
		} finally {
			deleteTempFolder();
		}
	}

	public void deleteTempFolder() {
		try {
			if (instanceTempFolder != null) {
				Files.walkFileTree(instanceTempFolder, new DeletingFileVisitor());
			}
		} catch (Exception e) {
			log.warn("Failed to delete the dedicated temporary folder created for this Undertow instance running on [ " + serverUrl + " ]: "
					+ e.getMessage());
		}
	}

	protected static class BasicInstanceFactory implements InstanceFactory<HttpServlet> {

		private final HttpServlet httpServlet;

		public BasicInstanceFactory(HttpServlet httpServlet) {
			this.httpServlet = httpServlet;
		}

		@Override
		public InstanceHandle<HttpServlet> createInstance() throws InstantiationException {
			return new InstanceHandle<HttpServlet>() {

				@Override
				public HttpServlet getInstance() {
					return httpServlet;
				}

				@Override
				public void release() {
					httpServlet.destroy();
				}

			};
		}

	}

	protected static class FilterInstanceFactory implements InstanceFactory<Filter> {

		private final Filter filter;

		public FilterInstanceFactory(Filter filter) {
			this.filter = filter;
		}

		@Override
		public InstanceHandle<Filter> createInstance() throws InstantiationException {
			return new InstanceHandle<Filter>() {

				@Override
				public Filter getInstance() {
					return filter;
				}

				@Override
				public void release() {
					filter.destroy();
				}

			};
		}

	}

	/**
	 * <p>
	 * A SimpleFileVisitor for deleting directories recursively.
	 */
	private static class DeletingFileVisitor extends SimpleFileVisitor<Path> {

		@Override

		public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {

			if (attributes.isRegularFile()) {

				if (log.isTraceEnabled()) {
					log.trace("Deleting regular file: [ " + file.getFileName() + " ] ");
				}

				try {
					Files.deleteIfExists(file);
				} catch (Exception e) {
					if (log.isDebugEnabled()) {
						log.debug("Failed to delete " + file + ", deleting on exit...");
					}
					file.toFile().deleteOnExit();
				}

				if (log.isDebugEnabled()) {
					log.debug("Deleted [ " + file.getFileName() + " ] ");
				}

			}

			return FileVisitResult.CONTINUE;

		}

		@Override

		public FileVisitResult postVisitDirectory(Path directory, IOException ioe) throws IOException {

			if (log.isTraceEnabled()) {
				log.trace("Deleting directory: [ " + directory.getFileName() + " ] ");
			}

			try {
				Files.deleteIfExists(directory);
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to delete " + directory + ", deleting on exit...");
				}
				directory.toFile().deleteOnExit();
			}

			if (log.isDebugEnabled()) {
				log.debug("Deleted [ " + directory.getFileName() + " ] ");
			}

			return FileVisitResult.CONTINUE;

		}

		@Override

		public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {

			log.error("Unable to delete [ " + file.getFileName() + " ] : " + exception.getMessage(), exception);

			return FileVisitResult.CONTINUE;

		}

	}

}
