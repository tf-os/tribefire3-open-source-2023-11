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
package com.braintribe.model.processing.logs.processor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.StringEscapeUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.logging.Logger;
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
import com.braintribe.model.logs.request.LogsRequest;
import com.braintribe.model.logs.request.LogsResponse;
import com.braintribe.model.logs.request.SetLogLevel;
import com.braintribe.model.logs.request.SetLogLevelResponse;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.bootstrapping.jmx.TribefireRuntimeMBean;
import com.braintribe.model.processing.bootstrapping.jmx.TribefireRuntimeMBeanTools;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.weaving.impl.dispatch.DispatchingServiceProcessor;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.utils.DateTools;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.collection.api.MultiMap;
import com.braintribe.utils.collection.impl.ComparatorBasedNavigableMultiMap;

public class LogsProcessor extends DispatchingServiceProcessor<LogsRequest, LogsResponse> {

	private static Logger logger = Logger.getLogger(LogsProcessor.class);

	protected static final String tribefireRuntimeMBeanPrefix = "com.braintribe.tribefire:type=TribefireRuntime,name=";

	public static final String LOG_LOCATION = "com.braintribe.logging.juli.handlers.FileHandler.directory";

	// protected File logFolder;
	protected Supplier<String> userNameProvider;
	protected InstanceId localInstanceId;

	protected static final int newLineLength = "\r\n".length();

	protected ReentrantLock logFileCacheLock = new ReentrantLock();
	protected Map<String, File> knownLogFiles = new HashMap<>();
	protected MultiMap<String, File> knownLogFilesPerKey = null;
	protected long logFileCacheLastRefresh = -1L;

	private void loadLogFiles() {
		long now = System.currentTimeMillis();
		if ((now - logFileCacheLastRefresh) < Numbers.MILLISECONDS_PER_SECOND * 10) {
			return;
		}
		logFileCacheLastRefresh = now;
		logFileCacheLock.lock();
		try {
			String catalinaBase = TribefireRuntime.getContainerRoot().replaceAll("\\\\", "/");
			String logConfDir = catalinaBase + "/conf";

			MultiMap<String, File> logFilesPerKey = new ComparatorBasedNavigableMultiMap<String, File>(new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareTo(o2);
				}
			}, new FileComparator());
			Map<String, File> logFiles = new HashMap<>();

			Set<String> logDirs = new HashSet<String>();
			try {
				File logConfFolder = new File(logConfDir);
				for (File logConf : logConfFolder.listFiles(f -> f.getName().contains("logging.properties"))) {
					Properties logProps = new Properties();
					try (InputStream in = new BufferedInputStream(new FileInputStream(logConf))) {
						logProps.load(in);
					}
					String logLocationPath = logProps.getProperty(LOG_LOCATION);
					logLocationPath = logLocationPath.replaceAll("\\$\\{catalina.base\\}", catalinaBase);
					logDirs.add(logLocationPath);
				}

				Pattern keyPattern = Pattern.compile("^[A-Za-z]+([-_][A-Za-z]+)*");
				logDirs.stream().forEach(dir -> {
					Arrays.asList(new File(dir).listFiles(f -> f.isFile())).stream().forEach(file -> {
						String key = file.getName();
						if (key.equals(".DS_Store")) {
							// We're on a Mac
							return;
						}
						Matcher keyMatcher = keyPattern.matcher(key);
						if (keyMatcher.find()) {
							key = keyMatcher.group();
						}
						logFilesPerKey.put(key, file);
						logFiles.put(file.getName(), file);
					});
				});

			} catch (Exception e) {
				logger.error("Error while reading logging configuration files. " + e.getMessage(), e);
			}

			knownLogFilesPerKey = logFilesPerKey;
			knownLogFiles = logFiles;

		} finally {
			logFileCacheLock.unlock();
		}
	}

	@SuppressWarnings("unused")
	public GetLogLevelResponse getLogLevel(ServiceRequestContext context, GetLogLevel request) throws Exception {

		String applicationId = localInstanceId.getApplicationId();

		TribefireRuntimeMBean runtime = TribefireRuntimeMBeanTools.getTribefireCartridgeRuntime(applicationId);
		String logLevel = runtime.getProperty(TribefireRuntime.ENVIRONMENT_LOG_LEVEL);

		GetLogLevelResponse response = GetLogLevelResponse.T.create();
		if (!StringTools.isBlank(logLevel)) {
			response.setLogLevel(LogLevel.valueOf(logLevel));
		}
		response.setNodeId(localInstanceId.getNodeId());
		return response;
	}

	@SuppressWarnings("unused")
	public SetLogLevelResponse setLogLevel(ServiceRequestContext context, SetLogLevel request) throws Exception {

		String paramLogLevel = request.getLogLevel();

		String cartridgeName = localInstanceId.getApplicationId();
		final LogLevel logLevel;

		try {
			logLevel = LogLevel.valueOf(paramLogLevel);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Could not interpret the log level value: " + paramLogLevel, e);
		}

		// Change log level for cartridge
		TribefireRuntimeMBean tribefireCartrigdeRuntime = TribefireRuntimeMBeanTools.getTribefireCartridgeRuntime(cartridgeName);
		if (tribefireCartrigdeRuntime != null) {
			logger.debug(() -> "Setting loglevel " + logLevel + " on " + cartridgeName);

			tribefireCartrigdeRuntime.setProperty(TribefireRuntime.ENVIRONMENT_LOG_LEVEL, logLevel.name());
		} else {
			logger.info(() -> "Could not find TribefireRuntimeMBean for " + cartridgeName);
		}

		SetLogLevelResponse response = SetLogLevelResponse.T.create();
		return response;

	}

	protected Set<String> getCartridgeNames() {
		Set<String> cartridgeNames = new HashSet<String>();

		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName mBeanQueryName = new ObjectName(String.format("%s*", tribefireRuntimeMBeanPrefix));

			for (ObjectName mBeanName : mbs.queryNames(mBeanQueryName, null)) {
				if (mbs.isInstanceOf(mBeanName, TribefireRuntimeMBean.class.getName())) {
					String name = mBeanName.getKeyProperty("name");
					if (!StringTools.isBlank(name)) {
						cartridgeNames.add(name);
					}
				}
			}
		} catch (MalformedObjectNameException | InstanceNotFoundException e) {
			logger.error(String.format("Invalid TribefireRuntime MBean-Prefix: %s", tribefireRuntimeMBeanPrefix), e);
		}

		if (cartridgeNames.contains("master")) {
			cartridgeNames.remove("tribefire-services");
		}

		return cartridgeNames;
	}

	@SuppressWarnings("unused")
	public Logs getLog(ServiceRequestContext context, GetLogs request) throws Exception {

		loadLogFiles();
		Map<String, File> knownLogFilesRef = knownLogFiles;

		Logs logs = Logs.T.create();

		String fileName = request.getFilename();
		if (fileName == null || fileName.trim().length() == 0) {
			fileName = "*";
		}

		StringTokenizer tokenizer = new StringTokenizer(fileName, ".*", true);
		StringBuilder builder = new StringBuilder();
		boolean foundJoker = false;

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("*")) {
				foundJoker = true;
				builder.append(".*");
			} else if (token.equals(".") == false) {
				String quoted = Pattern.quote(token);
				builder.append(quoted);
			}
		}

		if (foundJoker == false) {
			if (fileName.contains("/") || fileName.contains("\\")) {
				throw new Exception("no paths allowed");
			} else {
				File file = knownLogFilesRef.get(fileName);
				if (file != null && file.exists()) {
					this.setFile(logs, file, "text/plain");
				} else {
					throw new FileNotFoundException("Could not find file " + fileName);
				}
			}
		} else {
			Date from = request.getFromDate();
			Date to = request.getToDate();

			Collection<File> logFiles = filterFiles(from, to, builder.toString());
			int logFilesCount = logFiles.size();
			int top = request.getTop();
			logFilesCount = Math.min(logFilesCount, top);
			if (logFilesCount <= 0) {
				logFilesCount = Integer.MAX_VALUE;
			}

			String dateStr = DateTools.encode(new Date(), DateTools.TERSE_DATETIME_FORMAT_2);

			String downloadFilenamePrefix = fileName;
			if (downloadFilenamePrefix.endsWith(".*")) {
				downloadFilenamePrefix = downloadFilenamePrefix.substring(0, downloadFilenamePrefix.length() - 2);
			}
			if (downloadFilenamePrefix.equals("*")) {
				downloadFilenamePrefix = "all";
			}
			downloadFilenamePrefix = FileTools.replaceIllegalCharactersInFileName(downloadFilenamePrefix, "");

			String name = String.format("%s-logs-%s.zip", downloadFilenamePrefix, dateStr);

			this.setZippedFile(logs, name, logFiles, logFilesCount);
		}

		return logs;
	}

	private Collection<File> filterFiles(Date from, Date to, String filenamePattern) {
		Pattern pattern = !StringTools.isBlank(filenamePattern) ? Pattern.compile(filenamePattern) : null;
		List<File> result = new ArrayList<>();
		MultiMap<String, File> knownLogFilesPerKeyRef = knownLogFilesPerKey;

		for (String key : knownLogFilesPerKeyRef.keySet()) {
			boolean acceptKey = true;
			if (pattern != null) {
				Matcher matcher = pattern.matcher(key);
				acceptKey = matcher.matches();
			}
			if (acceptKey) {
				Collection<File> filesPerKey = knownLogFilesPerKeyRef.getAll(key);
				for (File file : filesPerKey) {

					if (!file.exists()) {
						continue;
					}

					boolean acceptDate = true;
					if (from != null || to != null) {
						Date fileDate = new Date(file.lastModified());
						if (from != null && from.compareTo(fileDate) > 0) {
							acceptDate = false;
						}
						if (to != null && to.compareTo(fileDate) < 0) {
							acceptDate = false;
						}
					}

					if (acceptDate) {
						result.add(file);
					}
				}

			}
		}
		return result;
	}

	private void setFile(Logs logs, File file, String mimeType) {

		Resource callResource = Resource.createTransient(() -> new FileInputStream(file));

		callResource.setName(file.getName());
		callResource.setMimeType(mimeType);
		callResource.setFileSize(file.length());

		try {
			BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			if (attrs != null) {
				FileTime creationTime = attrs.creationTime();
				if (creationTime != null) {
					GregorianCalendar atime = new GregorianCalendar();
					atime.setTimeInMillis(creationTime.toMillis());
					callResource.setCreated(atime.getTime());
				}
			}
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not get the creation time of file " + file.getAbsolutePath(), e);
			}
		}

		try {
			PosixFileAttributes attrs = Files.readAttributes(file.toPath(), PosixFileAttributes.class);
			if (attrs != null) {
				UserPrincipal owner = attrs.owner();
				if (owner != null) {
					callResource.setCreator(owner.getName());
				}
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not get the owner of file " + file.getAbsolutePath(), e);
			}
		}

		logs.setLog(callResource);
	}

	private void setZippedFile(Logs logs, String name, Collection<File> logFiles, int logFilesCount) {

		Resource callResource = Resource.createTransient(new ZippingInputStreamProvider(name, logFiles, logFilesCount));

		callResource.setName(name);
		callResource.setMimeType("application/zip");
		callResource.setCreated(new Date());
		try {
			callResource.setCreator(this.userNameProvider.get());
		} catch (RuntimeException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not get the current user name.", e);
			}
		}

		logs.setLog(callResource);
	}

	@SuppressWarnings("unused")
	public LogFiles getLogFiles(ServiceRequestContext context, GetLogFiles request) throws Exception {

		loadLogFiles();
		MultiMap<String, File> knownLogFilesPerKeyRef = knownLogFilesPerKey;

		Date from = request.getFrom();
		Date to = request.getTo();

		LogFiles logFiles = LogFiles.T.create();

		for (String key : knownLogFilesPerKeyRef.keySet()) {
			LogFileBundle bundle = LogFileBundle.T.create();
			bundle.setBundleName(key);

			for (File f : knownLogFilesPerKeyRef.getAll(key)) {

				boolean acceptDate = true;
				if (from != null || to != null) {
					Date fileDate = new Date(f.lastModified());
					if (from != null && from.compareTo(fileDate) > 0) {
						acceptDate = false;
					}
					if (to != null && to.compareTo(fileDate) < 0) {
						acceptDate = false;
					}
				}
				if (acceptDate) {
					bundle.getFileNames().add(f.getName());
				}
			}
			logFiles.getLogBundles().add(bundle);
		}
		return logFiles;
	}

	@SuppressWarnings("unused")
	public LogContent getLogContent(ServiceRequestContext context, GetLogContent request) throws Exception {

		loadLogFiles();
		Map<String, File> knownLogFilesRef = knownLogFiles;

		String logFileName = request.getLogFile();
		long logMark = request.getMark();
		int logLines = request.getLines();

		LogContent logContentResult = LogContent.T.create();

		List<String> logContent = logContentResult.getContent();

		File logFile = knownLogFilesRef.get(logFileName);
		if (logFile != null && logFile.exists()) {

			BasicFileAttributes attr = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);

			logContentResult.setCreationDate(new Date(attr.creationTime().toMillis()));

			// Read result variables
			try {

				if (logMark < 0) {
					// Mark end of file
					logMark = logFile.length();

					// Read log file from bottom to top
					try (ReversedLinesFileReader logFileReader = new ReversedLinesFileReader(logFile, 1024, Charset.forName("UTF-8"))) {
						String logLine = null;
						int readLogLines = 0;

						// Read logLine until start is reached or logLines are reached
						while ((logLine = logFileReader.readLine()) != null && readLogLines < logLines) {
							// Add logLine to output
							logContent.add(0, StringEscapeUtils.escapeHtml(logLine));
							readLogLines++;
						}
					}
				} else {
					// Read log file from top starting at mark to bottom
					try (BufferedReader logFileReader = new BufferedReader(
							new InputStreamReader(new FileInputStream(logFile), Charset.forName("UTF-8")))) {
						logFileReader.skip(logMark);
						String logLine = null;
						int readLogLines = 0;

						// Read logLine until end is reached or logLines are reached
						while ((logLine = logFileReader.readLine()) != null && readLogLines < logLines) {
							// Add logLine to output
							logContent.add(StringEscapeUtils.escapeHtml(logLine));
							readLogLines++;

							// Add logLine to logMark
							logMark += logLine.length() + newLineLength;
						}
					}
				}

				logContentResult.setMark(logMark);

			} catch (Exception e) {
				logger.debug(() -> "Error while trying to read content of log file: " + logFileName, e);
			}
		}

		return logContentResult;
	}

	@Configurable
	@Required
	public void setUserNameProvider(Supplier<String> userNameProvider) {
		this.userNameProvider = userNameProvider;
	}
	@Configurable
	@Required
	public void setLocalInstanceId(InstanceId localInstanceId) {
		this.localInstanceId = localInstanceId;
	}

}
