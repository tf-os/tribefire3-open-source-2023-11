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
package com.braintribe.model.processing.bootstrapping;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.bootstrapping.jmx.TribefireRuntimeMBean;
import com.braintribe.model.processing.bootstrapping.jmx.TribefireRuntimeMBeanImpl;
import com.braintribe.model.processing.bootstrapping.listener.RuntimePropertyChangeListener;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.encryption.Cryptor;
import com.braintribe.utils.template.Template;
import com.braintribe.utils.template.TemplateException;
import com.braintribe.utils.template.model.MergeContext;

/**
 * <p>
 * All the environment variables that are used by tribefire
 * 
 * <p>
 * They include:
 * <ul>
 * 
 * <li>ENVIRONMENT_STORAGE_DIR: A variable that includes the path of the initial storage. If it is not set, the invoker
 * of TribeFireRuntime will have to handle this special case by a fall back mechanism. Otherwise, if it is provided, it
 * can either be an absolute or a relative path. In case of a relative path, it will be automatically appended to
 * {@link TribefireRuntime#getContainerRoot()}.</li>
 * 
 * <li>ENVIRONMENT_IS_CLUSTERED: Indicates that this tribefire component is running on a environment capable of being
 * clustered.</li>
 * 
 * <li>ENVIRONMENT_EXECUTION_MODE: Determines the execution mode of tribefire services, possible values being 'design',
 * 'runtime' and 'mixed' (default)</li>
 * 
 * </ul>
 * 
 * 
 *
 */
public class TribefireRuntime extends TribefireRuntimeDeprecation {

	private static Logger logger = Logger.getLogger(TribefireRuntime.class);

	protected static boolean networkInitialized = false;

	public static final String ENVIRONMENT_SERVICES_URL = "TRIBEFIRE_SERVICES_URL";
	public static final String ENVIRONMENT_PUBLIC_SERVICES_URL = "TRIBEFIRE_PUBLIC_SERVICES_URL";
	public static final String ENVIRONMENT_WEBSOCKET_URL = "TRIBEFIRE_WEBSOCKET_URL";

	public static final String ENVIRONMENT_IS_EXTENSION_HOST = "TRIBEFIRE_IS_EXTENSION_HOST";
	public static final String ENVIRONMENT_CONTAINER_ROOT_DIR = "TRIBEFIRE_CONTAINER_ROOT_DIR";
	public static final String ENVIRONMENT_STORAGE_DIR = "TRIBEFIRE_STORAGE_DIR";
	public static final String ENVIRONMENT_TMP_DIR = "TRIBEFIRE_TMP_DIR";
	public static final String ENVIRONMENT_CACHE_DIR = "TRIBEFIRE_CACHE_DIR";
	public static final String ENVIRONMENT_REPO_DIR = "TRIBEFIRE_REPO_DIR";
	public static final String ENVIRONMENT_DATA_DIR = "TRIBEFIRE_DATA_DIR";
	public static final String ENVIRONMENT_SETUP_INFO_DIR = "TRIBEFIRE_SETUP_INFO_DIR";

	public static final String ENVIRONMENT_SECURED_ENVIRONMENT = "TRIBEFIRE_SECURED_ENVIRONMENT";
	public static final String ENVIRONMENT_ACCEPT_SSL_CERTIFICATES = "TRIBEFIRE_ACCEPT_SSL_CERTIFICATES";
	public static final String ENVIRONMENT_IS_CLUSTERED = "TRIBEFIRE_IS_CLUSTERED";
	public static final String ENVIRONMENT_NODE_ID = "TRIBEFIRE_NODE_ID";
	public static final String ENVIRONMENT_EXECUTION_MODE = "TRIBEFIRE_EXECUTION_MODE";
	public static final String ENVIRONMENT_TENANT_ID = "TRIBEFIRE_TENANT_ID";
	public static final String ENVIRONMENT_EXCEPTION_EXPOSITION = "TRIBEFIRE_EXCEPTION_EXPOSITION";
	public static final String ENVIRONMENT_EXCEPTION_MESSAGE_EXPOSITION = "TRIBEFIRE_EXCEPTION_MESSAGE_EXPOSITION";
	public static final String ENVIRONMENT_STATEPROCESSING_THREADS = "TRIBEFIRE_STATEPROCESSING_THREADS";
	public static final String ENVIRONMENT_PREFER_IPV6 = "TRIBEFIRE_PREFER_IPV6";
	public static final String ENVIRONMENT_NETWORK_INTERFACE_BLACKLIST = "TRIBEFIRE_NETWORK_INTERFACE_BLACKLIST";
	public static final String ENVIRONMENT_EXTERNAL_PROPERTIES_LOCATION = "TRIBEFIRE_EXTERNAL_PROPERTIES_LOCATION";

	public static final String ENVIRONMENT_HOSTNAME = "TRIBEFIRE_HOSTNAME";
	public static final String ENVIRONMENT_IP_ADDRESS = "TRIBEFIRE_IP_ADDRESS";
	public static final String ENVIRONMENT_IP4_ADDRESS = "TRIBEFIRE_IP4_ADDRESS";
	public static final String ENVIRONMENT_IP6_ADDRESS = "TRIBEFIRE_IP6_ADDRESS";

	/**
	 * The directory pointing to the installation root which is used for further resolving of directories like the conf
	 * directory or the storage.
	 */
	public static final String ENVIRONMENT_INSTALLATION_ROOT_DIR = "TRIBEFIRE_INSTALLATION_ROOT_DIR";

	/**
	 * The directory (optional) containing external configuration files that are injected at startup.
	 */
	public static final String ENVIRONMENT_CONFIGURATION_DIR = "TRIBEFIRE_CONFIGURATION_DIR";

	/**
	 * Defines the URL pointing to an external configuration file which will be read at startup and fills the internal
	 * DeployableRegistry. e.g.: for external configuration of ConnectionPools.
	 */
	public static final String ENVIRONMENT_CONFIGURATION_INJECTION_URL = "TRIBEFIRE_CONFIGURATION_INJECTION_URL";

	/**
	 * Defines the name of the environment variable that contains an external configuration JSON which will be read at
	 * startup and fills the internal DeployableRegistry. e.g.: for external configuration of ConnectionPools.
	 */
	public static final String ENVIRONMENT_CONFIGURATION_INJECTION_ENVVARIABLE = "TRIBEFIRE_CONFIGURATION_INJECTION_ENVVARIABLE";

	/**
	 * Defines the maximum idle time of standard user sessions as a single string containing the value and time unit.
	 * e.g.: {@code 30m} = thirty minutes, {@code 2h} = two hours, {@code 1.5d} = one day and a half, {@code 1000s} =
	 * one thousand seconds etc.
	 */
	public static final String ENVIRONMENT_USER_SESSIONS_MAX_IDLE_TIME = "TRIBEFIRE_USER_SESSIONS_MAX_IDLE_TIME";

	/**
	 * Defines whether information about the user sessions created by the tribefire services instance are to be
	 * persisted to the user statistics access.
	 */
	public static final String ENVIRONMENT_USER_SESSIONS_STATISTICS_ENABLED = "TRIBEFIRE_USER_SESSIONS_STATISTICS_ENABLED";

	/**
	 * Defines whether the internal user sessions created by the tribefire services instance is to be recycled over
	 * time.
	 */
	public static final String ENVIRONMENT_INTERNAL_USER_SESSIONS_RECYCLING_ENABLED = "TRIBEFIRE_INTERNAL_USER_SESSIONS_RECYCLING_ENABLED";

	/**
	 * Defines the interval of the recycling of the internal user sessions created by the tribefire services. This
	 * interval must be shorted than the max age or max idle time of the internal user sessions.
	 */
	public static final String ENVIRONMENT_INTERNAL_USER_SESSIONS_RECYCLING_INTERVAL = "TRIBEFIRE_INTERNAL_USER_SESSIONS_RECYCLING_INTERVAL";

	/**
	 * Defines the maximum idle time of internal user sessions created by the tribefire services instance. When the
	 * recycling of the internal user sessions is disabled, this value is only set to the internal user session when the
	 * instance is shutting down. The value must be a single string containing the value and time unit. e.g.:
	 * {@code 30m} = thirty minutes, {@code 2h} = two hours, {@code 1.5d} = one day and a half, {@code 1000s} = one
	 * thousand seconds etc.
	 */
	public static final String ENVIRONMENT_INTERNAL_USER_SESSIONS_MAX_IDLE_TIME = "TRIBEFIRE_INTERNAL_USER_SESSIONS_MAX_IDLE_TIME";

	/**
	 * Defines the maximum age internal user sessions created by the tribefire services instance. The value must be a
	 * single string containing the value and time unit. e.g.: {@code 30m} = thirty minutes, {@code 2h} = two hours,
	 * {@code 1.5d} = one day and a half, {@code 1000s} = one thousand seconds etc.
	 */
	public static final String ENVIRONMENT_INTERNAL_USER_SESSIONS_MAX_AGE = "TRIBEFIRE_INTERNAL_USER_SESSIONS_MAX_AGE";

	// LOGGING
	public static final String ENVIRONMENT_LOG_LEVEL = "TRIBEFIRE_LOG_LEVEL";

	/**
	 * Defines the threshold of a query execution to enforce logging of the query on INFO level. (default: 10sec)
	 */
	public static final String ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_INFO = "TRIBEFIRE_QUERYTRACING_EXECUTIONTHRESHOLD_INFO";
	/**
	 * Defines the threshold of a query execution to enforce logging of the query on WARNING level. (default: 30sec)
	 */
	public static final String ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_WARNING = "TRIBEFIRE_QUERYTRACING_EXECUTIONTHRESHOLD_WARNING";
	/**
	 * Defines whether threads are renamed on runtime
	 */
	public static final String ENVIRONMENT_THREAD_RENAMING = "TRIBEFIRE_THREAD_RENAMING";
	/**
	 * Defines the prefix for all topics/queues used for messaging. If this is not set, the value of TRIBEFIRE_TENANT_ID
	 * will be used instead. If that is not set, no prefix will be used. If this value is "none", no prefix will be
	 * used.
	 */
	public static final String ENVIRONMENT_MESSAGING_DESTINATION_PREFIX = "TRIBEFIRE_MESSAGING_DESTINATION_PREFIX";
	/**
	 * Defines how long (in milliseconds) a multicast processor waits for responses from the known instances if no
	 * request timeout is given in the multicast request itself.
	 */
	public static final String ENVIRONMENT_MULTICAST_PROCESSING_TIMEOUT = "TRIBEFIRE_MULTICAST_PROCESSING_TIMEOUT";
	/**
	 * Defines the threshold (in milliseconds) of a multicast request processing to enforce logging of the query on
	 * WARNING level.
	 */
	public static final String ENVIRONMENT_MULTICAST_PROCESSING_WARNINGTHRESHOLD = "TRIBEFIRE_MULTICAST_PROCESSING_WARNINGTHRESHOLD";
	/**
	 * Defines the interval (in milliseconds) of keep alive signals for long running multicasted processes.
	 */
	public static final String ENVIRONMENT_MULTICAST_KEEP_ALIVE_INTERVAL = "TRIBEFIRE_MULTICAST_KEEP_ALIVE_INTERVAL";
	/**
	 * Defines the modules directory path.
	 */
	public static final String ENVIRONMENT_MODULES_DIR = "TRIBEFIRE_MODULES_DIR";

	// PLATFORM ASSETS
	/**
	 * Activates Platform Asset support.
	 */
	public static final String ENVIRONMENT_PLATFORM_SETUP_SUPPORT = "TRIBEFIRE_PLATFORM_SETUP_SUPPORT";

	/**
	 * Activated in case further analysis of installed or deployed assets is necessary. If set to true temporarily
	 * created asset artifacts are being kept in the respective temp folder (which is storage/tmp). This folder contains
	 * files like the specific part files as well as hashes of the artifact.
	 */
	public static final String ENVIRONMENT_KEEP_TRANSFERRED_ASSET_DATA = "TRIBEFIRE_KEEP_TRANSFERRED_ASSET_DATA";

	// APPLICATION URLs
	public static final String ENVIRONMENT_CONTROL_CENTER_URL = "TRIBEFIRE_CONTROL_CENTER_URL";
	public static final String ENVIRONMENT_EXPLORER_URL = "TRIBEFIRE_EXPLORER_URL";
	public static final String ENVIRONMENT_MODELER_URL = "TRIBEFIRE_MODELER_URL";
	public static final String ENVIRONMENT_TRIBEFIRE_JS_URL = "TRIBEFIRE_JS_URL";

	// PLATFORM MESSAGING DESTINATION NAMES
	public static final String ENVIRONMENT_MESSAGING_TOPIC_MULTICAST_REQUEST = "TRIBEFIRE_MESSAGING_TOPIC_MULTICAST_REQUEST";
	public static final String ENVIRONMENT_MESSAGING_TOPIC_MULTICAST_RESPONSE = "TRIBEFIRE_MESSAGING_TOPIC_MULTICAST_RESPONSE";
	public static final String ENVIRONMENT_MESSAGING_QUEUE_TRUSTED_REQUEST = "TRIBEFIRE_MESSAGING_QUEUE_TRUSTED_REQUEST";
	public static final String ENVIRONMENT_MESSAGING_TOPIC_TRUSTED_RESPONSE = "TRIBEFIRE_MESSAGING_TOPIC_TRUSTED_RESPONSE";
	public static final String ENVIRONMENT_MESSAGING_TOPIC_HEARTBEAT = "TRIBEFIRE_MESSAGING_TOPIC_HEARTBEAT";
	public static final String ENVIRONMENT_MESSAGING_TOPIC_UNLOCK = "TRIBEFIRE_MESSAGING_TOPIC_UNLOCK";
	public static final String ENVIRONMENT_MESSAGING_TOPIC_DBL_BROADCAST = "TRIBEFIRE_MESSAGING_TOPIC_DBL_BROADCAST";
	public static final String ENVIRONMENT_MESSAGING_TOPIC_DBL_REMOTE = "TRIBEFIRE_MESSAGING_TOPIC_DBL_REMOTE";
	public static final String ENVIRONMENT_MESSAGING_QUEUE_DBL_REMOTE = "TRIBEFIRE_MESSAGING_QUEUE_DBL_REMOTE";

	public static final String ENVIRONMENT_TRIBEFIRE_PUBLIC_PROPERTIES_LIST = "TRIBEFIRE_PUBLIC_PROPERTIES_LIST";
	private static final Set<String> ENVIRONMENT_PUBLIC_PROPERTIES;
	private static final Set<String> ENVIRONMENT_HIDDEN_PROPERTIES_REGEX;
	private static final Set<String> ENVIRONMENT_HIDDEN_PROPERTIES = new HashSet<>();
	private static final Map<String, PropertyValue> properties = new ConcurrentHashMap<>();

	private static ThreadLocal<Counter> startupLocal = new ThreadLocal<>();
	private static ThreadLocal<Counter> shutdownLocal = new ThreadLocal<>();

	// These properties are needed for providing the TF runtime in the JMX console
	private static Map<String, TribefireRuntimeMBean> tribefireRuntimeMBeans = new ConcurrentHashMap<>();

	public static final String ENVIRONMENT_COOKIE_PATH = "TRIBEFIRE_COOKIE_PATH";
	public static final String ENVIRONMENT_COOKIE_DOMAIN = "TRIBEFIRE_COOKIE_DOMAIN";
	public static final String ENVIRONMENT_COOKIE_HTTPONLY = "TRIBEFIRE_COOKIE_HTTPONLY";
	public static final String ENVIRONMENT_COOKIE_ENABLED = "TRIBEFIRE_COOKIE_ENABLED";

	// Security-related
	public static final String ENVIRONMENT_USERSESSION_IP_VERIFICATION = "TRIBEFIRE_USERSESSION_IP_VERIFICATION";
	public static final String ENVIRONMENT_USERSESSION_IP_VERIFICATION_ALIASES = "TRIBEFIRE_USERSESSION_IP_VERIFICATION_ALIASES";

	// DB-related general settings
	public static final String ENVIRONMENT_DATABASE_USE_BLOB_BUFFER = "TRIBEFIRE_DATABASE_USE_BLOB_BUFFER";

	// DCSA SHARED STORAGE
	public static final String ENVIRONMENT_DCSA_STORAGE = "TRIBEFIRE_DCSA_STORAGE";
	public static final String ENVIRONMENT_DCSA_STORAGE_LOCATION = "TRIBEFIRE_DCSA_STORAGE_LOCATION";

	// Injected CSA initializers
	public static final String ENVIRONMENT_MANIPULATION_PRIMING = "TRIBEFIRE_MANIPULATION_PRIMING"; // supposed to be
																									// _POSTINIT
	public static final String ENVIRONMENT_MANIPULATION_PRIMING_PREINIT = "TRIBEFIRE_MANIPULATION_PRIMING_PREINIT";

	// cortex CSA - temporary
	/* We have switched cortex in web-platform to initialized model/data together, stage by stage. Temporarily, if we
	 * want to return the logic to first all models, then all data, it's possible via setting this property to
	 * "true". */
	public static final String ENVIRONMENT_CORTEX_MODELS_FIRST = "TRIBEFIRE_CORTEX_MODELS_FIRST";

	// If set to true, default system accesses (auth, user-sessions, ..) are forced and CortexConfiguration is ignored.
	public static final String ENVIRONMENT_FORCE_DEFAULT_SYSTEM_ACCESSES = "TRIBEFIRE_FORCE_DEFAULT_SYSTEM_ACCESSES";

	// This will be set by either the MasterCartridgeWebApp (TFS) or CartridgeWebApp (Cartridge), when the web app has
	// finished the initialization procedure. This property will contain the timestamp of the startup (in ms since 1 Jan
	// 1970)
	public static final String ENVIRONMENT_INITIALIZATION_COMPLETED = "TRIBEFIRE_INITIALIZATION_COMPLETED";

	// This will be set when there is a mechanism that monitors whether TFS (or Cartridge) is reachable from the outside
	// (e.g., in Kubernetes)
	// If there is no such mechanism in place, this property will not be set. If there is a mechanism, this property
	// either contains the String "pending-" (followed by the time the monitoring started)
	// or "started-", followed by the timestamp on when it became available (as ms since 1.1.1970).
	public static final String ENVIRONMENT_STARTUP_STATE = "TRIBEFIRE_STARTUP_STATE";

	public static final String ENVIRONMENT_TRIBEFIRE_DECRYPT_SECRET = "TRIBEFIRE_DECRYPT_SECRET";
	public static final String DEFAULT_TRIBEFIRE_DECRYPTION_SECRET = "c36e99ec-e108-11e8-9f32-f2801f1b9fd1";

	public static final String ENVIRONMENT_TRIBEFIRE_WEB_LOGIN_RELATIVE_PATH = "TRIBEFIRE_WEB_LOGIN_RELATIVE_PATH";

	public final static Set<String> CACHEABLE_RUNTIME_PROPERTIES = ConcurrentHashMap.newKeySet();

	protected static Map<String, Set<RuntimePropertyChangeListener>> changeListener = new ConcurrentHashMap<>();
	protected static ReentrantLock changeListenerLock = new ReentrantLock();

	static {

		ENVIRONMENT_PUBLIC_PROPERTIES = new HashSet<String>();
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_PUBLIC_SERVICES_URL);
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_WEBSOCKET_URL);
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_CONTROL_CENTER_URL);
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_EXPLORER_URL);
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_TRIBEFIRE_JS_URL);
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_PLATFORM_SETUP_SUPPORT);
		ENVIRONMENT_PUBLIC_PROPERTIES.add(ENVIRONMENT_TRIBEFIRE_WEB_LOGIN_RELATIVE_PATH);
		setProperty(ENVIRONMENT_TRIBEFIRE_PUBLIC_PROPERTIES_LIST, StringTools.createStringFromCollection(ENVIRONMENT_PUBLIC_PROPERTIES, ", "));

		ENVIRONMENT_HIDDEN_PROPERTIES_REGEX = new HashSet<String>();
		ENVIRONMENT_HIDDEN_PROPERTIES_REGEX.add("TRIBEFIRE_CONFIGURATION_INJECTION_JSON.*");
		ENVIRONMENT_HIDDEN_PROPERTIES.add("TF_SYS_DB_PASSWORD");

		String preferIP6 = getProperty(ENVIRONMENT_PREFER_IPV6);
		if (preferIP6 != null) {
			boolean preferIP6Bool = Boolean.parseBoolean(preferIP6);
			NetworkTools.preferIPv6(preferIP6Bool);
		}
		String networkInterfaceBlackList = getProperty(ENVIRONMENT_NETWORK_INTERFACE_BLACKLIST);
		if (networkInterfaceBlackList != null && networkInterfaceBlackList.trim().length() > 0) {
			String[] elements = null;
			try {
				elements = networkInterfaceBlackList.split(",");
			} catch (Exception e) {
				logger.error("Could not split " + networkInterfaceBlackList, e);
			}
			if (elements != null && elements.length > 0) {
				for (String element : elements) {
					NetworkTools.addToNetworkInterfacesBlacklist(element.trim());
				}
			}
		}

		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_NODE_ID);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_TMP_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_DATA_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_REPO_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_CACHE_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_CONFIGURATION_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_STORAGE_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_MANIPULATION_PRIMING_PREINIT);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_CONTAINER_ROOT_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_INSTALLATION_ROOT_DIR);
		CACHEABLE_RUNTIME_PROPERTIES.add(ENVIRONMENT_EXTERNAL_PROPERTIES_LOCATION);

		initDefaults();

	}

	public static String setProperty(String propertyName, String propertyValue) {
		boolean trace = logger.isTraceEnabled();

		if (propertyName == null) {
			logger.trace("Somebody is trying to set a property with name null.");
			return null;
		}
		String oldValue = null;
		String newValue = propertyValue;

		PropertyValue oldPropertyValue = properties.get(propertyName);
		if (oldPropertyValue != null) {
			oldValue = oldPropertyValue.getValue();
		}

		// Log the according operation depending on the propertyValue.
		if (trace) {
			if (propertyValue == null) {
				logger.trace("Removing tf runtime property: " + propertyName);
			} else {
				logger.trace("Setting tf runtime property: " + propertyName + "=" + propertyValue);
			}
		}

		if (oldPropertyValue instanceof DynamicValue) {
			// A Dynamic value is configured for this property.
			// We want to keep the default behavior in case the property value will be cleared
			// So instead of just adding a StaticValue for this property we add the static value to the DynamicValue
			// If the passed propertyValue is null we implicitly clear the static value of the DynamicValue as well,
			// thus we ensure the default behavior again.
			DynamicValue oldDynamicPropertyValue = (DynamicValue) oldPropertyValue;
			oldDynamicPropertyValue.setValue(propertyValue);
		} else {
			// Depending on the propertValue we either set a new StaticValue or remove the property if value is null.
			if (propertyValue == null) {
				properties.remove(propertyName);
			} else {
				properties.put(propertyName, new StaticValue(propertyValue));
			}
		}

		informRuntimePropertyChangeListener(propertyName, oldValue, newValue);

		return oldValue;
	}

	/**
	 * Returns the value of a property with its property placeholder recursively resolved as an Optional. If any
	 * resolution during the recursion cannot be resolved the optional will be empty.
	 */
	public static Optional<String> getResolvedProperty(String propertyName) {
		String result = getProperty(propertyName, null, false);

		return (result == null) ? Optional.empty() : Optional.of(result);

	}

	private static class ResolutionTrackingMergeContext extends MergeContext {
		private boolean resolutionIncomplete;

		@Override
		public Object getVariableValue(String variableName) throws TemplateException {

			if (variableName.contains("(") && variableName.endsWith(")")) {
				int idx1 = variableName.indexOf("(");
				int idx2 = variableName.lastIndexOf(")");
				if (idx1 > 0 && idx2 > idx1) {
					String method = variableName.substring(0, idx1);
					String param = variableName.substring(idx1 + 1, idx2);
					switch (method) {
						case "decrypt":
							if ((param.startsWith("'") && param.endsWith("'")) || (param.startsWith("\"") && param.endsWith("\""))) {
								param = param.substring(1, param.length() - 1);
								String secret = getProperty(ENVIRONMENT_TRIBEFIRE_DECRYPT_SECRET);
								return Cryptor.decrypt(secret, null, null, null, param);
							}
							break;
						default:
							throw new RuntimeException("Unsupported variable function: " + method);
					}
				}
			}

			Optional<String> optional = getResolvedProperty(variableName);

			if (optional.isPresent())
				return optional.get();
			else {
				resolutionIncomplete = true;
				return "?";
			}
		}

		public boolean isResolutionIncomplete() {
			return resolutionIncomplete;
		}
	}

	public static String getProperty(String propertyName) {
		String value = getProperty(propertyName, null);
		if (value == null && ENVIRONMENT_SERVICES_URL.equals(propertyName)) {
			throw new RuntimeException(
					ENVIRONMENT_SERVICES_URL + " is not configured as Tribefire Runtime Property (e.g.: in conf/tribefire.properties)s");
		}
		return value;
	}

	public static String getProperty(String propertyName, String defaultValue) {
		return getProperty(propertyName, defaultValue, false);
	}

	public static String getProperty(String propertyName, String defaultValue, boolean explicitOnly) {
		return getPropertyValue(propertyName, defaultValue, explicitOnly);
	}

	private static String getPropertyValue(String propertyName, String defaultValue, boolean explicitOnly) {
		String rawValue = _getPropertyValue(propertyName, defaultValue, explicitOnly);
		if (rawValue == null)
			return null;

		Template template = Template.parse(rawValue);
		if (!template.containsVariables())
			return rawValue;

		try {
			ResolutionTrackingMergeContext mergeContext = new ResolutionTrackingMergeContext();
			String result = template.merge(mergeContext);

			if (mergeContext.isResolutionIncomplete()) {
				logger.info(() -> "The variable " + propertyName + " could not be resolved to an actual value (raw value is " + rawValue + ").");
				return null;
			}

			if (CACHEABLE_RUNTIME_PROPERTIES.contains(propertyName)) {
				setProperty(propertyName, result);
			}

			return result;

		} catch (TemplateException e) {
			throw Exceptions.unchecked(e, "Error while resolving property: " + propertyName);
		}
	}

	private static String _getPropertyValue(String propertyName, String defaultValue, boolean explicitOnly) {
		PropertyValue propertyValue = properties.get(propertyName);

		if (propertyValue != null) {
			if (!explicitOnly || propertyValue.isExplicit()) {
				String value = propertyValue.getValue();
				if (value != null)
					return value;
			}
		}

		return getLowLevelProperty(propertyName, defaultValue);
	}

	private static String getLowLevelProperty(String propertyName, String defaultValue) {
		String value = System.getProperty(propertyName);
		if (value != null)
			return value;

		value = System.getenv(propertyName);
		if (value != null)
			return value;

		return defaultValue;
	}

	public static Set<String> getPublicPropertyNames() {
		Set<String> propertyNames = getPropertyNames();
		propertyNames.retainAll(ENVIRONMENT_PUBLIC_PROPERTIES);
		return propertyNames;
	}

	public static Set<String> getPrivatePropertyNamesRegex() {
		return Collections.unmodifiableSet(ENVIRONMENT_HIDDEN_PROPERTIES_REGEX);
	}

	public static boolean isPropertyPrivate(String propertyName) {
		if (propertyName == null)
			return false;

		if (ENVIRONMENT_HIDDEN_PROPERTIES.contains(propertyName))
			return true;

		for (String regex : ENVIRONMENT_HIDDEN_PROPERTIES_REGEX)
			if (propertyName.matches(regex))
				return true;

		return false;
	}

	public static void setPropertyPrivate(String... propertyNames) {
		if (propertyNames != null && propertyNames.length > 0) {
			for (String propName : propertyNames) {
				ENVIRONMENT_HIDDEN_PROPERTIES.add(propName);
			}
		}
	}

	public static Set<String> getPropertyNames() {
		Set<String> tribeFireRunTimeProperties = new HashSet<String>();
		tribeFireRunTimeProperties.addAll(properties.keySet());

		Set<Object> systemPropertiesSet = System.getProperties().keySet();
		tribeFireRunTimeProperties = fillSetWithPropertyNames(systemPropertiesSet, tribeFireRunTimeProperties);

		Set<String> systemEnvSet = System.getenv().keySet();
		tribeFireRunTimeProperties = fillSetWithPropertyNames(systemEnvSet, tribeFireRunTimeProperties);

		return tribeFireRunTimeProperties;
	}

	public static Map<String, String> getPublicProperties() {

		Set<String> propertyNames = getPublicPropertyNames();
		Map<String, String> propertiesMap = new HashMap<String, String>();
		for (String propertyName : propertyNames) {

			String propertyValue = com.braintribe.model.processing.bootstrapping.TribefireRuntime.getProperty(propertyName);
			if (propertyValue != null) {
				propertiesMap.put(propertyName, propertyValue);
			}

		}

		return propertiesMap;
	}

	private static Set<String> fillSetWithPropertyNames(Set<? extends Object> propertiesSet, Set<String> tribeFireRunTimeProperties) {
		for (Object currentProperty : propertiesSet) {
			if (currentProperty instanceof String) {
				String currentPropertyName = (String) currentProperty;
				if (currentPropertyName.startsWith("TRIBEFIRE_")) {
					tribeFireRunTimeProperties.add(currentPropertyName);
				}
			}
		}
		return tribeFireRunTimeProperties;
	}

	public static boolean isStartingUp() {
		Counter counter = startupLocal.get();
		return counter != null;
	}

	public static boolean isShuttingDown() {
		Counter counter = shutdownLocal.get();
		return counter != null;
	}

	public static void enterStartup() {
		Counter counter = startupLocal.get();
		if (counter == null) {
			counter = new Counter();
			startupLocal.set(counter);
		}

		counter.count++;
	}

	public static void enterShutdown() {
		Counter counter = shutdownLocal.get();
		if (counter == null) {
			counter = new Counter();
			shutdownLocal.set(counter);
		}

		counter.count++;
	}

	public static void leaveStartup() {
		Counter counter = startupLocal.get();

		if (counter != null) {
			if (--counter.count == 0) {
				startupLocal.remove();
			}
		}
	}

	public static void leaveShutdown() {
		Counter counter = shutdownLocal.get();

		if (counter != null) {
			if (--counter.count == 0) {
				shutdownLocal.remove();
			}
		}
	}

	public static void shutdown() {
		startupLocal.remove();
		shutdownLocal.remove();
	}

	private static class Counter {

		public int count;
	}

	public static String getCookiePath() {
		return getProperty(ENVIRONMENT_COOKIE_PATH);
	}

	public static String getCookieDomain() {
		return getProperty(ENVIRONMENT_COOKIE_DOMAIN);
	}

	public static boolean getCookieHttpOnly() {
		String valueString = getProperty(ENVIRONMENT_COOKIE_HTTPONLY);
		boolean cookieHttpOnly = false;
		if (!StringTools.isBlank(valueString)) {
			cookieHttpOnly = valueString.equalsIgnoreCase("true");
		}
		return cookieHttpOnly;
	}

	public static boolean getCookieEnabled() {
		String valueString = getProperty(ENVIRONMENT_COOKIE_ENABLED);
		boolean cookieEnabled = true;
		if (!StringTools.isBlank(valueString)) {
			cookieEnabled = valueString.equalsIgnoreCase("true");
		}
		return cookieEnabled;

	}

	public static String getInstallationRoot() {
		return getProperty(ENVIRONMENT_INSTALLATION_ROOT_DIR);
	}

	public static String getContainerRoot() {
		return getProperty(ENVIRONMENT_CONTAINER_ROOT_DIR);
	}

	public static boolean isExtensionHost() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_IS_EXTENSION_HOST));
	}

	public static String getServicesUrl() {
		return getProperty(ENVIRONMENT_SERVICES_URL);
	}

	public static String getPublicServicesUrl() {
		return getProperty(ENVIRONMENT_PUBLIC_SERVICES_URL);
	}

	public static String getStorageDir() {
		return getProperty(ENVIRONMENT_STORAGE_DIR);
	}

	public static String getSetupInfoDir() {
		return getProperty(ENVIRONMENT_SETUP_INFO_DIR);
	}

	public static String getConfigurationDir() {
		String rawConfigurationDir = getResolvedProperty(ENVIRONMENT_CONFIGURATION_DIR).orElseGet(() -> null);
		File candidateConfigurationDir = new File(rawConfigurationDir);
		String containerRoot = getContainerRoot();

		if (candidateConfigurationDir.isAbsolute() || containerRoot == null) {
			return candidateConfigurationDir.getAbsolutePath();
		} else {
			return new File(new File(containerRoot), rawConfigurationDir).getAbsolutePath();
		}
	}

	public static boolean getAcceptSslCertificates() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_ACCEPT_SSL_CERTIFICATES));
	}

	public static boolean getExceptionExposition() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_EXCEPTION_EXPOSITION));
	}

	public static boolean getExceptionMessageExposition() {
		return !Boolean.FALSE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_EXCEPTION_MESSAGE_EXPOSITION));
	}

	public static boolean isClustered() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_IS_CLUSTERED));
	}

	public static String getExecutionMode() {
		return getProperty(ENVIRONMENT_EXECUTION_MODE);
	}

	public static String getControlCenterUrl() {
		return getProperty(ENVIRONMENT_CONTROL_CENTER_URL);
	}

	public static String getExplorerUrl() {
		return getProperty(ENVIRONMENT_EXPLORER_URL);
	}

	public static String getTribefireJsUrl() {
		return getProperty(ENVIRONMENT_TRIBEFIRE_JS_URL);
	}

	public static String getModelerUrl() {
		return getProperty(ENVIRONMENT_MODELER_URL);
	}

	public static boolean getPlatformSetupSupport() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_PLATFORM_SETUP_SUPPORT));
	}

	public static boolean getKeepTransferredAssetData() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_KEEP_TRANSFERRED_ASSET_DATA));
	}

	public static boolean getForceDefaultSystemAccesses() {
		return Boolean.TRUE.toString().equalsIgnoreCase(getProperty(ENVIRONMENT_FORCE_DEFAULT_SYSTEM_ACCESSES));
	}

	/**
	 * @Deprecated, usage of this method should be replaced with {@link #hasExplicitProperty(String)}.
	 */
	@Deprecated
	public static boolean hasProperty(String propertyName) {
		return hasExplicitProperty(propertyName);
	}

	public static boolean hasExplicitProperty(String propertyName) {
		return (getPropertyValue(propertyName, null, true) != null);
	}

	public static void registerMbean(String... contextNames) {

		if (contextNames == null || contextNames.length == 0) {
			contextNames = new String[] { null };
		}
		for (String contextName : contextNames) {

			final String mbeanObjectNameString;
			if (contextName == null) {
				mbeanObjectNameString = "com.braintribe.tribefire:type=TribefireRuntime";
			} else {
				mbeanObjectNameString = "com.braintribe.tribefire:type=TribefireRuntime,name=" + contextName;
			}

			TribefireRuntimeMBean bean = tribefireRuntimeMBeans.computeIfAbsent(mbeanObjectNameString, name -> {
				TribefireRuntimeMBean tribefireRuntimeMBean = null;
				try {
					MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
					ObjectName mbeanName = new ObjectName(name);

					if (!mbs.isRegistered(mbeanName)) {
						tribefireRuntimeMBean = new TribefireRuntimeMBeanImpl();
						mbs.registerMBean(tribefireRuntimeMBean, mbeanName);
					} else {
						tribefireRuntimeMBean = JMX.newMBeanProxy(mbs, mbeanName, TribefireRuntimeMBean.class);
					}
				} catch (Throwable e) {
					logger.error("Could not register TribefireRuntimeMBean " + name, e);
				}
				return tribefireRuntimeMBean;
			});
			if (bean == null) {
				logger.debug(() -> "Could not create TribefireRuntimeMBean with name: " + mbeanObjectNameString);
			}
		}
	}
	public static void unregisterMbean() {

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		for (Map.Entry<String, TribefireRuntimeMBean> entry : tribefireRuntimeMBeans.entrySet()) {
			String name = entry.getKey();

			try {
				ObjectName mbeanName = new ObjectName(name);
				if (mbs.isRegistered(mbeanName)) {
					mbs.unregisterMBean(mbeanName);
				}
			} catch (Throwable e) {
				logger.error("Could not unregister TribefireRuntimeMBean " + name, e);
			}
		}
		tribefireRuntimeMBeans.clear();
	}

	public static void addPropertyChangeListener(String propertyName, RuntimePropertyChangeListener listener) {
		if (listener != null) {
			if (propertyName == null) {
				propertyName = "*";
			}
			changeListenerLock.lock();
			try {
				Set<RuntimePropertyChangeListener> set = changeListener.computeIfAbsent(propertyName, pn -> new HashSet<>());
				set.add(listener);
			} finally {
				changeListenerLock.unlock();
			}
		}
	}
	public static void removePropertyChangeListener(String propertyName, RuntimePropertyChangeListener listener) {
		if (listener != null) {
			if (propertyName == null) {
				propertyName = "*";
			}
			changeListenerLock.lock();
			try {
				Set<RuntimePropertyChangeListener> set = changeListener.get(propertyName);
				if (set != null) {
					set.remove(listener);
					if (set.isEmpty()) {
						changeListener.remove(propertyName);
					}
				}
			} finally {
				changeListenerLock.unlock();
			}
		}
	}

	private static void informRuntimePropertyChangeListener(String propertyName, String oldValue, String newValue) {
		Set<RuntimePropertyChangeListener> listenersToInform = new HashSet<>();
		changeListenerLock.lock();
		try {
			Set<RuntimePropertyChangeListener> set = changeListener.get(propertyName);
			if (set != null) {
				listenersToInform.addAll(set);
			}
			set = changeListener.get("*");
			if (set != null) {
				listenersToInform.addAll(set);
			}
		} finally {
			changeListenerLock.unlock();
		}
		boolean trace = logger.isTraceEnabled();
		String msg = trace ? propertyName + "=" + newValue + " (was: " + oldValue + ")" : null;
		for (RuntimePropertyChangeListener listener : listenersToInform) {
			try {
				if (trace)
					logger.trace("Informing listener " + listener + " about property change: " + msg);
				listener.propertyChanged(propertyName, oldValue, newValue);
			} catch (Throwable t) {
				logger.error("The listener " + listener + " threw an exception when listening for the property change " + propertyName + "="
						+ newValue + " (was: " + oldValue + ")", t);
			}
		}
	}

	private static interface PropertyValue {
		String getValue();
		boolean isExplicit();
	}

	private static class StaticValue implements PropertyValue {

		private final String value;

		public StaticValue(String value) {
			this.value = value;
		}
		@Override
		public String getValue() {
			return value;
		}
		@Override
		public boolean isExplicit() {
			return true;
		}
	}

	private static abstract class DynamicValue implements PropertyValue {
		private final String propertyName;
		private String value;

		public DynamicValue(String propertyName) {
			this.propertyName = propertyName;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			if (value != null) {
				return value;
			}
			return getLowLevelProperty(propertyName, getDefaultValue());
		}

		public abstract String getDefaultValue();

		@Override
		public boolean isExplicit() {
			return (value != null) || (getLowLevelProperty(propertyName, null) != null);
		}

	}

	private static class DynamicValueWithDefault extends DynamicValue {
		private final String defaultValue;

		public DynamicValueWithDefault(String propertyName, String defaultValue) {
			super(propertyName);
			this.defaultValue = defaultValue;
		}

		@Override
		public String getDefaultValue() {
			return defaultValue;
		}
	}

	private static void initDefaults() {

		// First we initialize all default values in a separate map for later reuse.

		setDefault(ENVIRONMENT_CONTAINER_ROOT_DIR, null);
		setDefault(ENVIRONMENT_IS_EXTENSION_HOST, "false");
		setDefault(ENVIRONMENT_ACCEPT_SSL_CERTIFICATES, "true");
		setDefault(ENVIRONMENT_EXCEPTION_EXPOSITION, "true");
		setDefault(ENVIRONMENT_IS_CLUSTERED, "false");
		setDefault(ENVIRONMENT_EXECUTION_MODE, "mixed");
		setDefault(ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_INFO, "10000");
		setDefault(ENVIRONMENT_QUERYTRACING_EXECUTIONTHRESHOLD_WARNING, "30000");
		setDefault(ENVIRONMENT_COOKIE_HTTPONLY, "false");
		setDefault(ENVIRONMENT_COOKIE_ENABLED, "true");
		setDefault(ENVIRONMENT_PLATFORM_SETUP_SUPPORT, "true");
		setDefault(ENVIRONMENT_KEEP_TRANSFERRED_ASSET_DATA, "false");
		setDefault(ENVIRONMENT_FORCE_DEFAULT_SYSTEM_ACCESSES, "false");
		setDefault(ENVIRONMENT_TRIBEFIRE_DECRYPT_SECRET, DEFAULT_TRIBEFIRE_DECRYPTION_SECRET);

		properties.put(ENVIRONMENT_STORAGE_DIR, new DynamicValue(ENVIRONMENT_STORAGE_DIR) {
			@Override
			public String getDefaultValue() {

				Optional<String> installationRootDir = getResolvedProperty(ENVIRONMENT_INSTALLATION_ROOT_DIR);
				if (installationRootDir.isPresent()) {
					return installationRootDir.get() + "/storage";

				} else {
					String containerRootDir = getProperty(ENVIRONMENT_CONTAINER_ROOT_DIR, ".");
					if (containerRootDir == null || containerRootDir.equals("null")) {
						containerRootDir = ".";
					}
					return containerRootDir + "/../storage";
				}

			}
		});
		properties.put(ENVIRONMENT_CONFIGURATION_DIR, new DynamicValue(ENVIRONMENT_CONFIGURATION_DIR) {
			@Override
			public String getDefaultValue() {

				Optional<String> installationRootDir = getResolvedProperty(ENVIRONMENT_INSTALLATION_ROOT_DIR);
				if (installationRootDir.isPresent()) {
					return installationRootDir.get() + "/conf";

				} else {
					String containerRootDir = getProperty(ENVIRONMENT_CONTAINER_ROOT_DIR, ".");
					if (containerRootDir == null || containerRootDir.equals("null")) {
						containerRootDir = ".";
					}
					return containerRootDir + "/../conf";
				}

			}
		});
		properties.put(ENVIRONMENT_MODULES_DIR, new DynamicValue(ENVIRONMENT_MODULES_DIR) {
			@Override
			public String getDefaultValue() {

				Optional<String> installationRootDir = getResolvedProperty(ENVIRONMENT_INSTALLATION_ROOT_DIR);
				if (installationRootDir.isPresent()) {
					return installationRootDir.get() + "/modules";

				} else {
					String containerRootDir = getProperty(ENVIRONMENT_CONTAINER_ROOT_DIR, ".");
					if (containerRootDir == null || containerRootDir.equals("null")) {
						containerRootDir = ".";
					}
					return containerRootDir + "/../modules";
				}

			}
		});

		properties.put(ENVIRONMENT_HOSTNAME, new DynamicValue(ENVIRONMENT_HOSTNAME) {
			@Override
			public String getDefaultValue() {
				try {
					return InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					logger.debug("Could not get the localhost.", e);
					return NetworkTools.getNetworkAddress().getHostName();
				}
			}
		});
		properties.put(ENVIRONMENT_IP4_ADDRESS, new DynamicValue(ENVIRONMENT_IP4_ADDRESS) {
			@Override
			public String getDefaultValue() {
				InetAddress iPv4NetworkInterface = NetworkTools.getIPv4NetworkInterface();
				if (iPv4NetworkInterface != null) {
					return iPv4NetworkInterface.getHostAddress();
				} else {
					return null;
				}
			}
		});
		properties.put(ENVIRONMENT_IP6_ADDRESS, new DynamicValue(ENVIRONMENT_IP6_ADDRESS) {
			@Override
			public String getDefaultValue() {
				InetAddress iPv6NetworkInterface = NetworkTools.getIPv6NetworkInterface();
				if (iPv6NetworkInterface != null) {
					return iPv6NetworkInterface.getHostAddress();
				} else {
					return null;
				}
			}
		});
		properties.put(ENVIRONMENT_IP_ADDRESS, new DynamicValue(ENVIRONMENT_IP_ADDRESS) {
			@Override
			public String getDefaultValue() {
				return NetworkTools.getNetworkAddress().getHostAddress();
			}
		});
	}

	private static void setDefault(String name, String defaultValue) {
		properties.put(name, new DynamicValueWithDefault(name, defaultValue));
	}
	
}
