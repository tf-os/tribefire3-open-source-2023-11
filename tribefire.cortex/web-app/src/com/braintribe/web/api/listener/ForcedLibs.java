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
package com.braintribe.web.api.listener;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.classloader.ClassLoaderTools;
import com.braintribe.web.api.util.AppTools;

/**
 * This servlet context listener makes sure that certain JAR files get precedence over others.
 * The most prominent use case is when logging libraries should be overruled so that all
 * log output is channeled to a single logging framework. 
 * 
 * This is achieved by extracting a list of JAR files into the /WEB-INF/classes directory.
 * By definition, classes in this directory are loaded before any class in /WEB/lib/*.jar
 * is used.
 * 
 * Usually, the list of JAR files that should be taken is stored in tribefire/app/web/forcedLibs.txt
 * somewhere in the classpath. It is possible to put a forcedLibs.txt file in /WEB-INF/ to override
 * the pre-packaged file from the classpath. This file could also be empty if this mechanism is 
 * not needed/desired.
 * 
 * It is desirable that this servlet context listener is invoked before any other context listener
 * gets started to prevent the loading of classes that should be actually ignored.
 */
public class ForcedLibs implements ServletContextListener {

	private static final Logger logger = Logger.getLogger(ForcedLibs.class);

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//nothing to do
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		boolean debug = logger.isDebugEnabled();
		if (debug) logger.debug("Starting to deploy forced libs if applicable.");
		ServletContext sc = sce.getServletContext();
		List<URL> jarUrls = getForcedLibs(sc, sc.getClassLoader());
		if (debug) logger.debug("Using the following JAR URLs: "+jarUrls);
		AppTools.deployJarFilesToClasses(sc, jarUrls);
		if (debug) logger.debug("Done deploying forced libs.");
	}

	/**
	 * Collects a list of JAR URLs that should be enforced. There are several possible sources.
	 * See {@link #getForcedLibsFromClassLoader(ClassLoader...)}, {@link #getForcedLibsFromLibFolder(ServletContext, ClassLoader...)},
	 * and {@link #getForcedLibsFromResources(ServletContext)} for details on possible
	 * locations.
	 * 
	 * @param servletContext The servlet context.
	 * @param classLoaders One or more classloaders.
	 * @return A list of JAR URLs. This list may be empty, but never null.
	 */
	public static List<URL> getForcedLibs(ServletContext servletContext, ClassLoader... classLoaders) {
		List<URL> result = new ArrayList<URL>();
		result.addAll(getForcedLibsFromLibFolder(servletContext, classLoaders));
		result.addAll(getForcedLibsFromResources(servletContext));
		result.addAll(getForcedLibsFromClassLoader(classLoaders));
		if (logger.isDebugEnabled()) logger.debug("Collected the following URLs for enforced libs: "+result);
		return result;
	}
	

	/**
	 * This method gets a list of enforced JAR file URLs by checking one or more classloaders
	 * for the resource tribefire/app/web/lib-forced/jarurls. If such a resource is available,
	 * the content of this resource must contain a list of URLs that point to JAR files
	 * that should be enforced. Note that this mechanism is not used at the time of writing,
	 * but is kept in place as a fallback mechanism.
	 * 
	 * @param classLoaders One or more classloaders. If this array is empty, no action will be taken.
	 * @return A list of URLs retrieved from resources identified by tribefire/app/web/lib-forced/jarurls.
	 * 	If no resource has been found, this list is empty, but never null.
	 */
	protected static List<URL> getForcedLibsFromClassLoader(ClassLoader... classLoaders) {

		List<URL> result = new ArrayList<URL>();

		boolean debug = logger.isDebugEnabled();
		
		String parentUrl = "tribefire/app/web/lib-forced/";
		if (classLoaders != null) {
			if (debug) logger.debug("Trying to load "+parentUrl+"jarurls from classloaders.");
			for (ClassLoader cl : classLoaders) {
				try {
					Enumeration<URL> urls = cl.getResources(parentUrl+"jarurls");
					if (urls != null) {
						while(urls.hasMoreElements()) {
							URL url = urls.nextElement();
							List<URL> jarUrls = AppTools.parseIndexFromURL(url);
							result.addAll(jarUrls);
						}
					}
				} catch (IOException e) {
					logger.error("Could not laod tribefire/app/web/lib-forced/jarurls from "+cl, e);
				}
			}
		}

		if (debug) logger.debug("Collected jarurl URLs from classloaders: "+result);
		return result;

	}

	/**
	 * Gets a list of JAR URLs by looking for either the context resource /WEB-INF/forcedLibs.txt
	 * or by using the realpath functionality to get access to this file. If this file is not available,
	 * the default list obtained from the classloader resources (identified by tribefire/app/web/forcedLibs.txt)
	 * will be taken.
	 * 
	 * @param servletContext The servlet context that is needed to get access to /WEB-INF/forcedLibs.txt.
	 * @param classLoaders One or more classloaders that are used if /WEB-INF/forcedLibs.txt does not exist.
	 * @return A list of obtained URLs. This list may be empty, but never null.
	 */
	protected static List<URL> getForcedLibsFromLibFolder(ServletContext servletContext, ClassLoader... classLoaders) {

		List<String> jarFilePatterns = new ArrayList<String>();

		boolean debug = logger.isDebugEnabled();
		
		String forcedLibsIndexFile = "/WEB-INF/forcedLibs.txt";
		boolean customFileExists = false;
		try {
			URL url = servletContext.getResource(forcedLibsIndexFile);
			if (url != null) {
				customFileExists = true;
				jarFilePatterns.addAll(AppTools.getContentLines(url));
				if (debug) logger.debug("Custom override in "+forcedLibsIndexFile+" found: "+jarFilePatterns);
			} else {
				String realPath = servletContext.getRealPath("/WEB-INF/forcedLibs.txt");
				if (realPath != null) {
					File indexFile = new File(realPath);
					if (indexFile.exists() && indexFile.isFile()) {
						customFileExists = true;
						jarFilePatterns.addAll(AppTools.getContentLines(indexFile.toURI().toURL()));
						if (debug) logger.debug("Custom override in (realpath) "+forcedLibsIndexFile+" found: "+jarFilePatterns);
					} else {
						if (debug) logger.debug("The realPath "+indexFile.getAbsolutePath()+" ("+realPath+") does not exist or is not a file.");
					}
				}

			}
		} catch(Exception e) {
			logger.error("Error while getting information about forced libs from "+forcedLibsIndexFile, e);
		}

		if (!customFileExists) {
			if (debug) logger.debug("No custom override found.");
			String path = "tribefire/app/web/forcedLibs.txt";
			if (classLoaders != null) {
				for (ClassLoader cl : classLoaders) {
					try {
						Enumeration<URL> urls = cl.getResources(path);
						if (urls != null) {
							while(urls.hasMoreElements()) {
								URL url = urls.nextElement();
								if (debug) logger.debug("Loading lib-forced index file "+url);
								jarFilePatterns.addAll(AppTools.getContentLines(url));
							}
						}
					} catch (IOException e) {
						logger.error("Could not laod tribefire/app/web/forcedLibs.txt from "+cl, e);
					}
				}
			}			
		}



		List<URL> result = new ArrayList<URL>();

		if (!jarFilePatterns.isEmpty()) {
			if (debug) logger.debug("Using the following JAR file patterns: "+jarFilePatterns);

			final List<Pattern> patterns = new ArrayList<Pattern>();
			for (String p : jarFilePatterns) {
				try {
					patterns.add(Pattern.compile(p));
				} catch(Exception e) {
					logger.error("Could not use pattern: "+p, e);
				}
			}

			String realPath = servletContext.getRealPath("/WEB-INF/lib");
			if (realPath != null) {
				File libFolder = new File(realPath);
				if (libFolder.exists() && libFolder.isDirectory()) {
					File[] allJars = libFolder.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							for (Pattern p : patterns) {
								if (p.matcher(name).matches()) {
									return true;
								}
							}
							return false;
						}
					});
					if (allJars != null) {
						for (File jarFile : allJars) {
							try {
								result.add(jarFile.toURI().toURL());
							} catch (MalformedURLException e) {
								logger.error("Error while trying to add "+jarFile.getAbsolutePath()+" to the list of forced libs.");
							}
						}
					}
				} else {
					if (debug) logger.debug("The realPath "+libFolder.getAbsolutePath()+" ("+realPath+") does not exist or is not a directory.");
					
					for (ClassLoader classloader : classLoaders) {
						try {
							Map<URI, ClassLoader> entries = ClassLoaderTools.getClassPathEntries(classloader);
							for (URI uri : entries.keySet()) {
								String name = StringTools.getSubstringAfterLast(uri.toString(), "/");
								for (Pattern p : patterns) {
									if (p.matcher(name).matches()) {
										result.add(uri.toURL());
									}
								}
							}
						} catch (Exception e) {
							logger.error("Error while trying to get all resources from classloader "+classloader, e);
						}
					}
					
					if (debug) logger.debug("Identified these JAR files as forced: "+result);
				}
			} else {
				if (debug) logger.debug("Could not find /WEB-INF/lib folder.");
			}

		} else {
			logger.debug("No forced-libs have been configured in an index file.");
		}

		return result;

	}
	
	/**
	 * This method gets a list of all JAR files that are contained in the special directory
	 * /WEB-INF/lib-forced. This mechanism can be used to place custom JAR files there without
	 * the need to specify them in a text file.
	 * 
	 * @param servletContext The servlet context that is needed to access /WEB-INF/lib-forced.
	 * @return A list of URLs pointing to the JAR files in /WEB-INF/lib-forced, or an empty list
	 *  if either the directory does not exist or is empty. Only JAR files will be included in this list.
	 */
	protected static List<URL> getForcedLibsFromResources(ServletContext servletContext) {

		boolean debug = logger.isDebugEnabled();
		
		String folder = "/WEB-INF/lib-forced/";
		String resourceUrl = folder+"index";
		try {
			URL url = servletContext.getResource(resourceUrl);
			if (url != null) {
				List<URL> jarUrls = AppTools.parseIndexFromURL(url);
				if (debug) logger.debug("Loaded URLs "+jarUrls+" from location "+url+" (resource URL is "+resourceUrl+")");
				return jarUrls;
			} else {
				if (debug) logger.debug("Could not find "+resourceUrl+" with help of the ServletContext. Trying realpath now.");
				String realPath = servletContext.getRealPath("/WEB-INF/lib-forced/");
				if (realPath != null) {
					File folderObject = new File(realPath);
					if (folderObject.exists() && folderObject.isDirectory()) {
						File[] files = folderObject.listFiles(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return name.toLowerCase().endsWith(".jar");
							}
						});
						if (files != null) {
							List<URL> result = new ArrayList<URL>();

							for (File f : files) {
								result.add(f.toURI().toURL());
							}
							if (debug) logger.debug("Returning the forced libs "+result);
							return result;
						}
					} else {
						if (debug) logger.debug("The realPath "+folder+"/"+realPath+" does not exist or is not a directory.");
					}
				} else {
					if (debug) logger.debug("Could not find the folder /WEB-INF/lib-forced/ by realpath");
				}

			}
		} catch(Exception e) {
			logger.error("Error while getting information about forced libs from "+resourceUrl, e);
		}
		return Collections.emptyList();
	}

}
