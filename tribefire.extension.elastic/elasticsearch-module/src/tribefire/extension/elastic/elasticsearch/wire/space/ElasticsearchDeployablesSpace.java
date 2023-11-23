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
package tribefire.extension.elastic.elasticsearch.wire.space;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;

import org.elasticsearch.plugins.Plugin;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.execution.CustomThreadFactory;
import com.braintribe.execution.ExtendedScheduledThreadPoolExecutor;
import com.braintribe.logging.Logger;
import com.braintribe.model.elasticsearchdeployment.ElasticsearchService;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.elasticsearch.ClientRegistry;
import com.braintribe.model.processing.elasticsearch.ElasticsearchConnectorImpl;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnector;
import com.braintribe.model.processing.elasticsearch.IndexedElasticsearchConnectorImpl;
import com.braintribe.model.processing.elasticsearch.app.ElasticsearchAdminServlet;
import com.braintribe.model.processing.elasticsearch.aspect.ExtendedFulltextAspect;
import com.braintribe.model.processing.elasticsearch.fulltext.FulltextProcessing;
import com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorker;
import com.braintribe.model.processing.elasticsearch.indexing.ElasticsearchIndexingWorkerImpl;
import com.braintribe.model.processing.elasticsearch.service.ElasticServiceProcessor;
import com.braintribe.model.processing.elasticsearch.service.HealthCheckProcessor;
import com.braintribe.model.processing.elasticsearch.util.DeployableUtils;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.elastic.elasticsearch.wares.NodeServlet;
import tribefire.module.wire.contract.ModuleReflectionContract;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.PlatformReflectionContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class ElasticsearchDeployablesSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(ElasticsearchDeployablesSpace.class);

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleResourcesContract resources;

	@Import
	private PlatformReflectionContract reflection;

	@Import
	private ModuleReflectionContract module;

	@Managed
	public NodeServlet service(ExpertContext<ElasticsearchService> context) {
		ElasticsearchService deployable = context.getDeployable();

		NodeServlet bean = new NodeServlet();
		bean.setBasePath(getFile("basePath", deployable.getBasePath(), "ELASTIC_SVC_BASEPATH_" + deployable.getExternalId(), true));
		bean.setDataPath(getFile("dataPath", deployable.getDataPath(), "ELASTIC_SVC_DATAPATH_" + deployable.getExternalId(), true));
		bean.setClusterName(getString("clusterName", deployable.getClusterName(), "ELASTIC_SVC_CLUSTERNAME_" + deployable.getExternalId(), false));
		bean.setPluginClasses(
				getClassSet("pluginClasses", deployable.getPluginClasses(), "ELASTIC_SVC_PLUGINCLASSES_" + deployable.getExternalId(), true));
		bean.setPathIdentifier(deployable.getPathIdentifier());
		bean.setPort(getInteger("port", deployable.getPort(), "ELASTIC_SVC_PORT_" + deployable.getExternalId(), true));
		bean.setHttpPort(getInteger("httpPort", deployable.getHttpPort(), "ELASTIC_SVC_HTTP_PORT_" + deployable.getExternalId(), true));
		bean.setBindHosts(getStringSet("bindHosts", deployable.getBindHosts(), "ELASTIC_SVC_BINDHOSTS_" + deployable.getExternalId(), true));
		bean.setPublishHost(getString("clusterName", deployable.getPublishHost(), "ELASTIC_SVC_PUBLISHHOST_" + deployable.getExternalId(), true));
		bean.setRepositoryPaths(
				getStringSet("repositoryPaths", deployable.getRepositoryPaths(), "ELASTIC_SVC_REPOSITORYPATHS_" + deployable.getExternalId(), true));
		bean.setRecoverAfterNodes(getInteger("recoverAfterNodes", deployable.getRecoverAfterNodes(),
				"ELASTIC_SVC_RECOVERAFTERNODES_" + deployable.getExternalId(), true));
		bean.setExpectedNodes(
				getInteger("expectedNodes", deployable.getExpectedNodes(), "ELASTIC_SVC_EXPECTEDNODES_" + deployable.getExternalId(), true));
		bean.setRecoverAfterTimeInS(getInteger("recoverAfterTimeInS", deployable.getRecoverAfterTimeInS(),
				"ELASTIC_SVC_RECOVERAFTERNODES_" + deployable.getExternalId(), true));
		bean.setClusterNodes(
				getStringSet("clusterNodes", deployable.getClusterNodes(), "ELASTIC_SVC_CLUSTERNODES_" + deployable.getExternalId(), true));
		bean.setExternalId(deployable.getExternalId());
		bean.setElasticPath(getFile("elasticPath", deployable.getElasticPath(), "ELASTIC_SVC_ELASTIC_" + deployable.getExternalId(), true));
		bean.setModuleClassLoader(module.moduleClassLoader());
		return bean;
	}

	@Managed
	public ElasticServiceProcessor reflectionProcessor() {
		ElasticServiceProcessor bean = new ElasticServiceProcessor();
		bean.setSessionFactory(tfPlatform.requestUserRelated().sessionFactory());
		bean.setDeployableUtils(deployableUtils());
		bean.setFulltextProcessing(fulltextProcessing());
		return bean;
	}

	@Managed
	public DeployableUtils deployableUtils() {
		DeployableUtils bean = new DeployableUtils();
		bean.setDeployRegistry(tfPlatform.deployment().deployRegistry());
		bean.setCortexSessionFactory(tfPlatform.requestUserRelated().cortexSessionSupplier());
		return bean;
	}

	@Managed
	public ElasticsearchAdminServlet adminServlet() {
		ElasticsearchAdminServlet bean = new ElasticsearchAdminServlet();
		bean.setRequestEvaluator(tfPlatform.requestProcessing().evaluator());
		bean.setCortexSessionFactory(tfPlatform.requestUserRelated().cortexSessionSupplier());

		return bean;
	}

	@Managed
	public ExtendedFulltextAspect fulltextAspect(ExpertContext<com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect> context) {
		com.braintribe.model.elasticsearchdeployment.aspect.ExtendedFulltextAspect deployable = context.getDeployable();

		IndexedElasticsearchConnector connector = context.resolve(deployable.getElasticsearchConnector(),
				com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector.T);
		ExtendedFulltextAspect bean = new ExtendedFulltextAspect();
		bean.setCascadingAttachment(deployable.getCascadingAttachment());
		bean.setElasticsearchConnector(connector);
		bean.setMaxFulltextResultSize(deployable.getMaxFulltextResultSize());
		bean.setMaxResultWindowSize(deployable.getMaxResultWindow());
		ElasticsearchIndexingWorker worker = context.resolve(deployable.getWorker(),
				com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker.T);
		bean.setWorker(worker);
		bean.setIgnoreUnindexedEntities(deployable.getIgnoreUnindexedEntities());
		bean.setFulltextProcessing(fulltextProcessing());

		return bean;
	}

	@Managed
	public ElasticsearchIndexingWorker worker(
			ExpertContext<com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker> context) {
		com.braintribe.model.elasticsearchdeployment.indexing.ElasticsearchIndexingWorker deployable = context.getDeployable();

		ElasticsearchIndexingWorkerImpl bean = new ElasticsearchIndexingWorkerImpl();
		IndexedElasticsearchConnector indexedConnector = context.resolve(deployable.getElasticsearchConnector(),
				com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector.T);
		bean.setElasticsearchConnector(indexedConnector);
		bean.setThreadCount(deployable.getThreadCount());
		Integer queueSize = deployable.getQueueSize();
		if (queueSize != null) {
			bean.setQueueSize(queueSize);
		}
		PersistenceGmSessionFactory sessionFactory = tfPlatform.systemUserRelated().sessionFactory();
		bean.setSessionFactory(sessionFactory::newSession);
		bean.setWorkerIdentification(deployable);
		bean.setScheduledThreadPool(scheduledThreadPool());
		bean.setFulltextProcessing(fulltextProcessing());

		return bean;
	}

	@Managed
	public ExtendedScheduledThreadPoolExecutor scheduledThreadPool() {
		ExtendedScheduledThreadPoolExecutor bean = new ExtendedScheduledThreadPoolExecutor(5,
				CustomThreadFactory.create().namePrefix("tribefire.scheduled-elasticsearch-"), new ThreadPoolExecutor.CallerRunsPolicy());
		bean.setAddThreadContextToNdc(true);
		bean.allowCoreThreadTimeOut(true);
		bean.setDescription("Scheduled Thread Pool");
		bean.setWaitForTasksToCompleteOnShutdown(false);

		return bean;
	}

	@Managed
	public ElasticsearchConnectorImpl connector(
			ExpertContext<? extends com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector> context) {
		com.braintribe.model.elasticsearchdeployment.ElasticsearchConnector deployable = context.getDeployable();

		ElasticsearchConnectorImpl bean = new ElasticsearchConnectorImpl();
		bean.setClusterName(getString("clusterName", deployable.getClusterName(), "ELASTIC_CON_CLUSTERNAME_" + deployable.getExternalId(), false));
		bean.setHost(getString("host", deployable.getHost(), "ELASTIC_CON_HOST_" + deployable.getExternalId(), true));
		bean.setPort(getInteger("port", deployable.getPort(), "ELASTIC_CON_PORT_" + deployable.getExternalId(), true));
		bean.setNodeName(getString("nodeName", deployable.getNodeName(), "ELASTIC_CON_NODENAME_" + deployable.getExternalId(), true));
		bean.setClusterSniff(deployable.getClusterSniff());
		bean.setClientRegistry(clientRegistry());

		return bean;
	}

	@Managed
	public IndexedElasticsearchConnectorImpl indexedConnector(
			ExpertContext<com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector> context) {
		com.braintribe.model.elasticsearchdeployment.IndexedElasticsearchConnector deployable = context.getDeployable();

		IndexedElasticsearchConnectorImpl bean = new IndexedElasticsearchConnectorImpl();
		bean.setClusterName(getString("clusterName", deployable.getClusterName(), "ELASTIC_CON_CLUSTERNAME_" + deployable.getExternalId(), false));
		bean.setHost(getString("host", deployable.getHost(), "ELASTIC_CON_HOST_" + deployable.getExternalId(), true));
		bean.setPort(getInteger("port", deployable.getPort(), "ELASTIC_CON_PORT_" + deployable.getExternalId(), true));
		bean.setNodeName(getString("nodeName", deployable.getNodeName(), "ELASTIC_CON_NODENAME_" + deployable.getExternalId(), true));
		bean.setClusterSniff(deployable.getClusterSniff());
		bean.setIndex(getString("index", deployable.getIndex(), "ELASTIC_CON_INDEX_" + deployable.getExternalId(), false));
		bean.setClientRegistry(clientRegistry());

		return bean;
	}

	@Managed
	private ClientRegistry clientRegistry() {
		ClientRegistry cr = new ClientRegistry();
		return cr;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	/**
	 * TODO: be aware that this should not be necessary; This should be part of a configuration management
	 */

	private File getFile(String propName, String filePath, String envKey, boolean allowNull) {
		boolean debug = logger.isDebugEnabled();

		if ((filePath == null || filePath.trim().length() == 0) && (envKey != null)) {
			filePath = reflection.getProperty(envKey);
			if (debug) {
				logger.debug("The configured property " + propName + " is null or empty. Using the environment variable value: " + filePath);
			}
		}
		if (filePath == null || filePath.trim().length() == 0) {
			if (allowNull) {
				if (debug) {
					logger.debug("The configured property " + propName + " is null or empty. As this is accepted, we'll take it.");
				}
				return null;
			}
			throw new RuntimeException("The property " + propName + " must not be null or empty.");
		}
		File f = new File(filePath);
		if (!f.isAbsolute()) {
			try {
				if (filePath.toLowerCase().startsWith("resources/")) {
					filePath = filePath.substring("resources/".length());
				}
				File resourcePath = resources.resource(filePath).asFile();
				f = resourcePath;
			} catch (Exception e) {
				throw new RuntimeException("Could not put the relative path " + filePath + " into relation of the resources directory.", e);
			}
			if (debug) {
				logger.debug("The file path " + filePath + " seems to be a relative path. Prepending the resources prefix: " + f.getAbsolutePath());
			}
		} else {
			if (debug) {
				logger.debug("Treating the file path " + filePath + " as an absolute path.");
			}
		}
		if (!f.exists()) {
			if (debug) {
				logger.debug("The file " + f.getAbsolutePath() + " which is configured for property " + propName
						+ " does not (yet) exist. It may be created later, though.");
			}
		} else {
			if (debug) {
				logger.debug("The file " + f.getAbsolutePath() + " which is configured for property " + propName + " exists.");
			}
		}

		return f;
	}

	private String getString(String propName, String configuredString, String envKey, boolean allowNull) {
		boolean debug = logger.isDebugEnabled();

		if ((configuredString == null || configuredString.trim().length() == 0) && (envKey != null)) {
			configuredString = reflection.getProperty(envKey);
			if (debug) {
				logger.debug("The configured property " + propName + " is null or empty. Using the environment variable value: " + configuredString);
			}
		}
		if (configuredString == null || configuredString.trim().length() == 0) {
			if (allowNull) {
				if (debug) {
					logger.debug("The configured property " + propName + " is null or empty. As this is accepted, we'll take it.");
				}
				return null;
			}
			throw new RuntimeException("The property " + propName + " must not be null or empty.");
		}
		if (debug) {
			logger.debug("The configured property " + propName + " is now: " + configuredString);
		}

		return configuredString;
	}

	private Integer getInteger(String propName, Integer configuredInteger, String envKey, boolean allowNull) {
		boolean debug = logger.isDebugEnabled();

		if (configuredInteger == null && (envKey != null)) {
			try {
				String property = reflection.getProperty(envKey);
				if (property == null) {
					throw new NullPointerException("The environment variable " + envKey + " is not defined.");
				}
				configuredInteger = Integer.parseInt(property);
				if (debug) {
					logger.debug(
							"The configured property " + propName + " is null or empty. Using the environment variable value: " + configuredInteger);
				}
			} catch (NumberFormatException nfe) {
				throw new RuntimeException("The value of the environment variable " + envKey + " is not a valid integer.", nfe);
			} catch (NullPointerException npe) {
				if (allowNull) {
					if (debug) {
						logger.debug("The configured property " + propName + " is null. As this is accepted, we'll take it.");
					}
					return null;
				}
				throw new RuntimeException("The property " + propName + " must not be null.", npe);
			}
		}
		if (configuredInteger == null && !allowNull) {
			throw new RuntimeException("The property " + propName + " must not be null.");
		}
		if (debug) {
			logger.debug("The configured property " + propName + " is: " + configuredInteger);
		}

		return configuredInteger;
	}

	@SuppressWarnings("unchecked")
	private Set<Class<? extends Plugin>> getClassSet(String propName, Set<String> configuredStringSet, String envKey, boolean allowNull) {
		boolean debug = logger.isDebugEnabled();

		if ((configuredStringSet == null || configuredStringSet.isEmpty()) && (envKey != null)) {
			String envSet = reflection.getProperty(envKey);
			if (debug) {
				logger.debug("The configured property " + propName + " is null or empty. Using the environment variable value: " + envSet);
			}

			if (envSet == null || envSet.trim().length() == 0) {
				if (allowNull) {
					if (debug) {
						logger.debug("The configured property " + propName + " is null. As this is accepted, we'll take it.");
					}
					return null;
				}
				throw new RuntimeException("The property " + propName + " must not be null or empty.");
			}
			String[] elements = envSet.split(",");
			if (elements != null && elements.length > 0) {
				configuredStringSet = new LinkedHashSet<String>();
				for (String e : elements) {
					if (e == null || e.equals("null")) {
						if (debug) {
							logger.debug("One element of " + elements + " seems to be null. Ignoring it.");
						}
					} else {
						configuredStringSet.add(e);
					}
				}
				if (debug) {
					logger.debug("Parsed set " + configuredStringSet + " for " + propName);
				}
			}
		}
		if (configuredStringSet == null || configuredStringSet.isEmpty()) {
			if (allowNull) {
				if (debug) {
					logger.debug("The configured property " + propName + " is null. As this is accepted, we'll take it.");
				}
				return null;
			}
			throw new RuntimeException("The property " + propName + " must not be null or empty.");
		}
		Set<Class<? extends Plugin>> classSet = new LinkedHashSet<>();
		for (String cls : configuredStringSet) {
			Class<? extends Plugin> clazz;
			try {
				clazz = (Class<? extends Plugin>) Class.forName(cls);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Could not find class " + cls + " for property " + propName);
			}
			classSet.add(clazz);
		}

		return classSet;
	}

	private Set<String> getStringSet(String propName, Set<String> configuredStringSet, String envKey, boolean allowNull) {
		boolean debug = logger.isDebugEnabled();

		if ((configuredStringSet == null || configuredStringSet.isEmpty()) && (envKey != null)) {
			String envSet = reflection.getProperty(envKey);
			if (debug) {
				logger.debug("The configured property " + propName + " is null or empty. Using the environment variable value: " + envSet);
			}

			if (envSet == null || envSet.trim().length() == 0) {
				if (allowNull) {
					if (debug) {
						logger.debug("The configured property " + propName + " is null. As this is accepted, we'll take it.");
					}
					return null;
				}
				throw new RuntimeException("The property " + propName + " must not be null or empty.");
			}
			String[] elements = envSet.split(",");
			if (elements != null && elements.length > 0) {
				configuredStringSet = new LinkedHashSet<String>();
				for (String e : elements) {
					if (e == null || e.equals("null")) {
						if (debug) {
							logger.debug("One element of " + elements + " seems to be null. Ignoring it.");
						}
					} else {
						configuredStringSet.add(e);
					}
				}
				if (debug) {
					logger.debug("Parsed set " + configuredStringSet + " for " + propName);
				}
			}
		}
		if (configuredStringSet == null || configuredStringSet.isEmpty()) {
			if (allowNull) {
				return null;
			}
			throw new RuntimeException("The property " + propName + " must not be null or empty.");
		}

		return configuredStringSet;
	}

	@Managed
	public HealthCheckProcessor healthCheckProcessor() {
		HealthCheckProcessor bean = new HealthCheckProcessor();
		bean.setCortexSessionSupplier(tfPlatform.systemUserRelated().cortexSessionSupplier());
		bean.setDeployableUtils(deployableUtils());
		return bean;
	}

	@Managed
	public FulltextProcessing fulltextProcessing() {
		FulltextProcessing bean = new FulltextProcessing();

		long maxFileSize = Numbers.MEGABYTE * 100;
		String maxFileSizeString = TribefireRuntime.getProperty("TRIBEFIRE_ELASTIC_FULLTEXT_MAXSIZE");
		if (!StringTools.isBlank(maxFileSizeString)) {
			try {
				maxFileSize = Long.parseLong(maxFileSizeString);
			} catch (Exception e) {
				logger.warn(() -> "Could not parse TRIBEFIRE_ELASTIC_FULLTEXT_MAXSIZE: " + maxFileSizeString, e);
			}
		}
		String acceptList = TribefireRuntime.getProperty("TRIBEFIRE_ELASTIC_FULLTEXT_ACCEPTLIST");
		String denyList = TribefireRuntime.getProperty("TRIBEFIRE_ELASTIC_FULLTEXT_DENYLIST");

		List<String> acceptedMimeTypes = null;
		List<String> deniedMimeTypes = null;

		if (StringTools.isAllBlank(acceptList, denyList)) {
			//@formatter:off
			acceptedMimeTypes = Arrays.asList(
					"application/pdf", 
					"application/msword",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
					"text/plain",
					"text/rtf",
					"application/rtf",
					"application/x-rtf",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.template",
					"application/vnd.ms-word.template.macroEnabled.12",
					"application/vnd.ms-word.document.macroEnabled.12",
					"application/vnd.ms-word.template.macroenabled.12",
					"application/vnd.ms-word.document.macroenabled.12",
					"application/vnd.ms-excel",
					"application/excel",
					"application/x-excel",
					"application/x-msexcel",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
					"text/csv",
					"text/comma-separated-values",
					"application/vnd.ms-excel.addin.macroEnabled.12",
					"application/vnd.ms-excel.sheet.binary.macroEnabled.12",
					"application/vnd.ms-excel.sheet.macroEnabled.12",
					"application/vnd.ms-excel.template.macroEnabled.12",
					"application/vnd.ms-excel.addin.macroenabled.12",
					"application/vnd.ms-excel.sheet.binary.macroenabled.12",
					"application/vnd.ms-excel.sheet.macroenabled.12",
					"application/vnd.ms-excel.template.macroenabled.12",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.template",
					"application/vnd.ms-powerpoint",
					"application/mspowerpoint",
					"application/x-mspowerpoint",
					"application/powerpoint",
					"application/vnd.openxmlformats-officedocument.presentationml.presentation",
					"application/vnd.openxmlformats-officedocument.presentationml.slideshow",
					"application/vnd.openxmlformats-officedocument.presentationml.template",
					"application/vnd.ms-powerpoint.template.macroEnabled.12",
					"application/vnd.ms-powerpoint.slideshow.macroEnabled.12",
					"application/vnd.ms-powerpoint.addin.macroEnabled.12",
					"application/vnd.ms-powerpoint.presentation.macroEnabled.12",
					"application/vnd.ms-powerpoint.template.macroenabled.12",
					"application/vnd.ms-powerpoint.slideshow.macroenabled.12",
					"application/vnd.ms-powerpoint.addin.macroenabled.12",
					"application/vnd.ms-powerpoint.presentation.macroenabled.12",
					"application/vnd.visio",
					"application/vnd.visio2013",
					"application/x-visio",
					"application/visio",
					"application/visio.drawing",
					"application/vsd",
					"application/x-vsd",
					"application/vnd.ms-visio.drawing",
					"application/vnd.ms-visio.drawing.macroenabled.12",
					"application/vnd.ms-visio.stencil",
					"application/vnd.ms-visio.stencil.macroenabled.12",
					"application/vnd.ms-visio.template",
					"application/vnd.ms-visio.template.macroenabled.12",
					"application/vnd.oasis.opendocument.presentation",
					"application/vnd.oasis.opendocument.text",
					"application/vnd.oasis.opendocument.spreadsheet",
					
					//HTML
					"text/html",
					"application/vnd.wap.xhtml+xml",
					"application/x-asp",
					"application/xhtml+xml",

					//XML
					"text/xml",

					//Email
					"message/rfc822",
					"application/vnd.ms-outlook",
					"application/x-mimearchive",
					"application/mbox",
					"application/vnd.ms-outlook-pst",
					
					// Source code
					"text/x-c++src",
					"text/x-groovy",
					"text/x-java-source",

					// Ebook
					"application/x-ibooks+zip",
					"application/epub+zip",

					// Feeds
					"application/atom+xml",
					"application/rss+xml",

					// iWork
					"application/vnd.apple.keynote",
					"application/vnd.apple.iwork",
					"application/vnd.apple.numbers",
					"application/vnd.apple.pages",


					// TNEF
					"application/vnd.ms-tnef",
					"application/x-tnef",
					"application/ms-tnef",


					// OpenDocument
					"application/x-vnd.oasis.opendocument.presentation",
					"application/vnd.oasis.opendocument.chart",
					"application/x-vnd.oasis.opendocument.text-web",
					"application/x-vnd.oasis.opendocument.image",
					"application/vnd.oasis.opendocument.graphics-template",
					"application/vnd.oasis.opendocument.text-web",
					"application/x-vnd.oasis.opendocument.spreadsheet-template",
					"application/vnd.oasis.opendocument.spreadsheet-template",
					"application/vnd.sun.xml.writer",
					"application/x-vnd.oasis.opendocument.graphics-template",
					"application/vnd.oasis.opendocument.graphics",
					"application/vnd.oasis.opendocument.spreadsheet",
					"application/x-vnd.oasis.opendocument.chart",
					"application/x-vnd.oasis.opendocument.spreadsheet",
					"application/vnd.oasis.opendocument.image",
					"application/x-vnd.oasis.opendocument.text",
					"application/x-vnd.oasis.opendocument.text-template",
					"application/vnd.oasis.opendocument.formula-template",
					"application/x-vnd.oasis.opendocument.formula",
					"application/vnd.oasis.opendocument.image-template",
					"application/x-vnd.oasis.opendocument.image-template",
					"application/x-vnd.oasis.opendocument.presentation-template",
					"application/vnd.oasis.opendocument.presentation-template",
					"application/vnd.oasis.opendocument.text",
					"application/vnd.oasis.opendocument.text-template",
					"application/vnd.oasis.opendocument.chart-template",
					"application/x-vnd.oasis.opendocument.chart-template",
					"application/x-vnd.oasis.opendocument.formula-template",
					"application/x-vnd.oasis.opendocument.text-master",
					"application/vnd.oasis.opendocument.presentation",
					"application/x-vnd.oasis.opendocument.graphics",
					"application/vnd.oasis.opendocument.formula",
					"application/vnd.oasis.opendocument.text-master",

					// Archives

					"application/zlib",
					"application/x-gzip",
					"application/x-bzip2",
					"application/x-compress",
					"application/x-java-pack200",
					"application/gzip",
					"application/x-bzip",
					"application/x-xz",
					"application/x-tar",
					"application/java-archive",
					"application/x-archive",
					"application/zip",
					"application/x-cpio",
					"application/x-tika-unix-dump",
					"application/x-7z-compressed",
					"application/x-rar-compressed"
					);
			//@formatter:on
		} else {
			acceptedMimeTypes = Arrays.asList(StringTools.splitString(acceptList, ","));
			deniedMimeTypes = Arrays.asList(StringTools.splitString(denyList, ","));
			if (acceptedMimeTypes.isEmpty()) {
				acceptedMimeTypes = null;
			}
			if (deniedMimeTypes.isEmpty()) {
				deniedMimeTypes = null;
			}
		}

		final List<String> finalAcceptedMimeTypes = acceptedMimeTypes;
		final List<String> finalDeniedMimeTypes = deniedMimeTypes;
		final long finalMaxFileSize = maxFileSize;

		Predicate<Resource> resourceAcceptance = r -> {
			Long fileSize = r.getFileSize();
			String name = r.getName();
			String mimeType = r.getMimeType();
			String context = "".concat(name).concat(" (Id: ").concat(r.getId()).concat(", Mime-Type: ").concat(mimeType).concat(")");
			if (fileSize != null && finalMaxFileSize > 0) {
				if (fileSize > finalMaxFileSize) {
					logger.debug(() -> "Resource " + context + " rejected because its size " + fileSize + " exceeds the maximum file size of "
							+ finalMaxFileSize);
					return false;
				}
			}

			if (StringTools.isBlank(mimeType)) {
				logger.debug(() -> "Could not get Mime-Type from resource " + context);
				return false;
			}
			if (finalAcceptedMimeTypes != null && finalDeniedMimeTypes == null) {
				boolean accepted = finalAcceptedMimeTypes.contains(mimeType);
				logger.debug(() -> "Resource " + context + " is accepted: " + accepted);
				return accepted;
			} else if (finalAcceptedMimeTypes == null && finalDeniedMimeTypes != null) {
				boolean denied = finalDeniedMimeTypes.contains(mimeType);
				logger.debug(() -> "Resource " + context + " is denied: " + denied);
				return !denied;
			} else {
				boolean accepted = finalAcceptedMimeTypes.contains(mimeType);
				boolean denied = finalDeniedMimeTypes.contains(mimeType);
				logger.debug(() -> "Resource " + context + " is accepted: " + accepted + " && denied: " + denied);
				return accepted && !denied;
			}
		};

		bean.setResourceAcceptance(resourceAcceptance);

		return bean;
	}

}
