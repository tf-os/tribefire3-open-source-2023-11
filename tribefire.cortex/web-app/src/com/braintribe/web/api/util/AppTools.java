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
package com.braintribe.web.api.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.utils.FileTools;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.StringTools;

/**
 * Utility methods that are mainly used by {@link com.braintribe.web.api.ApplicationLoader} and
 * {@link com.braintribe.web.api.listener.ForcedLibs}.
 */
public class AppTools {

	private static final Logger logger = Logger.getLogger(AppTools.class);

	protected static String loadUrlContent(URL url) throws IOException {
		InputStream is = null;
		try {
			is = url.openStream();
			String content = IOTools.slurp(is, "UTF-8").trim();
			return content;
		} finally {
			IOTools.closeCloseable(is, logger);
		}
	}

	public static List<String> getContentLines(URL url) throws IOException {
		List<String> result = new ArrayList<String>();
		String content = loadUrlContent(url);
		if (content != null && content.length() > 0) {
			String[] lines = content.split("\n");
			if (lines != null) {
				for (String line : lines) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#")) {
						result.add(line);
					}
				}
			}
		}
		return result;
	}

	public static List<URL> parseIndexFromURL(URL url) throws IOException {
		List<URL> result = new ArrayList<URL>();
		List<String> lines = getContentLines(url);
		for (String line : lines) {
			if (line.toLowerCase().endsWith(".jar")) {
				URL jarUrl = new URL(url, line);
				logger.debug("Adding JAR resource "+jarUrl);
				result.add(jarUrl);
			} else {
				logger.debug("Ignoring non-JAR file "+line);
			}
		}
		return result;
	}

	public static void deployJarFilesToClasses(ServletContext servletContext, List<URL> jarUrls) {
		String realPath = servletContext.getRealPath("/WEB-INF");
		if (logger.isDebugEnabled()) logger.debug("deployJarFilesToClasses: Using real path "+realPath);
		if (realPath == null) {
			return;
		}

		File realPathFile = new File(realPath);
		File classesDir = new File(realPathFile, "classes");
		if (!classesDir.exists()) {
			classesDir.mkdir();
		} else {
			cleanUp(jarUrls, classesDir);
		}
		unjarJarFiles(jarUrls, classesDir);
	}

	protected static void unjarJarFiles(List<URL> jarUrls, File classesDir) {
		boolean debug = logger.isDebugEnabled();
		
		if (debug) logger.debug("Extracting JARs "+jarUrls+" to "+classesDir.getAbsolutePath());
		for (URL url : jarUrls) {
			String indexName = getIndexName(url);
			File indexFile = new File(classesDir, indexName);
			if (!indexFile.exists()) {
				if (debug) logger.debug("Extracting JAR "+url);
				unjar(url, classesDir);
				if (debug) logger.debug("Done extracting JAR "+url);
			} else {
				if (debug) logger.debug("Index file "+indexFile.getAbsolutePath()+" already exists. Not extracting "+url);
			}
		}

	}

	protected static String getIndexName(URL url) {
		if (url == null) {
			return null;
		}
		String jarName = FileTools.getName(url.toString());
		String rawName = FileTools.getNameWithoutExtension(jarName);
		String indexName = rawName+".index";
		return indexName;
	}
	
	protected static void cleanUp(List<URL> activatedJarUrls, File classesDir) {

		boolean debug = logger.isDebugEnabled();
		
		final List<String> activatedIndexFiles = new ArrayList<String>();
		for (URL url : activatedJarUrls) {
			String indexName = getIndexName(url);
			activatedIndexFiles.add(indexName);
		}
		
		if (debug) logger.debug("These are the activated forced libs: "+activatedIndexFiles);

		File[] surplusIndexFiles = classesDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".index")) {
					if (activatedIndexFiles.contains(name)) {
						return false;
					} else {
						return true;
					}
				} else {
					return false;
				}
			}
		});
		if (surplusIndexFiles != null && surplusIndexFiles.length > 0) {
			if (debug) logger.debug("Cleaning up: "+StringTools.<File>join(",", surplusIndexFiles));
			for (File surplusIndexFile : surplusIndexFiles) {
				try {
					if (debug) logger.debug("Cleaning up: "+surplusIndexFile);
					List<String> lines = getContentLines(surplusIndexFile.toURI().toURL());
					for (String line : lines) {
						File file = new File(classesDir, line);
						if (file.exists() && file.isFile()) {
							file.delete();
						}
					}
					surplusIndexFile.delete();
				} catch (IOException e) {
					logger.error("Could not process file "+surplusIndexFile.getAbsolutePath(), e);
				}
			}
			if (debug) logger.debug("Cleaning up empty folders.");
			FileTools.cleanEmptyFolders(classesDir, false);
			if (debug) logger.debug("Done cleaning up empty folders.");
		} else {
			if (debug) logger.debug("No JAR had to be cleaned up.");
		}
	}

	protected static void unjar(URL jarUrl, File targetDir) {
		try (JarInputStream zin = new JarInputStream(jarUrl.openStream())) {
			List<String> entries = new ArrayList<>();
			ZipEntry zipEntry = null;
			
			while ((zipEntry = zin.getNextEntry()) != null) {
				String slashedPathName = zipEntry.getName();
				entries.add(slashedPathName);
				
				File targetFile = new File(targetDir, slashedPathName);
				
				if (zipEntry.isDirectory()) {
					// create directory because it maybe empty and it would be an information loss otherwise
					targetFile.mkdirs();
				}
				else {
					targetFile.getParentFile().mkdirs();
					
					try (OutputStream out = new FileOutputStream(targetFile)) {
						IOTools.transferBytes(zin, out, IOTools.BUFFER_SUPPLIER_8K);
					}
				}
				
				zin.closeEntry();
			}
			
			String jarName = FileTools.getName(jarUrl.toString());
			String rawName = FileTools.getNameWithoutExtension(jarName);
			String indexName = rawName+".index";
			File indexFile = new File(targetDir, indexName);
			StringBuilder sb = new StringBuilder();
			for (String entry : entries) {
				sb.append(entry);
				sb.append('\n');
			}
			IOTools.spit(indexFile, sb.toString(), "UTF-8", false);

		}
		catch (Exception e) {
			throw Exceptions.unchecked(e, "Error while unpacking jar: " + jarUrl + " to " + targetDir.getAbsolutePath());
		}
	}

	public static String getContextPath(ServletContextEvent sce) {
		String contextPath = "unknown";
		if (sce != null) {
			ServletContext ctx = sce.getServletContext();
			if (ctx != null) {
				contextPath = ctx.getContextPath();
			} else {
				contextPath = sce.toString();
			}
		}
		return contextPath;
	}
}
