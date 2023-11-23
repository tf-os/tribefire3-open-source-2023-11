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
package com.braintribe.web.servlet.logs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.json.simple.JSONObject;

import com.braintribe.cartridge.common.api.topology.LiveInstances;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.common.lcd.Pair;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.logging.LogLevel;
import com.braintribe.model.logs.request.GetLogContent;
import com.braintribe.model.logs.request.GetLogFiles;
import com.braintribe.model.logs.request.GetLogLevel;
import com.braintribe.model.logs.request.GetLogLevelResponse;
import com.braintribe.model.logs.request.GetLogs;
import com.braintribe.model.logs.request.LogContent;
import com.braintribe.model.logs.request.LogFileBundle;
import com.braintribe.model.logs.request.LogFiles;
import com.braintribe.model.logs.request.Logs;
import com.braintribe.model.logs.request.SetLogLevel;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.ComparatorBasedNavigableMultiMap;
import com.braintribe.web.servlet.BasicTemplateBasedServlet;

/**
 * This servlet provides a web page to access log files of the tribefire instance where this servlet is hosted. It
 * allows to: <br>
 * <ul>
 * <li>Download log files</li>
 * <li>Set the log level of individual Cartridges and tribefire-services</li>
 * <li>Tail the contents of individual log files</li>
 * </ul>
 * <br>
 * The top section of the About page allows a user to download log files (so-called "Log Bundles"). The dropdown box
 * presents a list of all available log files, grouped by their filenames. Hence, is multiple log files exist that share
 * the same name (except for the timestamp in the name), they can be downloaded as a bundle (i.e., a ZIP file). <br>
 * <br>
 * The following section allows a user to select one of the log files for live tailing at the bottom of the page. The
 * Log Viewer allows the user to specify how many lines of log should be in memory and how many of the should be
 * displayed on screen. Furthermore, the output could be halted or cleared. <br>
 * <br>
 * The next section can be used to change the log level of an individual Cartridge or the tribefire-services (if
 * present). Please note that the log levels presented in this interface may differ from the actual level names
 * designated by the underlying log infrastructure. tribefire and its components use an abstraction layer for logging
 * and is therefore independent of the underlying log infrastructure. Hence, it is also possible to completely replace
 * the logging infrastructure without the need to change the code. This also allows to make use of any log
 * infrastructure a servlet container may provide. By default, the standard Java Util Logging framework is used, of
 * which the following log levels are employed: <br>
 * <br>
 * <ul>
 * <li>FINER (called TRACE in tribefire)</li>
 * <li>FINE (called DEBUG in tribefire)</li>
 * <li>INFO</li>
 * <li>WARNING (called WARN in tribefire)</li>
 * <li>SEVERE (called ERROR in tribefire)</li>
 * </ul>
 * <br>
 * The Log Servlet does not support operation within a cluster. When a load balancer is used to access one of many
 * tribefire-services instances, the user may have no control which instance actually serves the content of the Logs
 * page. It can only be used by accessing a server directly. This will be supported in future versions.
 */
public class LogsServlet extends BasicTemplateBasedServlet implements InitializationAware {

	private static final long serialVersionUID = -1;
	private static Logger logger = Logger.getLogger(LogsServlet.class);

	private static final String logsTemplateLocation = "com/braintribe/web/servlet/logs/templates/tfLogOutput.html.vm";

	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withLocale(Locale.US);

	protected static final String analysis = "analysis";
	protected static final String setLogLevelPageName = "setLogLevel";
	protected static final String logContentPageName = "logContent";
	protected static final String downloadPackagePageName = "downloadPackage";
	protected static final int newLineLength = "\r\n".length();

	protected Evaluator<ServiceRequest> requestEvaluator;

	protected LiveInstances liveInstances;
	protected InstanceId localInstanceId;

	@Override
	public void postConstruct() {
		setTemplateLocation(logsTemplateLocation);
	}

	/**
	 * Main entry point to the servlet. The following path are acted upon:
	 * <ul>
	 * <li>/setLogLevel: Sets the log level of a specific Cartridge or tribefire-services.</li>
	 * <li>/logContent: Streams the latest part of log files (returns the content as a JSON object)</li>
	 * <li>/downloadPackage: Provides log files of a selected resources as a ZIP file.</li>
	 * <li>/: Presents the main page of the Logs Servlet</li>
	 * </ul>
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo != null) {
			if (pathInfo.equals(String.format("/%s", setLogLevelPageName))) {
				setLogLevel(req, resp);
			} else if (pathInfo.equals(String.format("/%s", logContentPageName))) {
				logContent(req, resp);
			} else if (pathInfo.startsWith(String.format("/%s/", downloadPackagePageName))) {
				try {
					download(req, resp);
				} catch (Exception e) {
					throw new ServletException("Error while trying to download a file.", e);
				}
			} else if (pathInfo.equals(String.format("/%s", analysis))) {
				doAnalysis(resp);
			} else if (pathInfo.equals("/")) {
				String serviceLogsUrl = "../logs";
				resp.sendRedirect(serviceLogsUrl);
			} else {
				logger.debug("Unknown path: " + pathInfo);
				super.service(req, resp);
			}
		} else {
			super.service(req, resp);
		}
	}

	private void doAnalysis(HttpServletResponse resp) {
		resp.setContentType("text/plain");
		try (PrintWriter writer = resp.getWriter()) {

			Set<String> btLoggerPackages = logger.getManagedLoggerPackages();
			btLoggerPackages.forEach(l -> writer.print("Managed package: " + l + "\n"));
			writer.print("\n");

			List<java.util.logging.Logger> btLoggers = getBtLoggers();
			List<Handler> allHandlers = this.getHandlers();

			if (allHandlers != null) {
				TreeSet<String> sorted = new TreeSet<>();
				allHandlers.forEach(h -> sorted.add("Handler: " + h + " (" + h.getLevel() + ")"));
				sorted.forEach(s -> writer.print(s + "\n"));
			}
			writer.print("\n");

			List<Handler> handlers = this.getMostProbableHandlers(allHandlers);

			if (handlers != null) {
				TreeSet<String> sorted = new TreeSet<>();
				handlers.forEach(h -> sorted.add("Probable Handler: " + h + " (" + h.getLevel() + ")"));
				sorted.forEach(s -> writer.print(s + "\n"));
			}
			writer.print("\n");

			Level lowestCommonLevel = this.getLowestCommonLevel(allHandlers, Level.SEVERE);
			writer.print("Lowest level: " + lowestCommonLevel + "\n\n");

			TreeSet<String> sorted = new TreeSet<>();
			for (java.util.logging.Logger btLogger : btLoggers) {
				StringBuilder sb = new StringBuilder();
				sb.append("Logger: " + btLogger.getName() + " (" + btLogger.getLevel() + ")");

				Handler[] handlerArray = btLogger.getHandlers();
				if (handlerArray != null && handlerArray.length > 0) {
					sb.append(" Special handlers: ");
					for (Handler h : handlerArray) {
						sb.append(" Handler: " + h);
					}
				}

				sorted.add(sb.toString());
			}
			sorted.forEach(s -> writer.print(s + "\n"));

			writer.print("\n");

			sorted = new TreeSet<>();
			List<java.util.logging.Logger> otherLoggers = getOtherLoggers();
			for (java.util.logging.Logger otherLogger : otherLoggers) {
				StringBuilder sb = new StringBuilder();
				sb.append("Other Logger: " + otherLogger.getName() + " (" + otherLogger.getLevel() + ")");

				Handler[] handlerArray = otherLogger.getHandlers();
				if (handlerArray != null && handlerArray.length > 0) {
					sb.append(" Special handlers: ");
					for (Handler h : handlerArray) {
						sb.append(" Handler: " + h);
					}
				}

				sorted.add(sb.toString());
			}
			sorted.forEach(s -> writer.print(s + "\n"));

			java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(LogsServlet.class.getName());

			writer.print("\n");

			while (julLogger != null) {
				writer.print("JUL Logger: " + julLogger.getName() + " (" + julLogger + " loaded by " + julLogger.getClass().getClassLoader() + ")\n");
				logger.debug("JUL Logger: " + julLogger.getName() + " (" + julLogger + ")");
				Handler[] julHandlers = julLogger.getHandlers();
				if (julHandlers != null && julHandlers.length > 0) {
					for (Handler h : julHandlers) {
						logger.debug(() -> "JUL Handler: " + h);
						writer.print("Handler: " + h + "\n");
					}
				}
				julLogger = julLogger.getParent();
			}

		} catch (Exception e) {
			logger.debug(() -> "Could not print analysis.", e);
		}
	}

	protected Level getLowestCommonLevel(List<Handler> allHandlers, Level newLevel) {
		if (allHandlers == null || allHandlers.isEmpty()) {
			return newLevel;
		}
		Level lowestLevel = newLevel;
		for (Handler handler : allHandlers) {
			Level handlerLevel = handler.getLevel();
			if (handlerLevel != null) {
				if (lowestLevel == null) {
					lowestLevel = handlerLevel;
				} else {
					if (handlerLevel.intValue() < lowestLevel.intValue()) {
						lowestLevel = handlerLevel;
					}
				}
			}
		}
		return lowestLevel;
	}

	protected List<Handler> getHandlers() {

		List<Handler> handlerList = new ArrayList<>();

		try {
			java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
			while (rootLogger != null) {
				logger.debug("Getting handlers from " + rootLogger);
				Handler[] handlerArray = rootLogger.getHandlers();
				if (handlerArray != null && handlerArray.length > 0) {
					handlerList.addAll(Arrays.asList(handlerArray));
				}
				rootLogger = rootLogger.getParent();
			}
		} catch (

		Throwable t) {
			logger.debug(() -> "Could not get handlers", t);
		}

		return handlerList;
	}

	protected List<java.util.logging.Logger> getBtLoggers() {
		LogManager logManager = LogManager.getLogManager();

		Set<String> btLoggerPackages = logger.getManagedLoggerPackages();
		List<java.util.logging.Logger> result = new ArrayList<>();
		try {

			Enumeration<String> names = logManager.getLoggerNames();
			if (names != null) {
				while (names.hasMoreElements()) {
					String name = names.nextElement();

					if (btLoggerPackages.stream().anyMatch(p -> name.startsWith(p))) {
						java.util.logging.Logger l = logManager.getLogger(name);
						result.add(l);
					}
				}
			}

		} catch (Throwable t) {
			logger.debug(() -> "Error while trying to identify managed loggers.", t);
		}
		if (result.size() <= 1) {
			logger.debug(() -> "Could not find any managed Loggers");
		}
		return result;
	}
	protected List<java.util.logging.Logger> getOtherLoggers() {
		LogManager logManager = LogManager.getLogManager();

		Set<String> btLoggerPackages = logger.getManagedLoggerPackages();
		List<java.util.logging.Logger> result = new ArrayList<>();
		try {

			Enumeration<String> names = logManager.getLoggerNames();
			if (names != null) {
				while (names.hasMoreElements()) {
					String name = names.nextElement();

					if (!btLoggerPackages.stream().anyMatch(p -> name.startsWith(p))) {
						java.util.logging.Logger l = logManager.getLogger(name);
						result.add(l);
					}
				}
			}

		} catch (Throwable t) {
			logger.debug(() -> "Error while trying to identify managed loggers.", t);
		}
		if (result.size() <= 1) {
			logger.debug(() -> "Could not find any managed Loggers");
		}
		return result;
	}
	protected List<Handler> getMostProbableHandlers(List<Handler> handlers) {
		if (handlers == null || handlers.isEmpty()) {
			return null;
		}
		List<Handler> winners = new ArrayList<>();
		List<Handler> others = new ArrayList<>();
		for (Handler candidate : handlers) {
			// We're not primarily interested in ConsoleHandlers
			if (!(candidate instanceof ConsoleHandler)) {

				String candidateFilename = this.getHandlerFileName(candidate);

				// Prefer the handler that has not warn in the name
				if (candidateFilename.toLowerCase().indexOf("warn") != -1) {
					others.add(0, candidate);
				} else {
					winners.add(candidate);
				}

			} else {
				others.add(candidate);
			}
		}
		if (winners.isEmpty() && !others.isEmpty()) {
			winners.add(others.get(0));
		}
		return winners;
	}
	protected String getHandlerFileName(Handler handler) {
		if (handler == null) {
			return "none";
		}
		try {
			// Note: we had to drop support for java.util.logging.FileHandler as reflection access to private members will be
			// prohibited in Java 11
			// and above
			// Same goes for org.apache.juli.AsyncFileHandler and org.apache.juli.FileHandler
			if (handler instanceof ConsoleHandler) {
				return "console";
			} else if (handler.getClass().getName().equals("com.braintribe.logging.juli.handlers.FileHandler")) {
				Method method = handler.getClass().getMethod("getFileKey", (Class[]) null);
				String fileKeyValue = (String) method.invoke(handler, (Object[]) null);
				return fileKeyValue;
			}
		} catch (Throwable t) {
			logger.trace(() -> "Could not get the filename of " + handler, t);
		}
		return "unknown";
	}

	@Override
	protected VelocityContext createContext(HttpServletRequest req, HttpServletResponse response) {

		Map<String, String[]> reqestParamMap = req.getParameterMap();

		String encodedFrom = req.getParameter("from"), fromDate = "";
		String encodedTo = req.getParameter("to"), toDate = "";
		Date from = null, to = null;

		String encodedShowLogLines = req.getParameter("showLogLines");
		String encodedLogLines = req.getParameter("logLines");
		int showLogLines = 15, logLines = 100;

		boolean advancedMode = LogsServletContext.requestHasParameter(reqestParamMap, "advancedMode");
		String logFile = (req.getParameter("logFile") != null ? req.getParameter("logFile") : "");
		boolean pauseLog = LogsServletContext.requestHasParameter(reqestParamMap, "pauseLog");
		boolean followTail = LogsServletContext.requestHasParameter(reqestParamMap, "followTail");

		String advancedModeChecked = (advancedMode ? "checked" : "");
		String pauseLogChecked = (pauseLog ? "checked" : "");
		String followTailChecked = (followTail ? "checked" : "");

		if (encodedFrom != null && encodedFrom.isEmpty() == false) {
			try {
				from = DateTools.decodeDateTime(addTimeToEncodedDate(encodedFrom, false), DATETIME_FORMATTER);
				fromDate = encodedFrom;
			} catch (Exception e) {
				throw new RuntimeException("parameter 'from' is invalid");
			}
		}
		if (encodedTo != null && encodedTo.isEmpty() == false) {
			try {
				from = DateTools.decodeDateTime(addTimeToEncodedDate(encodedTo, true), DATETIME_FORMATTER);
				toDate = encodedTo;
			} catch (Exception e) {
				throw new RuntimeException("parameter 'to' is invalid");
			}
		}

		if (encodedShowLogLines != null && encodedShowLogLines.isEmpty() == false) {
			try {
				showLogLines = Integer.parseInt(encodedShowLogLines);
			} catch (NumberFormatException e) {
				throw new RuntimeException("parameter 'showLogLines' is invalid");
			}
		}
		if (encodedLogLines != null && encodedLogLines.isEmpty() == false) {
			try {
				logLines = Integer.parseInt(encodedLogLines);
			} catch (NumberFormatException e) {
				throw new RuntimeException("parameter 'logLines' is invalid");
			}
		}

		// Get log files and variable for the table's colspace
		MultiMap<String, File> logFiles = getLogFiles(from, to);
		int logLevelCount = LogLevel.values().length;
		int logFilesCount = logFiles.keySet().size();

		VelocityContext velocityContext = new VelocityContext();

		// download
		velocityContext.put("toDate", toDate);
		velocityContext.put("fromDate", fromDate);
		velocityContext.put("logFiles", logFiles);
		velocityContext.put("logFilesCount", logFilesCount);

		// setLogLevel
		velocityContext.put("LogLevel", LogLevel.class);
		velocityContext.put("advancedMode", advancedMode);
		velocityContext.put("logLevelCount", logLevelCount);
		velocityContext.put("reqestParamMap", reqestParamMap);
		velocityContext.put("setLogLevelPageName", setLogLevelPageName);

		// logViewer
		velocityContext.put("advancedModeChecked", advancedModeChecked);
		velocityContext.put("pauseLogChecked", pauseLogChecked);
		velocityContext.put("followTailChecked", followTailChecked);

		velocityContext.put("logFile", logFile);
		velocityContext.put("showLogLines", showLogLines);
		velocityContext.put("logLines", logLines);
		velocityContext.put("pauseLog", pauseLog);
		velocityContext.put("followTail", followTail);

		velocityContext.put("liveApplications", liveInstances.liveApplications());
		velocityContext.put("liveLogLevels", getLiveLogLevels());

		velocityContext.put("clusterLogFiles", getClusterLogFiles());

		return velocityContext;
	}

	private MultiMap<String, File> getLogFiles(Date from, Date to) {
		GetLogFiles getLogFiles = GetLogFiles.T.create();
		getLogFiles.setFrom(from);
		getLogFiles.setTo(to);
		LogFiles logFiles = getLogFiles.eval(requestEvaluator).get();

		MultiMap<String, File> files = new ComparatorBasedNavigableMultiMap<String, File>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		}, new FileComparator());

		for (LogFileBundle bundle : logFiles.getLogBundles()) {
			String key = bundle.getBundleName();
			bundle.getFileNames().forEach(n -> files.put(key, new File(n)));
		}

		return files;
	}

	private Map<String, LogLevel> getLiveLogLevels() {

		Map<String, LogLevel> resultMap = new HashMap<>();

		try {
			GetLogLevel gl = GetLogLevel.T.create();
			MulticastRequest mr = MulticastRequest.T.create();
			mr.setServiceRequest(gl);

			EvalContext<? extends MulticastResponse> eval = mr.eval(requestEvaluator);
			MulticastResponse multicastResponse = eval.get();

			Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
			logger.trace(() -> "Received multicast responses: " + responses);

			for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {
				ServiceResult value = entry.getValue();
				InstanceId sender = entry.getKey();
				ResponseEnvelope responseEnvelope = value.asResponse();
				if (responseEnvelope != null) {
					GetLogLevelResponse result = (GetLogLevelResponse) responseEnvelope.getResult();

					logger.trace(() -> "Received a response from " + sender + ": " + result);

					if (result != null) {
						String appId = entry.getKey().getApplicationId();
						LogLevel logLevel = result.getLogLevel();
						resultMap.put(appId, logLevel);
					}
				} else {
					Failure failure = value.asFailure();
					if (failure != null) {
						Throwable throwable = FailureCodec.INSTANCE.decode(failure);
						logger.trace(() -> "Received failure from " + sender, throwable);
					} else {
						logger.trace(() -> "Received neither a response nor a failure from " + sender);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while trying to collect log levels from all applications", e);
		}
		return resultMap;
	}

	private void download(HttpServletRequest req, HttpServletResponse resp) throws Exception {

		String fileName = req.getParameter("file");
		String encodedFrom = req.getParameter("from");
		String encodedTo = req.getParameter("to");

		Date from = null;
		Date to = null;

		if (encodedFrom != null && encodedFrom.isEmpty() == false) {
			try {
				from = DateTools.decodeDateTime(addTimeToEncodedDate(encodedFrom, false), DATETIME_FORMATTER);
			} catch (Exception e) {
				throw new Exception("Parameter 'from' is invalid");
			}
		}
		if (encodedTo != null && encodedTo.isEmpty() == false) {
			try {
				to = DateTools.decodeDateTime(addTimeToEncodedDate(encodedTo, false), DATETIME_FORMATTER);
			} catch (Exception e) {
				throw new Exception("Parameter 'to' is invalid");
			}
		}
		int top = Integer.MAX_VALUE;

		String encodedTop = req.getParameter("top");
		if (encodedTop != null && encodedTop.isEmpty() == false) {
			try {
				top = Integer.parseInt(encodedTop);
			} catch (NumberFormatException e) {
				throw new Exception("Parameter 'top' is invalid");
			}
		}

		GetLogs getLogs = GetLogs.T.create();
		getLogs.setFilename(fileName);
		getLogs.setFromDate(from);
		getLogs.setToDate(to);
		getLogs.setTop(top);

		Logs logs = null;
		try {
			logs = getLogs.eval(requestEvaluator).get();
		} catch (Exception e) {
			throw new Exception("Error while trying to get logs files: " + getLogs, e);
		}

		Resource log = logs.getLog();

		resp.setContentType(log.getMimeType());
		Long fileSize = log.getFileSize();
		if (fileSize != null) {
			resp.setContentLength(fileSize.intValue());
		}
		resp.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", log.getName()));

		try (InputStream in = log.openStream()) {
			IOTools.pump(in, resp.getOutputStream(), 0xffff);
		}

	}

	private Map<String, Map<String, InstanceId>> getClusterLogFiles() {

		GetLogFiles glf = GetLogFiles.T.create();
		MulticastRequest mr = MulticastRequest.T.create();
		mr.setServiceRequest(glf);
		MulticastResponse multicastResponse = null;
		try {
			multicastResponse = mr.eval(requestEvaluator).get();
		} catch (Exception e) {
			logger.info(() -> "Error while trying to get the log files of all nodes", e);
			return null;
		}

		Map<String, Map<String, InstanceId>> result = new HashMap<>();

		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
		for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {
			InstanceId sender = entry.getKey();
			ServiceResult singleResult = entry.getValue();
			ResponseEnvelope asResponse = singleResult.asResponse();
			if (asResponse != null) {

				LogFiles logFilesOfNode = (LogFiles) asResponse.getResult();
				List<String> logFiles = new ArrayList<>();
				logFilesOfNode.getLogBundles().forEach(bundle -> {
					logFiles.addAll(bundle.getFileNames());
				});
				Map<String, InstanceId> currentMap = result.computeIfAbsent(sender.getNodeId(), s -> new TreeMap<>());
				logFiles.stream().forEach(f -> currentMap.put(f, sender));
			} else {
				Failure asFailure = singleResult.asFailure();
				if (asFailure != null) {
					Throwable throwable = FailureCodec.INSTANCE.decode(asFailure);
					logger.debug(() -> "Received failure from " + sender, throwable);
				} else {
					logger.debug(() -> "Received neither result nor a failure from " + sender);
				}
			}
		}

		result = shortenAndSortNodeIds(result);

		return result;

	}

	private Map<String, Map<String, InstanceId>> shortenAndSortNodeIds(Map<String, Map<String, InstanceId>> result) {
		if (result == null) {
			return null;
		}
		String[] nodeIds = result.keySet().toArray(new String[0]);
		String commonPrefix = StringUtils.getCommonPrefix(nodeIds);
		if (commonPrefix != null) {
			int len = commonPrefix.length();
			if (len > 0) {
				Map<String, Map<String, InstanceId>> shortenedMap = new TreeMap<>();
				result.entrySet().forEach(e -> {
					String key = e.getKey();
					String shortenedKey = key.substring(len).trim();
					shortenedMap.put(shortenedKey, e.getValue());
				});
				return shortenedMap;
			}
		}
		return result;
	}

	private void setLogLevel(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		String paramLogLevel = req.getParameter("logLevel");

		if (paramLogLevel == null || paramLogLevel.trim().length() == 0) {
			throw new ServletException("The parameter 'logLevel' is missing or empty.");
		}

		SetLogLevel setLogLevel = SetLogLevel.T.create();
		setLogLevel.setLogLevel(paramLogLevel);

		MulticastRequest mr = MulticastRequest.T.create();
		mr.setServiceRequest(setLogLevel);

		try {
			mr.eval(requestEvaluator).get();
		} catch (Exception e) {
			throw new ServletException("Error while trying to set the log level " + paramLogLevel, e);
		}

		// Get url parameters and remove own parameters from it
		Map<String, String> params = LogsServletContext.getParameters(req.getParameterMap());
		params.remove("cartridgeName");
		params.remove("logLevel");

		// Append parameters to parent url and redirect to that url
		String serviceLogsUrl = LogsServletContext.appendParameter("../logs", params);
		resp.sendRedirect(serviceLogsUrl);
	}

	private void logContent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json");

		// Log read parameters
		String encodedLogFile = req.getParameter("logFile");
		String encodedLogMark = req.getParameter("logMark");
		String encodedLogLines = req.getParameter("logLines");

		long logMark = -1;
		int logLines = -1;

		Pair<String, InstanceId> fileSpecification = parseFilenameSpecification(encodedLogFile);
		if (fileSpecification == null) {
			resp.sendError(400, "parameter 'logFile' is invalid");
			return;
		}

		// Load parameters
		if (encodedLogMark != null && encodedLogMark.isEmpty() == false) {
			try {
				logMark = Long.parseLong(encodedLogMark);
			} catch (NumberFormatException e) {
				resp.sendError(400, "parameter 'logMark' is invalid");
				return;
			}
		}
		if (encodedLogLines != null && encodedLogLines.isEmpty() == false) {
			try {
				logLines = Integer.parseInt(encodedLogLines);
				if (logLines < 0) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				resp.sendError(400, "parameter 'logLines' is invalid");
				return;
			}
		}

		try {

			GetLogContent glc = GetLogContent.T.create();
			glc.setLines(logLines);
			glc.setMark(logMark);
			glc.setLogFile(fileSpecification.first());

			MulticastRequest mr = MulticastRequest.T.create();
			mr.setAddressee(fileSpecification.second());
			mr.setTimeout(Numbers.MILLISECONDS_PER_SECOND * 10L);
			mr.setServiceRequest(glc);

			EvalContext<? extends MulticastResponse> eval = mr.eval(requestEvaluator);
			MulticastResponse multicastResponse = eval.get();

			Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
			logger.trace(() -> "Received multicast responses: " + responses);

			Map<String, Object> resultMap = new HashMap<String, Object>();

			for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {
				ServiceResult value = entry.getValue();
				InstanceId sender = entry.getKey();
				ResponseEnvelope responseEnvelope = value.asResponse();
				if (responseEnvelope != null) {
					LogContent result = (LogContent) responseEnvelope.getResult();

					logger.trace(() -> "logContent: Received a response from " + sender + ": " + result);

					if (result != null) {
						// Fill result map
						Date creationTimeObject = result.getCreationDate();
						if (creationTimeObject != null) {
							String creationTime = DateTools.encode(creationTimeObject, DateTools.ISO8601_DATE_WITH_MS_FORMAT);
							resultMap.put("creationDate", creationTime);
						}
						resultMap.put("mark", result.getMark());
						resultMap.put("content", result.getContent());

						break;
					}
				} else {
					Failure failure = value.asFailure();
					if (failure != null) {
						Throwable throwable = FailureCodec.INSTANCE.decode(failure);
						logger.trace(() -> "logContent: Received failure from " + sender, throwable);
					} else {
						logger.trace(() -> "logContent: Received neither a response nor a failure from " + sender);
					}
				}
			}

			// Serialize result map with JSON
			new JSONObject(resultMap).writeJSONString(resp.getWriter());

		} catch (Exception e) {
			logger.debug(() -> "Error while trying to get log content for " + fileSpecification, e);
			resp.sendError(500, "could not get the content of " + fileSpecification);
			return;
		}
	}

	private Pair<String, InstanceId> parseFilenameSpecification(String encodedLogFile) {

		int idx1 = encodedLogFile.indexOf("appId:");
		int idx2 = encodedLogFile.indexOf("nodeId:");
		int idx3 = encodedLogFile.indexOf("logFilename:");
		if (idx1 == -1 || idx2 <= idx1 || idx3 <= idx2) {
			logger.debug(() -> "The encoded log filename " + encodedLogFile + " is not as expected.");
			return null;
		}

		String appId = encodedLogFile.substring(idx1 + 6, idx2 - 1);
		String nodeId = encodedLogFile.substring(idx2 + 7, idx3 - 1);
		String filename = encodedLogFile.substring(idx3 + 12);
		InstanceId instanceId = InstanceId.T.create();
		instanceId.setApplicationId(appId);
		instanceId.setNodeId(nodeId);

		return new Pair<>(filename, instanceId);
	}

	private String addTimeToEncodedDate(String encodedDate, boolean endOfDay) {
		int timeDefined = encodedDate.indexOf('T');
		if (timeDefined == -1) {
			encodedDate += (endOfDay ? "T23:59" : "T00:00");
		}

		return encodedDate;
	}

	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

	@Configurable
	@Required
	public void setLiveInstances(LiveInstances liveInstances) {
		this.liveInstances = liveInstances;
	}
	@Configurable
	@Required
	public void setLocalInstanceId(InstanceId localInstanceId) {
		this.localInstanceId = localInstanceId;
	}

}
