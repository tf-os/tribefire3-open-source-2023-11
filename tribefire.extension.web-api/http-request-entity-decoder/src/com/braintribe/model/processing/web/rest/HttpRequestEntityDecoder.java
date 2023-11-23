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
package com.braintribe.model.processing.web.rest;

import javax.servlet.http.HttpServletRequest;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.web.rest.impl.HttpRequestEntityDecoderImpl;

/**
 * <p>
 * This class is the main entry point to decode an {@link HttpServletRequest} into one or more entities.<br />
 * This class only decodes URL parameters as well as Header parameters (decoding the body is out of scope for this
 * decoder).
 * </p>
 * 
 * <pre>
 * <code>
 * 	// instruct how to decode standard headers (do once for multiple requests)
 * 	StandardHeadersMapper<MyStandardHeaders> mapper = StandardHeadersMapper.mapToProperties(MyStandardHeaders.T);
 * 
 * 	// decode a single request
 * 	HttpRequestDecoder.createFor(request)
 * 		// decode URL and generic header parameters in entity1
 * 		.target("prefix1", entity1)
 * 		// decode URL, generic and standard header parameters in entity2
 * 		.target("prefix2", entity2, mapper)
 * 		// start decoding
 * 		.decode();
 * 
 *	// entity1 and entity2 now contain the decoded values.
 * </code>
 * </pre>
 * </p>
 * 
 * @author Neidhart.Orlich
 */
public interface HttpRequestEntityDecoder extends DecoderTargetRegistry {
	String SERIALIZED_REQUEST = "serialized-request";

	/**
	 * Creates a new decoder for the given request. Decodes values from URL as well as header parameters.
	 * 
	 * @param httpRequest
	 *            the request to decode, must not be {@code null}
	 * @return a newly created decoder, never {@code null}
	 */
	static HttpRequestEntityDecoder createFor(HttpServletRequest httpRequest) {
		return createFor(httpRequest, HttpRequestEntityDecoderOptions.defaults());
	}

	/**
	 * Creates a new decoder for the given request.
	 * 
	 * @param httpRequest
	 *            the request to decode, must not be {@code null}
	 * @return a newly created decoder, never {@code null}
	 */
	static HttpRequestEntityDecoder createFor(HttpServletRequest httpRequest, HttpRequestEntityDecoderOptions options) {
		return new HttpRequestEntityDecoderImpl(httpRequest, options);
	}

	/**
	 * Adds a new target entity to receive values from the decoded {@link HttpServletRequest}.
	 * 
	 * @param prefix
	 *            the prefix to use to bypass ordering, must not be {@code null}
	 * @param target
	 *            the target entity to decode into, must not be {@code null}
	 * @return this decoder
	 */
	HttpRequestEntityDecoder target(String prefix, GenericEntity target);
	
	@Override
	HttpRequestEntityDecoder target(String prefix, GenericEntity target, Runnable onSet);

	/**
	 * Adds a new target entity to receive values from the decoded {@link HttpServletRequest}.
	 * 
	 * @param prefix
	 *            the prefix to use to bypass ordering, must not be {@code null}
	 * @param target
	 *            the target entity to decode into, must not be {@code null}
	 * @param standardHeadersMapper
	 *            the mapper object to decode standard headers, or {@code null} if not decoding standard headers
	 * @return this decoder
	 */
	<T extends GenericEntity> HttpRequestEntityDecoder target(String prefix, T target, StandardHeadersMapper<? super T> standardHeadersMapper);

	/**
	 * Decode the request: fill in registered targets from the content of the {@link HttpServletRequest}.
	 */
	void decode();

}
