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
package com.braintribe.util.servlet.util;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import com.braintribe.logging.Logger;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.html.HtmlTools;

public class ServletTools {

	private static final Logger logger = Logger.getLogger(ServletTools.class);

	public static String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

	/**
	 * Gets the <code>Accept</code> headers of the provided request and returns all accepted mime types in a sorted list. Note that the resulting mime
	 * types will always be lower-case, regardless of the actual values in the request.
	 * 
	 * @param request
	 *            The request that should be inspected.
	 * @return The list of accepted mime types. The list might be empty, but it will never be null.
	 */
	public static List<String> getAcceptedMimeTypes(HttpServletRequest request) {

		boolean trace = logger.isTraceEnabled();
		boolean debug = logger.isDebugEnabled();

		List<String> result = new ArrayList<>();

		TreeMap<Double, List<String>> typesByWeight = new TreeMap<>(new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				// We want the highest to be at the start of the map
				return o2.compareTo(o1);
			}
		});
		if (request != null) {
			Enumeration<String> headers = request.getHeaders("Accept");
			if (headers != null) {
				while (headers.hasMoreElements()) {
					String header = headers.nextElement();
					String[] values = StringTools.splitCommaSeparatedString(header, true);
					if (values != null) {
						for (String value : values) {
							int index = value.indexOf(";");
							double weight = 1d;
							if (index >= 0) {
								String typeOnly = value.substring(0, index);
								String weightString = value.substring(index + 1).trim();
								if (!StringTools.isBlank(weightString)) {
									weightString = weightString.replace(" ", "").toLowerCase();
									if (weightString.startsWith("q=")) {
										weightString = weightString.replace("q=", "");

										try {
											weight = Double.parseDouble(weightString);
										} catch (NumberFormatException e) {
											if (debug)
												logger.debug("Could not parse weight of " + value + ", which should be a number between 0 and 1.", e);
											weight = 1d;
										}
										if (weight < 0) {
											weight = 0d;
										} else if (weight > 1) {
											weight = 1d;
										}
									} else {
										if (trace)
											logger.trace("Unsupported parameter in Accept header value: " + value);
									}
								}
								value = typeOnly;
							}
							if (!StringTools.isBlank(value)) {
								List<String> list = typesByWeight.computeIfAbsent(weight, w -> new ArrayList<>());
								list.add(value.toLowerCase());
							}
						}
					}
				}
			}
		}
		for (List<String> list : typesByWeight.values()) {
			result.addAll(list);
		}

		return result;
	}

	/**
	 * Given the servlet consuming this request is registered for '/x/y' on a web-context accessed with '/services' this method returns:
	 * '/services/x/y' for a URL: 'http://foo.bar/services/x/y/z'
	 */
	public static String getFullServletPath(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String pathInfo = request.getPathInfo();

		logger.trace(() -> "Build relative base path based on request URI: " + request.getRequestURI() + " and pathInfo: " + pathInfo);

		String basePath = requestURI;
		if (pathInfo != null) {
			basePath = requestURI.substring(0, requestURI.length() - pathInfo.length());
		}
		String result = basePath;
		logger.trace(() -> "Calculated relative base path to: " + result);
		return result;
	}

	/**
	 * Given the servlet consuming this request is registered for '/x/y' on a web-context accessed with '/services' this method returns: '/services'
	 * for a URL: 'http://foo.bar/services/x/y/z'
	 */
	public static String getServletContextPath(HttpServletRequest request) {
		return getCustomServletPath(request, request.getServletPath());
	}

	public static String getCustomServletPath(HttpServletRequest request, String pathSuffix) {
		String basePath = getFullServletPath(request);
		String servicesPath = basePath.substring(0, basePath.length() - pathSuffix.length());
		logger.trace(() -> "Calculated servicePath to: " + servicesPath);
		return servicesPath;
	}

	/**
	 * Returns a single parameter value of the request. If the request does not contain this parameter, the default value will be returned.
	 * 
	 * @param request
	 *            The request that may contain the parameter.
	 * @param key
	 *            The name of the parameter.
	 * @param defaultValue
	 *            The default value to be returned if the parameter is not defined.
	 * @return The value of the parameter (if present) or the default value.
	 * @throws IllegalArgumentException
	 *             Thrown when either request or key is null.
	 */
	public static String getSingleParameter(HttpServletRequest request, String key, String defaultValue) {
		if (request == null) {
			throw new IllegalArgumentException("The request must not be nul.");
		}
		if (key == null) {
			throw new IllegalArgumentException("The key must not be nul.");
		}
		String parameterValue = request.getParameter(key);
		if (parameterValue == null) {
			return defaultValue;
		}
		return parameterValue;
	}

	/**
	 * <p>
	 * Determines whether the given {@link HttpServletRequest} is a multipart request.
	 */
	public static boolean isMultipart(HttpServletRequest request) {

		if (request.getContentType() == null)
			return false;

		return (request.getContentType().toLowerCase().startsWith("multipart"));
	}

	/**
	 * <p>
	 * Returns {@code true} if the given {@link Part} must be treated as resources' content.
	 */
	public static boolean isContentPart(Part part) {
		if (part == null || part.getName() == null)
			return false;
		return (part.getName().equals("content"));
	}

	/**
	 * <p>
	 * Retrieves the file name from a multipart {@link Part}.
	 * 
	 * @param part
	 *            {@link Part} to be inspected for file name
	 * @return The file name as extracted from the given part
	 * @throws Exception
	 *             In case no {@code content-disposition} header or {@code filename} associated to it is found
	 */
	public static String getFileName(Part part) throws Exception {
		String contentDisp = part.getHeader("content-disposition");
		if (contentDisp == null)
			throw new Exception("Part is missing content-disposition header");

		String[] contentDispositionChunks = StringTools.splitSemicolonSeparatedString(contentDisp, true);

		String filenameCandidate = null;
		String encodedFilenameCandidate = null;

		for (String cd : contentDispositionChunks) {
			if (cd.trim().startsWith("filename")) {

				// Either filename="..."
				// Or filename*= UTF-8''...(url-encoded)
				// Or filename*= ISO-8859-1''...(url-encoded)

				cd = cd.trim();

				int index = cd.indexOf('=');
				String key = cd.substring(0, index);
				String value = cd.substring(index + 1).trim();

				if (key.equalsIgnoreCase("filename*")) {

					String check = value.toLowerCase();
					if (check.startsWith("utf-8''")) {

						String encoded = value.substring(7).trim();
						try {
							String filename = URLDecoder.decode(encoded, "UTF-8");
							encodedFilenameCandidate = filename;
						} catch (Exception e) {
							throw new Exception("Could not UTF-8 decode value " + encoded + " from " + cd, e);
						}

					} else if (check.startsWith("iso-8859-1''")) {

						String encoded = value.substring(12).trim();
						try {
							String filename = URLDecoder.decode(encoded, "ISO-8859-1");
							encodedFilenameCandidate = filename;
						} catch (Exception e) {
							throw new Exception("Could not ISO-8859-1 decode value " + encoded + " from " + cd, e);
						}

					} else {
						throw new Exception("Unsupported format for content-disposition filename: " + cd);
					}

				} else {

					String encoded = value.replace("\"", "");
					try {
						String filename = HtmlTools.unescapeHtml(encoded);
						filenameCandidate = filename;
					} catch (Exception e) {
						throw new Exception("Could not unescape value " + encoded + " from " + cd, e);
					}
				}

			}
		}

		String filename = encodedFilenameCandidate != null ? encodedFilenameCandidate : filenameCandidate;

		if (filename == null && logger.isDebugEnabled()) {
			logger.debug("No 'filename' attribute found in the Content-Disposition header: [ " + contentDisp + " ]");
		}
		return filename;
	}

	public static String stringify(ServletRequest request) {
		if (request instanceof HttpServletRequest) {
			return stringify((HttpServletRequest) request);
		}
		if (request == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try {

			sb.append(request.getServerName());

			Map<String, String[]> parameterMap = request.getParameterMap();
			if (parameterMap != null) {
				for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
					String key = entry.getKey();
					String[] values = entry.getValue();
					sb.append(", ");
					sb.append(key);
					if ((values != null) && (values.length > 1)) {
						sb.append("={");
					} else {
						sb.append("=");
					}
					sb.append(StringTools.createStringFromArray(values));
					if ((values != null) && (values.length > 1)) {
						sb.append("}");
					}
				}
			}

		} catch (Exception e) {
			logger.error("Could not stringify ServletRequest: " + request, e);
		}
		return sb.toString();
	}

	/**
	 * Creates a String representation of a HttpServletRequest. It will include parameters and cookies. It makes an effort to hide password
	 * parameters.
	 * 
	 * @param request
	 *            The request to stringify.
	 * @return A String representation of the provided request.
	 */
	public static String stringify(HttpServletRequest request) {
		if (request == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		try {

			sb.append(request.getMethod());
			sb.append(" ");
			sb.append(getFullURL(request));

			Map<String, String[]> parameterMap = request.getParameterMap();
			if (parameterMap != null) {
				for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
					String key = entry.getKey();
					String[] values = entry.getValue();
					sb.append(", ");
					sb.append(key);
					String lowerCaseKey = key != null ? key.toLowerCase() : "";
					if (lowerCaseKey.contains("pass") || lowerCaseKey.contains("secret") || lowerCaseKey.contains("pwd")) {
						sb.append("=***");
					} else {
						if ((values != null) && (values.length > 1)) {
							sb.append("={");
						} else {
							sb.append("=");
						}
						sb.append(StringTools.createStringFromArray(values));
						if ((values != null) && (values.length > 1)) {
							sb.append("}");
						}
					}
				}
			}

			Cookie[] cookies = request.getCookies();
			if (cookies != null && cookies.length > 0) {
				sb.append("; Cookies: ");
				for (int i = 0; i < cookies.length; ++i) {
					Cookie c = cookies[i];
					if (c != null) {
						if (i > 0) {
							sb.append(",");
						}
						sb.append("[");
						sb.append("Domain:" + c.getDomain());
						sb.append(", Name:" + c.getName());
						sb.append(", Comment:" + c.getComment());
						sb.append(", MaxAge:" + c.getMaxAge());
						sb.append(", Path:" + c.getPath());
						sb.append(", Value:" + c.getValue());
						sb.append(", Secure:" + c.getSecure());
						sb.append("]");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Could not stringify HttpServletRequest: " + request, e);
		}
		return sb.toString();
	}
}
