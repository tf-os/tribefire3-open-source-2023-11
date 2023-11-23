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
/*
 *
 *  Copyright 2011 Rajendra Patil
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package tribefire.extension.elastic.elasticsearch.wares.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.braintribe.cfg.Configurable;
import com.braintribe.logging.Logger;

import tribefire.extension.elastic.elasticsearch.wares.filter.common.AbstractFilter;
import tribefire.extension.elastic.elasticsearch.wares.filter.common.Constants;
import tribefire.extension.elastic.elasticsearch.wares.filter.compression.CompressedHttpServletRequestWrapper;
import tribefire.extension.elastic.elasticsearch.wares.filter.compression.CompressedHttpServletResponseWrapper;
import tribefire.extension.elastic.elasticsearch.wares.filter.compression.EncodedStreamsFactory;

/**
 * Servlet Filter implementation class CompressionFilter to handle compressed requests and also respond with compressed
 * contents supporting gzip, compress or deflate compression encoding. Visit
 * http://code.google.com/p/webutilities/wiki/CompressionFilter for more details.
 *
 */
public class CompressionFilter extends AbstractFilter {

	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(CompressionFilter.class);

	/**
	 * The threshold number of bytes) to compress
	 */
	private int compressionThreshold = Constants.DEFAULT_COMPRESSION_SIZE_THRESHOLD;

	/**
	 * To mark the request that it is processed
	 */
	private static final String PROCESSED_ATTR = CompressionFilter.class.getName() + ".PROCESSED";

	/**
	 * To mark the request that response compressed
	 */
	private static final String COMPRESSED_ATTR = CompressionFilter.class.getName() + ".COMPRESSED";

	/**
	 * Threshold
	 */
	private static final String INIT_PARAM_COMPRESSION_THRESHOLD = "compressionThreshold";

	@Override
	public void init(FilterConfig initFilterConfig) throws ServletException {
		super.init(initFilterConfig);
		int compressionMinSize = readInt(initFilterConfig.getInitParameter(INIT_PARAM_COMPRESSION_THRESHOLD), this.compressionThreshold);
		if (compressionMinSize > 0) { // priority given to configured value
			this.compressionThreshold = compressionMinSize;
		}

		if (logger.isTraceEnabled()) {
			logger.trace("Filter initialized with: " + INIT_PARAM_COMPRESSION_THRESHOLD + ":" + String.valueOf(this.compressionThreshold));
		}
	}

	protected int readInt(String parameter, int defaultValue) {
		if (parameter == null || parameter.trim().length() == 0) {
			return defaultValue;
		}
		try {
			int result = Integer.parseInt(parameter);
			return result;
		} catch (Exception e) {
			logger.debug("Could not parse " + parameter, e);
		}
		return defaultValue;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		ServletRequest req = getRequest(request);
		ServletResponse resp = getResponse(request, response);
		request.setAttribute(PROCESSED_ATTR, Boolean.TRUE);
		chain.doFilter(req, resp);
		if (resp instanceof CompressedHttpServletResponseWrapper) {
			CompressedHttpServletResponseWrapper compressedResponseWrapper = (CompressedHttpServletResponseWrapper) resp;
			try {
				compressedResponseWrapper.close(); // so that stream is finished and closed.
			} catch (IOException ex) {
				logger.error("Response was already closed: ", ex);
			}
			if (compressedResponseWrapper.isCompressed()) {
				req.setAttribute(COMPRESSED_ATTR, Boolean.TRUE);
			}
		}
	}

	private static ServletRequest getRequest(ServletRequest request) {

		boolean trace = logger.isTraceEnabled();

		if (!(request instanceof HttpServletRequest)) {
			if (trace) {
				logger.trace("No Compression: non http request");
			}
			return request;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String contentEncoding = httpRequest.getHeader(Constants.HTTP_CONTENT_ENCODING_HEADER);
		if (contentEncoding == null) {
			if (trace) {
				logger.trace("No Compression: Request content encoding is: " + contentEncoding);
			}
			return request;
		}
		if (!EncodedStreamsFactory.isRequestContentEncodingSupported(contentEncoding)) {
			if (trace) {
				logger.trace("No Compression: unsupported request content encoding: " + contentEncoding);
			}
			return request;
		}
		if (trace) {
			logger.trace("Decompressing request: content encoding : " + contentEncoding);
		}
		return new CompressedHttpServletRequestWrapper(httpRequest, EncodedStreamsFactory.getFactoryForContentEncoding(contentEncoding));
	}

	private static String getAppropriateContentEncoding(String acceptEncoding) {
		if (acceptEncoding == null) {
			return null;
		}
		String contentEncoding = null;
		if (Constants.CONTENT_ENCODING_IDENTITY.equals(acceptEncoding.trim())) {
			return contentEncoding; // no encoding to be applied
		}
		String[] clientAccepts = acceptEncoding.split(",");
		// !TODO select best encoding (based on q) when multiple encoding are accepted by client
		// @see http://stackoverflow.com/questions/3225136/http-what-is-the-preferred-accept-encoding-for-gzip-deflate
		for (String accepts : clientAccepts) {
			if (Constants.CONTENT_ENCODING_IDENTITY.equals(accepts.trim())) {
				return contentEncoding;
			} else if (EncodedStreamsFactory.SUPPORTED_ENCODINGS.containsKey(accepts.trim())) {
				contentEncoding = accepts; // get first matching encoding
				break;
			}
		}
		return contentEncoding;
	}

	private ServletResponse getResponse(ServletRequest request, ServletResponse response) {

		boolean trace = logger.isTraceEnabled();

		if (response.isCommitted() || request.getAttribute(PROCESSED_ATTR) != null) {
			if (trace) {
				logger.trace("No Compression: Response committed or filter has already been applied");
			}
			return response;
		}
		if (!(response instanceof HttpServletResponse) || !(request instanceof HttpServletRequest)) {
			if (trace) {
				logger.trace("No Compression: non http request/response");
			}
			return response;
		}
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String acceptEncoding = httpRequest.getHeader(Constants.HTTP_ACCEPT_ENCODING_HEADER);
		String contentEncoding = getAppropriateContentEncoding(acceptEncoding);
		if (contentEncoding == null) {
			if (trace) {
				logger.trace("No Compression: Accept encoding is : " + acceptEncoding);
			}
			return response;
		}
		String requestURI = httpRequest.getRequestURI();
		if (!isURLAccepted(requestURI)) {
			if (trace) {
				logger.trace("No Compression: For path: " + requestURI);
			}
			return response;
		}
		String userAgent = httpRequest.getHeader(Constants.HTTP_USER_AGENT_HEADER);
		if (!isUserAgentAccepted(userAgent)) {
			if (trace) {
				logger.trace("No Compression: For User-Agent: " + userAgent);
			}
			return response;
		}
		EncodedStreamsFactory encodedStreamsFactory = EncodedStreamsFactory.getFactoryForContentEncoding(contentEncoding);
		if (trace) {
			logger.trace("Compressing response: content encoding : " + contentEncoding);
		}
		return new CompressedHttpServletResponseWrapper(httpResponse, encodedStreamsFactory, contentEncoding, compressionThreshold, this);
	}

	@Configurable
	public void setCompressionThreshold(int compressionThreshold) {
		this.compressionThreshold = compressionThreshold;
	}

}
