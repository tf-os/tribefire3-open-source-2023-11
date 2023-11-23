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
package com.braintribe.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.logging.Logger;
import com.braintribe.mimetype.MimeTypeDetector;
import com.braintribe.mimetype.PlatformMimeTypeDetector;
import com.braintribe.utils.IOTools;

/**
 * This is a simple servlet that allows to download single files that are contained in a configurable root folder.
 * <p>
 * The relative path to the file to be downloaded is taken from the request's {@link HttpServletRequest#getPathInfo()}.
 * "../" and "./" are supported. However special care is taken so that this servlet does not stream files from outside
 * its root folder.
 * <p>
 * There is also a simple mechanism that sets the mimetype of the response when it detects certain well known file
 * extensions on the file name of the resource to be downloaded.
 */
public class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 3266630280219605924L;

	private static final Logger log = Logger.getLogger(ResourceServlet.class);

	private Path publicResourcesDirectory;
	private MimeTypeDetector mimeTypeDetector = PlatformMimeTypeDetector.instance;

	public void setPublicResourcesDirectory(Path publicResourcesDirectory) {
		this.publicResourcesDirectory = publicResourcesDirectory;
	}
	
	public void setMimeTypeDetector(MimeTypeDetector mimeTypeDetector) {
		this.mimeTypeDetector = mimeTypeDetector;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String resourcePath = request.getPathInfo();
		File resourceFile = getResourceFile(resourcePath);

		if (!resourceFile.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Can't retrieve public resource: Did not find '" + resourcePath + "'.");
			return;
		}

		if (!resourceFile.isFile()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Can't retrieve public resource: resource is a folder: '" + resourcePath
					+ "'. Please request the respective child resources individually.");
			return;
		}

		if (resourceFile.length() <= Integer.MAX_VALUE) {
			response.setContentLength((int) resourceFile.length());
		}

		String mimeType = mimeTypeDetector.getMimeType(resourceFile, resourceFile.getName());
		if (mimeType != null) {
			response.setContentType(mimeType);
		}

		try (InputStream in = new BufferedInputStream(new FileInputStream(resourceFile), IOTools.SIZE_64K)) {
			IOTools.pump(in, response.getOutputStream());
		}
	}

	/* package-private */ File getResourceFile(String resourcePathParam) {
		if (resourcePathParam == null || resourcePathParam.matches("/*")) {
			throw new IllegalArgumentException("Can't retrieve public resource: No path specified.");
		}

		String resourcePath = resourcePathParam.replaceAll("^/*", ""); // remove leading slash(es)
		Path resourceSubPath = Paths.get(resourcePath).normalize();

		if (resourceSubPath.startsWith("..")) {
			throw new IllegalArgumentException(
					"Can't retrieve public resource: provided resource path points to a file outside the public resources parent path: "
							+ resourcePath);
		}

		return publicResourcesDirectory.resolve(resourceSubPath).toFile();
	}

}
