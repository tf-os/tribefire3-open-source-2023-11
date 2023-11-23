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
package com.braintribe.web.servlet.auth;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.logging.Logger;

public class PathUtils {

	public static String buildCurrentRelativePath(HttpServletRequest request, Logger logger) {
		String requestURI = request.getRequestURI();
		String pathInfo = request.getPathInfo();
		logger.trace(() -> "Build relative base path based on request URI: "+request.getRequestURI()+" and pathInfo: "+pathInfo);

		
		String basePath = requestURI;
		if (pathInfo != null) {
			basePath = requestURI.substring(0, requestURI.length()-pathInfo.length());
		}
		String result = basePath;
		logger.trace(() -> "Calculated relative base path to: "+result);
		return result;
	}

	public static String buildCurrentServicesPath(HttpServletRequest request, Logger logger) {
		return buildCurrentServicesPath(request, request.getServletPath(), logger);
	}
	
	public static String buildCurrentServicesPath(HttpServletRequest request, String pathSuffix, Logger logger) {
		String basePath = buildCurrentRelativePath(request, logger);
		String servicesPath = basePath.substring(0, basePath.length()-pathSuffix.length());
		logger.trace(() -> "Calculated servicePath to: "+servicesPath);
		return servicesPath;
	}

}
