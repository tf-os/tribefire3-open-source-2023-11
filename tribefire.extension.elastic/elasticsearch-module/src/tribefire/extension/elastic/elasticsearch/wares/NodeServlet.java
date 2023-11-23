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
/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package tribefire.extension.elastic.elasticsearch.wares;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.elasticsearch.action.admin.cluster.node.info.PluginsAndModules;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.util.concurrent.ThreadContext;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.shutdown.JvmShutdownWatcher;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.utils.lcd.StopWatch;
import com.braintribe.web.api.WebApps;

import tribefire.extension.elastic.elasticsearch.PluginEnabledNode;
import tribefire.extension.elastic.elasticsearch.TfSystemPlugin;

/**
 * A servlet that can be used to dispatch requests to elasticsearch. A {@link Node} will be started, reading config from
 * either <tt>elasticsearch.json</tt> or <tt>elasticsearch.yml</tt> but, by default, with its internal HTTP interface
 * disabled.
 * <p/>
 * <p>
 * The node is registered as a servlet context attribute under <tt>elasticsearchNode</tt> so it is easily accessible
 * from other web resources if needed.
 * <p/>
 * <p>
 * The servlet can be registered under a prefix URI, and it will automatically adjust to handle it.
 */
public class NodeServlet extends HttpServlet implements LifecycleAware {

	private static final Logger logger = Logger.getLogger(NodeServlet.class);

	private ClassLoader moduleClassLoader;

	private static final long serialVersionUID = -8400258212829102951L;
	public static String NODE_KEY = "elasticsearchNode";
	public static String NAME_PREFIX = "org.elasticsearch.";

	protected PluginEnabledNode node;

	protected RestController restController;
	protected NamedXContentRegistry namedXContentRegistry;

	protected File jsonConfiguration = null;
	protected File ymlConfiguration = null;
	protected Map<String, String> additionalParameters = null;
	protected boolean httpEnabled = false;

	protected File configurationPath = null;
	protected File dataPath = null;
	protected File logPath = null;

	protected File basePath = null;
	protected File elasticPath = null;

	protected String publishHost = null;
	protected Set<String> bindHosts = null;
	protected Integer httpPort = 9200;
	protected Integer port = 9300;
	protected String nodeName = null;
	protected String clusterName = null;
	protected String externalId = null;

	protected Set<Class<? extends Plugin>> pluginClasses = null;
	protected Set<String> repositoryPaths = null;

	protected static PluginEnabledNode sharedNode = null;

	protected int maxClauseCount = 10240;

	protected String pathIdentifier = null;

	// Cluster-specific settings
	protected Integer recoverAfterNodes = null;
	protected Integer expectedNodes = null;
	protected Integer recoverAfterTimeInS = null;
	protected Set<String> clusterNodes = null;

	protected static CountDownLatch shutDownProceeding = null;
	protected Thread startupThread = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	public void postConstruct() {
		JvmShutdownWatcher.addThreadToIgnore(".*elastic.*");

		StopWatch stopWatch = new StopWatch();

		CountDownLatch cd = shutDownProceeding;
		if (cd != null) {
			try {
				cd.await(20L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.debug(() -> "Got interrupted while waiting for shutdown procedure to finish.");
				return;
			}
			stopWatch.intermediate("Waiting for previous shutdown");
		}
		shutDownProceeding = null;

		final Object nodeAttribute = sharedNode;
		if (nodeAttribute == null || !(nodeAttribute instanceof Node)) {
			if (nodeAttribute != null) {
				logger.info("Warning: overwriting attribute with key \"" + NODE_KEY + "\" and type \"" + nodeAttribute.getClass().getName() + "\".");
			}
			logger.debug("Initializing elasticsearch Node");
			// TODO: check migration from 2.2.1
			// Settings.Builder settings = Settings.settingsBuilder();
			Settings.Builder settings = Settings.builder();

			this.loadConfiguration(this.getJsonConfiguration(), settings);
			this.loadConfiguration(this.getYmlConfiguration(), settings);

			stopWatch.intermediate("Load Configuration");

			if (this.additionalParameters != null) {
				logger.debug("Setting additional parameters: " + this.additionalParameters);
				for (Map.Entry<String, String> entry : this.additionalParameters.entrySet()) {
					settings.put(entry.getKey(), entry.getValue());
				}
			}
			if (this.repositoryPaths != null && !this.repositoryPaths.isEmpty()) {
				logger.debug("Setting repository paths: " + this.repositoryPaths);
				settings.putArray("path.repo", this.repositoryPaths.toArray(new String[this.repositoryPaths.size()]));
			}

			this.overridePathSetting(settings, "path.conf", this.getConfigurationPath());
			this.overridePathSetting(settings, "path.data", this.getDataPath());
			this.overridePathSetting(settings, "path.logs", this.getLogPath());
			this.overridePathSetting(settings, "path.home", this.getElasticPath());

			settings.put("node.name", this.getNodeName());
			settings.put("indices.query.bool.max_clause_count", this.maxClauseCount);
			// settings.put("index.max_result_window", maxResultWindow.intValue());

			this.setNetworkSettings(settings);
			this.setClusterSettings(settings);

			stopWatch.intermediate("Override Configuration");

			Settings builtSettings = settings.build();

			if (logger.isDebugEnabled()) {
				logger.debug("Starting Elastic node with settings: " + builtSettings.getAsMap());
			}

			startupThread = new Thread(() -> {

				ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
				try {
					if (this.moduleClassLoader != null) {
						Thread.currentThread().setContextClassLoader(this.moduleClassLoader);
					}

					Instant start = NanoClock.INSTANCE.instant();

					this.node = new PluginEnabledNode(builtSettings, this.getPluginClasses());
					this.node.start();

					// this.installPlugins(builtSettings);
					logPlugins();

					// node = NodeBuilder.nodeBuilder().settings(settings).node();

					sharedNode = this.node;

					if (node != null) {
						namedXContentRegistry = TfSystemPlugin.getxContentRegistry();
						restController = TfSystemPlugin.getRestController();
					}

					logger.debug(() -> "Starting Elastic service took " + StringTools.prettyPrintDuration(start, true, ChronoUnit.MILLIS));

				} catch (Throwable e) {
					logger.error("Could not startup node with settings: " + builtSettings.getAsMap(), e);
				} finally {
					startupThread = null;
					Thread.currentThread().setContextClassLoader(origClassLoader);
				}

			});
			startupThread.setDaemon(true);
			startupThread.setName("Elastic Startup");
			startupThread.setContextClassLoader(NodeServlet.class.getClassLoader());
			startupThread.start();

			stopWatch.intermediate("Node Start");

		} else {
			logger.debug("Using pre-initialized elasticsearch Node");
			this.node = (PluginEnabledNode) nodeAttribute;
		}

		logger.debug(() -> "NodeServlet postConstruct took: " + stopWatch);
	}

	protected void logPlugins() {

		StringBuilder sb = new StringBuilder();

		PluginsService pluginsService = this.node.getPluginsService();
		if (pluginsService != null) {
			PluginsAndModules info = pluginsService.info();
			/* PluginsInfo info = pluginsService.info(); */
			if (info != null) {
				List<PluginInfo> pluginInfos = info.getPluginInfos();
				/* List<PluginInfo> pluginInfos = info.getInfos(); */
				if (pluginInfos != null) {
					for (PluginInfo pluginInfo : pluginInfos) {
						sb.append(pluginInfo.getName());
						sb.append(": ");
						sb.append(pluginInfo.getClassname());
						sb.append("\n");
					}
				}
			}
		}
		logger.debug("Classpath Plugins: " + sb.toString());
	}

	protected void setNetworkSettings(Builder settings) {
		if (this.port != null && this.port > 0) {
			logger.debug("Using port: " + this.port);
			settings.put("transport.tcp.port", this.port.intValue());
		}

		if (this.httpPort != null && this.httpPort > 0) {
			logger.debug("Using httpPort: " + this.httpPort);
			settings.put("http.port", this.httpPort.intValue());

		}

		String pHost = this.getPublishHost();
		if (pHost != null) {
			settings.put("network.publish_host", pHost);
		}
		Set<String> bindHostSet = this.getBindHosts();
		if (bindHostSet != null && !bindHostSet.isEmpty()) {
			String[] bindHostArray = bindHostSet.toArray(new String[bindHostSet.size()]);
			settings.putArray("network.bind_host", bindHostArray);
		}
	}

	protected void setClusterSettings(Builder settings) {
		settings.put("cluster.name", this.clusterName);
		if (this.recoverAfterNodes != null) {
			settings.put("gateway.recover_after_nodes", this.recoverAfterNodes.intValue());
		}
		if (this.expectedNodes != null) {
			settings.put("gateway.expected_nodes", this.expectedNodes.intValue());
		}
		if (this.recoverAfterTimeInS != null) {
			settings.put("recover_after_time", "" + this.recoverAfterTimeInS.intValue() + "s");
		}
		if (this.clusterNodes != null && !this.clusterNodes.isEmpty()) {

			// Add port if it's missing. Assumption is that the other nodes will use the same port
			Set<String> nodesAndPorts = new HashSet<>();
			clusterNodes.forEach(n -> {
				if (n.indexOf(':') == -1) {
					n = n + ":" + port;
					nodesAndPorts.add("[" + n + "]:" + port);
				}
				nodesAndPorts.add(n);
			});

			Set<String> hostAddresses = NetworkTools.getHostAddresses(null, true);
			Set<String> myAddressesAndPort = hostAddresses.stream().map(h -> h + ":" + port).collect(Collectors.toSet());
			myAddressesAndPort.add("localhost:" + port);

			logger.debug("Other cluster nodes: " + nodesAndPorts + ", my addresses: " + myAddressesAndPort);

			Iterator<String> iterator = nodesAndPorts.iterator();
			while (iterator.hasNext()) {
				String clusterNode = iterator.next();

				if (myAddressesAndPort.contains(clusterNode)) {
					// ignore own IP address
					iterator.remove();
				}
			}

			logger.debug("Other cluster nodes: " + nodesAndPorts);

			String[] clusterNodesArray = nodesAndPorts.toArray(new String[nodesAndPorts.size()]);

			settings.putArray("discovery.zen.ping.unicast.hosts", clusterNodesArray);
			settings.put("node.max_local_storage_nodes", nodesAndPorts.size() + 1);

		}

	}

	protected void overridePathSetting(Builder settings, String key, File path) {
		settings.put(key, path.getAbsolutePath());
	}

	protected void loadConfiguration(File file, Settings.Builder settings) throws RuntimeException {
		if (file != null && file.exists()) {
			try (FileInputStream fis = new FileInputStream(file)) {
				settings.loadFromStream(file.getAbsolutePath(), fis);
				logger.debug("Settings loaded from " + file.getAbsolutePath());
			} catch (Exception e) {
				throw new RuntimeException("Error while trying to read configuration from " + file.getAbsolutePath(), e);
			}
		}
	}

	private boolean waitForStartupFinished(boolean cancelIfRunning) {
		Thread sut = startupThread;
		if (sut != null) {
			try {
				sut.join(Numbers.MILLISECONDS_PER_SECOND * 20);
				if (cancelIfRunning && sut.isAlive()) {
					logger.debug(() -> "Trying to interrupt service startup");
					sut.interrupt();
				}
			} catch (InterruptedException e) {
				logger.debug(() -> "Got interrupted while waiting for startup to finish.");
				return false;
			}
		}
		return true;
	}

	@Override
	public void preDestroy() {
		waitForStartupFinished(true);

		if (node != null) {
			sharedNode = null;

			shutDownProceeding = new CountDownLatch(1);

			Thread t = new Thread(this::closeNode);
			t.setDaemon(true);
			t.setName("Elastic Node Shutdown");
			t.start();
		}
	}

	private void closeNode() {
		Map<String, String> nodeSettingsInformation = null;
		try {
			nodeSettingsInformation = getNodeSettings(node);

			// TODO: check migration from 2.2.1
			node.close();
		} catch (IOException e) {
			String msg = "Could not close node with settings: " + nodeSettingsInformation + " - " + e.getMessage()
					+ " - continue anyway. Check debug log for details!";
			logger.warn(msg);
			logger.debug(msg, e);
		} finally {
			CountDownLatch cd = shutDownProceeding;
			if (cd != null) {
				cd.countDown();
			}
		}
	}

	protected Map<String, String> getNodeSettings(Node node) {
		Environment environment = node.getEnvironment();
		Map<String, String> nodeSettingsInformation = new HashMap<>();
		nodeSettingsInformation.put("initial", "Could not determine node settings");
		if (environment != null) {
			Settings settings = environment.settings();
			if (settings != null) {
				nodeSettingsInformation = settings.getAsMap();
			}
		}

		return nodeSettingsInformation;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		waitForStartupFinished(false);

		Map<String, List<String>> headerMap = new HashMap<>();
		Enumeration<String> headerNames = req.getHeaderNames();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				Enumeration<String> headers = req.getHeaders(name);
				if (headers != null) {
					List<String> values = new ArrayList<String>();
					headerMap.put(name, values);
					while (headers.hasMoreElements()) {
						String value = headers.nextElement();
						values.add(value);
					}
				}
			}
		}

		String fullUrl = getFullURL(req);
		logger.trace(() -> fullUrl);

		ServletRestRequest request = new ServletRestRequest(this.pathIdentifier, req, namedXContentRegistry, fullUrl, headerMap);
		ServletRestChannel channel = new ServletRestChannel(request, resp);
		try {
			// TODO: check migration from 2.2.1
			// restController.dispatchRequest(request, channel);
			ThreadContext threadContext = createThreadContext(req);
			restController.dispatchRequest(request, channel, threadContext);
			channel.latch.await();
		} catch (Exception e) {
			throw new IOException("failed to dispatch request", e);
		}
		if (channel.sendFailure != null) {
			throw channel.sendFailure;
		}
	}

	public static String getFullURL(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder(request.getRequestURL());
		String queryString = request.getQueryString();

		if (queryString == null) {
			return sb.toString();
		} else {
			return sb.append('?').append(queryString).toString();
		}
	}

	protected ThreadContext createThreadContext(HttpServletRequest req) {
		Settings settings = Settings.builder().put("ServletClass", this.getClass()).put("RestController.nodeName", restController.nodeName())
				.put("RestController.toString", restController.toString()).put("RequestURL", req.getRequestURL())
				.put("ServletPath", req.getServletPath()).build();
		ThreadContext threadContext = new ThreadContext(settings);
		return threadContext;
	}

	static class ServletRestChannel extends AbstractServletRestChannel {

		final HttpServletResponse resp;

		final CountDownLatch latch;

		IOException sendFailure;

		ServletRestChannel(RestRequest restRequest, HttpServletResponse resp) {
			super(restRequest);
			this.resp = resp;
			this.latch = new CountDownLatch(1);
		}

		@Override
		protected HttpServletResponse getServletResponse() {
			return resp;
		}

		@Override
		protected void errorOccured(IOException e) {
			sendFailure = e;
		}

		@Override
		protected void finish() {
			latch.countDown();
		}
	}

	@Configurable
	public void setJsonConfiguration(File jsonConfiguration) {
		this.jsonConfiguration = jsonConfiguration;
	}
	public File getJsonConfiguration() {
		if (this.jsonConfiguration == null) {
			this.jsonConfiguration = new File(this.getConfigurationPath(), "elasticsearch.json");
		}
		return this.jsonConfiguration;
	}
	@Configurable
	public void setYmlConfiguration(File ymlConfiguration) {
		this.ymlConfiguration = ymlConfiguration;
	}
	public File getYmlConfiguration() {
		if (this.ymlConfiguration == null) {
			this.ymlConfiguration = new File(this.getConfigurationPath(), "elasticsearch.yml");
		}
		return this.ymlConfiguration;
	}
	@Configurable
	public void setAdditionalParameters(Map<String, String> additionalParameters) {
		this.additionalParameters = additionalParameters;
	}
	@Configurable
	public void setHttpEnabled(boolean httpEnabled) {
		this.httpEnabled = httpEnabled;
	}
	public File getConfigurationPath() {
		if (this.configurationPath == null) {
			this.configurationPath = new File(this.getBasePath(), "config");
		}
		return this.configurationPath;
	}
	@Configurable
	public void setConfigurationPath(File configurationPath) {
		this.configurationPath = configurationPath;
	}

	public File getDataPath() {
		if (this.dataPath == null) {
			this.dataPath = new File(this.getBasePath(), "data");
		}
		if (!this.dataPath.exists()) {
			this.dataPath.mkdirs();
		}
		return this.dataPath;
	}
	@Configurable
	public void setDataPath(File dataPath) {
		this.dataPath = dataPath;
	}

	public File getLogPath() {
		if (this.logPath == null) {
			this.logPath = new File(this.getBasePath(), "log");
		}
		if (!this.logPath.exists()) {
			this.logPath.mkdirs();
		}
		return this.logPath;
	}
	@Configurable
	public void setLogPath(File logPath) {
		this.logPath = logPath;
	}

	public File getBasePath() {
		if (basePath == null) {
			File realPath = WebApps.realPath();
			basePath = new File(realPath, "resources/res");
		}
		return basePath;
	}
	@Configurable
	public void setBasePath(File basePath) {
		this.basePath = basePath;
	}

	public File getElasticPath() {
		if (elasticPath == null) {
			File realPath = WebApps.realPath();
			elasticPath = new File(realPath, "resources/elastic");
		}
		return elasticPath;
	}
	@Configurable
	public void setElasticPath(File elasticPath) {
		this.elasticPath = elasticPath;
	}

	@Configurable
	@Required
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@Configurable
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getNodeName() {
		if (this.nodeName == null || this.nodeName.trim().length() == 0 || this.nodeName.equals("null")) {
			this.nodeName = this.externalId + "@" + TribefireRuntime.getProperty(TribefireRuntime.ENVIRONMENT_NODE_ID);
		}
		return this.nodeName;
	}
	@Configurable
	public void setPluginClasses(Set<Class<? extends Plugin>> pluginClasses) {
		this.pluginClasses = pluginClasses;
	}
	public Set<Class<? extends Plugin>> getPluginClasses() {
		if (this.pluginClasses == null) {
			this.pluginClasses = Collections.<Class<? extends Plugin>> emptySet();
		}
		this.pluginClasses.add(TfSystemPlugin.class);
		return this.pluginClasses;
	}
	@Configurable
	public void setPathIdentifier(String pathIdentifier) {
		this.pathIdentifier = pathIdentifier;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Configurable
	public void setPort(Integer port) {
		if (port != null && port > 0) {
			this.port = port;
		}
	}

	@Configurable
	public void setHttpPort(Integer httpPort) {
		if (httpPort != null && httpPort > 0) {
			this.httpPort = httpPort;
		}
	}

	@Configurable
	public void setRepositoryPaths(Set<String> repositoryPaths) {
		this.repositoryPaths = repositoryPaths;
	}

	@Configurable
	public void setMaxClauseCount(int maxClauseCount) {
		this.maxClauseCount = maxClauseCount;
	}

	@Configurable
	public void setRecoverAfterNodes(Integer recoverAfterNodes) {
		if (recoverAfterNodes != null) {
			this.recoverAfterNodes = recoverAfterNodes;
		}
	}

	@Configurable
	public void setExpectedNodes(Integer expectedNodes) {
		this.expectedNodes = expectedNodes;
	}

	@Configurable
	public void setRecoverAfterTimeInS(Integer recoverAfterTimeInS) {
		if (recoverAfterTimeInS != null) {
			this.recoverAfterTimeInS = recoverAfterTimeInS;
		}
	}

	@Configurable
	public void setClusterNodes(Set<String> clusterNodes) {
		if (clusterNodes != null && !clusterNodes.isEmpty() && clusterNodes.iterator().next() != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Configured cluster nodes: " + clusterNodes);
			}
			this.clusterNodes = clusterNodes;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No cluster nodes configured: " + clusterNodes);
			}
		}
	}

	public String getPublishHost() {
		if (this.publishHost == null || this.publishHost.trim().length() == 0) {
			InetAddress localAddress = NetworkTools.getNetworkAddress();
			if (localAddress == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Could not establish a 'good' local address.");
				}
			} else {
				this.publishHost = localAddress.getHostAddress();
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing address is " + this.publishHost);
		}
		return publishHost;
	}

	@Configurable
	public void setPublishHost(String publishHost) {
		this.publishHost = publishHost;
	}

	public Set<String> getBindHosts() {
		if (this.bindHosts == null || this.bindHosts.isEmpty() || this.bindHosts.iterator().next() == null) {
			this.bindHosts = new HashSet<String>();
			this.bindHosts.add("0.0.0.0");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Binding to " + this.bindHosts);
		}
		return bindHosts;
	}

	@Configurable
	public void setBindHosts(Set<String> bindHosts) {
		this.bindHosts = bindHosts;
	}

	@Required
	@Configurable
	public void setModuleClassLoader(ClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;

	}

}
