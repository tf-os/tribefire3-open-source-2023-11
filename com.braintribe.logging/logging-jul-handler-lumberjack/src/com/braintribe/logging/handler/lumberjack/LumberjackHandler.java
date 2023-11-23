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
package com.braintribe.logging.handler.lumberjack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.collections.dispatchcollector.DispatchCollector;
import com.braintribe.collections.dispatchcollector.DispatchReceiver;
import com.braintribe.logging.handler.lumberjack.logpackage.CombinedLogPackage;
import com.braintribe.logging.handler.lumberjack.logpackage.LogPackage;
import com.braintribe.logging.juli.ConfigurationException;
import com.braintribe.logging.juli.JulExtensionsHelpers;
import com.braintribe.logging.ndc.mbean.NestedDiagnosticContext;
import com.braintribe.utils.StringTools;

/*
 * Example configuration:

	<bean id="lumberjack.formatter" class="com.braintribe.logging.juli.formatters.simple.SimpleFormatter">
		<constructor-arg value="%4$-7s %7$-33s '%5$s' %6$s [%9$s]%n" />
	</bean>
	<bean class="com.braintribe.logging.handler.lumberjack.LumberjackHandler">
		<property name="host" value="localhost" />
		<property name="port" value="5000" />
		<property name="level" value="FINEST" />
		<property name="encoding" value="UTF-8" />
		<property name="formatter" ref="lumberjack.formatter" />
	</bean>

 */

/**
 * This is a Java Util Logging handler that forwards log records to a Lumberjack receiver. See
 * https://github.com/elastic/logstash-forwarder/blob/master/PROTOCOL.md for details on the protocol.
 */
public class LumberjackHandler extends Handler implements DispatchReceiver<LogPackage> {

	protected final static Logger logLogger = Logger.getLogger(LumberjackHandler.class.getName());

	public static final String ENVIRONMENT_NODE_ID = "TRIBEFIRE_NODE_ID";

	public final static String MAP_KEY_LEVEL = "level";
	public final static String MAP_KEY_LOGGER = "loggerClass";
	public final static String MAP_KEY_SOURCEMETHOD = "sourceMethod";
	public final static String MAP_KEY_SOURCECLASS = "sourceClass";
	public final static String MAP_KEY_SEQUENCENUMBER = "sequenceNumber";
	public final static String MAP_KEY_THROWABLE = "throwable";
	public final static String MAP_KEY_CLASS = "class";
	public final static String MAP_KEY_THREADID = "threadId";
	public final static String MAP_KEY_MESSAGE = "message";
	public final static String MAP_KEY_NODE = "node";
	public final static String MAP_KEY_MILLISECONDS = "dateMs";
	public final static String MAP_KEY_DATE = "date";
	public final static String MAP_KEY_FORMATTEDMESSAGE = "formattedMessage";
	public final static String MAP_KEY_CARTRIDGE = "cartridge";
	public final static String MAP_KEY_IP4 = "ip4";
	public final static String MAP_KEY_IP6 = "ip6";
	public final static String MAP_KEY_TAGS = "tags";

	protected String host = null;
	protected int port = -1;

	protected long nextErrorMessage = -1L;
	protected long errorInterval = 60000L;
	protected boolean includeMdc = true;
	protected boolean includeNdc = true;
	protected String mdcPrefix = "MDC_";
	protected String ndcPrefix = "NDC_";
	protected Set<String> loggersToAttach = null;
	protected boolean includeSource = false;
	protected int socketTimeout = 60000;

	protected long logPackageQueueOfferTimeoutInMs = 50;
	protected int errorOutput = 1000;

	protected final static String className = LumberjackHandler.class.getName();

	protected ExecutorService executor = null;
	protected LinkedBlockingQueue<CombinedLogPackage> logPackageQueue = null;
	protected int queueSize = 2000;
	protected int workers = 2;
	protected List<LumberjackSender> senders = new ArrayList<>();
	protected List<Future<Void>> senderFutures = new ArrayList<>();

	protected static AtomicInteger sequencer = new AtomicInteger(0);

	protected enum Lifecycle {
		none,
		running,
		closed
	}

	protected Lifecycle currentState = Lifecycle.none;

	protected int frameSize = 10;
	protected long dispatchInterval = 5000L;

	protected DispatchCollector<LogPackage> logPackageCollector = null;

	protected String nodeId = null;

	protected long retryInterval = 1000L; // 1 second
	protected long maxRetryInterval = 3600000L; // 1 hour

	protected long errorCount = 0;
	protected boolean ssl = true;
	protected String cartridge = null;
	protected String ip4 = null;
	protected String ip6 = null;
	protected String tags = null;

	public LumberjackHandler() {
		initialize();
	}

	public LumberjackHandler(boolean manualInitialize) {
		if (!manualInitialize) {
			initialize();
		}
		// do manual initialization
	}

	public void initialize() {

		if (this.currentState == Lifecycle.running) {
			return;
		}
		this.currentState = Lifecycle.running;

		this.configureHandler();

		if (this.nodeId == null) {
			this.nodeId = getLowLevelProperty(ENVIRONMENT_NODE_ID, null);
		}
		if (this.nodeId != null) {
			Map<String, Object> map = new HashMap<>();
			List<String> propertyNames = StringTools.getPatternVariables(this.nodeId);
			if (!propertyNames.isEmpty()) {
				for (String propertyName : propertyNames) {
					String value = getLowLevelProperty(propertyName, "");
					if (value != null) {
						map.put(propertyName, value);
					}
				}
				this.nodeId = StringTools.patternFormat(this.nodeId, map, "");
			}
		}

		this.logPackageCollector = new DispatchCollector<>(this.frameSize, this.dispatchInterval, this);

		this.logPackageQueue = new LinkedBlockingQueue<>(this.queueSize);
		this.executor = Executors.newFixedThreadPool(this.workers, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("Lumberjack Sender");
				t.setDaemon(true);
				return t;
			}
		});

		for (int i = 0; i < this.workers; ++i) {
			LumberjackSender sender = new LumberjackSender(this.logPackageQueue, this.host, this.port, i, this.socketTimeout, this.ssl);
			sender.setRetryInterval(this.retryInterval);
			sender.setMaxRetryInterval(this.maxRetryInterval);
			this.senders.add(sender);
			senderFutures.add(this.executor.submit(sender));
		}
	}

	protected void configureHandler() {
		if (this.host != null && this.port != -1) {
			// Configuration via IOC already done
			return;
		}

		try {
			this.host = JulExtensionsHelpers.getProperty(getClass(), "host", true, null, String.class);
			this.port = JulExtensionsHelpers.getProperty(getClass(), "port", true, null, Integer.class);
			this.nodeId = JulExtensionsHelpers.getProperty(getClass(), "nodeId", false, this.nodeId, String.class);
			this.errorInterval = JulExtensionsHelpers.getProperty(getClass(), "errorInterval", false, this.errorInterval, Long.class);
			this.includeMdc = JulExtensionsHelpers.getProperty(getClass(), "includeMdc", false, this.includeMdc, Boolean.class);
			this.includeNdc = JulExtensionsHelpers.getProperty(getClass(), "includeNdc", false, this.includeNdc, Boolean.class);
			this.mdcPrefix = JulExtensionsHelpers.getProperty(getClass(), "mdcPrefix", false, this.mdcPrefix, String.class);
			this.ndcPrefix = JulExtensionsHelpers.getProperty(getClass(), "ndcPrefix", false, this.ndcPrefix, String.class);
			this.includeSource = JulExtensionsHelpers.getProperty(getClass(), "includeSource", false, this.includeSource, Boolean.class);
			this.queueSize = JulExtensionsHelpers.getProperty(getClass(), "queueSize", false, this.queueSize, Integer.class);
			this.workers = JulExtensionsHelpers.getProperty(getClass(), "workers", false, this.workers, Integer.class);
			this.frameSize = JulExtensionsHelpers.getProperty(getClass(), "frameSize", false, this.frameSize, Integer.class);
			this.dispatchInterval = JulExtensionsHelpers.getProperty(getClass(), "dispatchInterval", false, this.dispatchInterval, Long.class);
			this.retryInterval = JulExtensionsHelpers.getProperty(getClass(), "retryInterval", false, this.retryInterval, Long.class);
			this.maxRetryInterval = JulExtensionsHelpers.getProperty(getClass(), "maxRetryInterval", false, this.maxRetryInterval, Long.class);
			this.socketTimeout = JulExtensionsHelpers.getProperty(getClass(), "socketTimeout", false, this.socketTimeout, Integer.class);
			this.ssl = JulExtensionsHelpers.getProperty(getClass(), "ssl", false, this.ssl, Boolean.class);
			this.cartridge = JulExtensionsHelpers.getProperty(getClass(), "cartridge", false, this.cartridge, String.class);
			this.ip4 = JulExtensionsHelpers.getProperty(getClass(), "ip4", false, this.ip4, String.class);
			this.ip6 = JulExtensionsHelpers.getProperty(getClass(), "ip6", false, this.ip6, String.class);
			this.tags = JulExtensionsHelpers.getProperty(getClass(), "tags", false, this.tags, String.class);

			if (this.ip4 == null || this.ip4.trim().length() == 0) {
				this.ip4 = "127.0.0.1";
			}
			if (this.ip6 == null || this.ip6.trim().length() == 0) {
				this.ip6 = "::1";
			}

			final String encoding = JulExtensionsHelpers.getProperty(getClass(), "encoding", true, "UTF-8", String.class);
			try {
				super.setEncoding(encoding);
			} catch (final UnsupportedEncodingException e) {
				throw new ConfigurationException("Error while setting encoding " + encoding + "!");
			}

			final String levelString = JulExtensionsHelpers.getProperty(getClass(), "level", true, Level.ALL.getName(), String.class);
			super.setLevel(Level.parse(levelString));

			final String formatterName = JulExtensionsHelpers.getProperty(getClass(), "formatter", true, null, String.class);
			super.setFormatter(JulExtensionsHelpers.createInstance(formatterName, Formatter.class, null,
					"Error while instantiating formatter " + formatterName + "!"));

			final String filterName = JulExtensionsHelpers.getProperty(getClass(), "filter", false, null, String.class);
			super.setFilter(
					JulExtensionsHelpers.createInstance(filterName, Filter.class, null, "Error while instantiating filter " + filterName + "!"));

			final String errorManagerName = JulExtensionsHelpers.getProperty(getClass(), "errorManager", false, null, String.class);
			super.setErrorManager(JulExtensionsHelpers.createInstance(errorManagerName, ErrorManager.class, new ErrorManager(),
					"Error while instantiating errorManager " + errorManagerName + "!"));

		} catch (Exception e) {
			this.logInternalError(Level.WARNING, "configureHandler", "Error while initializing the LumberjackHandler.", e);
		}
	}

	public void destroy() throws Exception {

		if (this.currentState != Lifecycle.running) {
			return;
		}
		this.currentState = Lifecycle.closed;

		Set<String> loggerNames = this.getLoggersToAttach();
		for (String loggerName : loggerNames) {
			Logger logger = Logger.getLogger(loggerName);
			logger.removeHandler(this);
		}
		for (LumberjackSender sender : this.senders) {
			sender.stopProcessing();
		}
		for (Future<Void> future : this.senderFutures) {
			try {
				future.get(1000L, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				logLogger.logp(Level.FINE, "LumberjackHandler", "destroy", "Could not wait for sender shutdown.");
			}
		}
		if (this.executor != null) {
			this.executor.shutdown();
		}
		if (this.logPackageCollector != null) {
			this.logPackageCollector.shutdown();
		}
	}

	@Override
	public void publish(LogRecord record) {

		// First check if there is actually anything to do for us
		if (!super.isLoggable(record)) {
			return;
		}

		if (this.currentState != Lifecycle.running) {
			return;
		}

		// Prevent an end-less loop
		String loggerName = record.getLoggerName();
		if (loggerName != null && loggerName.equals(className)) {
			// This is our own log line; returning immediately to prevent endless loops
			return;
		}
		// Check if a queue has been configured
		if (this.host == null) {
			this.logInternalError(Level.WARNING, "publish", "No host configured.", null);
			return;
		}
		if (this.port == -1) {
			this.logInternalError(Level.WARNING, "publish", "No port configured.", null);
			return;
		}

		LogPackage logPackage;
		try {
			logPackage = this.createLogPackage(record);
			this.addNestedDiagnosticContext(logPackage);
		} catch (Exception e) {
			this.logInternalError(Level.SEVERE, "publish",
					"Could not create a message for record " + record + " and host " + this.host + ":" + this.port, e);
			return;
		}

		boolean forceForwarding = record.getLevel().intValue() >= Level.WARNING.intValue();
		this.logPackageCollector.add(logPackage, forceForwarding);

	}

	protected void addNestedDiagnosticContext(LogPackage logPackage) throws Exception {
		if (this.includeMdc) {
			Map<String, String> mdc = NestedDiagnosticContext.getMdc();
			if (mdc != null) {
				String prefix = this.getMdcPrefix();
				for (Map.Entry<String, String> entry : mdc.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key != null && value != null) {
						logPackage.addProperty(prefix + key, value);
					}
				}
			}
		}
		if (this.includeNdc) {
			Deque<String> ndc = NestedDiagnosticContext.getNdc();
			if (ndc != null) {
				String prefix = this.getNdcPrefix();
				int index = 0;
				for (String ndcElement : ndc) {
					logPackage.addProperty(prefix + index, ndcElement);
					index++;
				}
			}
		}
	}

	public String getMdcPrefix() {
		if (this.mdcPrefix == null) {
			this.mdcPrefix = "MDC_";
		}
		return this.mdcPrefix;
	}

	public String getNdcPrefix() {
		if (this.ndcPrefix == null) {
			this.ndcPrefix = "NDC_";
		}
		return ndcPrefix;
	}

	protected LogPackage createLogPackage(LogRecord record) throws Exception {

		Formatter formatter = super.getFormatter();
		String recordMessage = formatter.format(record);

		Map<String, String> properties = new HashMap<>();

		Level level = record.getLevel();
		if (level != null) {
			properties.put(MAP_KEY_LEVEL, level.toString());
		}
		properties.put(MAP_KEY_MESSAGE, record.getMessage());
		properties.put(MAP_KEY_LOGGER, record.getLoggerName());
		String dateInMs = "" + record.getMillis();
		// The date will be kept as ms after 1970
		properties.put(MAP_KEY_MILLISECONDS, dateInMs);
		// The date will be translated from ms after 1970 to a date format
		properties.put(MAP_KEY_DATE, dateInMs);
		if (this.includeSource) {
			// Including source class and method... This might be expensive as it inspects the current stacktrace
			properties.put(MAP_KEY_SOURCEMETHOD, record.getSourceMethodName());
			properties.put(MAP_KEY_SOURCECLASS, record.getSourceClassName());
		}
		properties.put(MAP_KEY_SEQUENCENUMBER, "" + record.getSequenceNumber());
		Throwable t = record.getThrown();
		if (t != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			properties.put(MAP_KEY_THROWABLE, sw.toString());
		}
		Class<?> cls = record.getClass();
		if (cls != null) {
			properties.put(MAP_KEY_CLASS, cls.getName());
		}
		properties.put(MAP_KEY_THREADID, "" + record.getThreadID());
		properties.put(MAP_KEY_FORMATTEDMESSAGE, recordMessage);
		properties.put(MAP_KEY_NODE, this.nodeId);
		properties.put(MAP_KEY_CARTRIDGE, this.cartridge);
		properties.put(MAP_KEY_IP4, this.ip4);
		properties.put(MAP_KEY_IP6, this.ip6);
		if (tags != null && tags.length() != 0) {
			properties.put(MAP_KEY_TAGS, this.tags);
		}

		LogPackage logPackage = new LogPackage(sequencer.getAndIncrement(), recordMessage, properties);

		return logPackage;
	}

	protected void logInternalError(Level level, String method, String message, Throwable t) {

		// We're using a timestamp here to prevent the handler from flooding the log system
		// Only every n milliseconds, an internal log message is forwarded to the logging
		// facility.

		long now = System.currentTimeMillis();

		if (this.nextErrorMessage > now) {
			return;
		}

		this.nextErrorMessage = now + this.errorInterval;

		logLogger.logp(level, className, method, message, t);
	}

	@Override
	public void flush() {
		// Nothing to do
	}

	@Override
	public void close() throws SecurityException {
		try {
			this.destroy();
		} catch (Exception e) {
			this.logInternalError(Level.WARNING, "close", "Could not destroy handler.", e);
		}
	}

	@Override
	public void receive(Collection<LogPackage> collection) {

		if (collection == null || collection.isEmpty()) {
			return;
		}

		CombinedLogPackage combinedLogPackage = new CombinedLogPackage();
		for (LogPackage pendingLogPackage : collection) {
			combinedLogPackage.addLogPackage(pendingLogPackage);
		}
		try {
			if (!this.logPackageQueue.offer(combinedLogPackage, logPackageQueueOfferTimeoutInMs, TimeUnit.MILLISECONDS)) {
				// waiting should cover peaks
				if ((errorCount % errorOutput) == 0) {
					String initialInformation = null;
					if (errorCount == 0) {
						initialInformation = "LogPackage collection size: " + collection.size() + "; Output each " + errorOutput + " errors.";
					}

					this.logInternalError(Level.WARNING, "Log",
							"There is a backlog in log messages to be sent via Lumberjack: " + (this.logPackageQueue.size()) + " dropped messages: "
									+ errorCount + (initialInformation == null ? "" : (" - InitialInformation: " + initialInformation)),
							null);
				}
				errorCount++;
			} else {
				errorCount = 0;
			}
		} catch (InterruptedException e) {
			this.logInternalError(Level.SEVERE, "Log", "Offer on logPackageQueue was interruped during waiting.", null);
			Thread.currentThread().interrupt();
		}

	}

	protected static String getLowLevelProperty(String propertyName, String defaultValue) {
		String value = System.getProperty(propertyName);

		if (value != null) {
			return value;
		}

		value = System.getenv(propertyName);

		if (value != null) {
			return value;
		}

		return defaultValue;
	}

	@Required
	public void setHost(String host) {
		this.host = host;
	}
	@Required
	public void setPort(int port) {
		this.port = port;
	}
	@Configurable
	public void setErrorInterval(long errorInterval) {
		this.errorInterval = errorInterval;
	}
	@Configurable
	public void setIncludeMdc(boolean includeMdc) {
		this.includeMdc = includeMdc;
	}
	@Configurable
	public void setIncludeNdc(boolean includeNdc) {
		this.includeNdc = includeNdc;
	}
	@Configurable
	public void setMdcPrefix(String mdcPrefix) {
		this.mdcPrefix = mdcPrefix;
	}
	@Configurable
	public void setNdcPrefix(String ndcPrefix) {
		this.ndcPrefix = ndcPrefix;
	}
	public Set<String> getLoggersToAttach() {
		if (this.loggersToAttach == null) {
			this.loggersToAttach = new HashSet<>();
			this.loggersToAttach.add("");
		}
		return loggersToAttach;
	}
	@Configurable
	public void setLoggersToAttach(Set<String> loggersToAttach) {
		this.loggersToAttach = loggersToAttach;
	}
	@Configurable
	public void setIncludeSource(boolean includeSource) {
		this.includeSource = includeSource;
	}
	@Configurable
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
	@Configurable
	public void setWorkers(int workers) {
		this.workers = workers;
	}
	@Configurable
	public void setFrameSize(int frameSize) {
		this.frameSize = frameSize;
	}
	@Configurable
	public void setDispatchInterval(long dispatchInterval) {
		this.dispatchInterval = dispatchInterval;
	}
	@Configurable
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	@Configurable
	public void setRetryInterval(long retryInterval) {
		this.retryInterval = retryInterval;
	}
	@Configurable
	public void setMaxRetryInterval(long maxRetryInterval) {
		this.maxRetryInterval = maxRetryInterval;
	}
	@Configurable
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}
	@Configurable
	public void setCartridge(String cartridge) {
		this.cartridge = cartridge;
	}
	@Configurable
	public void setTags(String tags) {
		this.tags = tags;
	}
	@Configurable
	public void setLogPackageQueueOfferTimeoutInMs(long logPackageQueueOfferTimeoutInMs) {
		this.logPackageQueueOfferTimeoutInMs = logPackageQueueOfferTimeoutInMs;
	}
	@Configurable
	public void setErrorOutput(int errorOutput) {
		this.errorOutput = errorOutput;
	}
}
