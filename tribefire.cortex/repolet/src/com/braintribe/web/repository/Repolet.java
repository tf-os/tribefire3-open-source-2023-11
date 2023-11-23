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
package com.braintribe.web.repository;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.service.common.context.UserSessionAspect;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.collection.impl.AttributeContexts;
import com.braintribe.web.repository.output.BreadCrumb;
import com.braintribe.web.repository.output.RepoletVelocityWriter;
import com.braintribe.web.repository.output.RepoletWriter;
import com.braintribe.web.servlet.auth.Constants;

/**
 * Repository Servlet
 * 
 */
public class Repolet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private List<Supplier<URL>> repositoryProviders = Collections.<Supplier<URL>> emptyList();
	private boolean cacheableResponses;
	private boolean listDirectoryContents;
	private Set<String> directoryContentsIncludes;
	private Set<String> directoryContentsExcludes;
	private Predicate<String> directoryContentsFilter;
	private boolean onFailurePrintFriendlyPage;
	private boolean onFailurePrintInspectedPaths;
	private Map<String, String> mimeTypeMap = Collections.<String, String> emptyMap();
	private String defaultMimeType = "application/octet-stream";

	private static final RepoletWriter<Writer> repoletWriter = new RepoletVelocityWriter();

	private static final Logger log = Logger.getLogger(Repolet.class);
	private Map<String, Supplier<?>> contextProviders;

	/**
	 * @param repositoryProviders
	 *            the repositoryProviders to set
	 */
	@Required
	public void setRepositoryProviders(List<Supplier<URL>> repositoryProviders) {
		this.repositoryProviders = repositoryProviders;
	}

	/**
	 * @param cacheableResponses
	 *            the cacheableResponses to set
	 */
	@Configurable
	public void setCacheableResponses(boolean cacheableResponses) {
		this.cacheableResponses = cacheableResponses;
	}

	/**
	 * @param listDirectoryContents
	 *            the listDirectoryContents to set
	 */
	@Configurable
	public void setListDirectoryContents(boolean listDirectoryContents) {
		this.listDirectoryContents = listDirectoryContents;
	}

	/**
	 * @param directoryContentsIncludes
	 *            the directoryContentsIncludes to set
	 */
	@Configurable
	public void setDirectoryContentsIncludes(Set<String> directoryContentsIncludes) {
		this.directoryContentsIncludes = directoryContentsIncludes;
	}

	/**
	 * @param directoryContentsExcludes
	 *            the directoryContentsExcludes to set
	 */
	@Configurable
	public void setDirectoryContentsExcludes(Set<String> directoryContentsExcludes) {
		this.directoryContentsExcludes = directoryContentsExcludes;
	}

	/**
	 * @param directoryContentsFilter
	 *            the directoryContentsFilter to set
	 */
	@Configurable
	public void setDirectoryContentsFilter(Predicate<String> directoryContentsFilter) {
		this.directoryContentsFilter = directoryContentsFilter;
	}

	/**
	 * @param onFailurePrintFriendlyPage
	 *            the onFailurePrintFriendlyPage to set
	 */
	@Configurable
	public void setOnFailurePrintFriendlyPage(boolean onFailurePrintFriendlyPage) {
		this.onFailurePrintFriendlyPage = onFailurePrintFriendlyPage;
	}

	/**
	 * @param onFailurePrintInspectedPaths
	 *            the onFailurePrintInspectedPaths to set
	 */
	@Configurable
	public void setOnFailurePrintInspectedPaths(boolean onFailurePrintInspectedPaths) {
		this.onFailurePrintInspectedPaths = onFailurePrintInspectedPaths;
	}

	/**
	 * @param mimeTypeMap
	 *            the mimeTypeMap to set
	 */
	@Configurable
	public void setMimeTypeMap(Map<String, String> mimeTypeMap) {
		this.mimeTypeMap = (mimeTypeMap != null ? mimeTypeMap : Collections.<String, String> emptyMap());
	}

	/**
	 * @param defaultMimeType
	 *            the defaultMimeType to set
	 */
	@Configurable
	public void setDefaultMimeType(String defaultMimeType) {
		this.defaultMimeType = defaultMimeType;
	}

	@Configurable
	public void setContextProviders(Map<String, Supplier<?>> contextProviders) {
		this.contextProviders = contextProviders;
	}

	@Override
	protected void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {

		RepoletResults results = findResources(httpRequest);

		if (results.notFound())
			replyWithNotFound(httpRequest, httpResponse);
		else
			serveResults(results, httpRequest, httpResponse);

	}

	/**
	 * 
	 * @param results
	 * @param httpRequest
	 *            The HTTP request
	 * @param httpResponse
	 *            The HTTP response
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected void serveResults(RepoletResults results, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		if (results.foundFile()) {

			streamPath(results.getFile(), httpResponse);

		} else if (listDirectoryContents) {
			// directory contents processing only occurs if listDirectoryContents is true

			// fix url for directories if necessary
			if (!httpRequest.getRequestURI().endsWith("/")) {
				httpResponse.sendRedirect(httpRequest.getRequestURL().append("/").toString());
				return;
			}

			enlistPaths(results.getDirectories(), httpRequest, httpResponse);

		} else {
			if (log.isDebugEnabled())
				log.debug("listing of directory contents was suppressed because listDirectoryContents is set to " + listDirectoryContents);
		}
	}

	/**
	 * 
	 * @param directories
	 * @param httpRequest
	 *            The HTTP request
	 * @param httpResponse
	 *            The HTTP response
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected void enlistPaths(List<Path> directories, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		Collection<String> entries = new TreeSet<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				if (o1 == null && o2 == null) {
					return 0;
				} else if (o2 == null) {
					return 1;
				} else if (o1 == null) {
					return -1;
				} else {
					return o1.toLowerCase().compareTo(o2.toLowerCase());
				}
			}
		});

		for (Path directory : directories)
			collectEntries(directory, entries);

		String path = httpRequest.getPathInfo();
		printList(path, buildCrumbs(path), entries, httpRequest, httpResponse);
	}

	/**
	 * 
	 * @param directory
	 * @param entries
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected void collectEntries(Path directory, Collection<String> entries) throws IOException {
		DirectoryStream<Path> dirStream = null;
		try {
			dirStream = Files.newDirectoryStream(directory, getPathFilter(directory));
			String entry = null;
			for (Path p : dirStream) {
				entry = p.getFileName().toString();

				if (!entry.endsWith("/") && Files.isDirectory(p))
					entry += "/";

				entries.add(entry);
			}
		} finally {
			close(dirStream);
		}
	}

	/**
	 * 
	 * @param directory
	 * @return
	 */
	protected DirectoryStream.Filter<Path> getPathFilter(Path directory) {

		FileSystem fs = directory.getFileSystem();
		final Set<PathMatcher> includeMatchers = buildIncludeMatchers(fs);
		final Set<PathMatcher> excludeMatchers = buildExcludeMatchers(fs);

		return new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) {

				if (entry == null)
					return false;

				if (directoryContentsFilter == null && includeMatchers.isEmpty() && excludeMatchers.isEmpty()) {
					if (log.isDebugEnabled()) {
						log.debug("[ " + entry + " ] will be listed as no filter nor include/exclude patterns are configured");
					}
					return true;
				}

				if (directoryContentsFilter != null) {
					boolean matchedFilter = directoryContentsFilter.test(entry.toString());
					if (log.isDebugEnabled()) {
						log.debug("[ " + entry.toString() + " ] was " + ((matchedFilter) ? "accepted" : "blocked")
								+ " by directoryContentsFilter filter [ " + directoryContentsFilter.getClass().getSimpleName() + " ]");
					}
					return matchedFilter;
				}

				for (PathMatcher excludeMatcher : excludeMatchers) {
					if (excludeMatcher.matches(entry)) {
						if (log.isDebugEnabled()) {
							log.debug("[ " + entry + " ] matched exclude pattern.");
						}
						return false;
					}
				}

				for (PathMatcher includeMatcher : includeMatchers) {
					if (includeMatcher.matches(entry)) {
						if (log.isDebugEnabled()) {
							log.debug("[ " + entry + " ] matched include pattern.");
						}
						return true;
					}
				}

				boolean noIncludePatternConfigured = includeMatchers.isEmpty();

				if (log.isDebugEnabled()) {
					log.debug("[ " + entry + " ] will "
							+ (noIncludePatternConfigured
									? "be listed as it did not match the configured exclude patterns and no include patterns are configured"
									: "not be listed as it did not match the configured include patterns"));
				}

				return noIncludePatternConfigured;
			}
		};
	}

	/**
	 * 
	 * @param httpRequest
	 *            The HTTP request
	 * @return
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected RepoletResults findResources(HttpServletRequest httpRequest) throws IOException {

		if (log.isDebugEnabled())
			log.debug("started search for " + httpRequest.getPathInfo());

		RepoletResults results = new RepoletResults();
		Path resource = null;

		for (Supplier<URL> repoProvider : repositoryProviders) {

			resource = findResource(repoProvider, httpRequest);

			if (resource == null)
				continue;

			if (Files.isDirectory(resource)) {
				results.addDirectory(resource);
			} else {
				results.setFile(resource);
				return results;
			}
		}

		if (log.isDebugEnabled() && results.notFound())
			log.debug("resource [ " + httpRequest.getPathInfo() + " ] not present in any configured repository");

		return results;
	}

	/**
	 * 
	 * @param resource
	 * @param httpResponse
	 *            The HTTP response
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected void streamPath(Path resource, HttpServletResponse httpResponse) throws IOException {
		InputStream fileIn = null;
		OutputStream httpOut = null;
		try {
			setCommonResponseHeaders(httpResponse);
			httpResponse.setContentType(getMimeType(resource));
			fileIn = Files.newInputStream(resource, StandardOpenOption.READ);
			httpOut = httpResponse.getOutputStream();
			pump(fileIn, httpOut);
			httpOut.flush();
		} finally {
			close(fileIn);
		}
	}

	/**
	 * 
	 * @param resource
	 * @return
	 */
	protected String getMimeType(Path resource) {
		String mimeType = mimeTypeMap.get(getExtension(resource));
		if (mimeType != null)
			return mimeType;
		return defaultMimeType;
	}

	/**
	 * 
	 * @param resource
	 * @return
	 */
	protected static String getExtension(Path resource) {
		String fileName = resource.getFileName().toString();
		int idx = fileName.lastIndexOf('.');
		if (idx < 0)
			return "";
		else
			return fileName.substring(idx + 1).toLowerCase();
	}

	/**
	 * Resolves http request's path info with a repository path, returning an absolute {@link Path} to the requested file or
	 * directory.
	 * 
	 * @param repositoryPath
	 *            {@link Path} representation of a repository root
	 * 
	 * @param pathInfo
	 *            Path info as fetched from {@link HttpServletRequest#getPathInfo()}
	 * 
	 * @return Absolute {@link Path} of {@code pathInfo} resolved against {@code repositoryPath}
	 */
	protected Path buildResourcePath(Path repositoryPath, String pathInfo) {

		if (pathInfo == null)
			pathInfo = "";
		else if (pathInfo.charAt(0) == '/')
			pathInfo = pathInfo.substring(1);

		return repositoryPath.resolve(pathInfo);
	}

	/**
	 * 
	 * @param repoProvider
	 * @param httpRequest
	 *            The HTTP request
	 * @return
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected Path findResource(Supplier<URL> repoProvider, HttpServletRequest httpRequest) throws IOException {
		try {
			Path repositoryPath = toPath(repoProvider.get());
			Path resourcePath = buildResourcePath(repositoryPath, httpRequest.getPathInfo());

			if (Files.exists(resourcePath)) {
				if (log.isDebugEnabled())
					log.debug("resource [ " + httpRequest.getPathInfo() + " ] found in repository [ " + repositoryPath
							+ " ]. resolved resouce path was [ " + resourcePath + " ]");
				return resourcePath;
			}

			if (log.isDebugEnabled())
				log.debug("resource [ " + httpRequest.getPathInfo() + " ] not found in repository [ " + repositoryPath
						+ " ]. resolved resouce path was [ " + resourcePath + " ]");

			if (onFailurePrintInspectedPaths)
				collectInspectedPath(resourcePath, httpRequest);

			return null;

		} catch (Exception e) {
			throw new IOException(e.getClass().getName() + " error while looking for the resource: " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param resourcePath
	 * @param httpRequest
	 *            The HTTP request
	 */
	protected void collectInspectedPath(Path resourcePath, HttpServletRequest httpRequest) {
		getInspectedPathList(httpRequest).add(resourcePath.toString());
	}

	/**
	 * 
	 * @param httpRequest
	 *            The HTTP request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<String> getInspectedPathList(HttpServletRequest httpRequest) {

		if (httpRequest.getAttribute("inspectedPathList") == null)
			httpRequest.setAttribute("inspectedPathList", new ArrayList<String>());

		return (List<String>) httpRequest.getAttribute("inspectedPathList");
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	protected List<BreadCrumb> buildCrumbs(String path) {

		List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
		if (path == null || path.trim().equals("/") || path.trim().isEmpty()) {
			breadCrumbs.add(new BreadCrumb("home", ""));
			return breadCrumbs;
		}

		path = "home" + (path.charAt(0) == '/' ? path : "/" + path);

		if (path.charAt(path.length() - 1) == '/')
			path = path.substring(0, path.length() - 1);

		String[] p = path.split("/");
		for (int i = 0, d = p.length - 1, r = d; i < p.length; i++, r = d - i) {
			breadCrumbs.add(new BreadCrumb(p[i], StringUtils.repeat("../", r)));
		}

		return breadCrumbs;
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected Path toPath(URL url) throws IOException {
		try {
			return Paths.get(url.toURI());
		} catch (URISyntaxException e) {
			throw new IOException("failed to create a path from url [ " + url + " ] : " + e.getMessage(), e);
		}
	}

	/**
	 * Sets the set of response header that is common to all responses (stream, resource listing, resource not found and et
	 * cetera)
	 * 
	 * @param httpResponse
	 *            The HTTP response
	 */
	protected void setCommonResponseHeaders(HttpServletResponse httpResponse) {

		final long ONE_DAY = 24 * 60 * 60 * 1000;
		final long now = System.currentTimeMillis();

		httpResponse.setDateHeader("Last-Modified", now);

		if (cacheableResponses) {
			httpResponse.setHeader("Cache-Control", "public,max-age=" + (ONE_DAY / 1000));
		} else {
			httpResponse.setHeader("Cache-Control", "no-store, no-cache");
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setDateHeader("Expires", 0);
		}

	}

	/**
	 * Handles HttpServletResponse in case the resource is not found in any of the underlying repositories
	 * 
	 * @param httpRequest
	 *            The HTTP request
	 * @param httpResponse
	 *            The HTTP response
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected void replyWithNotFound(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		setCommonResponseHeaders(httpResponse);
		httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);

		if (onFailurePrintFriendlyPage)
			printNotFound(httpRequest, httpResponse);
	}

	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	protected static void pump(InputStream in, OutputStream out) throws IOException {
		byte buffer[] = new byte[4096];

		int count;
		while ((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}
	}

	/**
	 * Closes a closeable, quietly.
	 * 
	 * @param closeable
	 */
	protected void close(Closeable closeable) {

		if (closeable == null)
			return;

		try {
			closeable.close();
		} catch (Exception e) {
			log.error(e.getClass().getSimpleName() + " error occurred while closing " + closeable.getClass().getName() + ": " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * @param fs
	 * @return
	 */
	private Set<PathMatcher> buildIncludeMatchers(FileSystem fs) {
		return buildPathMatcher(directoryContentsIncludes, fs);
	}

	/**
	 * 
	 * @param fs
	 * @return
	 */
	private Set<PathMatcher> buildExcludeMatchers(FileSystem fs) {
		return buildPathMatcher(directoryContentsExcludes, fs);
	}

	/**
	 * 
	 * @param patterns
	 * @param fs
	 * @return
	 */
	private Set<PathMatcher> buildPathMatcher(Collection<String> patterns, FileSystem fs) {

		if (patterns == null || patterns.isEmpty())
			return Collections.<PathMatcher> emptySet();

		Set<PathMatcher> matchers = new HashSet<PathMatcher>(patterns.size());
		for (String pattern : patterns)
			matchers.add(fs.getPathMatcher(pattern));

		return matchers;
	}

	/**
	 * 
	 * @param path
	 * @param parent
	 * @param entries
	 * @param httpResponse
	 *            The HTTP response
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	private void printList(String path, Collection<BreadCrumb> breadCrumbs, Collection<String> entries, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException {

		setCommonResponseHeaders(httpResponse);
		httpResponse.setContentType("text/html");

		PrintWriter httpOut = httpResponse.getWriter();
		try {

			repoletWriter.writeList(path, breadCrumbs, entries, httpOut, buildRequestAttributes(httpRequest));
			httpOut.flush();
		} finally {
			close(httpOut);
		}
	}

	private Map<String, Object> buildRequestAttributes(HttpServletRequest httpRequest) {
		Map<String, Object> attributes = new HashMap<String, Object>();

		Optional<UserSession> userSessionOptional = AttributeContexts.peek().findAttribute(UserSessionAspect.class);

		attributes.put("request", httpRequest);
		attributes.put("userSession", userSessionOptional.orElse(null));
		attributes.put("sessionUser", userSessionOptional.map(UserSession::getUser).orElse(null));
		attributes.put("userIconSrc", httpRequest.getAttribute(Constants.REQUEST_PARAM_SESSIONUSERICONURL));

		if (contextProviders != null) {
			for (Map.Entry<String, Supplier<?>> contextEntry : contextProviders.entrySet()) {
				try {
					attributes.put(contextEntry.getKey(), contextEntry.getValue().get());
				} catch (RuntimeException e) {
					log.error("Error while adding context entry for key: " + contextEntry.getKey(), e);
				}
			}
		}

		return attributes;
	}

	/**
	 * 
	 * @param httpRequest
	 *            The HTTP request
	 * @param httpResponse
	 *            The HTTP response
	 * @throws IOException
	 *             Thrown in the event of an error.
	 */
	private void printNotFound(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		PrintWriter httpOut = httpResponse.getWriter();
		try {
			repoletWriter.writeNotFound(httpRequest.getPathInfo(), onFailurePrintInspectedPaths, getInspectedPathList(httpRequest), httpOut,
					buildRequestAttributes(httpRequest));
			httpOut.flush();
		} finally {
			close(httpOut);
		}
	}

	/**
	 * Groups paths found for the requested path info
	 */
	public class RepoletResults {

		private Path file;
		private List<Path> directories = new ArrayList<Path>();

		public boolean notFound() {
			return file == null && directories.isEmpty();
		}

		public boolean foundFile() {
			return file != null;
		}

		public Path getFile() {
			return file;
		}

		public void setFile(Path file) {
			this.file = file;
		}

		public List<Path> getDirectories() {
			return directories;
		}

		public void addDirectory(Path directory) {
			directories.add(directory);
		}

	}

}
