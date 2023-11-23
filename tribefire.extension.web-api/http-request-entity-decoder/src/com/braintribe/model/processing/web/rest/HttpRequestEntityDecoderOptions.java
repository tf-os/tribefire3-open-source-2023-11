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

import java.util.List;

import com.braintribe.model.processing.web.rest.impl.HttpRequestEntityDecoderOptionsImpl;

/**
 * The options object when decoding Http Requests.
 * 
 *
 */
public interface HttpRequestEntityDecoderOptions {
	
	/**
	 * @return a fresh set of default options, to be customized by the called
	 */
	static HttpRequestEntityDecoderOptions defaults() {
		return new HttpRequestEntityDecoderOptionsImpl();
	}

	/**
	 * @return {@code true} if the decoder should not read the header parameters (both standard and generic), defaults
	 *         to {@code false}
	 */
	boolean isIgnoringHeaders();

	/**
	 * Set ignoringHeaders.
	 * 
	 * @param ignoringHeaders
	 *            {@code true} if the decoder should not read the header parameters (both standard and generic),
	 *            defaults to {@code false}
	 * @return this
	 */
	HttpRequestEntityDecoderOptions setIgnoringHeaders(boolean ignoringHeaders);

	/**
	 * @return {@code true} if the decoder should not throw a bad request when no entity is found for a URL parameter,
	 *         defaults to {@code false}
	 */
	boolean isIgnoringUnmappedUrlParameters();

	/**
	 * Set ignoringUnmappedUrlParameters.
	 * 
	 * @param ignoringUnmappedUrlParameters
	 *            {@code true} if the decoder should not throw a bad request when no entity is found for a URL
	 *            parameter, defaults to {@code false}
	 * @return this
	 */
	HttpRequestEntityDecoderOptions setIgnoringUnmappedUrlParameters(boolean ignoringUnmappedUrlParameters);

	/**
	 * @return {@code true} if the decoder should not throw a bad request when no entity is found for a header (both
	 *         standard and generic), defaults to {@code false}
	 */
	boolean isIgnoringUnmappedHeaders();

	/**
	 * Sets ignoringUnmappedHeaders.
	 * 
	 * @param ignoringUnmappedHeaders
	 *            {@code true} if the decoder should not throw a bad request when no entity is found for a header (both
	 *            standard and generic), defaults to {@code false}
	 * @return this
	 */
	HttpRequestEntityDecoderOptions setIgnoringUnmappedHeaders(boolean ignoringUnmappedHeaders);

	/**
	 * @return the type resolver that the decoder use for object properties, or {@code null} if none
	 */
	PropertyTypeResolver getPropertyTypeResolver();

	/**
	 * Set the resolver that the decoder use for object properties.
	 * 
	 * @param propertyTypeResolver the type resolver, or {@code null} if none
	 * 
	 * @return this
	 */
	HttpRequestEntityDecoderOptions setPropertyTypeResolver(PropertyTypeResolver propertyTypeResolver);
	
	List<String> getIgnoredParameters();
	HttpRequestEntityDecoderOptions setIgnoredParameters(List<String> ignoredParameters);
	HttpRequestEntityDecoderOptions addIgnoredParameter(String ignoredParameter);

	HttpRequestEntityDecoderOptions setNullAware(boolean nullAware);
	boolean isNullAware();
}
