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
package com.braintribe.logging.juli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

import org.apache.juli.ClassLoaderLogManager;
import org.apache.juli.PackageVisibilityAccessHelper;

/**
 * An extension of {@link ClassLoaderLogManager} which {@link #readConfiguration(ClassLoader) extends} the way how web application specific logging
 * configuration files (<code>logging.properties</code>) are searched. Instead of only searching the web applications classpath
 * (<code>WEB-INF/...</code>) this log manager first tries to determine the web application name (from classloader) and then checks for the logging
 * configurations at the following locations:<br/>
 * <ul>
 * <li>[host_dir]/conf/[WebappName]_logging.properties</li>
 * <li>[host_dir]/conf/logging/[WebappName]_logging.properties</li>
 * </ul>
 * If no configuration is found, the log manager behaves like the standard {@link ClassLoaderLogManager}.
 *
 * @author michael.lafite
 */
public class BtClassLoaderLogManager extends ClassLoaderLogManager {

	public static final String WEBAPPCLASSLOADER_CLASSNAME = "org.apache.catalina.loader.WebappClassLoader";
	public static final String PARALLELWEBAPPCLASSLOADER_CLASSNAME = "org.apache.catalina.loader.ParallelWebappClassLoader";
	public static final String LOGGING_CONF_TEMPLATE = "[WebappName]_logging.properties";

	// static {
	// System.setProperty(DEBUG_PROPERTY, "true");
	// }

	/**
	 * See {@link BtClassLoaderLogManager}.
	 */
	@Override
	protected void readConfiguration(final ClassLoader classLoader) throws IOException {

		JulExtensionsHelpers.setDebugEnabled(Boolean.getBoolean(ClassLoaderLogManager.DEBUG_PROPERTY));

		String webappName = "unknown";
		String classloaderName = classLoader.getClass().getName();
		if (classloaderName.equals(WEBAPPCLASSLOADER_CLASSNAME) || classloaderName.equals(PARALLELWEBAPPCLASSLOADER_CLASSNAME)) {
			final WebappInfo webappInfo = getWebappInfo((URLClassLoader) classLoader);

			if (webappInfo != null) {

				webappName = webappInfo.webappName;

				final String expectedLoggingPropertiesFileUrl1 = webappInfo.hostFolderUrl + "/conf/" + webappInfo.webappName + "_logging.properties";
				final String expectedLoggingPropertiesFileUrl2 = webappInfo.hostFolderUrl + "/conf/logging/" + webappInfo.webappName
						+ "_logging.properties";

				File loggingPropertiesFile = JulExtensionsHelpers.returnFirstExistingFile(expectedLoggingPropertiesFileUrl1,
						expectedLoggingPropertiesFileUrl2);

				if (loggingPropertiesFile == null) {
					String appName = webappInfo.webappName.toLowerCase();
					// TODO: find better way to decide for which apps to create logging properties
					boolean createLoggingPropertiesFileFromTemplate = appName.contains("cartridge") || appName.contains("tribefire-services");
					if (createLoggingPropertiesFileFromTemplate) {
						try {
							loggingPropertiesFile = JulExtensionsHelpers.createLoggingConfigFromTemplate(webappInfo.webappName,
									webappInfo.hostFolderUrl + "/conf/", LOGGING_CONF_TEMPLATE);
						} catch (Exception e) {
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							e.printStackTrace(pw);
							debugIfEnabled("readConfiguration", pw.toString());
						}
					}
				}

				if (loggingPropertiesFile != null) {

					debugIfEnabled("readConfiguration", "Found webapp-specific logging configuration for " + webappInfo.webappName
							+ ". Reading configuration from " + loggingPropertiesFile.getPath() + " ...");

					final InputStream is = new FileInputStream(loggingPropertiesFile);

					// ************************************************************************
					/**
					 * The following code lines are supposed to be similar to the ones at the end of the overridden method. Differences are:<br/>
					 * The "is ==/!= null" checks are skipped, since we know the input stream is set in our case.<br/>
					 * The PackageVisibilityAccessHelper is used to invoke constructors which otherwise would not be accessible.<br/>
					 * Things like "final" our "super." are added.
					 */
					final Logger localRootLogger = new RootLogger();
					final ClassLoaderLogInfo classLoaderLogInfo = PackageVisibilityAccessHelper
							.invokeClassLoaderLogInfoConstructor(PackageVisibilityAccessHelper.invokeLogNodeConstructor(null, localRootLogger));
					super.classLoaderLoggers.put(classLoader, classLoaderLogInfo);
					readConfiguration(is, classLoader);
					addLogger(localRootLogger);
					// ************************************************************************

					debugIfEnabled("readConfiguration", "Finished reading webapp-specific logging configuration for " + webappInfo.webappName + ".");
					return;
				} else {
					debugIfEnabled("readConfiguration",
							"No webapp-specific logging configuration for webapp " + webappInfo.webappName + " found. (Expected file path was "
									+ expectedLoggingPropertiesFileUrl1 + " or " + expectedLoggingPropertiesFileUrl2 + ".)");
				}

			} else {
				final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
				webappName = getWebappName(urlClassLoader);
				if (JulExtensionsHelpers.isEmpty(webappName)) {
					debugIfEnabled("readConfiguration",
							"Couldn't get webapp name from class loader (which is expected e.g. for webapps/ROOT): " + urlClassLoader);
				} else if (urlClassLoader.getURLs().length == 0) {
					debugIfEnabled("readConfiguration",
							"Couldn't get webapp info for webapp '" + webappName + "' from WebappClassloader from class loader. No URLs specified. ");
				} else {
					debugIfEnabled("readConfiguration",
							"Couldn't get webapp info for webapp '" + webappName + "' from WebappClassloader from class loader. Number of URLs: "
									+ urlClassLoader.getURLs().length + ", First URL: " + urlClassLoader.getURLs()[0]);
				}
			}
		} else {
			debugIfEnabled("readConfiguration",
					"Couldn't get webapp info from classloader (which is expected for standard Tomcat webapps, e.g. webapps/docs). ClassLoader type: "
							+ classLoader.getClass() + ", ClassLoader: " + classLoader);
		}

		try {
			super.readConfiguration(classLoader);
		} catch (IOException ioe) {
			throw new IOException("IOException while reading configuration for webapp: " + webappName, ioe);
		} catch (RuntimeException re) {
			throw new RuntimeException("RuntimeException while reading configuration for webapp: " + webappName, re);
		} catch (Throwable t) {
			throw new RuntimeException("Exception while reading configuration for webapp: " + webappName, t);
		}
	}

	private static WebappInfo getWebappInfo(final URLClassLoader webappClassLoader) {

		final String webappName = getWebappName(webappClassLoader);

		if (JulExtensionsHelpers.isEmpty(webappName)) {
			/* if context name is not available, we do not continue. (We could still try to get everything from URLs, but context should always be
			 * set.) */
			return null;
		}

		JulExtensionsHelpers.assertNotEmpty(webappName, "Couldn't determine webapp name from classloader! (loader: " + webappClassLoader + ")");

		// first we try to get the webapp info based on the classloader's URLs.
		WebappInfo webappInfo = getWebappInfoBasedOnClassloaderUrls(webappName, webappClassLoader);

		if (webappInfo == null) {
			// webapp folder not found yet. This is expected to happen, when the context is started from Eclipse/Sysdeo.
			// --> try to guess the folder
			webappInfo = getWebappInfoBasedOnCatalinaBaseAndCheckFolders(webappName);
		}

		return webappInfo;
	}

	private static WebappInfo getWebappInfoBasedOnClassloaderUrls(final String webappName, final URLClassLoader urlClassLoader) {
		String webappFolderUrl = null;
		for (final URL url : urlClassLoader.getURLs()) {
			final String urlString = url.toString();
			if (urlString.matches(".+/webapps/" + webappName + "/WEB-INF/.+")) {
				// we found our webapp folder
				webappFolderUrl = urlString.substring(0, urlString.lastIndexOf("/WEB-INF"));
			}
		}

		if (webappFolderUrl == null) {
			return null;
		}

		final String hostFolderUrl = webappFolderUrl.substring(0, webappFolderUrl.lastIndexOf("/webapps/" + webappName));

		final WebappInfo info = new WebappInfo(webappName, webappFolderUrl, hostFolderUrl);
		return info;
	}

	private static WebappInfo getWebappInfoBasedOnCatalinaBaseAndCheckFolders(final String webappName) {

		// 1. catalina.base
		// 2. catalina.home
		// 3. working dir

		String catalinaBaseDir = System.getProperty("catalina.base");
		if (!JulExtensionsHelpers.isEmpty(catalinaBaseDir)) {
			String catalinaBaseDirUrl = JulExtensionsHelpers
					.removeTrailingFileSeparators(JulExtensionsHelpers.toURLString(new File(catalinaBaseDir)));
			if (JulExtensionsHelpers.fileUrlAvailable(catalinaBaseDirUrl + "/webapps")) {
				WebappInfo info = new WebappInfo(webappName, null, catalinaBaseDirUrl);
				return info;
			}
		}

		String catalinaHomeDir = System.getProperty("catalina.home");
		if (!JulExtensionsHelpers.isEmpty(catalinaHomeDir)) {
			String catalinaHomeDirUrl = JulExtensionsHelpers
					.removeTrailingFileSeparators(JulExtensionsHelpers.toURLString(new File(catalinaHomeDir)));
			if (JulExtensionsHelpers.fileUrlAvailable(catalinaHomeDirUrl + "/webapps")) {
				WebappInfo info = new WebappInfo(webappName, null, catalinaHomeDirUrl);
				return info;
			}
		}

		String workingDir = new File(new File(".").toURI()).getParentFile().getPath();
		if (JulExtensionsHelpers.fileAvailable(workingDir)) {
			String workingDirUrl = JulExtensionsHelpers.removeTrailingFileSeparators(JulExtensionsHelpers.toURLString(new File(workingDir)));
			if (JulExtensionsHelpers.fileUrlAvailable(workingDirUrl + "/webapps")) {
				WebappInfo info = new WebappInfo(webappName, null, workingDirUrl);
				return info;
			}
		}

		return null;
	}

	public static String getWebappName(final URLClassLoader webappClassLoader) {
		final String contextName = (String) JulExtensionsHelpers.invokeNoArgumentMethod(webappClassLoader, "getContextName");
		if (contextName == null) {
			return null;
		}
		return contextName.replace("/", "");
	}

	private void debugIfEnabled(final String methodName, final String message) {
		JulExtensionsHelpers.debugIfEnabled(getClass(), methodName, message);
	}

	/**
	 * Holds information about a webapp.
	 */
	private static class WebappInfo {

		private final String webappName;
		private final String webappFolderUrl;
		private final String hostFolderUrl;

		private WebappInfo(final String webappName, final String webappFolderUrl, final String hostFolderUrl) {
			this.webappName = webappName;
			this.webappFolderUrl = webappFolderUrl;
			this.hostFolderUrl = hostFolderUrl;
		}

		@Override
		public String toString() {
			return "WebappInfo [webappName=" + this.webappName + ", webappFolderUrl=" + this.webappFolderUrl + ", hostFolderUrl=" + this.hostFolderUrl
					+ "]";
		}
	}

}
