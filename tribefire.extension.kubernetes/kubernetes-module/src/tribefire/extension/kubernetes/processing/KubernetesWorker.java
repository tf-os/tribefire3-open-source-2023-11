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
package tribefire.extension.kubernetes.processing;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
import com.braintribe.logging.Logger.LogLevel;
import com.braintribe.model.kubernetes.runtime.KubernetesRuntime;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.bootstrapping.jmx.TribefireRuntimeMBean;
import com.braintribe.model.processing.bootstrapping.jmx.TribefireRuntimeMBeanTools;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerContext;
import com.braintribe.model.processing.worker.api.WorkerException;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.ResponseEntityInputStream;
import com.braintribe.transport.http.util.HttpTools;
import com.braintribe.util.network.NetworkDetectionContext;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

import tribefire.module.api.EnvironmentDenotations;

public class KubernetesWorker implements Worker, Runnable {

	private final static Logger logger = Logger.getLogger(KubernetesWorker.class);

	private KubernetesRuntime rt;

	private CloseableHttpClient client;
	private final Set<String> ipAddresses = new HashSet<>();
	private String bearer = null;
	private String initiativeName = null;
	private String cartridgeId = null;
	private TribefireRuntimeMBean runtime = null;
	private boolean run = true;
	private String url = null;

	private boolean shutdownHookSet = false;

	private EnvironmentDenotations environmentDenotations;

	@Required
	public void setEnvironmentDenotations(EnvironmentDenotations environmentDenotations) {
		this.environmentDenotations = environmentDenotations;
	}

	@Override
	public void start(WorkerContext workerContext) throws WorkerException {

		runtime = TribefireRuntimeMBeanTools.getTribefireCartridgeRuntime("master");
		if (runtime == null) {

			logger.info(() -> "Cannot access the master tribefire runtime. Not checking for Kubernetes availability.");

		} else {
			String useDefault = runtime.getProperty("TRIBEFIRE_KUBERNETES_RUNTIME_USEDEFAULT");
			if (StringTools.isBlank(useDefault) || useDefault.equalsIgnoreCase("true")) {
				rt = KubernetesRuntime.T.create();
			} else {
				rt = environmentDenotations.lookup("kubernetes-runtime-availability-configuration");
				if (rt == null) {
					throw new WorkerException("Could not find a entity with bindId kubernetes-runtime-availability-configuration.");
				}
			}
			setShutdownHook();
			workerContext.submit(this);
		}
	}

	@Override
	public void stop(WorkerContext workerContext) throws WorkerException {
		run = false;
	}

	private static void logAndRecord(LogLevel logLevel, StringBuilder logCollector, String message, Throwable t) {
		if (logger.isLevelEnabled(logLevel)) {
			logger.log(logLevel, message);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			logCollector.append(sdf.format(new Date()) + ": ");
			logCollector.append(logLevel.name() + ": ");
			logCollector.append(message);
			logCollector.append('\n');
			if (t != null) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				logCollector.append(sw.toString());
				logCollector.append('\n');
			}

		}
	}
	@Override
	public void run() {

		StringBuilder logCollector = new StringBuilder();
		try {
			runtime.setProperty(TribefireRuntime.ENVIRONMENT_STARTUP_STATE, "pending-" + System.currentTimeMillis());

			DefaultHttpClientProvider clientProvider = new DefaultHttpClientProvider();
			clientProvider.setSocketTimeout(5000);
			client = clientProvider.provideHttpClient();

			Long checkInterval = rt.getCheckIntervalMs();
			if (checkInterval == null) {
				checkInterval = Long.valueOf(Numbers.MILLISECONDS_PER_SECOND);
			}

			Long maxWaitTime = rt.getMaxWaitTimeMs();
			if (maxWaitTime == null) {
				maxWaitTime = Long.valueOf(Numbers.MILLISECONDS_PER_MINUTE * 2);
			}

			determineInitiativeName();
			determineCartridgeId();
			acquireLocalIpAddresses();
			loadBearer();
			determineUrl();

			logAndRecord(LogLevel.DEBUG, logCollector, "Starting to check the availability at " + url, null);

			long watchStart = System.currentTimeMillis();
			while (run) {

				long now = System.currentTimeMillis();
				boolean reachable = checkReachability(logCollector);
				if (reachable) {
					logAndRecord(LogLevel.INFO, logCollector,
							"Achieved availability status. Initiative name: " + initiativeName + ", Cartridge Id: " + cartridgeId, null);
					runtime.setProperty(TribefireRuntime.ENVIRONMENT_STARTUP_STATE, "started-" + now);
					break;
				}

				long waitTime = now - watchStart;
				if (waitTime > maxWaitTime) {
					throw new Exception("Exceeded the maximum wait time " + StringTools.prettyPrintDuration(maxWaitTime, true, ChronoUnit.MILLIS));
				}

				try {
					Thread.sleep(checkInterval);
				} catch (InterruptedException ie) {
					throw new Exception("Got interrupted while waiting for the reachability of this pod after "
							+ StringTools.prettyPrintDuration(waitTime, true, ChronoUnit.MILLIS));
				}
			}

		} catch (Exception e) {
			if (runtime != null) {
				runtime.setProperty(TribefireRuntime.ENVIRONMENT_STARTUP_STATE, null);
			}

			logger.warn("Error while trying to monitor the availability of this pod in Kubernetes.\nCollected log:\n" + logCollector.toString(), e);

		} finally {
			IOTools.closeCloseable(client, logger);
			logAndRecord(LogLevel.DEBUG, logCollector, "Kubernetes Runtime Watcher fulfilled its task.", null);
		}
	}

	private void determineUrl() {
		url = rt.getCheckUrl();
		if (StringTools.isBlank(url)) {

			String tmpUrl = runtime.getProperty("TRIBEFIRE_KUBERNETES_AVAILABILITY_CHECK_URL");
			if (!StringTools.isBlank(tmpUrl)) {
				url = tmpUrl;
			} else {

				String workspaceName = runtime.getProperty("WORKSPACE_NAME");
				if (StringTools.isBlank(workspaceName)) {
					throw new RuntimeException("Could not determine the workspace name.");
				}

				url = "https://kubernetes.default.svc/api/v1/namespaces/" + workspaceName + "/endpoints/" + cartridgeId;
			}
		}
		logger.debug(() -> "Using the check URL " + url);
	}

	private void acquireLocalIpAddresses() throws Exception {
		List<NetworkDetectionContext> contexts = NetworkTools.getNetworkDetectionContexts();
		if (contexts != null && !contexts.isEmpty()) {
			for (NetworkDetectionContext ctx : contexts) {
				addToAddressList(ipAddresses, ctx.getInet4Address());
				addV6ToAddressList(ipAddresses, ctx.getInet6Address());
			}
		} else {
			throw new Exception("Could not identify any local IP address.");
		}

		logger.debug(() -> "Local IP addresses: " + ipAddresses);
	}

	private void determineCartridgeId() {
		cartridgeId = rt.getCartridgeId();
		if (StringTools.isBlank(cartridgeId)) {
			String podId = runtime.getProperty("TF_OPERATOR_POD_ID");
			if (StringTools.isBlank(podId)) {
				throw new RuntimeException("TF_OPERATOR_POD_ID is not set.");
			}

			if (!podId.startsWith(initiativeName)) {
				throw new RuntimeException("It was expected that the pod ID " + podId + " starts with the name of the initiative: " + initiativeName);
			}

			String remain = podId.substring(initiativeName.length() + 1);
			int tfs = remain.lastIndexOf("tribefire-master-");
			int cartridge = remain.lastIndexOf("cartridge-");

			if (tfs != -1) {
				cartridgeId = podId.substring(0, initiativeName.length() + tfs + "tribefire-master".length() + 1);
			} else if (cartridge != -1) {
				cartridgeId = podId.substring(0, initiativeName.length() + cartridge + "cartridge".length() + 1);
			}

			if (StringTools.isBlank(cartridgeId)) {
				throw new RuntimeException(
						"Could not identify the cartridge ID by using the podId " + podId + " and the initiative name " + initiativeName);
			}
		}

		logger.debug(() -> "Identified cartridgeId: " + cartridgeId);
	}

	private void determineInitiativeName() {
		initiativeName = rt.getInitiativeName();
		if (StringTools.isBlank(initiativeName)) {
			initiativeName = runtime.getProperty("INITIATIVE_NAME");
			if (StringTools.isBlank(initiativeName)) {
				throw new RuntimeException("The INITIATIVE_NAME is not set.");
			}
		}
		logger.debug(() -> "Identified initiative: " + initiativeName);
	}

	private void loadBearer() {
		String token = rt.getCheckToken();
		String filename;
		if (!StringTools.isBlank(token)) {
			if (!token.startsWith("file://")) {
				bearer = token;
				return;
			}
			filename = token.substring("file://".length());
		} else {
			filename = "/var/run/secrets/kubernetes.io/serviceaccount/token";
		}
		logger.debug(() -> "Loading token from " + filename);

		File f = new File(filename);
		if (!f.exists()) {
			throw new RuntimeException("Could not find file " + filename);
		}
		try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
			bearer = IOTools.slurp(is, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Error while trying to read file " + filename, e);
		}
		if (StringTools.isBlank(bearer)) {
			throw new RuntimeException("Unable to load any bearer information from " + filename);
		}
	}

	private void addV6ToAddressList(Set<String> ipAddresses, Inet6Address inet6Address) {
		if (inet6Address == null) {
			return;
		}
		addToAddressList(ipAddresses, inet6Address);

		String hostAddress = removeZone(inet6Address.getHostAddress());
		if (!StringTools.isBlank(hostAddress)) {
			ipAddresses.add(hostAddress);
		}
	}

	private static String removeZone(String address) {
		if (StringTools.isBlank(address)) {
			return null;
		}
		int index = address.indexOf('%');
		if (index > 0) {
			return address.substring(0, index).trim();
		}
		return address;
	}

	private void addToAddressList(Set<String> ipAddresses, InetAddress inet4Address) {
		if (inet4Address == null) {
			return;
		}
		String hostAddress = inet4Address.getHostAddress();
		if (!StringTools.isBlank(hostAddress)) {
			ipAddresses.add(hostAddress);
		}
		String hostName = inet4Address.getHostName();
		if (!StringTools.isBlank(hostName)) {
			ipAddresses.add(hostName);
		}
		String canonicalHostName = inet4Address.getCanonicalHostName();
		if (!StringTools.isBlank(canonicalHostName)) {
			ipAddresses.add(canonicalHostName);
		}
	}

	private boolean checkReachability(StringBuilder logCollector) {

		CloseableHttpResponse response = null;
		try {
			HttpGet get = new HttpGet(url);
			get.addHeader("Authorization", "Bearer " + bearer);

			logAndRecord(LogLevel.TRACE, logCollector, "Getting the results of " + url + " now and bearer: " + bearer, null);
			response = client.execute(get);
			logAndRecord(LogLevel.TRACE, logCollector, "Got the results of " + url, null);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {

				String resultBody = readBody(logCollector, response);

				logAndRecord(LogLevel.WARN, logCollector, "Got a non-200 response from " + url + ": " + response + "\nBody:\n" + resultBody, null);

			} else {

				String resultBody = readBody(logCollector, response);

				if (StringTools.isEmpty(resultBody)) {
					logAndRecord(LogLevel.TRACE, logCollector, "The result of " + url + " is empty.", null);
				} else {

					Set<String> readyIpAddresses = retrieveReadyIpAddresses(resultBody);

					for (String addr : ipAddresses) {
						if (readyIpAddresses.contains(addr)) {
							logAndRecord(LogLevel.DEBUG, logCollector, "Found address " + addr + " in response from " + url, null);
							return true;
						}
					}

					logAndRecord(LogLevel.TRACE, logCollector, "Could not find any of the local IP addresses (" + ipAddresses
							+ ") in the list of ready IP addresses (" + readyIpAddresses + ")", null);

				}
			}

		} catch (Exception e) {

			logAndRecord(LogLevel.WARN, logCollector, "Error while trying to check the availability of this pod by calling " + url, null);

		} finally {
			IOTools.closeCloseable(response, logger);
		}

		return false;
	}

	private static Set<String> retrieveReadyIpAddresses(String jsonBody) throws Exception {
		JSONParser parser = new JSONParser();
		JSONObject object = (JSONObject) parser.parse(jsonBody);

		Set<String> readyIpAddresses = new HashSet<>();
		JSONArray subsets = (JSONArray) object.get("subsets");
		for (int i = 0; i < subsets.size(); ++i) {
			JSONObject entry = (JSONObject) subsets.get(i);
			Object addresses = entry.get("addresses");
			if (addresses != null) {
				JSONArray addrArray = (JSONArray) addresses;
				for (int j = 0; j < addrArray.size(); ++j) {
					JSONObject addrEntry = (JSONObject) addrArray.get(j);
					Object ipObject = addrEntry.get("ip");
					if (ipObject instanceof String) {
						readyIpAddresses.add((String) ipObject);
					}
				}
			}

		}
		return readyIpAddresses;
	}

	private String readBody(StringBuilder logCollector, CloseableHttpResponse response) {
		String resultBody = null;

		try (InputStream is = new ResponseEntityInputStream(response); ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			IOTools.pump(is, os);
			resultBody = os.toString("UTF-8");

			logAndRecord(LogLevel.TRACE, logCollector, "Received from URL " + url + "\n" + StringTools.asciiBoxMessage(resultBody, -1), null);

			return resultBody;

		} catch (Exception e) {
			logAndRecord(LogLevel.ERROR, logCollector, "Error while downloading status from " + url, e);
		} finally {
			HttpTools.consumeResponse(response);
		}

		return null;
	}

	private void setShutdownHook() {
		if (shutdownHookSet) {
			return;
		}
		shutdownHookSet = true;

		String active = TribefireRuntime.getProperty("TRIBEFIRE_KUBERNETES_RUNTIME_THREADDUMP_ON_SHUTDOWN");
		// TODO: change later; keeping it on by default for debugging purposes

		if (!StringTools.isBlank(active) && active.equalsIgnoreCase("false")) {
			logger.debug(() -> "Not setting the shutdown hook for logging a threaddump at shutdown.");
			return;
		} else {
			logger.debug(() -> "Setting the shutdown hook for logging a threaddump at shutdown.");
		}

		Runtime.getRuntime().addShutdownHook(new Thread("PluginFactory::shutdownHook") {
			@Override
			public void run() {
				try {
					String threaddump = ThreadDumpTools.getThreadDump();
					logger.debug(() -> "Threadddump at shutdown:\n" + threaddump);
				} catch (Exception e) {
					logger.info(() -> "Error while trying to get a threaddump at shutdown.", e);
				}
			}
		});
	}
}
